package nds.weixin.ext.dispose;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;

public class EventDispose implements IMessageDispose{
	private static Logger logger= LoggerManager.getInstance().getLogger(EventDispose.class.getName());
	
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc,JSONObject jo) {
		String eventType=jo.optString("Event");
		logger.debug("eventType->"+eventType);
		Hashtable<String,IMessageDispose> disposes= MessageDisposeFactory.getInstance(wpc.getWxPublic().getAppId()).getDisposes();

		if(nds.util.Validator.isNull(eventType)){return ;}
		eventType=eventType.toLowerCase();
		String eventkey= jo.optString("EventKey","");
		if("subscribe".equalsIgnoreCase(eventType)&&eventkey.startsWith("qrscene_")) {
			try {
				jo.put("EventKey", eventkey.replaceAll("qrscene_", ""));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			eventType="scan";
		}
		
		String className=null;
		IMessageDispose dispose=null;
		EventType et=EventType.getEventType(eventType);
		
		className=et.getEDispose();
		if(disposes==null){disposes=new Hashtable<String,IMessageDispose>();}
		if(disposes.containsKey(eventType)){
			dispose=disposes.get(eventType);
		}else {
			try{
				dispose =(IMessageDispose)Class.forName(className).newInstance();
				disposes.put(eventType, dispose);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		dispose.dispose(request, response,wpc, jo);
	}

}
