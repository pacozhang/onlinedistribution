package nds.weixin.ext.dispose;

import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;

public class LinkDispose implements IMessageDispose{
	private static Logger logger= LoggerManager.getInstance().getLogger(LinkDispose.class.getName());
	
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc, JSONObject jo) {
		String title=jo.optString("Title");
		String description=jo.optString("Description");
		String url=jo.optString("Url");	
		logger.debug("picUrl->"+description);
		
		String fromUsername = jo.optString("FromUserName");  
        String toUsername = jo.optString("ToUserName");  
        String time = new Date().getTime()+""; 
		String textTpl = "<xml>"+  
                "<ToUserName><![CDATA[%1$s]]></ToUserName>"+  
                "<FromUserName><![CDATA[%2$s]]></FromUserName>"+  
                "<CreateTime>%3$s</CreateTime>"+  
                "<MsgType><![CDATA[%4$s]]></MsgType>"+  
                "<MediaId><![CDATA[%5$s]]></MediaId>"+  
                "</xml>";    
		
		String resultStr=null;
        String msgType = "link";   
        /*resultStr = String.format(textTpl, fromUsername, toUsername, time, msgType, mediaId);  
        
        logger.debug("result->"+resultStr);
		try{
			PrintWriter pw=response.getWriter();
			pw.print(resultStr);
			pw.flush();
			pw.close();
		}catch(Exception e){
			logger.debug("link error->"+e.getMessage());
			e.printStackTrace();
		}*/
	}

}
