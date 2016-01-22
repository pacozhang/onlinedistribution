package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.rest.RestUtils;
import nds.util.NDSException;
import nds.util.Tools;
import nds.weixin.ext.SipStatus;
import nds.weixin.ext.tools.VipPhoneVerifyCode;

public class OpencardCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		JSONObject jo=null;
		ValueHolder vh =new ValueHolder();
		
		//判断公司ID 与VIP ID是否在参数中传入
		try {
			jo = (JSONObject) event.getParameterValue("jsonObject");
			jo=jo.optJSONObject("params");
		}catch(Exception e) {
			logger.error("params error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "开卡异常请重试");
			return vh;
		}
		
		if (jo==null||!jo.has("companyid")||!jo.has("vipid")) {
			logger.error("params error:not put companyid or vipid");
			vh.put("code", "-1");
			vh.put("message", "开卡异常请重试");
			return vh;
		}
		
		int vipid=jo.optInt("vipid",-1);
		int companyid=jo.optInt("companyid",-1);
		
		if (companyid<=0 || vipid<=0) {
			logger.error("params error:companyid:"+companyid+",vipid:"+vipid);
			vh.put("code", "-1");
			vh.put("message", "开卡异常请重试");
			return vh;
		}
		
		//判断接通线下参数
		List all=null;
		try {
			all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,wc.wxparam,nvl(ifs.ismesauth,'N') from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+companyid);
		} catch (Exception e) {
			logger.error("select set offline params error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("messae", "开卡异常请重试");
			return vh ;
		}
		if (all==null||all.size()<=0) {
			logger.error("select set offline params error:not find data");
			vh.put("code", "-1");
			vh.put("messae", "开卡异常请重试");
			return vh ;
		}
		all=(List)all.get(0);
		
		String serverUrl=String.valueOf(all.get(0));
		boolean isErp="Y".equalsIgnoreCase(String.valueOf(all.get(2)));
		String SKEY=(String)((List)all.get(0)).get(3);
		boolean isVerifyCode="Y".equalsIgnoreCase(String.valueOf(all.get(4)));
		if(isErp&&(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY))) {
			logger.error("SERVERuRL OR SKEY IS NULL");
			vh.put("code", "-1");
			vh.put("messae", "开卡异常请重试");
			return vh ;
		}
		
		//判断是否需要短信验证
		String verifycode=jo.optString("verifycode");
		String phone=jo.optString("PHONENUM");
		if(isVerifyCode) {
			if(nds.util.Validator.isNull(verifycode)) {
				vh.put("code", "-1");
				vh.put("messae", "验证码为空，请输入");
				return vh ;
			}
			if(nds.util.Validator.isNull(phone)) {
				vh.put("code", "-1");
				vh.put("messae", "手机号为空，请输入");
				return vh ;
			}
			vh=VipPhoneVerifyCode.verifyphonecode(vipid, phone, verifycode);
			if(vh==null) {
				logger.error("opencard verifyvipcode error:call VipPhoneVerifyCode.verifyphonecode error");
				vh.put("code", "-1");
				vh.put("message", "验证码信息异常，请重新输入");
				return vh;
			}
			if(!"0".equals(vh.get("code"))) {
				logger.error("opencard verifyvipcode error:"+vh.get("message"));
				return vh;
			}
		}
		
		//未接通线下时，开卡送积分与券
		if(!isErp) {
			logger.debug("未接通ERP");
			//线上发券
			ArrayList params=new ArrayList();
			params.add(vipid);
			ArrayList para=new ArrayList();
			para.add( java.sql.Clob.class);
			
			try {
				Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_onlinecoupon",params,para);
				String res=(String)list.iterator().next();
				logger.debug("online send coupon result->"+res);
			}catch (QueryException e) {
				logger.debug("online send coupon erroe->"+e.getMessage());
				e.printStackTrace();
			}
			
			//开卡送积分
			ArrayList returnparam=new ArrayList();
			ArrayList sendparam=new ArrayList();
			JSONObject sendintegral=new JSONObject();
			try {
				int senndintegral=QueryEngine.getInstance().doQueryInt("select nvl(vbs.awardintegral,0) from wx_vip v left join wx_vipbaseset vbs on v.viptype=vbs.id where v.id=?", new Object[] {vipid});
				if(senndintegral<=0) {
					logger.debug("");
					vh.put("code", "0");
					vh.put("message", "开卡成功");
					return vh;
				}
				sendintegral.put("vipid", vipid);
				sendintegral.put("integral", senndintegral);
				sendintegral.put("description", "开卡送积分");
				sendparam.add(sendintegral.toString());
				returnparam.add(java.sql.Clob.class);
				
				Collection list=QueryEngine.getInstance().executeFunction("wx_vip_adjustintegral",sendparam,returnparam);
				String res=(String)list.iterator().next();
				logger.debug("online opencard adjustintegral result->"+res);
				
				vh.put("code", "0");
				vh.put("message", "开卡成功");
			}catch(Exception e) {
				logger.debug("opencard adjustintegral error:"+e.getLocalizedMessage());
				e.printStackTrace();
				vh.put("code", "-1");
				vh.put("message", "开卡失败，请重试");
			}
			return vh;
		}
		
		//调用线下开卡
		String ts=String.valueOf(System.currentTimeMillis());
		String sign=null;
		try {
			sign = nds.util.MD5Sum.toCheckSumStr(companyid + ts+ SKEY);
		} catch (IOException e) {
			logger.debug("opencard md5 error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "开卡失败，请重试");
			return vh;
		}
		HashMap<String, String> params =new HashMap<String, String>();
		//Connection conn = QueryEngine.getInstance().getConnection();
		boolean isSendCoupon=false;
		String couponUseType="0";
		String couponCode="";
		//conn.setAutoCommit(false);
		List al=null;
		JSONObject offparam=new JSONObject();
		JSONObject offvipinfo=new JSONObject();
		JSONObject offcouponinfo=new JSONObject();
		//组织参数
		try {
			al = QueryEngine.getInstance().doQueryList("select vp.wechatno,vp.vipcardno,vp.store_id,NVL(vt.ISSEND,'N'),cp.num,cp.usetype1,vt.code,nvl(cp.value,'0'),nvl(vp.integral,0),to_char(decode(nvl(cp.validay,0),0,nvl(cp.starttime,sysdate), sysdate), 'YYYYMMDD'),to_char(decode(nvl(cp.validay,0),0, nvl(cp.endtime, add_months(cp.starttime, 1)),sysdate+cp.validay), 'YYYYMMDD'),wt.code,vp.relname,vp.gender,vp.birthday,vp.contactaddress"+
					" from wx_vip vp LEFT JOIN wx_vipbaseset vt ON vp.viptype=vt.id LEFT JOIN WX_COUPON CP ON NVL(vt.LQTYPE,-1)=cp.id left join wx_store wt on vp.store_id=wt.id"+
					" WHERE vp.id=?",new Object[] {vipid});
		} catch (Exception e) {
			logger.error("opencard find vip error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "开卡失败，请重试");
			return vh;
		}
		
		if(all==null||al.size()<=0) {
			logger.error("opencard find vip error:not find data by vipid:"+vipid);
			vh.put("code", "-1");
			vh.put("message", "开卡失败");
			return vh;
		}
		
		al=(List)al.get(0);
		int integral=0;
		String coupontypecode=String.valueOf(al.get(4));

		try {
			integral=Tools.getInt(al.get(8), 0);
			offvipinfo.put("openid", String.valueOf (al.get(0)));
			offvipinfo.put("cardid",companyid);
			offvipinfo.put("wshno",String.valueOf(al.get(1)));
			offvipinfo.put("shopid",String.valueOf(al.get(2)));
			offvipinfo.put("viptype",String.valueOf(al.get(6)));
			offvipinfo.put("credit",integral);
			offvipinfo.put("storecode",String.valueOf(al.get(11)));
			offvipinfo.put("name",String.valueOf(al.get(12)));
			offvipinfo.put("gender",String.valueOf(al.get(13)));
			offvipinfo.put("birthday",String.valueOf(al.get(14)));
			offvipinfo.put("contactaddress",String.valueOf(al.get(15)));				
			offvipinfo.put("phonenum",String.valueOf(al.get(16)));

			isSendCoupon="Y".equalsIgnoreCase(String.valueOf(al.get(3)));
			couponUseType= String.valueOf(((List)al.get(0)).get(5));
			logger.debug("isSendCoupon->"+isSendCoupon+",couponCode->"+couponCode+",couponUseType->"+couponUseType);
			if(isSendCoupon||!"1".equalsIgnoreCase(couponUseType)&&nds.util.Validator.isNotNull(coupontypecode)) {
				offvipinfo.put("coupon",coupontypecode);
				offvipinfo.put("couponval",String.valueOf(al.get(7)));
				offvipinfo.put("begintime",String.valueOf(al.get(9)));
				offvipinfo.put("endtime",String.valueOf(al.get(10)));
				offparam.put("couponinfo", offcouponinfo);
			}
			offparam.put("vipinfo", offvipinfo);
			
			params.put("args[params]", offparam.toString());
			params.put("format", "JSON");
			params.put("client", "");
			params.put("ver","1.0");
			params.put("ts",ts);
			params.put("sig",sign);
			params.put("method","openCard");
		} catch (JSONException e) {
			logger.error("set offline params error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "开卡失败，请重试");
			return vh;
		}
		
		
		//商用线下开卡
		try{
			vh=RestUtils.sendRequest(serverUrl,params,"POST");
			//String url=serverUrl+"?"+RestUtils.delimit(params.entrySet(),true);
			//vh=RestUtils.sendRequest(url,null,"GET");
		} catch (Throwable e) {
			logger.debug("open card offline error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "开卡失败，请重试");
			return vh;
		}
		if(vh==null) {
			logger.error("open card offline error->return null");
			vh.put("code", "-1");
			vh.put("message", "开卡失败，请重试");
			return vh;
		}
		
		//处理线下开卡返回结果
		//{"result":{"data":{"code":"26f5lb99fr0-0","couponId":"6F5Lb99Fr"},"card":{"balance":0,"level":215,"no":"WX140515000000002","credit":0},"openid":"owAZBuEBBLn-LQ_5ebcbkSh_wFDk","cardid":"37"},"errMessage":"微生活会员开卡成功！","errCode":0}
		String result=(String) vh.get("message");
		logger.debug("open offline code result->"+result);
		JSONObject offjo=null;
		try {
			offjo= new JSONObject(result);
		}catch(Exception e) {
			vh.put("code", "-1");
			vh.put("message", "开卡失败，请重试");
			return vh;
		}
		
		//判断线下开卡是否成功
		if(offjo==null||offjo==JSONObject.NULL) {
			vh.put("code", "-1");
			vh.put("message", "线下开卡异常，请重试");
			return vh;
		}
		if(offjo.optInt("errCode",-1)!=0) {
			vh.put("code", "-1");
			vh.put("message", offjo.optString("errMessage"));
			return vh;
		}
		if(!offjo.has("result")) {
			vh.put("code", "-1");
			vh.put("message", "线下开卡异常，请重试");
			return vh;
		}
			
		//判断线下开卡是否返回会员信息
		JSONObject resjo=offjo.optJSONObject("result");
		if(resjo==null||resjo==JSONObject.NULL||!resjo.has("card")&&resjo.optJSONObject("card").has("no")) {
			vh.put("code", "-1");
			vh.put("message", "线下开卡异常，请重试");
			return vh;
		}
		
		//开卡送积分
		if(integral>0) {
			ArrayList returnparam=new ArrayList();
			ArrayList sendparam=new ArrayList();
			JSONObject sendintegral=new JSONObject();
			try {
				sendintegral.put("vipid", vipid);
				sendintegral.put("integral", integral);
				sendintegral.put("description", "开卡送积分");
				sendparam.add(sendintegral.toString());
				returnparam.add(java.sql.Clob.class);
				
				Collection list=QueryEngine.getInstance().executeFunction("wx_vip_adjustintegral",sendparam,returnparam);
				String res=(String)list.iterator().next();
				logger.debug("online opencard adjustintegral result->"+res);
			}catch(Exception e) {
				logger.error("opencard send integral error:"+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		//开卡送券
		
		JSONObject vipmessage=resjo.optJSONObject("data");
		if(isSendCoupon&&nds.util.Validator.isNotNull(coupontypecode)) {
			String couponcode=null;
			if(vipmessage!=null&&vipmessage!=JSONObject.NULL&&vipmessage.has("code")) {
				couponcode=vipmessage.optString("code");
			}
			JSONObject consumejo=new JSONObject();
			
			ArrayList paramss=new ArrayList();
			paramss.add(companyid);

			ArrayList para=new ArrayList();
			para.add(java.sql.Clob.class);
			
			try {
				consumejo.put("vipid", vipid);
				consumejo.put("couponcode",coupontypecode);
				consumejo.put("tickno",couponcode);
				paramss.add(consumejo.toString());
				
				Collection list=QueryEngine.getInstance().executeFunction("wx_coupon_$r_send",paramss,para);
				String res=(String)list.iterator().next();
				logger.debug("online brecommend send coupon result->"+res);
				//JSONObject tempjo=new JSONObject(res);
				//if(tempjo!=null&&"0".equals(tempjo.optString("code","-1"))){insertcount=1;}
			}catch (Exception e) {
				logger.debug("online brecommend send coupon erroe->"+e.getMessage());
				e.printStackTrace();
			}
		}
		
		//修改会员资料
		JSONObject offvipjo=resjo.optJSONObject("card");
		
		String sql="update wx_vip v set v.docno,v.vipcardno,v.viptype,v.store_id,v.isbd,v.opencard_status)="
				+" (select ?,?,nvl(vbs.id,ov.viptype),nvl(s.id,ov.store_id),?,2 from wx_vip ov left join wx_vipbaseset vbs on vbs.code=? and vbs.ad_client_id=? and vbs.code is not null left join wx_store s on s.code=? and s.ad_client_id=? and s.code is not null  where ov.id=?"
				+" where v.id=?";
		
		try {
			QueryEngine.getInstance().executeUpdate(sql, new Object[] {offvipjo.optString("no"),offvipjo.optString("wshno"),offvipjo.optString("isbd","N"),offvipjo.optString("level"),companyid,offvipjo.optString("shopcode"),companyid,vipid,vipid});
		} catch (Exception e) {
			logger.error("opencard update vip error:"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		vh.put("code", "0");
		vh.put("message", "开卡失败，请重试");
		return vh;
	}

}
