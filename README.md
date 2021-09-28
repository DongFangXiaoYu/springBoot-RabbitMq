#### 1.安装配置

```java
查看mq镜像: docker search rabbitmq:management
下载mq镜像: docker pull rabbitmq:management
安装镜像:docker run -d --name rabbit -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin -p 15672:15672 -p 5672:5672 -p 25672:25672 -p 61613:61613 -p 1883:1883 rabbitmq:management

说明
5672:默认的客户端连接的端口
15672：默认的web管理界面的端口
 命令中的【RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin】是web管理平台的用户名和密码
【 -p 15672:15672】 是控制平台docker映射到系统的对应端口
【 -p 5672:5672】 是应用程序的访问端口

访问地址
http://ip:15672
```

如果是linux服务器，首先开放服务器端口，例如阿里云，先配置安全组：

![image-20210926180156983](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926180156983.png)

添加：

![image-20210926180312617](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926180312617.png)

**开始安装：**

查询 `docker search rabbitmq:management`

![image-20210926175821524](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926175821524.png)

下载 `docker search rabbitmq:management`

安装 `docker run -d --name rabbit -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin -p 15672:15672 -p 5672:5672 -p 25672:25672 -p 61613:61613 -p 1883:1883 rabbitmq:management`

![image-20210926175859729](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926175859729.png)

安装成功

![image-20210926180008591](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926180008591.png)

访问地址：
**http://你的ip:15672**

#### 2.测试

先创建一个连接类：

```java
package boot.spring.controller;

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
        factory.setHost("你的ip");
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

```



##### 2.1简单模式

一个生产者，一个消费者。

原理图：![image-20210927094435976](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927094435976.png)

发送：

```java
package boot.spring.controller.easy;

import boot.spring.controller.ConnectionUtil;
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

```

![image-20210926185732305](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926185732305.png)

生产的一条消息未被消费：

![image-20210926185843146](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926185843146.png)



接收：

```java
package boot.spring.controller.easy;

import boot.spring.controller.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @description 简单模式一个生产者一个消费者
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

```

![image-20210926185917182](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926185917182.png)

已被消费：

![image-20210926185945971](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210926185945971.png)



##### 2.2 work模式

竞争消费者模式

一个生产者，多个消费者，每个消费者获取到的消息唯一，生产的消息会被消费者瓜分。

原理图：![image-20210927094551158](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927094551158.png)

生产100条消息：

```java
package boot.spring.controller.work;

import boot.spring.controller.ConnectionUtil;
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
```

![image-20210927095421093](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927095421093.png)

消费者1：

```java
package boot.spring.controller.work;

import boot.spring.controller.ConnectionUtil;
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
```

**消费者1，消费了100条消息中的一半：**

![image-20210927095602819](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927095602819.png)

消费者2：

```java
package boot.spring.controller.work;

import boot.spring.controller.ConnectionUtil;
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
```

**消费者2消费了100条消息的另一半：**

![image-20210927095726835](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927095726835.png)

##### 2.3 订阅模式

生产者将消息发送到交换机，消费者从交换机获取消息。

原理图：![image-20210927100245937](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927100245937.png)

生产者发送消息到交换机：

```java
package boot.spring.controller.exchange;

import boot.spring.controller.ConnectionUtil;
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
```

生产者产生的消息：

![image-20210927101447291](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927101447291.png)

消费者1：

```java
package boot.spring.controller.exchange;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 3.订阅者模式：一个生产者发送的消息会被多个消费者获取 * @AUTHER: sk * @DATE: 2021/9/26 **/public class Resive1 {    //消费者1    public final static String QUEUE_NAME = "test_queue_exchange_1";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上        channel.queueBind(QUEUE_NAME,Send.EXCHANGE_NAME,"");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("消费者1："+message);        }    }}
```

![image-20210927101551786](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927101551786.png)

消费者2：

```java
package boot.spring.controller.exchange;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 3.订阅者模式：一个生产者发送的消息会被多个消费者获取 * @AUTHER: sk * @DATE: 2021/9/26 **/public class Resive2 {    //消费者2    public final static String QUEUE_NAME = "test_queue_exchange_2";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上        channel.queueBind(QUEUE_NAME,Send.EXCHANGE_NAME,"");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("消费者2："+message);        }    }}
```

![image-20210927101633853](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927101633853.png)

由此可见，订阅者模式中，所有的消费者都通过交换机收到了消息。

##### 2.4 路由模式

生产者发送消息到队列中时可自定义一个key，消费者可根据key去选择对应的消息，各取所需。
注意：路由key，**是一种完全匹配**，只有匹配到的消费者才能消费消息。

原理图：![image-20210927102501361](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927102501361.png)

生产者生产带key的消息：（key=“dog”）

```java
package boot.spring.controller.rout;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;/** * @description 4.路由模式:发送消息到交换机并且要指定路由key * @AUTHER: sk * @DATE: 2021/9/26 **/public class RoutSend {    public static final String EXCHANGE_NAME = "test_exchange_direct";    //生产者    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        //声明交换机 fanout：交换机类型 主要有fanout,direct,topics三种        channel.exchangeDeclare(EXCHANGE_NAME,"direct");        String message = "路由模式产生的消息!";        channel.basicPublish(EXCHANGE_NAME,"dog",null,message.getBytes());        System.out.println(message);        channel.close();        connection.close();    }}
```

