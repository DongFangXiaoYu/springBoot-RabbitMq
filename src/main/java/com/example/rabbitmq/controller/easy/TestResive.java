package com.example.rabbitmq.controller.easy;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;


/**
 * @description 1.简单模式一个生产者一个消费者
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class TestResive {
    //消费者消费消息
    public static void main(String[] args) throws Exception {
        //获取连接和通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        //声明通道
        channel.queueDeclare(TestSend.QUEUE_NAME,false,false,false,null);
        //定义消费者
        QueueingConsumer consumer = new QueueingConsumer(channel);
        //监听队列
        channel.basicConsume(TestSend.QUEUE_NAME,true,consumer);

        while(true){
            //这个方法会阻塞住，直到获取到消息
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println("接收到消息："+message);
        }
    }
}
