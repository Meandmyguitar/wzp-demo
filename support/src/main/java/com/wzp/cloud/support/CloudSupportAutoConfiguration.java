package com.wzp.cloud.support;

import com.wzp.cloud.support.amqp.AmqpPublisher;
import com.wzp.cloud.support.amqp.AmqpPublisherImpl;
import com.wzp.cloud.support.amqp.EnvelopeJsonConverter;
import com.wzp.cloud.support.event.MessageBusEventSpi;
import com.wzp.cloud.support.event.EventBus;
import com.wzp.cloud.support.lock.DistributedLockManager;
import com.wzp.cloud.support.lock.JdbcDistributedLockManager;
import com.wzp.cloud.support.lock.RedisDistributedLockManager;
import com.wzp.cloud.support.lock.ScheduledJdbcDistributedLockManager;
import com.wzp.cloud.support.logbook.HttpLogFormatter;
import com.wzp.cloud.support.metrics.MemoryUsageListener;
import com.demo.msgbus.*;
import com.rabbitmq.client.Channel;
import com.wzp.cloud.support.msgbus.*;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.instrument.async.TraceableScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.zalando.logbook.spring.LogbookAutoConfiguration;
import reactor.core.scheduler.Schedulers;

import javax.sql.DataSource;
import java.util.concurrent.*;

@Configuration
public class CloudSupportAutoConfiguration {

    @EnableConfigurationProperties(MetricsProperties.class)
    @Configuration
    public static class MetricsConfiguration {

        @Bean
        public MemoryUsageListener memoryUsageListener(MetricsProperties properties) {
            MemoryUsageListener memoryUsageSystem = new MemoryUsageListener();
            memoryUsageSystem.setPercentageUsageThreshold(properties.getMemoryCollectionUsageThreshold());
            return memoryUsageSystem;
        }
    }

    @EnableConfigurationProperties(SharedProperties.class)
    @Configuration
    public static class SharedConfiguration {

        @Bean
        public TraceableScheduledExecutorService traceableScheduledExecutorService(SharedProperties properties, BeanFactory beanFactory) {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(properties.getScheduledThreadPool());
            return new TraceableScheduledExecutorService(beanFactory, executorService);
        }
    }

    @ConditionalOnClass({RabbitTemplate.class, Channel.class})
    @EnableConfigurationProperties(AmqpProperties.class)
    @Configuration
    @AutoConfigureAfter({RabbitAutoConfiguration.class})
    @Import(LockConfiguration.class)
    public static class AmqpConfiguration {

        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @ConditionalOnBean(DistributedLockManager.class)
        @Bean
        public AmqpPublisher amqpPublisher(
                AmqpTemplate amqpTemplate,
                DistributedLockManager lockManager,
                DataSource dataSource,
                TraceableScheduledExecutorService traceableScheduledExecutorService,
                AmqpProperties properties, BeanFactory beanFactory) {
            ThreadPoolExecutor workExecutor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.DAYS, new ArrayBlockingQueue<>(properties.getBufferQueueMaxSize()));
            return new AmqpPublisherImpl(
                    amqpTemplate,
                    lockManager,
                    dataSource,
                    traceableScheduledExecutorService,
                    new TraceableScheduledExecutorService(beanFactory, workExecutor),
                    properties.getRetryInterval(),
                    properties.getRetryBefore());
        }

        @Bean
        public MessageConverter messageConverter() {
            return new EnvelopeJsonConverter(Thread.currentThread().getContextClassLoader());
        }

    }

    @EnableConfigurationProperties(DistributedLockProperties.class)
    @Configuration
    public static class LockConfiguration {

        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        @ConditionalOnBean(DataSource.class)
        @ConditionalOnProperty(prefix = "demo.lock", name = "type", havingValue = "jdbc")
        @Bean
        public DistributedLockManager jdbcDistributedLockManager(
                DataSource dataSource,
                TraceableScheduledExecutorService traceableScheduledExecutorService,
                DistributedLockProperties properties) {
            return new ScheduledJdbcDistributedLockManager(
                    new JdbcDistributedLockManager(dataSource), traceableScheduledExecutorService,
                    properties.getJdbc().getRenewalInterval(),
                    properties.getJdbc().getCleanInterval());
        }

        @ConditionalOnProperty(prefix = "demo.lock", name = "type", havingValue = "redis")
        @Bean
        public DistributedLockManager redisDistributedLockManager(
                DistributedLockProperties properties) {
            DistributedLockProperties.RedisContainer redis = properties.getRedis();
            if (redis.getUrl() == null) {
                throw new IllegalStateException("demo.lock.redis.url未设置");
            }
            return new RedisDistributedLockManager(redis.getUrl(), redis.getDatabase());
        }

    }

    @Configuration
    @AutoConfigureBefore(LogbookAutoConfiguration.class)
    public static class LogbookConfiguration {

        @ConditionalOnMissingBean(org.zalando.logbook.HttpLogFormatter.class)
        @Bean
        public HttpLogFormatter httpLogFormatter() {
            return new HttpLogFormatter();
        }

    }


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Configuration
    @EnableConfigurationProperties(EventBusProperties.class)
    public static class EventBusConfiguration {

        @ConditionalOnProperty(value = "demo.event.type", havingValue = "SIMPLE")
        @Bean
        public MessageBus simpleMessageBus(Environment environment) {
            return new SimpleMessageBus(environment.getRequiredProperty("spring.application.name"));
        }

        @ConditionalOnProperty(value = "demo.event.type", havingValue = "AMQP")
        @Bean
        public MessageBus amqpMessageBus(
                EventBusProperties properties,
                DataSource dataSource,
                DistributedLockManager lockManager,
                Environment environment,
                TraceableScheduledExecutorService traceableScheduledExecutorService) {

            EventBusProperties.AmqpEventBus amqp = properties.getAmqp();

            ReactiveAmqpMessageBusOption option = ReactiveAmqpMessageBusOption
                    .of(environment.getRequiredProperty("spring.application.name"))
                    .backpressureBufferSize(amqp.getBackpressureBufferSize())
                    .consumeMaxRetryNum(amqp.getConsumeMaxRetryNum())
                    .parallelism(amqp.getParallelism())
                    .scheduler(Schedulers.elastic());
            ReactiveAmqpMessageBus bus = new ReactiveAmqpMessageBus(SupportUtils.createConnectionFactory(amqp.getRabbitUrl()), dataSource, option);

            return new ReactiveAmqpMessageBusBean(
                    bus, traceableScheduledExecutorService,
                    lockManager,
                    amqp.getRetryInterval(),
                    amqp.getRetryRecently(),
                    amqp.getRetryFarthest(),
                    amqp.getParallelism());
        }

        @ConditionalOnProperty(value = "demo.event.type")
        @Bean
        public EventBus eventBus(MessageBus messageBus, Environment environment) {
            String group = environment.getRequiredProperty("spring.application.name");
            return new EventBus(new MessageBusEventSpi(messageBus), group);
        }

    }
}
