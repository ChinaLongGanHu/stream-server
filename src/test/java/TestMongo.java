import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.to8to.commons.json.JSONObject;
import com.to8to.commons.mongo.DBParam;
import com.to8to.commons.mongo.MongoEntityClient;



public class TestMongo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		MongoEntityClient mongo = new MongoEntityClient(new DBParam("58.67.156.56:27001", null, null, "event_log"));
 
		DBCursor c = mongo.cursor("event_log", null, null, null, 0, 0);
		
		Set<String> userSet = new HashSet<String>();
		Set<String> session_resource = new HashSet<String>();
		while(c.hasNext())
		{
			BasicDBObject obj = (BasicDBObject)c.next();
			
			System.out.println(obj);
			
			userSet.add(obj.getString("user_id"));
			
			session_resource.add(obj.getString("session_id") + "-" + obj.getString("visit_resouce"));
		}
		
		System.out.println("user#:" + userSet.size());
		
		System.out.println("session-resource#:" + userSet.size());
		
/*		BasicDBObject obj = new BasicDBObject();
		String id = "china";
		obj.put("_id", id);
		obj.put("name", "china");
		obj.put("title", "zeming");
		
		mongo.insert("_test_table", obj); */
		
		//mongo.getField("_test_table", query, field)
		
		//System.out.println(mongo.count("_test_table"));
	    
	    JSONObject jsonObject = new JSONObject();
	    jsonObject.put("abc", null);
	    
	    System.out.println(jsonObject.toString());
	    
		
	}

}
