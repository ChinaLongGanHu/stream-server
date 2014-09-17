import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.to8to.commons.mq.IMessageHandler;
import com.to8to.commons.mq.RabbitMQConsumer;
import com.to8to.commons.mq.RabbitMQParam;
import com.to8to.commons.mq.RabbitMQProducer;


public class TestRabbitMQ {

	public static void send()
	{

		RabbitMQParam param = new RabbitMQParam().withHost("192.168.3.62");
		RabbitMQProducer prod = new RabbitMQProducer(param, "clickstream_data");		
		prod.send(MessageProperties.PERSISTENT_TEXT_PLAIN, "abc".getBytes());

		prod.close();
		
	}
	
	
	public static void recv()
	{
		RabbitMQParam param = new RabbitMQParam().withHost("192.168.3.62");
		
		RabbitMQConsumer con = new RabbitMQConsumer(param, "clickstream_data");
		
		con.pollNonBlock(new IMessageHandler(){

			@Override
			public boolean handle(Delivery delivery) throws Exception {

				String msg = new String(delivery.getBody());
				
				System.out.println("handle:" + msg);
				
				return true;

			}});
		
		con.close();
	}
	
	public static void main(String[] args) {

//		send();
		
//		recv();
//	    PutLogReq putLogReq = new PutLogReq();
//	    putLogReq.setCurrent_id("aaaaaaaaaaaaa");
//	    putLogReq.setUser_id("agbccc");
//	    putLogReq.setUser_type(2);
//	    putLogReq.setSession_id("uuuuuuuuuu");
//	    putLogReq.setUser_location("abc");
//	    putLogReq.setNetwork_type("2G");
//	    putLogReq.setVisit_resouce("http://www.baidu.com");
//	  //将前端发送的信息存储到RabbitMQ
//     JSONObject jsonObject = new JSONObject();
//     jsonObject.put("user_id",putLogReq.getUser_id());
//     jsonObject.put("user_type", putLogReq.getUser_type());
//     jsonObject.put("session_id", putLogReq.getSession_id());
//     jsonObject.put("user_location", putLogReq.getUser_location());
//     jsonObject.put("ip_address", putLogReq.getIp_address());
//     jsonObject.put("os_version", putLogReq.getOs_version());
//     jsonObject.put("os_type", putLogReq.getOs_type());
//     jsonObject.put("product_name", putLogReq.getProduct_name());
//     jsonObject.put("product_version", putLogReq.getProduct_version());
//     jsonObject.put("user_agent", putLogReq.getUser_agent());
//     jsonObject.put("explorer_version", putLogReq.getExplorer_version());
//     jsonObject.put("sp_type", putLogReq.getSp_type());
//     jsonObject.put("network_type", putLogReq.getNetwork_type());
//     jsonObject.put("visit_time", putLogReq.getVisit_time());
//     jsonObject.put("leave_time", putLogReq.getLeave_time());
//     jsonObject.put("event_type", putLogReq.getEvent_type());
//     jsonObject.put("event_name", putLogReq.getEvent_name());
//     jsonObject.put("visit_from",putLogReq.getVisit_from());
//     jsonObject.put("visit_resouce", putLogReq.getVisit_resouce());
//     jsonObject.put("device_type", putLogReq.getDevice_type());
//     jsonObject.put("device_id", putLogReq.getDevice_id());
//     jsonObject.put("display_solution", putLogReq.getDisplay_solution());
//     jsonObject.put("parent_id", putLogReq.getParent_id());
//     jsonObject.put("current_id", putLogReq.getCurrent_id());
//     String string = jsonObject.toJSONString();
//		    System.out.println(string);
		
		RabbitMQParam param = new RabbitMQParam().withHost("58.67.156.53");
		param.timeoutSec = 20;
		
		RabbitMQProducer prod = new RabbitMQProducer(param, "clickstream_data");
		
		prod.send(MessageProperties.PERSISTENT_BASIC, "111".getBytes());
	}

}
