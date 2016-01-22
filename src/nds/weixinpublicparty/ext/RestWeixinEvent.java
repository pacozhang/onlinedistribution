package nds.weixinpublicparty.ext;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.control.web.binhandler.BinaryHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.util.Tools;
import nds.util.Validator;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.dispose.MessageDisposeFactory;
import nds.weixin.ext.tools.RestUtils;
import nds.weixin.ext.tools.WXBizMsgCrypt;
import nds.weixin.ext.tools.XMLParse;

public class RestWeixinEvent
  implements BinaryHandler
{
  private Logger logger = LoggerManager.getInstance().getLogger(RestWeixinEvent.class.getName());
  private static long NETWORK_DELAY_SECONDS = 600000L;

  public void init(ServletContext context)
  {
    NETWORK_DELAY_SECONDS = Tools.getInt(WebUtils.getProperty("rest.timewindow", "10"), 10) * 1000 * 60;
  }

  public void process(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    String pathInfo = request.getPathInfo();
    if (Validator.isNull(pathInfo)) return;

    String appid = null;
    int index = pathInfo.lastIndexOf("/");
    if (index > 0) {
      appid = pathInfo.substring(index + 1);
    }
    logger.debug("appid:"+appid);
    
    //判断是否是发布公众号：wx570bc396a51b8ff8
	if("wx570bc396a51b8ff8".equalsIgnoreCase(appid)) {
		String charset=request.getCharacterEncoding();
		charset=charset==null?"iso8859-1":charset;
		
		//获取请求数据
		JSONObject verifys=new JSONObject();
		InputStream inputStream = request.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedInputStream bis = null;
		byte[] buf = new byte[1024];
		bis = new BufferedInputStream(inputStream);
		for (int len = 0; (len = bis.read(buf)) != -1;){
			baos.write(buf,0,len);
		}
	    inputStream.close(); 
	    String result=baos.toString(charset);
	    logger.debug("test message:"+result);
		
		//String pappid=WebUtils.getProperty("currentpublicparty","wx73b758959e1ef0f2");
		WePublicparty wpp=WePublicpartyManger.getInstance().getWpc();//.getByAppid(pappid);
		String pappid=wpp.getAppid();
		if(wpp==null) {
			logger.error("not find wepublicpartyby appid:"+pappid);
			return;
		}
		
		String msg_signature=request.getParameter("msg_signature");
		String timestamp=request.getParameter("timestamp");
		String nonce=request.getParameter("nonce");
		String signature=request.getParameter("signature");
		String echostr=request.getParameter("echostr");
		boolean issuccess=false;
		
		
		result= XMLParse.extract(result);
	    WXBizMsgCrypt pc = new WXBizMsgCrypt(wpp.getToken(), wpp.getNewencodingaeskey(), wpp.getAppid());
	    issuccess=pc.verifyMsg(msg_signature, timestamp, nonce, result);
	    
	    if(!issuccess) {
	    	pc = new WXBizMsgCrypt(wpp.getToken(), wpp.getOldencodingaeskey(), wpp.getAppid());
	    	issuccess=pc.verifyMsg(msg_signature, timestamp, nonce, result);
	    	if(issuccess) {wpp.setCurrentencodingaeskey(wpp.getOldencodingaeskey());}
	    }
		
	    if(nds.util.Validator.isNotNull(echostr)) {
	    	issuccess=pc.verifyUrl(signature, timestamp, nonce, echostr);
		}
	    if(!issuccess) {
	    	logger.error("verify error");
	    	return;
	    }
	    
	    String eventinfo=pc.decryptMsg(result);
	    logger.debug("eventinfo->"+eventinfo);
	    
	    JSONObject jo=org.json.XML.toJSONObject(eventinfo);
		jo=jo.optJSONObject("xml");
	    
	    String msgType=jo.optString("MsgType");
	    
	    String touser=jo.optString("ToUserName");
	  	String fromuser=jo.optString("FromUserName");
	  	
	  	String rxml=null;
	  	String resultxml=null;
	  	String tstr=String.valueOf(System.currentTimeMillis()/1000);
	  	String nstr=RandomStringUtils.randomAlphanumeric(43);
	    //如果是事件
	    if("event".equalsIgnoreCase(msgType)) {
	    	resultxml="<xml>"
				         +"<ToUserName><![CDATA["+fromuser+"]]></ToUserName>"
				         +"<FromUserName><![CDATA["+touser+"]]></FromUserName>"
				         +"<CreateTime>"+tstr+"</CreateTime>"
				         +"<MsgType><![CDATA[text]]></MsgType>"
				         +"<Content><![CDATA["+jo.optString("Event")+"from_callback]]></Content>"
				         +"</xml>";
	    	//加密回复消息
	    	rxml=pc.encryptMsg(resultxml, tstr, nstr);
	    	PrintWriter pw=response.getWriter();
			pw.print(rxml);
			pw.flush();
			pw.close();
	    }else if("TESTCOMPONENT_MSG_TYPE_TEXT".equalsIgnoreCase(jo.optString("Content"))){
	    	resultxml="<xml>"
			         +"<ToUserName><![CDATA["+fromuser+"]]></ToUserName>"
			         +"<FromUserName><![CDATA["+touser+"]]></FromUserName>"
			         +"<CreateTime>"+tstr+"</CreateTime>"
			         +"<MsgType><![CDATA[text]]></MsgType>"
			         +"<Content><![CDATA[TESTCOMPONENT_MSG_TYPE_TEXT_callback]]></Content>"
			         +"</xml>";
	    	//加密回复消息
	    	rxml=pc.encryptMsg(resultxml, tstr, nstr);
	    	PrintWriter pw=response.getWriter();
			pw.print(rxml);
			pw.flush();
			pw.close();
	    }else if(jo.optString("Content").startsWith("QUERY_AUTH_CODE:")){
	    	PrintWriter pw=response.getWriter();
			pw.print("");
			pw.flush();
			pw.close();
			
			String contnet=jo.optString("Content");
			String weixinpublicauthorizer=WebUtils.getProperty("weixin.get_authorized_access_token_URL","");
			String authorizedcode=contnet.replace("QUERY_AUTH_CODE:", "");
			JSONObject authorizedinfo=new JSONObject();
			
			WxPublicpartyControl wppc=WxPublicpartyControl.getInstance(pappid);
			
			JSONObject atjo=wppc.getAccessToken();
			//判断ACCESSTOKEN是否获取成功
			if(atjo==null||!"0".equals(atjo.optString("code"))) {				
				return;
			}
			
			String accesstoken=atjo.optJSONObject("data").optString("component_access_token");
			
			String url=weixinpublicauthorizer+accesstoken;
			
			JSONObject param=new JSONObject();
			try {
				param.put("component_appid", pappid);
				param.put("authorization_code", authorizedcode);
			}catch(Exception e){
				
			}
			
			ValueHolder vh=null;
			
			logger.debug("get user authorizer user openid");
			
			JSONObject tjo=null;
			try {
				vh=RestUtils.sendRequest_buff(url, param.toString(), "POST");
				String resultau=(String) vh.get("message");
				tjo=new JSONObject(resultau);
				logger.debug("get public authorizer result->"+resultau);
			}catch(Exception e){
				logger.debug("get public authorizer error->"+e.getLocalizedMessage());
				e.printStackTrace();
			}
			
			tjo=tjo.optJSONObject("authorization_info");
			logger.debug("authorizer_access_token:"+tjo.optString("authorizer_access_token"));
			
			resultxml="<xml>"
			         +"<ToUserName><![CDATA["+fromuser+"]]></ToUserName>"
			         +"<FromUserName><![CDATA["+touser+"]]></FromUserName>"
			         +"<CreateTime>"+tstr+"</CreateTime>"
			         +"<MsgType><![CDATA[text]]></MsgType>"
			         +"<Content><![CDATA["+authorizedcode+"_from_api]]></Content>"
			         +"</xml>";
			rxml="{\"touser\":\""+fromuser+"\",\"msgtype\":\"text\",\"text\":{\"content\":\""+authorizedcode+"_from_api\"}}";
			
			//加密回复消息
	    	//rxml=pc.encryptMsg(resultxml, tstr, nstr);
			url=WebUtils.getProperty("weixin.we_send_message_URL","");
			HashMap<String, String> params =new HashMap<String, String>();
			params.put("access_token",tjo.optString("authorizer_access_token"));
			try{
				url+=RestUtils.delimit(params.entrySet(), false);
			}catch(Exception e){}
			
			logger.debug("send_message");
			try{
				vh=RestUtils.sendRequest_buff(url, rxml, "POST");
				logger.debug("send message result:"+vh.get("message"));
			}catch(Exception e) {
				logger.error("send message error:"+e.getLocalizedMessage());
			}
	    }
	    logger.debug("resultxml:"+resultxml);
	    //logger.debug("resutl str:"+rxml);
	   return;
	}

    MessageDisposeFactory mdf = MessageDisposeFactory.getInstance(appid);
    mdf.dispose(request, response);
  }
}