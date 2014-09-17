import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.protocol.TCompactProtocol;

import com.to8to.commons.utils.ThriftUtil;
import com.to8to.kitt.ThriftMessageCodec;
import com.to8to.userevent.thrift.LogService;
import com.to8to.userevent.thrift.PutLogReq;
import com.to8to.userevent.thrift.UserEvent;


public class TestThriftEncode {

	public static void main(String[] args) {

		ThriftMessageCodec codec = new ThriftMessageCodec(LogService.class, new TCompactProtocol.Factory());
		
		short number = 1;
		
		PutLogReq req = new PutLogReq();
		
        req.setUid("ABCDEFG");
        //req.setUt(number);
        req.setSid("abcdefg");
        req.setUl("jjjjjjjjjjjj");
        req.setIp("192.168.3.162");
        req.setOsv(number);
        req.setOst(number);
        req.setPn("fffffffffffff");
        req.setPv("xxxxxxxxxxxxx");
        req.setUa(number);
        req.setEv("uuuuuuuuuuuu");
        req.setSt(number);
        req.setNt(number);
        req.setDt(number);
        req.setDi("xxxxxxxxxxx");
        req.setDs("jamesjamesjamesjames");
        req.setLt("12:30:30");
        List<UserEvent> e =new ArrayList<UserEvent>(); 
        UserEvent event = new UserEvent();
        event.setVt("abcdedf");
        event.setEt(number);
        event.setCi("abcdeffff");
        event.setEn("aiaiaiaiaiaia");
        event.setVr("vrvrvrvrvr");
        e.add(event);
        UserEvent event2 = new UserEvent();
        event2.setVt("abcdedf");
        event2.setEt(number);
        event2.setCi("abcdeffff");
        event2.setEn("aiaiaiaiaiaia");
        event2.setVr("vrvrvrvrvr");
        e.add(event2);
        req.setE(e);

		System.out.println(req);
		
		byte[] bytes = codec.encode(req);
		
//		System.out.println(Object2Json.toJsonString(req).length());
		
		System.out.println(bytes.length);
		
		PutLogReq req2 = new PutLogReq();
		
		codec.decode(bytes, req2);

		System.out.println(req2);
		
		String json = ThriftUtil.thriftObject2Json(req);
		
//		ThriftUtil.thriftObject2JsonObject(req);
		
		System.out.println(json);
	}

}
