package com.to8to.userevent.writer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.to8to.commons.mongo.DBParam;
import com.to8to.commons.mongo.MongoEntityClient;
import com.to8to.commons.mq.IMessageHandler;
import com.to8to.commons.mq.RabbitMQConsumer;
import com.to8to.commons.mq.RabbitMQParam;
import com.to8to.commons.utils.Config;
import com.to8to.commons.utils.StringUtil;
import com.to8to.commons.utils.ThriftUtil;
import com.to8to.kitt.ThriftMessageCodec;
import com.to8to.userevent.thrift.LogService;
import com.to8to.userevent.thrift.PutLogReq;

public class EventLogWriter implements IMessageHandler
{
	public static Logger logger = LoggerFactory.getLogger(EventLogWriter.class);

	MongoEntityClient mongo;

	String tablename = "UserEventLog";

	ThriftMessageCodec codec = new ThriftMessageCodec(LogService.class, new TCompactProtocol.Factory());

    public EventLogWriter()
    {
        Config cf = new Config("event_log_writer.properties");
        tablename = cf.get("mongdb.tablename");
        String host = cf.get("mongdb.host");
        String database = cf.get("mongdb.database");
        String user = cf.get("mongdb.user");
        if (StringUtil.isEmpty(user))
            user = null;
        String password = cf.get("mongdb.password");
        if (StringUtil.isEmpty(password))
            password = null;
        DBParam param = new DBParam(host, user, password, database);
        logger.debug("init dbparam param: " + param.toString());
        mongo = new MongoEntityClient(param);
    }

    public void removeNullKeys(BasicDBObject obj)
    {
        List<String> removeKeys = new LinkedList<String>();

        for (String k : obj.keySet())
        {
            Object v = obj.get(k);

            if (obj.get(k) == null)
            {
                removeKeys.add(k);
            }

            if (v instanceof String)
            {
                String vstring = (String) v;
                if (StringUtil.isEmpty(vstring))
                {
                    removeKeys.add(k);
                }
            }
        }

        for (String k : removeKeys)
            obj.remove(k);
    }

    @Override
    public boolean handle(Delivery delivery)
    {

        try
        {
            byte[] bytes = delivery.getBody();

            PutLogReq req = new PutLogReq();

            codec.decode(bytes, req);

            String json = ThriftUtil.thriftObject2Json(req);

            logger.debug("handle json: " + json);

            BasicDBObject o = (BasicDBObject) JSON.parse(json);

            removeNullKeys(o);

            BasicDBList list = (BasicDBList) o.get("e");

            for (Object eobj : list)
            {
                BasicDBObject obj = (BasicDBObject) eobj;
                removeNullKeys(obj);
            }

            o.put("_ct", System.currentTimeMillis());

            WriteResult wr = mongo.insert(tablename, o);

            if (wr == null)
                logger.error("insert failed: " + o);
            else
                logger.debug("insert success!");
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e.getCause());
        }
        return true;
    }

    public static void main(String[] args)
    {
        Config cf = new Config("event_log_writer.properties");
        String host = cf.get("rabbitmq.host");
        final RabbitMQParam param = new RabbitMQParam();
        param.withHost(host);
        String user = cf.get("rabbitmq.user");
        String password = cf.get("rabbitmq.password");
        final String queue = cf.get("rabbitmq.queue");
        int threadPoolSize = Integer
                .parseInt(cf.get("rabbitmq.threadPoolSize"));
        if (!StringUtil.isEmpty(password))
            param.setPassword(password);
        if (!StringUtil.isEmpty(user))
            param.setUser(user);
        logger.debug("rabbitmq param: " + param.toString());
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < threadPoolSize; i++)
        {
            threadPool.submit(new Runnable()
            {
                public void run()
                {
                    RabbitMQConsumer con = new RabbitMQConsumer(param, queue);
                    con.pollBlock(new EventLogWriter());
                }
            });
        }
        threadPool.shutdown();
        try
        {
            while (!threadPool.awaitTermination(10000, TimeUnit.MILLISECONDS))
            {
                Thread.sleep(10000);
            }
        }
        catch (InterruptedException e)
        {
            logger.error(e.getMessage(), e.getCause());
        }
    }

}
