package com.example.rabbitmq.controller.topic;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @description 5.路由模式:消费者将队列绑定到交换机时需要指定通配符路由key
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class TopicResive3 {

    //消费者3
    public final static String QUEUE_NAME = "test_queue_topic_3";
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //绑定队列到交换机上,并制定路由键匹配规则为"cat.#"
        channel.queueBind(QUEUE_NAME, TopicSend.EXCHANGE_NAME,"cat.#");
        channel.basicQos(1);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(QUEUE_NAME,true,consumer);

        while(true){
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println("TopicResive3:"+message);
        }
    }


}
