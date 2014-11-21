/**
 * @Title: LogIface.java
 * @Package com.to8to.userevent.server
 * @Description: TODO
 * Copyright: Copyright (c) 2014
 * Company:To8To
 *
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:50:21
 * @version V1.0
 */
package com.to8to.userevent.server;

import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.rabbitmq.client.MessageProperties;
import com.to8to.commons.mq.RabbitMQParam;
import com.to8to.commons.mq.RabbitMQProducer;
import com.to8to.commons.utils.Config;
import com.to8to.commons.utils.StringUtil;
import com.to8to.kitt.ThriftMessageCodec;
import com.to8to.userevent.thrift.LogService;
import com.to8to.userevent.thrift.PutLogReq;

/**
 * @ClassName: LogIface
 * @Description: TODO
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:50:21
 *
 */
public class LogIface implements LogService.Iface
{
    public static Logger                 logger          = LoggerFactory
                                                                 .getLogger(LogServer.class);
    private RabbitMQParam                param           = new RabbitMQParam();
    private RabbitMQProducer             prod            = null;

    private ThriftMessageCodec           codec           = new ThriftMessageCodec(
                                                                 LogService.class,
                                                                 new TCompactProtocol.Factory());

    private static Cache<String, String> cache           = null;

    /**
     * Title: Description:构造函数初始化config参数，包含rabbitmq,MongDB，点击流需要校验的参数
     */
    public LogIface()
    {

        Config cf = new Config("event_log_writer.properties");

        int expireTime = cf.getInt("ip.expireTime", 3);
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS).build();

        String host = cf.get("rabbitmq.host");
        param.withHost(host);
        String user = cf.get("rabbitmq.user");
        String password = cf.get("rabbitmq.password");
        String queue = cf.get("rabbitmq.queue");
        if (!StringUtil.isEmpty(password))
            param.setPassword(password);
        if (!StringUtil.isEmpty(user))
            param.setUser(user);
        logger.debug("event_log_writer.properties param:" + param.toString());
        prod = new RabbitMQProducer(param, queue); 
    }

    @Override
    public void putLog(PutLogReq putLogReq) throws TException
    {
        // 数据校验，不符合格式的数据，不能进入rabbitmq
        putLogReq = validateReq(putLogReq);
        if (putLogReq != null)
        {
            logger.debug("putLog message string:" + putLogReq);
            byte[] bytes = codec.encode(putLogReq);
            prod.send(MessageProperties.PERSISTENT_BASIC, bytes);
        }
        else
        {
            logger.debug("putLog message putLogReq format is invalidate");
        }
    }

    /**
     * 
     * @method: validateReq
     * @Description: 校验入队列的参数的格式是否符合要求
     * @param @param putLogReq
     * @param @return
     * @return PutLogReq
     * @throws
     */
    public static PutLogReq validateReq(PutLogReq putLogReq)
    {

        // 不能为空入队列的putLogReq
        if (putLogReq == null)
        {
            logger.debug("validateReq putLogReq is null");
            return null;
        }
        else
        {
            logger.debug("putLogReq toString: " + putLogReq.toString());
        }

        // 校验用户ipadress不为空，而且在一定的时间范围内，如果连续发送数据，则拒绝此条数据
        logger.debug("validateReq putLogReq.getIp(): " + putLogReq.getIp());

        if (StringUtil.isEmpty(putLogReq.getIp()))
        {
            logger.debug("validateReq ipadress is null");
            return null;
        }
        else if (!validateIp(putLogReq.getIp()))
        {
            logger.debug("validateReq ipadress is null");
            return null;
        }

        return putLogReq;
    }
    /**
     * 
     * @method: validateIp
     * @Description: 校验入队列的IP是否符合要求，给IP设置一个过期时间，如果在此过期时间内，连续收到此iP发送的数据，则拒绝此IP
     * @param @param ip
     * @param @return
     * @return boolean
     * @throws
     */
    public static boolean validateIp(String ip)
    {
        if (cache.getIfPresent(ip) == null)
        {
            cache.put(ip, ip);
            return true;
        }
        return false;
    }

    @Override
    public String test(String hello) throws TException
    {
        return null;
    }

}
