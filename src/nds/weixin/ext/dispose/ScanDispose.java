package nds.weixin.ext.dispose;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

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
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.tools.AesException;
import nds.weixin.ext.tools.SendWXMessage;
import nds.weixin.ext.tools.WXBizMsgCrypt;
import nds.weixin.ext.tools.WeCreateOrSearchVip;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanDispose implements IMessageDispose {
	private static Logger logger=LoggerManager.getInstance().getLogger(ScanDispose.class.getName());
	
	
	@Override
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc, JSONObject jo) {
		int vipid=0;
		String code="";
		
		JSONObject vipjo=null;
		WeUtils wu=wpc.getWxPublic();
		
		WeCreateOrSearchVip wcos=new WeCreateOrSearchVip();
		
		//synchronized(this) {
			vipjo=wcos.vipDispose(wu, jo);
		//}
				
		
		if(vipjo==null||!vipjo.has("code")||!vipjo.has("vipid")){return;}
		code=vipjo.optString("code","-1");
		if(!"0".equals(code)) {return;}
		
		vipid=vipjo.optInt("vipid",0);
		
		//判断会员是否存在或创建成功
		if(vipid<=0) {return;}

		ArrayList params=new ArrayList();
		params.add(wu.getAd_client_id());
		JSONObject pa=new JSONObject();
		JSONObject pxml=new JSONObject();
		try {
			pxml.put("FromUserName",jo.optString("FromUserName"));
			pxml.put("ToUserName",jo.optString("ToUserName"));
			pxml.put("MsgType",jo.optString("Event"));
			pxml.put("EventKey",jo.optString("EventKey"));
			pxml.put("Ticket",jo.optString("Ticket"));
			pa.put("xml", pxml);

			/*WeMeunManager wm =WeMeunManager.getInstance("0");
			try {
				wm.createMenu(wc, "");
			}catch(Exception e) {e.printStackTrace();}*/
			
			
			params.add(org.json.XML.toString(pa));
			logger.debug("params->"+org.json.XML.toString(pa));
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		
		String resultStr=null;
		ArrayList para=new ArrayList();
		para.add(java.sql.Clob.class);
		try {
			Collection list=QueryEngine.getInstance().executeFunction("wx_rqcodemessage_$r_scan",params,para);
			resultStr=(String)list.iterator().next();
			logger.debug("result->"+resultStr);
		} catch (QueryException e1) {
			logger.debug("ScanDispose execute function erroe->"+e1.getMessage());
			e1.printStackTrace();
		}
		
		if(nds.util.Validator.isNotNull(resultStr)) {
			JSONObject messagejo=null;
			try {
				//messagejo=org.json.XML.toJSONObject("xml");
				messagejo=org.json.XML.toJSONObject(resultStr);
				messagejo=messagejo.optJSONObject("xml");
			}catch(Exception e) {
				logger.debug("parse jsonobject error->"+e.getLocalizedMessage());
			}

			if(messagejo!=null&&messagejo.has("CommandType")) {
				//String mt=messagejo.optString("MsgType");
				String ct=messagejo.optString("CommandType");
				String cn=messagejo.optString("CommandContent");
				
				//if("command".equalsIgnoreCase(mt)&&"1".equalsIgnoreCase(ct)) {
				if(nds.util.Validator.isNotNull(cn)) {
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
					event.setParameter("command", cn);
					
					ValueHolder vh=null;
					try {
						vh=controller.handleEvent(event);
					}catch(Exception e) {
						logger.debug("execute command error->"+e.getLocalizedMessage());
						return;
					}
					if(vh!=null&&vh.get("code")=="0") {
						try {
							//messagejo.put("MsgType", "text");
							messagejo.remove("AD_CLIENT_ID");
							messagejo.remove("CommandType");
							messagejo.remove("CommandValue");
							messagejo.remove("CommandContent");
							//messagejo.put("Content",vh.get("message"));
							resultStr= org.json.XML.toString(messagejo,"xml");							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		logger.debug("1result->"+resultStr);
		SendWXMessage.sendWXMessage(request,response,resultStr);
	}

}
