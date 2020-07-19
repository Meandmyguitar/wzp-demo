package com.wzp.cloud.support.amqp;

/**
 * 可靠的AMQP消息队列发送类，必须在事务内调用
 * <p>
 *     发送的消息会预先存库，仅在事务提交才会发送，如果事务回滚则不会发送。
 *     如果发送时出错，会随后自动补发。
 *     注意：同一个消息可能会发送多次，消费者需要谨慎处理
 * </p>
 * <p>
 *     发送的消息需要实现Serializable接口以备序列化和反序列化
 * </p>
 *
 * @see Envelope
 */
public interface AmqpPublisher {

    /**
     * 异步发送消息
     */
    void send(String exchange, String routingKey, Envelope message);

    /**
     * 同步发送消息
     */
    void sendSync(String exchange, String routingKey, Envelope message);
}
