import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.Gson;
import com.to8to.commons.utils.Object2Json;
import com.to8to.userevent.thrift.PutLogReq;


public class TestJackson {

	public static void main(String[] args)
	{
		ObjectMapper mapper = new ObjectMapper();
		
		PutLogReq r = new PutLogReq();
		
		try {
			String str = mapper.writeValueAsString(r);
			
			System.out.println(str);
			
			Gson gson = new Gson();
			
			str = gson.toJson(r);
			
			System.out.println(str);
			
			str = Object2Json.toJsonString(r);
			
			System.out.println(str);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
