package com.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "demo.lock")
public class DistributedLockProperties {

    /**
     * 锁的类型
     */
    private ContainerType type = ContainerType.JDBC;

    private JdbcContainer jdbc = new JdbcContainer();

    private RedisContainer redis = new RedisContainer();

    public ContainerType getType() {
        return type;
    }

    public void setType(ContainerType type) {
        this.type = type;
    }

    public JdbcContainer getJdbc() {
        return jdbc;
    }

    public void setJdbc(JdbcContainer jdbc) {
        this.jdbc = jdbc;
    }

    public RedisContainer getRedis() {
        return redis;
    }

    public void setRedis(RedisContainer redis) {
        this.redis = redis;
    }

    public enum ContainerType {

        /**
         * 基于数据库的锁
         */
        JDBC,

        REDIS

    }

    public static class JdbcContainer {

        private Duration renewalInterval = Duration.ofSeconds(30);

        private Duration cleanInterval = Duration.ofSeconds(60);

        private Duration invalidTimeout = Duration.ofSeconds(60);

        private Duration defaultTryLockTimeout = Duration.ofSeconds(5);

        public Duration getRenewalInterval() {
            return renewalInterval;
        }

        public void setRenewalInterval(Duration renewalInterval) {
            this.renewalInterval = renewalInterval;
        }

        public Duration getCleanInterval() {
            return cleanInterval;
        }

        public void setCleanInterval(Duration cleanInterval) {
            this.cleanInterval = cleanInterval;
        }

        public Duration getInvalidTimeout() {
            return invalidTimeout;
        }

        public void setInvalidTimeout(Duration invalidTimeout) {
            this.invalidTimeout = invalidTimeout;
        }

        public Duration getDefaultTryLockTimeout() {
            return defaultTryLockTimeout;
        }

        public void setDefaultTryLockTimeout(Duration defaultTryLockTimeout) {
            this.defaultTryLockTimeout = defaultTryLockTimeout;
        }
    }

    public static class RedisContainer {

        private String url;

        private int database = 0;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }
    }
}
