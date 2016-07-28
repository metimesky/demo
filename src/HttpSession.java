
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 *  http session component
 */

public class HttpSession {

	static Logger logger = Logger.getLogger(HttpSession.class.getName());
	 
	static Map<String, SessionModel> sessionIDMap = new ConcurrentHashMap<>();
	static Thread checkSessionThread = null;

	static {
		check();
	}
	
	public static String generateSessionId() {
		String s = UUID.randomUUID().toString();
		return s;
	}

	//新增适HttpExchange
	public static Object getObjFromSession(HttpExchange exchange, String obj_key) {
		String sessionid = exchange.getRequestHeaders().get("sessionid").get(0);
		if (sessionid == null) {
			return null;
		}else {
			SessionModel s = sessionIDMap.get(sessionid);
			return s.getVByK(obj_key);
		}
	}


	//新增适HttpExchange
	public static void setSession(String obj_key, Object obj, HttpExchange exchange) {
		String sessionid = exchange.getRequestHeaders().get("sessionid").get(0);
		if (sessionid == null) {
			String sid = generateSessionId();
			SessionModel m = addNewSessionBySessionID(sid);
			m.setKV(obj_key, obj);
		}else {
			SessionModel m = getSession(sessionid);
			m.setKV(obj_key, obj);
		}
	}
	
	public static SessionModel getSession(String sessionID) {
		SessionModel s = sessionIDMap.get(sessionID);
		return s;
	}
	
	public static boolean isHave(String sid) {
		SessionModel s = sessionIDMap.get(sid);
		return s != null;
	}
	
	public static void removeSession(String sessionID) {
		sessionIDMap.remove(sessionID);
	}
	
	public static SessionModel addNewSessionBySessionID(String sessionID) {
		SessionModel s = sessionIDMap.get(sessionID);
		if(s == null) {
			s = new SessionModel("sessionid", sessionID);
			sessionIDMap.put(sessionID, s);
			return s;
		}
		return s;
	}
	
	
	public static void check() {
		if(checkSessionThread == null) {
			checkSessionThread = new checkSessionThread();
			checkSessionThread.setDaemon(true);
			checkSessionThread.start();
		}
	}
}

 class SessionModel {

	private HashMap<String, Object> valueMap = new HashMap<>();
	private int defaultTimeoutSeconds = 60 * 20;
	private HttpCookie c = null;
	public long createTime = 0;

	public SessionModel(String cookieName, String cookieValue) {
		//session 存到cookie送至浏览器
		c = new HttpCookie(cookieName, cookieValue);
		c.setMaxAge(defaultTimeoutSeconds);
		createTime = System.currentTimeMillis();
	}

	public void setTimeout(int second) { c.setMaxAge(second);}

	public void setKV(String key, Object obj) { valueMap.put(key, obj);}

	public Object getVByK(String key) { return valueMap.get(key);}

	public HttpCookie getCookie() { return c;}
}

class checkSessionThread extends Thread {
	@Override
	public void run() {
		while(true) {
			Iterator<Entry<String, SessionModel>> i = HttpSession.sessionIDMap.entrySet().iterator();
			while(i.hasNext()) {
				Entry<String, SessionModel> entry = (Entry<String, SessionModel>)i.next();
				SessionModel se = entry.getValue();
				String k = entry.getKey();
				HttpCookie c = se.getCookie();
				long interval = System.currentTimeMillis() - se.createTime;
				if(interval >= c.getMaxAge()*1000) {
					i.remove();
				}
			}
		}
	}
}
