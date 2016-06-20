package nds.weixin.ext.dispose;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.ClientControllerWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.QueryRequestImpl;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.tools.SendWXMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class ClickDispose implements IMessageDispose {
	private static Logger logger= LoggerManager.getInstance().getLogger(ClickDispose.class.getName());
	@Override
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc, JSONObject jo) {
		String message=jo.optString("EventKey","");
		logger.debug("Content->"+message);
		
		ValueHolder vh=null;
		String resultStr=null;
		ArrayList para=new ArrayList();
		para.add(java.sql.Clob.class);
		WeUtils wu=wpc.getWxPublic();
		
		
		if(message.startsWith("command")) {
			JSONObject messagejo=jo;
			message=message.replace("command", "");
			String fromuser=messagejo.optString("FromUserName");
			String touser=messagejo.optString("ToUserName");
			
			
			int vipid=0;
			List jumpurl=null;
			
			try {
				jumpurl=QueryEngine.getInstance().doQueryList("select jp.commandtype,jp.commandcontent from wx_jumpurl jp where jp.id="+message);
				vipid=QueryEngine.getInstance().doQueryInt("select v.id from wx_vip v where v.wechatno=? and v.ad_client_id=?", new Object[] {fromuser,wu.getAd_client_id()});
			}catch(Exception e) {
				
			}
			if(jumpurl==null||jumpurl.size()<=0) {return;}
			
			String Content=String.valueOf(((List)jumpurl.get(0)).get(1));
			String CommandType=String.valueOf(((List)jumpurl.get(0)).get(0));
			
			try {
				messagejo.put("WX_VIP_ID", vipid);
				messagejo.put("FromUserName", touser);
				messagejo.put("ToUserName", fromuser);
				messagejo.put("CommandValue", message);
				
				messagejo.put("AD_CLIENT_ID", wu.getAd_client_id());
			} catch (JSONException e2) {
				e2.printStackTrace();
			}
				
				
			JSONObject paramsjo=new JSONObject();
			try {
				messagejo.put("AD_CLIENT_ID", wu.getAd_client_id());
				paramsjo.put("params", messagejo);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			ClientControllerWebImpl controller=(ClientControllerWebImpl)WebUtils.getServletContextManager().getActor(nds.util.WebKeys.WEB_CONTROLLER);
			DefaultWebEvent event=new DefaultWebEvent("CommandEvent");
			event.put("jsonObject",paramsjo);
			event.setParameter("command", Content);

			//YYfffsssss
			try {
				vh=controller.handleEvent(event);
			}catch(Exception e) {
				logger.debug("execute command error->"+e.getLocalizedMessage());
				return;
			}
			if(vh!=null) {
				try {
					messagejo.put("MsgType", "text");
					messagejo.remove("CommandType");
					messagejo.remove("CommandValue");
					messagejo.put("Content",vh.get("message"));
					resultStr= org.json.XML.toString(messagejo,"xml");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			try{
				PrintWriter pw=response.getWriter();
				pw.print(resultStr);
				pw.flush();
				pw.close();
			}catch(Exception e){
				logger.debug("ClickDispose error->"+e.getMessage());
				e.printStackTrace();
			}
			return;
		}
		
		
		ArrayList params=new ArrayList();
		params.add(wu.getAd_client_id());
		
		JSONObject pa=new JSONObject();
		JSONObject pxml=new JSONObject();
		
		try {
			pxml.put("FromUserName",jo.optString("FromUserName"));
			pxml.put("ToUserName",jo.optString("ToUserName"));
			pxml.put("MsgType",jo.optString("MsgType"));
			pxml.put("KeyWords",message);
			pa.put("xml", pxml);

			params.add(org.json.XML.toString(pa));
			
			/*JSONObject filejo=new JSONObject();
			filejo.put("type", "voice");
			filejo.put("mediaType", "form-data");
			filejo.put("filePath", "D:\\NEWBOS\\ÁåÉù.mp3");

			String mid=WeFile.uploadFile(wc, filejo);
			logger.debug("mid->"+mid);*/
			logger.debug("params->"+org.json.XML.toString(pa));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		try {
			Collection list=QueryEngine.getInstance().executeFunction("wx_message_$r_replyq",params,para);
			resultStr=(String)list.iterator().next();
		} catch (QueryException e) {
			logger.debug("ClickDispose->erroe,"+e.getMessage());
			e.printStackTrace();
		}
		
		logger.debug("result->"+resultStr);
		SendWXMessage.sendWXMessage(request,response,resultStr);
	}//sss

}
