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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.MessageProperties;
import com.to8to.commons.mq.RabbitMQParam;
import com.to8to.commons.mq.RabbitMQProducer;
import com.to8to.commons.utils.Config;
import com.to8to.commons.utils.StringUtil;
import com.to8to.kitt.ThriftMessageCodec;
import com.to8to.userevent.thrift.LogService;
import com.to8to.userevent.thrift.PutLogReq;
import com.to8to.userevent.thrift.UserEvent;

/**
 * @ClassName: LogIface
 * @Description: TODO
 * @author JAMES-LIU
 * @date 2014年8月28日 下午4:50:21
 *
 */
public class LogIface implements LogService.Iface
{
    public static Logger       logger          = LoggerFactory
                                                       .getLogger(LogIface.class);
    private RabbitMQParam      param           = new RabbitMQParam();
    private RabbitMQProducer   prod            = null;
    private static Set<Integer>         ov_version_set  = new HashSet<Integer>();
    private static Set<Integer>         ov_type_set     = new HashSet<Integer>();
    private static Set<Integer>         user_agent_set  = new HashSet<Integer>();
    private static Set<Integer>         device_type_set = new HashSet<Integer>();

    private ThriftMessageCodec codec           = new ThriftMessageCodec(
                                                       LogService.class,
                                                       new TCompactProtocol.Factory());
    /**
    * Title: 
    * Description:构造函数初始化config参数，包含rabbitmq,MongDB，点击流需要校验的参数
     */
    public LogIface()
    {
        Config cf = new Config("event_log_writer.properties");
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
        
        Config config = new Config("userevent.properties");
        String[] os_version = config.get("os_version").split(",");
        for (int i = 0; i < os_version.length; i++)
        {
            ov_version_set.add(Integer.parseInt(os_version[i]));
        }
        String[] os_type = config.get("os_type").split(",");
        for (int i = 0; i < os_type.length; i++)
        {
            ov_type_set.add(Integer.parseInt(os_type[i]));
        }
        String[] user_agent = config.get("user_agent").split(",");
        for (int i = 0; i < user_agent.length; i++)
        {
            user_agent_set.add(Integer.parseInt(user_agent[i]));
        }
        String[] device_type = config.get("device_type").split(",");
        for (int i = 0; i < device_type.length; i++)
        {
            device_type_set.add(Integer.parseInt(device_type[i]));
        }
    }

    @Override
    public void putLog(PutLogReq putLogReq) throws TException
    {
        
        //数据校验，不符合格式的数据，不能进入rabbitmq
        
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
        
        //不能为空入队列的putLogReq
        if (putLogReq == null)
        {
            logger.debug("validateReq putLogReq is null");
            return null;
        }
        else
        {
            logger.debug("putLogReq toString: "+putLogReq.toString()); 
        }
            
        //操作系统os_version的范围必须在配置文件userevent.properties定义的数值范围内
        int os_version = putLogReq.getOsv();
        
        logger.debug("validateReq os_version: "+os_version);
        
        if (!ov_version_set.contains(os_version))
        {
            logger.debug("validateReq os_version is invalidate");
            return null;
        }

        //操作系统os_type的范围必须在配置文件userevent.properties定义的数值范围内
        int os_type = putLogReq.getOst();
        
        logger.debug("validateReq os_type: "+os_type);
        
        if (!ov_type_set.contains(os_type))
        {
            logger.debug("validateReq os_type is invalidate");
            return null;
        }
        
        //操作系统ua(user_agent)的范围必须在配置文件userevent.properties定义的数值范围内
        int ua = putLogReq.getUa();
        
        logger.debug("validateReq ua: "+ua);
        
        if (!user_agent_set.contains(ua))
        {
            logger.debug("validateReq ua is invalidate");
            return null;
        }
            
        //校验device_type在配置文件userevent.properties定义的范围内
        int dt = putLogReq.getDt();
        
        logger.debug("validateReq dt: "+dt);
        
        if (!device_type_set.contains(dt))
        {
            logger.debug("validateReq dt is invalidate");
            return null;
        }
        
        //校验用户leave_time不为空，而且取值范围要大于当天的开始时间的毫秒数，小于当天结束时间的毫秒数
        
        logger.debug("validateReq putLogReq.getLt(): "+putLogReq.getLt());
        
        if (StringUtil.isEmpty(putLogReq.getLt()))
        {
            logger.debug("validateReq putLogReq.getLt() isEmpty: "+putLogReq.toString());
            return null;
        }
        
        if(!validateTime(putLogReq.getLt()))
        {
            logger.debug("validateReq putLogReq.getLt() is invalidate");
            return null;
        }
            
        //校验一条接受到的数据中，其中记录的用户日志数据，至少包含一条PV的数据
        if (putLogReq.getE() == null || putLogReq.getE().size() <= 0)
        {
            logger.debug("validateReq putLogReq.getE() is invalidate");
            return null;
        }
        
        //校验接受数据中，PV和CV的具体参数，主要是校验CV的ID不为空，visit_time不为空,visit_resouce不是空
        
        List<UserEvent> e = putLogReq.getE();
        
        List<UserEvent> e2 = new ArrayList<UserEvent>();
        
        for (UserEvent ue : e)
        {
            //校验CV的ID不为空，visit_time不为空,visit_resouce不是空
            if (!StringUtil.isEmpty(ue.getCi())
                    && !StringUtil.isEmpty(ue.getVt()) && !StringUtil.isEmpty(ue.getVr()))
            {
                //校验用户visit_time不为空，而且取值范围要大于当天的开始时间的毫秒数，小于当天结束时间的毫秒数
                if(validateTime(ue.getVt()))
                {
                    //经过两次if检查后，符合条件的数据，再加入到list中
                    e2.add(ue);
                }
            }
        }

        if (e2.size() > 0)
        {
            putLogReq.setE(e2);
            logger.debug("putLogReq setE size: "+putLogReq.getE().size());
        }
        else
        {
            putLogReq = null;
        }

        return putLogReq;
    }

