package com.lanmaoly.cloud.support.lock;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class JdbcDistributedLockManager implements DistributedLockManager {

    public static void ddl(DataSource dataSource) throws DataAccessException {
        String sql = "CREATE TABLE\n" +
                "    T_SYS_LOCK\n" +
                "    (\n" +
                "        NAME VARCHAR(50) NOT NULL,\n" +
                "        OWNER VARCHAR(36),\n" +
                "        CREATE_TIME DATETIME NOT NULL,\n" +
                "        LOCKED_TIME DATETIME,\n" +
                "        REMARK VARCHAR(200),\n" +
                "        PRIMARY KEY (NAME)\n" +
                "    )";
        Util.execute(dataSource, sql);

        sql = "CREATE INDEX I_SYS_LOCK_1 ON T_SYS_LOCK (LOCKED_TIME)";
        Util.execute(dataSource, sql);
    }

    private final Logger logger = LoggerFactory.getLogger(JdbcDistributedLockManager.class);

    // 超过此时长未更新即视为失效
    private int invalidTimeout;

    private int maxRetryCount;

    // 默认的尝试加锁超时时间，单位毫秒
    private int defaultTryLockTimeout;

    private DataSource dataSource;

    private final JdbcDistributedLockManagerMetrics metrics = new JdbcDistributedLockManagerMetrics();

    private final List<JdbcDistributedLock> locks;

    public JdbcDistributedLockManager(DataSource dataSource) {
        this(dataSource, 3, 5000, 60 * 1000);
    }

    public JdbcDistributedLockManager(DataSource dataSource, int maxRetryCount, int defaultTryLockTimeout, int invalidTimeout) {
        this.dataSource = dataSource;
        this.maxRetryCount = maxRetryCount;
        this.defaultTryLockTimeout = defaultTryLockTimeout;
        this.invalidTimeout = invalidTimeout;
        locks = new ArrayList<>();
    }

    public int getInvalidTimeout() {
        return invalidTimeout;
    }

    JdbcDistributedLockManagerMetrics getMetrics() {
        return metrics;
    }

    @Override
    public DistributedLock newLock(String name) throws DistributedLockException, InterruptedException {
        return new ReentrantDistributedLock(newJdbcDistributedLock(name));
    }

    JdbcDistributedLock newJdbcDistributedLock(String name) throws DistributedLockException, InterruptedException {

        if (!insert(name, maxRetryCount)) {
            metrics.getNewLockFailCounter().increment();
            throw new DistributedLockException("尝试插入T_SYS_LOCK失败");
        }

        return new JdbcDistributedLock(dataSource, name);
    }

    void registerLock(JdbcDistributedLock lock) {
        synchronized (locks) {
            locks.add(lock);
        }
    }

    void removeLock(JdbcDistributedLock lock) {
        synchronized (locks) {
            Iterator<JdbcDistributedLock> iter = locks.iterator();
            while (iter.hasNext()) {
                JdbcDistributedLock l = iter.next();
                if (l == lock) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    void renewal() {
        try {

            logger.info("开始延展锁的有效时间");
            ArrayList<JdbcDistributedLock> locks;
            synchronized (this.locks) {
                locks = new ArrayList<>(this.locks);
            }

            String sql = "update T_SYS_LOCK set LOCKED_TIME=? where NAME=? and OWNER=?";
            locks.forEach(o -> {
                if (Util.execute(dataSource, sql, new Date(), o.getName(), o.id) != 1) {
                    logger.warn("展期失败 name={}, id={}", o.getName(), o.getId());
                }
            });
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * 清理失效的记录
     */
    void clean() {
        try {
            logger.info("开始清理失效的记录");

            long t = System.currentTimeMillis() - invalidTimeout;
            String sql = "update T_SYS_LOCK set OWNER=null where LOCKED_TIME < ? and OWNER is not null";
            int c = Util.execute(dataSource, sql, new Date(t));

            logger.info("成功清理 {} 条", c);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean insert(String name, int retry) throws InterruptedException {
        boolean ok = false;
        for (int i = 0; i < retry + 1; i++) {
            LocalDateTime now = LocalDateTime.now();
            if (tryInsert(name)) {
                ok = true;
                break;
            }
            // 随机等待一段时间
            Util.randomSleep(50, 100);

            metrics.getRetryTimer().record(Duration.between(now, LocalDateTime.now()));
        }

        return ok;
    }

    /**
     * 尝试创建记录
     */
    private boolean tryInsert(String name) {
        String sql;
        try {
            sql = "select NAME from T_SYS_LOCK where NAME = ?";
            List<Map<String, Object>> list = Util.query(dataSource, sql, name);
            if (list.size() > 0) {
                return true;
            }

            sql = "insert into T_SYS_LOCK (NAME, CREATE_TIME) values (?, ?)";
            if (1 != Util.execute(dataSource, sql, name, new Date())) {
                throw new IllegalStateException();
            }
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        } catch (DataAccessException e) {
            throw new DistributedLockException(e.getMessage(), e);
        }
    }


    static class JdbcDistributedLockManagerMetrics {

        private Timer retryTimer;

        private Counter newLockFailCounter;

        public JdbcDistributedLockManagerMetrics() {
            retryTimer = Metrics.timer("cloud.support.lock.jdbc.retry");

            Tag t = new ImmutableTag("name", "new-lock-fail");
            newLockFailCounter = Metrics.counter("cloud.support.lock.jdbc.error", Collections.singletonList(t));
        }

        public Timer getRetryTimer() {
            return retryTimer;
        }

        public Counter getNewLockFailCounter() {
            return newLockFailCounter;
        }
    }

    class JdbcDistributedLock implements DistributedLock {

        private DataSource dataSource;

        private String name;

        private String id;

        JdbcDistributedLock(DataSource dataSource, String name) {
            this.dataSource = dataSource;
            this.name = name;
            this.id = UUID.randomUUID().toString().replaceAll("-", "");
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        @Override
        public void lock() throws InterruptedException {
            long start = System.currentTimeMillis();
            do {
                if (tryLockCore()) {
                    return;
                }
                Util.randomSleep(100, 200);
            } while (System.currentTimeMillis() - start <= defaultTryLockTimeout);
            throw new DistributedLockException("加锁超时");
        }

        @Override
        public void unlock() {
            String sql = "update T_SYS_LOCK set OWNER=NULL where NAME=? and OWNER=?";
            if (1 != Util.execute(dataSource, sql, name, id)) {
                logger.warn("解锁时更新记录失败 name={}, id={}", name, id);
            }
            removeLock(this);
        }

        @Override
        public String toString() {
            return "JdbcDistributedLock{" +
                    "name='" + name + '\'' +
                    '}';
        }

        boolean tryLockCore() {
            String sql = "update T_SYS_LOCK\n" +
                    "set OWNER       = ?,\n" +
                    "    LOCKED_TIME = ?\n" +
                    "where NAME = ?\n" +
                    "  and OWNER is null";
            boolean ok = Util.execute(dataSource, sql, id, new Date(), name) == 1;
            if (ok) {
                registerLock(this);
            }
            return ok;
        }
    }
}
