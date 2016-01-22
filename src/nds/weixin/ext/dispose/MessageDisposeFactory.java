package nds.weixin.ext.dispose;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.util.NDSException;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;
import nds.weixin.ext.tools.WXBizMsgCrypt;
import nds.weixin.ext.tools.XMLParse;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageDisposeFactory {
	private WxPublicControl wpc;
	private WxPublicpartyControl wppc;
	
	private Hashtable<String,IMessageDispose> disposes;
	private static final String CONTENT_TYPE_TEXT = "text/html; charset=UTF-8";
	
	private static Logger logger= LoggerManager.getInstance().getLogger(MessageDisposeFactory.class.getName());
	private static Hashtable<String,MessageDisposeFactory> factorys;
	
	private MessageDisposeFactory(WxPublicControl wpc){
		this.wpc=wpc;
		this.wppc=wpc.getPpc();
	}
	
	public Hashtable<String,IMessageDispose> getDisposes(){
		return this.disposes;
	}
	
	/**
	 * 
	 * @param pappid	公众号APPID
	 * @return
	 */
 	public static synchronized MessageDisposeFactory getInstance(String pappid){
		if(nds.util.Validator.isNull(pappid)){return null;}
		
		MessageDisposeFactory instance=null;
		if(factorys==null){
			factorys=new Hashtable<String,MessageDisposeFactory>();
			
			WxPublicControl twpc=null;
			
			WeUtilsManager wum=WeUtilsManager.getInstance();
			WeUtils wu=wum.getByAppid(pappid);
			twpc=WxPublicControl.getInstance(pappid);
			
			instance=new MessageDisposeFactory(twpc);
			factorys.put(pappid, instance);
		}else if(factorys.containsKey(pappid)){
			instance=factorys.get(pappid);
		}else{
			WxPublicControl twpc=WxPublicControl.getInstance(pappid);
			
			WeUtilsManager wum=WeUtilsManager.getInstance();
			WeUtils wu=wum.getByAppid(pappid);
			
			instance=new MessageDisposeFactory(twpc);
			factorys.put(pappid, instance);
		}

		return instance;
	}
	
	public void dispose(HttpServletRequest request, HttpServletResponse response) throws Exception{
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
	    
	    
        logger.debug("request string->"+result);
        JSONObject jo=null;

        try{
        	jo=org.json.XML.toJSONObject(result);
        	jo=jo.optJSONObject("xml");
        	logger.debug("request json->"+jo.toString());
        }catch(Exception e){
        	logger.debug("publicparty error->"+e.getLocalizedMessage());
        	e.printStackTrace();
        	PrintWriter pw=response.getWriter();
			pw.print("success");
			pw.flush();
			pw.close();
        	return;
        }
		
        WeUtils wu=wpc.getWxPublic();
		WePublicparty wpp=WePublicpartyManger.getInstance().getWpc();//.getByAppid(wu.getPublicpartyappid());
		if(wpp==null) {
			logger.error("not find wepublicpartyby appid:"+wpp.getAppid());
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
        
        jo=org.json.XML.toJSONObject(eventinfo);
    	jo=jo.optJSONObject("xml");
       
        
        String className=null;
		String msgType=jo.optString("MsgType");
		if(nds.util.Validator.isNull(msgType)){
			PrintWriter pw=response.getWriter();
			pw.print("success");
			pw.flush();
			pw.close();
        	return;
        }
		
		msgType=msgType.toLowerCase();
		IMessageDispose dispose=null;
		EventType et=EventType.getEventType(msgType);
		
		
		
		//同一对象的此代码块线程同步
		//synchronized(this) {
			if(disposes==null){disposes=new Hashtable<String,IMessageDispose>();}
			if(disposes.containsKey(msgType)){
				dispose=disposes.get(msgType);
			}else {
				try{
					className=et.getEDispose();
					dispose =(IMessageDispose)Class.forName(className).newInstance();
					disposes.put(msgType, dispose);
				}catch(Exception e){
					logger.error("dispose event error:"+e.getLocalizedMessage());
					e.printStackTrace();
					try{
						PrintWriter pw=response.getWriter();
						pw.print("success");
						pw.flush();
						pw.close();
					}catch(Exception e1){
						logger.debug("return error->"+e1.getMessage());
						e.printStackTrace();
					}
				}
			}
		//}
		
		dispose.dispose(request, response,wpc, jo);
		String sql="update wx_vip_inqury vi set vi.lastvisitdate=sysdate where vi.wechatno=? and vi.ad_client_id=?";
		try {
			QueryEngine.getInstance().executeUpdate(sql, new Object[] {jo.optString("FromUserName"),wu.getAd_client_id()});
		}catch(Exception e) {
			logger.debug("update vip lastvisitdate error->"+e.getMessage());
		}
	}
}
