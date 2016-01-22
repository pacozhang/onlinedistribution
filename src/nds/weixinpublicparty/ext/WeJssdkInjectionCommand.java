package nds.weixinpublicparty.ext;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.wxap.util.SignatureUtil;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class WeJssdkInjectionCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();

		JSONObject jo = (JSONObject)event.getParameterValue("jsonObject");
		
		
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			logger.debug("get params error->"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		if(jo==null) {
			logger.debug("params is error->is null");
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}
		
		int companyid=jo.optInt("ad_client_id",-1);
		
		WeUtils wu=WeUtilsManager.getByAdClientId(companyid);
		if(wu==null) {
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}
		
		
		String url=jo.optString("url");
		logger.debug("url->"+url);
		
		if(nds.util.Validator.isNull(url)) {
			vh.put("code", "-1");
			vh.put("message", "参数错误");
			return vh;
		}else {
			try {
				url=java.net.URLDecoder.decode(url,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			int index=url.indexOf('#');
			if(index>0) {url=url.substring(0, index);}
			logger.debug("url->"+url);
			/*
			index=url.indexOf('?');			
			if(index>0) {
				try {
					url=url.substring(0, index)+"?"+java.net.URLEncoder.encode(url.substring(index+1), "UTF-8");
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			}
			*/
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			logger.debug("publish menu error->not find WeControl WeControl.getInstance("+wu.getCustomId()+")");
			vh.put("code","-1");
			vh.put("message","参数错误");
			return vh;
		}
		
		JSONObject jstoken=wc.getJssdk_access_token();
		//判断ACCESSTOKEN是否获取成功
		if(jstoken==null||!"0".equals(jstoken.optString("code"))) {
			try {
				vh.put("code", "-1");
				vh.put("message", "请重新授权");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return vh;
		}
				
		String token=jstoken.optJSONObject("data").optString("ticket");
		if(nds.util.Validator.isNull(token)) {
			try {
				vh.put("code", -1);
				vh.put("message", "请重新授权");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return vh;
		}
		
		//设置返回参数
		String timestamp=String.valueOf(System.currentTimeMillis()/1000);	//时间戳
		String randomstr=RandomStringUtils.randomAlphanumeric(16);			//随机字段串
		Map mp=new HashMap();
		mp.put("timestamp", timestamp);
		mp.put("noncestr", randomstr);
		mp.put("jsapi_ticket", token);
		mp.put("url", url);
		
		
		//数据签名
		SignatureUtil su=new SignatureUtil();
		Map middlemp=su.doSort(mp, false);
		Object value=null;
		StringBuffer sb = new StringBuffer();
		for(Object key:middlemp.keySet()) {
			value=middlemp.get(key);
			if(sb.length()>0) {sb.append("&");}
			sb.append(key + "=" + value);
		}
		logger.debug("signstring->"+sb.toString());
		String sign=DigestUtils.shaHex(sb.toString());
		logger.debug("sign->"+sign);
		
		/*
		String string1="jsapi_ticket="+mp.get("jsapi_ticket")
					  +"&noncestr="+mp.get("noncestr")
					  +"&timestamp="+mp.get("timestamp")
					  +"&url="+mp.get("url");
		MessageDigest crypt=null;
		try {
			logger.debug("string1:"+string1);
			crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(string1.getBytes("UTF-8"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

        
        Formatter formatter = new Formatter();
        for (byte b : crypt.digest())
        {
            formatter.format("%02x", b);
        }
        sign = formatter.toString();
        formatter.close();
        logger.debug("sign:"+sign);
        */
		
		
		JSONObject signjo=new JSONObject();
		try {
			signjo.put("appid", wu.getAppId());
			signjo.put("signature", sign);
			signjo.put("timestamp", mp.get("timestamp"));
			signjo.put("noncestr", mp.get("noncestr"));
		}catch(Exception e) {
			
		}

		vh.put("code", "0");
		vh.put("message", "操作成功");
		vh.put("data", signjo);
		logger.debug("signinfo->"+signjo.toString());
		
		return vh;
	}

}