    /**
     * 
       * @method: validateTime
       * @Description: 校验入队列的时间，必须符合时间格式yyyyMMddHHmmss.SSS，而且时间转换成毫秒后，小于当天的结束时间，大于当天的开始时间
       * @param @param nowtime
       * @param @return    
       * @return boolean
       * @throws
     */
    public static boolean validateTime(String nowtime)
    {
        if(matchTime(nowtime))
        {
            Long currentTime = getNowTime(nowtime);
            Long timenight = getTimesnight();
            Long timemorning = getTimesmorning();
            logger.debug("currentTime: "+currentTime+" timemorning: "+timemorning+" timenight: "+timenight);
            if(currentTime<=timenight && currentTime>=timemorning)
            {
                return true;
            }
            else
            {
                logger.debug("convertTime nowtime's format is not in the range");
                return false;
            }

        }
        else
        {
            logger.debug("convertTime nowtime's format is wrong");
        }
        
        return false;
    }
    
    /**
     * 
       * @method: matchTime
       * @Description: 使用正则表达式，校验时间的string格式 yyyyMMddHHmmss.SSS
       * @param @param nowtime
       * @param @return    
       * @return boolean
       * @throws
     */
    public static boolean matchTime(String nowtime)
    {
        String patterns= "(^[0-9]{14})\\.([0-9]{0,3})?$";
        Pattern pattern = Pattern.compile(patterns);
        Matcher matcher = pattern.matcher(nowtime);
        return matcher.matches();
    }
    
    /**
     * 
       * @method: getTimesnight
       * @Description: 获取当天的第二天开始的0点时间，毫秒单位
       * @param @return    
       * @return Long
       * @throws
     */
    public static Long getTimesnight()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * 
       * @method: getTimesmorning
       * @Description: 获取当天时间开始的毫秒时间
       * @param @return    
       * @return Long
       * @throws
     */
    public static Long getTimesmorning()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * 
       * @method: getNowTime
       * @Description: 获取入参的时间，转换为毫秒后的时间
       * @param @param nowtime
       * @param @return    
       * @return Long
       * @throws
     */
    public static Long getNowTime(String nowtime)
    {
        Date d = null;
        SimpleDateFormat sf = null;
        try
        {
            String[] nowtimeArray = nowtime.split("\\.");
            //入参时间格式在.之后，有数据的，类似20140910231159.19
            if(nowtimeArray.length==2)
            {
                sf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
                d = sf.parse(nowtime);
                return d.getTime();
            }
            else if(nowtimeArray.length==1)//入参时间格式在.之后，有数据的，类似20140910231159.或者20140910231159
            {
                sf = new SimpleDateFormat("yyyyMMddHHmmss");
                d = sf.parse(nowtimeArray[0]);
                return d.getTime();
            }
        }
        catch (Exception e)
        {
            logger.error("getNowTime parse time error"+e.getMessage());
        }
        return 0L;
    }

    @Override
    public String test(String hello) throws TException
    {
        return null;
    }
    
}
