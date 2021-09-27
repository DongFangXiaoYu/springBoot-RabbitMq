package com.example.rabbitmq.controller.rout;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @description 4.路由模式:发送消息到交换机并且要指定路由key
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class RoutSend {
    public static final String EXCHANGE_NAME = "test_exchange_direct";
    //生产者
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        //声明交换机 fanout：交换机类型 主要有fanout,direct,topics三种
        channel.exchangeDeclare(EXCHANGE_NAME,"direct");

        String message = "路由模式产生的消息!";
        channel.basicPublish(EXCHANGE_NAME,"dog",null,message.getBytes());
        System.out.println(message);
        channel.close();
        connection.close();
    }


}
