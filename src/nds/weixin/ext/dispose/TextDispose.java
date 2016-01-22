package nds.weixin.ext.dispose;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nds.control.util.ValueHolder;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.query.SPResult;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.dispose.IMessageDispose;
import nds.weixin.ext.tools.RestUtils;
import nds.weixin.ext.tools.SendWXMessage;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TextDispose implements IMessageDispose{
	private static Logger logger= LoggerManager.getInstance().getLogger(TextDispose.class.getName());
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc,JSONObject jo) {
		String message=jo.optString("Content");
		logger.debug("Content->"+message);
		WeUtils wu=wpc.getWxPublic();
		
		ArrayList params=new ArrayList();
		params.add(wu.getAd_client_id());
		
		//Ìí¼Ó¼ÇÂ¼
		int vipid=0;
		String sql="select v.id from wx_vip v where v.wechatno=? and v.ad_client_id=?";
		try {
			vipid=QueryEngine.getInstance().doQueryInt(sql, new Object[] {jo.optString("FromUserName"),wu.getAd_client_id()});
			logger.debug("add weixin message search vipid->"+vipid);
		} catch (QueryException e4) {
			logger.debug("add weixin message search vipid error->"+e4.getMessage());
			e4.printStackTrace();
		}
		sql="insert into wx_messgae(id,ad_client_id,wx_vip_id,customer,message,send_time,modifieddate,creationdate) values(get_Sequences('WX_MESSGAE'),?,?,?,?,sysdate,sysdate,sysdate)";
		try {
			QueryEngine.getInstance().executeUpdate(sql, new Object[] {wu.getAd_client_id(),vipid,jo.optString("FromUserName"),message});
		} catch (QueryException e3) {
			logger.debug("add weixin message error->"+e3.getMessage());
			e3.printStackTrace();
		}
		
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
		} catch (JSONException e2) {
			e2.printStackTrace();
		}
		
		String resultStr=null;
		ArrayList para=new ArrayList();
		para.add(java.sql.Clob.class);
		try {
			Collection list=QueryEngine.getInstance().executeFunction("wx_message_$r_reply",params,para);
			resultStr=(String)list.iterator().next();
		} catch (QueryException e1) {
			logger.debug("TextDispose->erroe,"+e1.getMessage());
			e1.printStackTrace();
		}
		
		logger.debug("result->"+resultStr);
		SendWXMessage.sendWXMessage(request,response,resultStr);
	}

}
