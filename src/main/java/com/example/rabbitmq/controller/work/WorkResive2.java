package com.example.rabbitmq.controller.work;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @description 2.work模式:一个生产者多个消费者
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class WorkResive2 {

    //消费者2  手动模式
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("test2",false,false,false,null);

        channel.basicQos(1);

        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.basicConsume("test2",true,consumer);

        while(true){
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println("recive1:"+message);
            Thread.sleep(10);
            //channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
        }
    }

}
