package com.example.rabbitmq.controller.easy;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @description 1.简单模式:一个生产者一个消费者
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class TestSend {
    public final static String QUEUE_NAME = "test-queue";
    //创建队列，发送消息
    public static void main(String[] args) throws Exception {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //创建通道
        Channel channel = connection.createChannel();
        //声明创建队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //消息内容
        String message = "Hello World!";
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        System.out.println("发送消息："+message);
        //关闭连接和通道
        channel.close();
        connection.close();
    }

}
