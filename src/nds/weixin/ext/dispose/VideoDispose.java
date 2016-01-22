package nds.weixin.ext.dispose;

import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;

public class VideoDispose implements IMessageDispose{
	private static Logger logger= LoggerManager.getInstance().getLogger(VideoDispose.class.getName());
	
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc,JSONObject jo) {
		String mediaId=jo.optString("MediaId");
		String thumbMediaId=jo.optString("ThumbMediaId");
		logger.debug("thumbMediaId->"+thumbMediaId);
		
		String fromUsername = jo.optString("FromUserName");  
        String toUsername = jo.optString("ToUserName");  
        String time = new Date().getTime()+""; 
		String textTpl = "<xml>"+  
                "<ToUserName><![CDATA[%1$s]]></ToUserName>"+  
                "<FromUserName><![CDATA[%2$s]]></FromUserName>"+  
                "<CreateTime>%3$s</CreateTime>"+  
                "<MsgType><![CDATA[%4$s]]></MsgType>"+  
                "<MediaId><![CDATA[%5$s]]></MediaId>"+  
                "<Title><![CDATA[%5$s]]></Title>"+  
                "<Description><![CDATA[%5$s]]></Description>"+  
                "</xml>";    
		
		String resultStr=null;
        String msgType = "video";   
        resultStr = String.format(textTpl, fromUsername, toUsername, time, msgType, mediaId,"testtitle","textdescription");  
        
        logger.debug("result->"+resultStr);
		try{
			PrintWriter pw=response.getWriter();
			pw.print(resultStr);
			pw.flush();
			pw.close();
		}catch(Exception e){
			logger.debug("video error->"+e.getMessage());
			e.printStackTrace();
		}
	}
}
