/**
 * @Title: LogTest.java
 * @Package com.to8to.userevent.client
 * @Description: TODO
 * Copyright: Copyright (c) 2014
 * Company:To8To
 *
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:56:26
 * @version V1.0
 */
package com.to8to.userevent.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.to8to.commons.utils.Config;
import com.to8to.userevent.thrift.LogService;
import com.to8to.userevent.thrift.PutLogReq;
import com.to8to.userevent.thrift.UserEvent;
import com.to8to.userevent.writer.EventLogWriter;

/**
 * @ClassName: LogTest
 * @Description: TODO
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:56:26
 *
 */
public class LogTest
{

    public static Logger serverlogger   = LoggerFactory.getLogger(EventLogWriter.class);
    
    TTransport        transport;
    TProtocol         protocol;
    LogService.Client client;

    public boolean open(String host, int port)
    {
        transport = new TSocket(host, port);
        transport = new TFramedTransport(transport);
        try
        {
            transport.open();
        }
        catch (TTransportException e)
        {
            return false;
        }

        protocol = new TCompactProtocol(transport);
        client = new LogService.Client(protocol);
        return true;
    }

    public void test(int i)
    {
        
        short number = 1;
        PutLogReq putLogReq = new PutLogReq();
        putLogReq.setUid("ABCDEFG"+i);
        putLogReq.setCid("ABCDEFG");
        putLogReq.setSid("ABCDEFG");
        putLogReq.setUl("ABCDEFG");
        putLogReq.setIp("192.168.3.555");
        putLogReq.setOsv((short)0);//V 0,1,2,3,4,5,6,7,8,9,404
        putLogReq.setOst((short) 5);//V 0,1,2,3,4,5,404
        putLogReq.setPn("ABCDEFG");
        putLogReq.setPv("ABCDEFG");
        putLogReq.setUa((short)4);//V 0,1,2,3,4,5,404
        putLogReq.setEv("ABCDEFG");
        putLogReq.setDt(number);//V 0,1,2,3,4,404
        putLogReq.setDi("ABCDEFG");
        putLogReq.setDs("ABCDEFG");
        putLogReq.setLt("20140919174559.");//V

        List<UserEvent> e = new ArrayList<UserEvent>();

        UserEvent event = new UserEvent();
        event.setVt("20140919174559.55");//V
        event.setEt(number);
        event.setCi("ABCDEFG");
        event.setEn("ABCDEFG");
        event.setVr("ABCDEFG");
        e.add(event);

        //V
        UserEvent event2 = new UserEvent();
        event2.setVt("20140919174559.55");//V
        event2.setEt(number);
        event2.setCi("ABCDEFG");
        event2.setEn("ABCDEFG");
        event2.setVr("ABCDEFG");
        e.add(event2);

        putLogReq.setE(e);

        try
        {
            client.putLog(putLogReq);
        }
        catch (TException ception)
        {
            serverlogger.error(ception.getMessage(), ception.getCause());
        }
    }

    public boolean close()
    {
        transport.close();
        return true;
    }

    /**
     * @param args
     * @throws TException
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws TException, InterruptedException
    {
        try
        {
            
            String instance = "1";
            if (args.length > 0)
                instance = args[0];
            String configfile = "event_log_server_" + instance + ".properties";
            serverlogger.info("config file: " + configfile);
            Config cf = new Config(configfile);
            final int port = cf.getInt("port", 28888);
            final String host = cf.get("host", null);
            
            serverlogger.info("server param: " + " host: " + host + " port:" + port);
            
            LogTest c = new LogTest();
            for(int i=0;i<500;i++)
            {
                c.open(host, port);
                c.test(i);
                c.close();
                Thread.currentThread().sleep(100);
                serverlogger.info("========================"+i+"========================");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            serverlogger.error("log test error: "+e.getMessage());
            serverlogger.error("log test error2: "+e.getStackTrace());
        }
        
        
    }

}
