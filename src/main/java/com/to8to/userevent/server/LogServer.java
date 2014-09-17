/**
 * @Title: LogServer.java
 * @Package com.to8to.userevent.server
 * @Description: TODO
 * Copyright: Copyright (c) 2014
 * Company:To8To
 *
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:52:25
 * @version V1.0
 */
package com.to8to.userevent.server;

import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.to8to.commons.utils.Config;
import com.to8to.kitt.SimpleThriftBizHandler;
import com.to8to.kitt.ThriftServer;
import com.to8to.kitt.ThriftServerChannelInitializer;
import com.to8to.userevent.thrift.LogService;

/**
 * @ClassName: LogServer
 * @Description: TODO
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:52:25
 *
 */
public class LogServer
{
    public static Logger logger = LoggerFactory.getLogger(LogServer.class);

    public static void main(String[] args)
    {
        String instance = "1";
        if (args.length > 0)
            instance = args[0];
        String configfile = "event_log_server_" + instance + ".properties";
        logger.info("config file: " + configfile);
        Config cf = new Config(configfile);
        final int port = cf.getInt("port", 38888);
        final String host = cf.get("host", null);
        logger.info("server param: " + " host: " + host + " port:" + port);
        LogIface iface = new LogIface();
        LogService.Processor<LogIface> processor = new LogService.Processor<LogIface>(
                iface);
        ThriftServerChannelInitializer channelInitilizer = new ThriftServerChannelInitializer(
                LogService.class, new TCompactProtocol.Factory(), false,
                new SimpleThriftBizHandler(iface, processor));
        ThriftServer.start(channelInitilizer, host, port, null);
    }

}
