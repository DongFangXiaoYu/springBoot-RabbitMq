package com.example.rabbitmq.controller.work;

import com.example.rabbitmq.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * @description 2.work模式:一个生产者多个消费者
 * @AUTHER: sk
 * @DATE: 2021/9/26
 **/

public class WorkSend2 {
    public final static String QUEUE_NAME = "test2";
    //消息生产者
    public static void main(String[] args) throws Exception {
        //获取连接和通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        String message = "";
        for(int i = 0; i<100; i++){
            message = "" + i;
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
            System.out.println("发送消息："+message);
            Thread.sleep(i);
        }

        channel.close();
        connection.close();
    }


}
