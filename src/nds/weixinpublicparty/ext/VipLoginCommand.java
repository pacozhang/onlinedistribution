package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.rest.RestUtils;
import nds.util.NDSException;
import nds.weixin.ext.tools.StringUtils;

public class VipLoginCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) 
			throws NDSException,RemoteException {
		JSONObject jo=null;
		ValueHolder vh=new ValueHolder();
		
		try {
			jo = (JSONObject) event.getParameterValue("jsonObject");
			jo=jo.optJSONObject("params");
		}catch(Exception e) {
			logger.error("params error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		
		
		//判断公司ID 与VIP ID是否在参数中传入
		if (jo==null||!jo.has("companyid")||!jo.has("vipid")) {
			logger.error("params error:not put companyid or vipid");
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		
		int vipid=jo.optInt("vipid",-1);
		int companyid=jo.optInt("companyid",-1);
		
		if (companyid<=0 || vipid<=0 ) {
			logger.error("params error:companyid:"+companyid+",vipid:"+vipid);
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
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
			vh.put("messae", "登陆异常请重试");
			return vh ;
		}
		if (all==null||all.size()<=0) {
			logger.error("select set offline params error:not find data");
			vh.put("code", "-1");
			vh.put("messae", "登陆异常请重试");
			return vh ;
		}
		all=(List)all.get(0);
		
		String serverUrl=StringUtils.valueOf(all.get(0));
		boolean isErp="Y".equalsIgnoreCase(StringUtils.valueOf(all.get(2)));
		String SKEY=StringUtils.valueOf(all.get(3));
		boolean isVerifyCode="Y".equalsIgnoreCase(StringUtils.valueOf(all.get(4)));
		if(isErp&&(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY))) {
			logger.error("SERVERuRL OR SKEY IS NULL");
			vh.put("code", "-1");
			vh.put("messae", "登陆异常请重试");
			return vh ;
		}
		
		//判断会员是否已注册或已领卡
		String ops=null;
		List vip=null;
		try {
			vip=QueryEngine.getInstance().doQueryList("select v.opencard_status,v.wechatno,v.vipcardno,vbs.code,s.code,g.code,v.integral,v.lastamt,v.name,v.gender,v.birthday,v.docno from wx_vip v join wx_vipbaseset vbs on v.viptype=vbs.id left join wx_store s on v.store_id=s.id left join wx_guide g on v.guide=g.id where v.id=?",new Object[]{vipid});
		} catch (Exception e) {
			logger.error("select vipinfo error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("messae", "登陆异常请重试");
			return vh ;
		}
		if (vip==null||vip.size()<=0) {
			logger.error("select set offline params error:not find data");
			vh.put("code", "-1");
			vh.put("messae", "登陆异常请重试");
			return vh ;
		}
		vip=(List)vip.get(0);
		ops=String.valueOf(vip.get(0));
		String openid=StringUtils.valueOf(vip.get(1));
		String vipno=StringUtils.valueOf(vip.get(2));
		String viptypecode=StringUtils.valueOf(vip.get(3));
		String storecode=StringUtils.valueOf(vip.get(4));
		String guidecode=StringUtils.valueOf(vip.get(5));
		String integral=StringUtils.valueOf(vip.get(6));
		String amount=StringUtils.valueOf(vip.get(7));
		String vipname=StringUtils.valueOf(vip.get(8));
		String sex=StringUtils.valueOf(vip.get(9));
		String birthday=StringUtils.valueOf(vip.get(10));
		String docno=StringUtils.valueOf(vip.get(11));
		
		if("2".equals(ops)) {
			vh.put("code", "0");
			vh.put("messae", "会员已领卡。");
			return vh ;
		}
		
		
		String brandcode=jo.optString("brandcode");
		String mobile=jo.optString("mobile");
		
		if(nds.util.Validator.isNull(brandcode)) {
			vh.put("code", "-1");
			vh.put("messae", "请输入品牌");
			return vh ;
		}
		if(nds.util.Validator.isNull(mobile)) {
			vh.put("code", "-1");
			vh.put("messae", "请输入手机");
			return vh ;
		}
		
		if(!isErp) {
			vh.put("code", "-2");
			vh.put("messae", "客户未接通线下，请走领卡流程。");
			return vh ;
		}
		
		
		//
		String ts=String.valueOf(System.currentTimeMillis());
		String sign=null;
		try {
			sign = nds.util.MD5Sum.toCheckSumStr(companyid + ts+ SKEY);
		} catch (IOException e) {
			logger.debug("opencard md5 error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		
		JSONObject offparam=new JSONObject();
		HashMap<String, String> params =new HashMap<String, String>();
		try {
			offparam.put("brandcode", brandcode);
			offparam.put("mobile", mobile);
			offparam.put("openid", openid);
			offparam.put("vipno", vipno);
			offparam.put("typecode", viptypecode);
			offparam.put("storecode", storecode);
			offparam.put("guidecode", guidecode);
			offparam.put("integral", integral);
			offparam.put("amount", amount);
			offparam.put("name", vipname);
			offparam.put("sex", sex);
			offparam.put("birthday", birthday);
			offparam.put("docno", docno);
			offparam.put("cardid", String.valueOf(companyid));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		
		params.put("args[params]", offparam.toString());
		params.put("args[cardid]", String.valueOf(companyid));
		params.put("format", "JSON");
		params.put("client", "");
		params.put("ver","1.0");
		params.put("ts",ts);
		params.put("sig",sign);
		params.put("method","viplogin");
		
		try{
			vh=RestUtils.sendRequest(serverUrl,params,"POST");
		} catch (Throwable e) {
			logger.debug("login offline error->"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		if(vh==null) {
			logger.error("login offline error->return null");
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		
		//处理线下开卡返回结果
		String result=(String) vh.get("message");
		logger.debug("login offline result->"+result);
		JSONObject offjo=null;
		try {
			offjo= new JSONObject(result);
		}catch(Exception e) {
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		
		//判断线下开卡是否成功
		if(offjo==null||offjo==JSONObject.NULL) {
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		if(offjo.optInt("errCode",-1)!=0) {
			vh.put("code", offjo.optString("errCode","-1"));
			vh.put("message", offjo.optString("errMessage"));
			return vh;
		}
		if(!offjo.has("result")) {
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		offjo=offjo.optJSONObject("result");
		
		String sql="update wx_vip v set (v.viptype,v.vippassword,v.store_id,v.opendate,v.integral,v.lastamt,v.opencard_status,v.phonenum,v.idcard,v.gender,v.birthday,v.contactaddress,v.email,v.docno,v.relname,v.province,v.city,v.area)="
				   +"(select nvl(vbs.id,v.viptype),?,nvl(s.id,v.store_id),to_number(to_char(sysdate,'yyyyMMdd')),?,?,2,?,?,?,?,?,?,?,?,?,?,? from web_client c left join wx_vipbaseset vbs on c.ad_client_id=? and vbs.ad_client_id = c.ad_client_id and vbs.code=? left join wx_store s on s.code=? and s.ad_client_id=? where c.ad_client_id=?)"
				   +"where v.id=?";
		try {
			QueryEngine.getInstance().executeUpdate(sql, new Object[] {offjo.optString("psd"),offjo.optDouble("integral", 0),offjo.optDouble("amt", 0),offjo.optString("mobile"),offjo.optString("idcard"),offjo.optString("gender"),offjo.optString("birthday"),offjo.optString("address"),offjo.optString("email"),offjo.optString("docno"),offjo.optString("name"),offjo.optString("province"),offjo.optString("city"),offjo.optString("area"),companyid,offjo.optString("typecode"),offjo.optString("storecode"),companyid,companyid,vipid});
		} catch (Exception e) {
			logger.error("login update vip error:"+e.getLocalizedMessage());
			e.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "登陆异常请重试");
			return vh;
		}
		
		
		vh.put("code", "0");
		vh.put("message", "登陆成功");
		return vh;
	}

}
