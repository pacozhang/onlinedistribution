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

public class VoiceDispose implements IMessageDispose{
	private static Logger logger= LoggerManager.getInstance().getLogger(VoiceDispose.class.getName());
	
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc,JSONObject jo) {
		String mediaId=jo.optString("MediaId");
		String format=jo.optString("Format");
		logger.debug("format->"+format);
		
		String resultStr=null;
        String msgType = "voice"; 
		String fromUsername = jo.optString("FromUserName");  
        String toUsername = jo.optString("ToUserName");  
        String time = new Date().getTime()+""; 
		String textTpl =null;
		if(jo.has("Recognition")){
			msgType="text";
			mediaId=jo.optString("Recognition");
			textTpl="<xml>"+  
	                "<ToUserName><![CDATA[%1$s]]></ToUserName>"+  
	                "<FromUserName><![CDATA[%2$s]]></FromUserName>"+  
	                "<CreateTime>%3$s</CreateTime>"+  
	                "<MsgType><![CDATA[%4$s]]></MsgType>"+  
	                "<Content><![CDATA[%5$s]]></Content>"+ 
	                "</xml>";
			
			textTpl="<xml>"+
					"<ToUserName><![CDATA[owAZBuK5jTrP_T696-kBwuRdl55I]]></ToUserName>"+
					"<FromUserName><![CDATA[gh_4cf169ac83a5]]></FromUserName>"+
					"<CreateTime>20140416060913</CreateTime>"+
					"<MsgType><![CDATA[news]]></MsgType>"+
					"<ArticleCount>1</ArticleCount>"+
					"<Articles>"+
						"<item>"+
							"<Title><![CDATA[6662]]></Title>"+
							"<Description><![CDATA[6662]]></Description>"+
							"<PicUrl><![CDATA[ ]]></PicUrl>"+
							"<Url><![CDATA[ ]]></Url>"+
						"</item>"+
					"</Articles>"+
				"</xml>";
		}else{
			textTpl="<xml>"+  
	                "<ToUserName><![CDATA[%1$s]]></ToUserName>"+  
	                "<FromUserName><![CDATA[%2$s]]></FromUserName>"+  
	                "<CreateTime>%3$s</CreateTime>"+  
	                "<MsgType><![CDATA[%4$s]]></MsgType>"+  
	                "<MediaId><![CDATA[%5$s]]></MediaId>"+  
	                "</xml>"; 
		}
		  
        resultStr = String.format(textTpl, fromUsername, toUsername, time, msgType, mediaId);  
        
        logger.debug("result->"+resultStr);
		try{
			PrintWriter pw=response.getWriter();
			pw.print(textTpl);
			pw.flush();
			pw.close();
		}catch(Exception e){
			logger.debug("voice error->"+e.getMessage());
			e.printStackTrace();
		}
	}

}
