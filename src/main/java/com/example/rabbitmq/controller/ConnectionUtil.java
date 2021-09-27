package com.example.rabbitmq.controller;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @description
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class ConnectionUtil {
    /**
     * 获取连接
     * @return Connection
     * @throws Exception
     */
    public static Connection getConnection() throws Exception {
        //定义连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("你的ip");//mq安装位置的ip
        factory.setPort(5672);
        //设置vhost
        factory.setVirtualHost("/");
        factory.setUsername("admin");
        factory.setPassword("admin");
        //通过工厂获取连接
        Connection connection = factory.newConnection();
        return connection;
    }
}
