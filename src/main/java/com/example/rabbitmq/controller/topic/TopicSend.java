package com.example.rabbitmq.controller.topic;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @description 5.路由模式:发送消息到交换机并且要指定通配符路由
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class TopicSend {
    //生产者
    public static final String EXCHANGE_NAME = "test_exchange_topic";

    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        //声明交换机 topic：交换机类型
        channel.exchangeDeclare(EXCHANGE_NAME,"topic");
        String message = "通配符模式产生的消息";
        channel.basicPublish(EXCHANGE_NAME,"dog.1",null,message.getBytes());
        System.out.println(message);
        channel.close();
        connection.close();
    }

}