![image-20210927102827292](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927102827292.png)

消费者1：(key=“dog”)

```java
package boot.spring.controller.rout;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 4.路由模式:消费者将队列绑定到交换机时需要指定路由key * @AUTHER: sk * @DATE: 2021/9/26 **/public class RoutResive1 {    //消费者1    public final static String QUEUE_NAME = "test_queue_direct_1";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上,并制定路由键为"dog"        channel.queueBind(QUEUE_NAME, RoutSend.EXCHANGE_NAME,"dog");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("RoutResive1:"+message);        }    }}
```

![image-20210927103033299](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927103033299.png)

消费者2：（key=“cat”）

```java
package boot.spring.controller.rout;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 4.路由模式:消费者将队列绑定到交换机时需要指定路由key * @AUTHER: sk * @DATE: 2021/9/26 **/public class RoutResive2 {    //消费者2    public final static String QUEUE_NAME = "test_queue_direct_2";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上,并制定路由键为"cat"        channel.queueBind(QUEUE_NAME, RoutSend.EXCHANGE_NAME,"cat");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("RoutResive2:"+message);        }    }}
```

![image-20210927103119368](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927103119368.png)

很显然，消费者1获取到了消息，消费者2并没有获取到消息，因为消费者2的key与生产者的key不一致。

##### 2.5 通配符模式

原理和路由模式类似，只是key值作了模糊匹配而已。

- *（星号）可以正好代替一个词。

- \# (hash) 可以代替零个或多个单词

- topic交换器通过模式匹配分配消息的路由键属性，将路由键和某个模式进行匹配，此时队列需要绑定到一个模式上。它将路由键和绑定键的字符串切分成单词，这些单词之间用点隔开。它同样也会识别两个通配符：符号“#”和符号“*”。#匹配0个或多个单词，*匹配一个单词。如下图所示：

  ![20180628164513643](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/20180628164513643.png)

原理图：![image-20210927103418777](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927103418777.png)

生产者产生消息：

```java
package boot.spring.controller.topic;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;/** * @description 5.路由模式:发送消息到交换机并且要指定通配符路由 * @AUTHER: sk * @DATE: 2021/9/26 **/public class TopicSend {    //生产者    public static final String EXCHANGE_NAME = "test_exchange_topic";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        //声明交换机 topic：交换机类型        channel.exchangeDeclare(EXCHANGE_NAME,"topic");        String message = "通配符模式产生的消息";        channel.basicPublish(EXCHANGE_NAME,"dog.1",null,message.getBytes());        System.out.println(message);        channel.close();        connection.close();    }}
```

![image-20210927104718676](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927104718676.png)

消费者1：

```java
package boot.spring.controller.topic;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 5.路由模式:消费者将队列绑定到交换机时需要指定通配符路由key * @AUTHER: sk * @DATE: 2021/9/26 **/public class TopicResive1 {    //消费者1    public final static String QUEUE_NAME = "test_queue_topic_1";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上,并制定路由键匹配规则为"dog.*"        channel.queueBind(QUEUE_NAME, TopicSend.EXCHANGE_NAME,"dog.*");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("TopicResive1:"+message);        }    }}
```

![image-20210927104759387](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927104759387.png)

消费者2：

```java
package boot.spring.controller.topic;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 5.路由模式:消费者将队列绑定到交换机时需要指定通配符路由key * @AUTHER: sk * @DATE: 2021/9/26 **/public class TopicResive2 {    //消费者2    public final static String QUEUE_NAME = "test_queue_topic_2";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上,并制定路由键匹配规则为"#.1"        channel.queueBind(QUEUE_NAME, TopicSend.EXCHANGE_NAME,"#.1");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("TopicResive2:"+message);        }    }}
```

![image-20210927104900197](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927104900197.png)

消费者3：

```java
package boot.spring.controller.topic;import boot.spring.controller.ConnectionUtil;import com.rabbitmq.client.Channel;import com.rabbitmq.client.Connection;import com.rabbitmq.client.QueueingConsumer;/** * @description 5.路由模式:消费者将队列绑定到交换机时需要指定通配符路由key * @AUTHER: sk * @DATE: 2021/9/26 **/public class TopicResive3 {    //消费者3    public final static String QUEUE_NAME = "test_queue_topic_3";    public static void main(String[] args) throws Exception {        Connection connection = ConnectionUtil.getConnection();        Channel channel = connection.createChannel();        channel.queueDeclare(QUEUE_NAME,false,false,false,null);        //绑定队列到交换机上,并制定路由键匹配规则为"cat.#"        channel.queueBind(QUEUE_NAME, TopicSend.EXCHANGE_NAME,"cat.#");        channel.basicQos(1);        QueueingConsumer consumer = new QueueingConsumer(channel);        channel.basicConsume(QUEUE_NAME,true,consumer);        while(true){            QueueingConsumer.Delivery delivery = consumer.nextDelivery();            String message = new String(delivery.getBody());            System.out.println("TopicResive3:"+message);        }    }}
```

![image-20210927104935796](https://shukai.oss-cn-hangzhou.aliyuncs.com/Typora/image-20210927104935796.png)

结果：消费者1和消费者2可以收到消息，消费者3不能收到消息。









