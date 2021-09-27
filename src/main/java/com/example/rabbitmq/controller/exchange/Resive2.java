package com.example.rabbitmq.controller.exchange;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @description 3.订阅者模式：一个生产者发送的消息会被多个消费者获取
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class Resive2 {
    //消费者2
    public final static String QUEUE_NAME = "test_queue_exchange_2";
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //绑定队列到交换机上
        channel.queueBind(QUEUE_NAME,Send.EXCHANGE_NAME,"");
        channel.basicQos(1);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(QUEUE_NAME,true,consumer);

        while(true){
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println("消费者2："+message);
        }
    }


}
