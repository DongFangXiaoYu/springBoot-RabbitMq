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

public class WorkResive1 {

    //消费者1  自动模式
    public static void main(String[] args) throws Exception {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(WorkSend2.QUEUE_NAME,false,false,false,null);

        //同一时刻服务器只发送一条消息给消费端
        channel.basicQos(1);

        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.basicConsume(WorkSend2.QUEUE_NAME,false,consumer);

        while(true){
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println("recive1:"+message);
            Thread.sleep(100);
            //消息消费完给服务器返回确认状态，表示该消息已被消费
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
        }
    }

}
