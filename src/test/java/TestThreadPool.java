import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.to8to.commons.mq.IMessageHandler;
import com.to8to.commons.mq.RabbitMQConsumer;
import com.to8to.commons.mq.RabbitMQParam;

public class TestThreadPool
{

    public static void main(String[] args)
    {

        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++)
        {
            threadPool.submit(new Runnable()
            {
                public void run()
                {
                    RabbitMQParam param = new RabbitMQParam()
                            .withHost("192.168.3.62");

                    RabbitMQConsumer con = new RabbitMQConsumer(param,
                            "clickstream_data");
                    con.pollBlock(new IMessageHandler()
                    {
                        @Override
                        public boolean handle(Delivery delivery)
                                throws Exception
                        {
                            String msg = new String(delivery.getBody());
                            // 持久化数据到mongdb
                            return true;
                        }

                    });
                }
            });
        }
    }

}
