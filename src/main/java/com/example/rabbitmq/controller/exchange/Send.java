package com.example.rabbitmq.controller.exchange;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @description 3.订阅者模式：一个生产者发送的消息会被多个消费者获取
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class Send {
    public static final String EXCHANGE_NAME = "test_exchange_fanout";
    //生产者，发送消息到交换机
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        //声明交换机 fanout：交换机类型 主要有fanout,direct,topics三种
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");

        String message = "订阅模式：消息007!";
        channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes());
        System.out.println(message);
        channel.close();
        connection.close();
    }

}
