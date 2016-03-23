package nds.weixin.ext.smsplatform;

import static java.lang.System.out;

import java.util.HashMap;

import org.json.JSONObject;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.NDSException;
import nds.weixin.ext.tools.RestUtils;

public class ZhutongPhoneMessage implements ISmsPlatform{
	private static Logger logger= LoggerManager.getInstance().getLogger(ZhutongPhoneMessage.class.getName());

	@Override
	public ValueHolder sendMessage(JSONObject jo) {
		ValueHolder vh=new ValueHolder();

		String url = jo.optString("url") + "/webservice/burgeonsmsservice.asmx/SendSMS2";


        HashMap<String, String> dict = new HashMap<String, String>();
        dict.put("userId", jo.optString("username"));
        dict.put("userPwd", jo.optString("userpwd"));
        dict.put("sendTime", null);
        dict.put("mobiless", jo.optString("phone"));
        dict.put("smsContent", jo.optString("content"));
        dict.put("addSerial", "");
        dict.put("smstype", "");
        dict.put("channel", "");

        try
        {
        	vh=RestUtils.sendRequest(jo.optString("url"), dict, "POST");
        	String result=(String) vh.get("message");
        	logger.debug("send sms resutl->"+result);

            if (nds.util.Validator.isNotNull(result))
            {
            	JSONObject rjo=null;
            	try{
            		rjo=org.json.XML.toJSONObject(result);
            		rjo=rjo.optJSONObject("string");
                	rjo=new JSONObject(rjo.optString("content"));
                	
                	vh.put("code", rjo.optString("Code","-1"));
                	vh.put("message", rjo.optString("Message","ÐÅÏ¢·¢ËÍÊ§°Ü"));
                	vh.put("data", rjo);
                }catch(Exception e){
                	logger.error("dispose send sms result error:"+e.getLocalizedMessage());
                	e.printStackTrace();
                	vh.put("code", "-1");
                	vh.put("message", "ÐÅÏ¢·¢ËÍÊ§°Ü");
                	return vh;
                }
            }
            else {
            	vh.put("code", "-1");
            	vh.put("message", "ÐÅÏ¢·¢ËÍÊ§°Ü");
            	return vh;
            }
        }
        catch (Exception ce)
        {
        	logger.error("send phone error:"+ce.getLocalizedMessage());
        	ce.printStackTrace();
        	vh.put("code", "-1");
        	vh.put("message", "¶ÌÐÅ·¢ËÍÊ§°Ü");
        }
        
		return vh;
	}
}
