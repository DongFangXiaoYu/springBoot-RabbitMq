package com.example.rabbitmq.controller.rout;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @description 4.路由模式:消费者将队列绑定到交换机时需要指定路由key
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class RoutResive2 {

    //消费者2
    public final static String QUEUE_NAME = "test_queue_direct_2";
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //绑定队列到交换机上,并制定路由键为"cat"
        channel.queueBind(QUEUE_NAME, RoutSend.EXCHANGE_NAME,"cat");
        channel.basicQos(1);
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(QUEUE_NAME,true,consumer);
        while(true){
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println("RoutResive2:"+message);
        }
    }


}
