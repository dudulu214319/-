package com.dudulu.seckill_my.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息发送者
 *
 * @ClassName: MQSender
 */
@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀信息
     * @param message
     * @return void
     **/
    public void sendSeckillMessage(String message) {
        log.info("发送消息" + message);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", message); // 被点号“．”分隔开的每一段独立的字符串称为一个单词

        // 可以考虑设置分布式唯一id（幂等性，唯一索引，重复消费）和过期时间（消息堆积，前端反馈）
//        public void makeOrderDirect(Long userId, Long productId, int num) {
//
//            // 1: 定义交换机
//            String exchangeName = "direct_order_exchange";
//            // 2: 路由key
//            String routeKey = "sms";
//            // 3.模拟用户下单
//            String orderId = UUID.randomUUID().toString();
//
//            //4.给消息设置过期时间
//            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
//                @Override
//                public Message postProcessMessage(Message message) throws AmqpException {
//                    //设置过期时间，超过5秒消息就会消失
//                    message.getMessageProperties().setExpiration("5000");
//                    //设置编码格式
//                    message.getMessageProperties().setContentEncoding("UTF-8");
//                    return message;
//                }
//            };
//
//            //5. 通过MQ来完成消息的分发
//            //将messagePostProcessor对象放进发送消息的方法中即可
//            rabbitTemplate.convertAndSend(exchangeName, routeKey, orderId, messagePostProcessor);
        }
    }

//    public void send(Object msg) {
//        log.info("发送消息：" + msg);
////        rabbitTemplate.convertAndSend("queue", msg);
//        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);
//    }
//
//
//    public void send01(Object msg) {
//        log.info("发送red" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
//    }
//
//    public void send02(Object msg) {
//        log.info("发送red" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
//    }
//
//
//    public void send03(Object msg) {
//        log.info("发送消息(QUEUE01接收)：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "queue.red.message", msg);
//    }
//
//
//    public void send04(Object msg) {
//        log.info("发送消息(QUEUE02接收)：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "green.queue.green.message", msg);
//    }
//
//
//    public void send05(String msg) {
//        log.info("发送消息(QUEUE01和QUEUE02接收)：" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "fast");
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headersExchange", "", message);
//    }
//
//    public void send06(String msg) {
//        log.info("发送消息(QUEUE01接收)：" + msg);
//        MessageProperties properties = new MessageProperties();
//        properties.setHeader("color", "red");
//        properties.setHeader("speed", "normal");
//        Message message = new Message(msg.getBytes(), properties);
//        rabbitTemplate.convertAndSend("headersExchange", "", message);
//    }
