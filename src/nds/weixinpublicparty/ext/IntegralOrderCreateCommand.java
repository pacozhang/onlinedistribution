package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.rest.RestUtils;
import nds.util.NDSException;

public class IntegralOrderCreateCommand extends Command{
	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		JSONObject tjo = (JSONObject)event.getParameterValue("jsonObject");
		try {
			tjo=new JSONObject(tjo.optString("params"));
		} catch (Exception e) {
			logger.debug("get params error->"+e.getLocalizedMessage());
			e.printStackTrace();
		}
/*		if(tjo==null||!tjo.has("cardno")||!tjo.has("docno")||!tjo.has("integral")||tjo.optInt("ad_client_id",-1)<=0) {
			logger.debug("integralExchange params error->"+tjo.toString());
			vh.put("code","-1");
			vh.put("message","integralExchange params error");
			return vh;
		}*/
		
		int vipid = tjo.optInt("WX_VIP_ID");
		//int addressid = tjo.optInt("WX_ADDRESS_ID");
		int tot_qty = tjo.optInt("TOT_QTY");
		int integral= tjo.optInt("INTEGRAL");
		int brand_appendgoodsid = tjo.optInt("WX_BRAND_APPENDGOODS_ID");
		//int alias_id = tjo.optInt("WX_ALIAS_ID");
		int ad_client_id=tjo.optInt("ad_client_id");
		
		String name = tjo.optString("name");
		String phonenum = tjo.optString("phonenum");
		String province = tjo.optString("province");
		String city = tjo.optString("city");
		String regionid = tjo.optString("regionid");
		String address = tjo.optString("address");
		
		
		//String remarks = tjo.optString("REMARKS");
		
		if(hasErrorParams(vipid,//addressid,
				tot_qty,integral,brand_appendgoodsid,ad_client_id,
				name,phonenum,province,city,regionid,address)){
			vh.put("code","-1");
			vh.put("message","IntegralOrderCreateCommand params error"+tjo.toString());
			return vh;
		}
		
		//1.验证vip积分是否足够
		JSONObject vipjo = QueryEngine.getInstance().doQueryObject("select t.integral,t.vipcardno from wx_vip t where t.id=?",new Object[]{vipid},false);
		
		int vip_integral = vipjo.optInt("integral");
		String cardno = vipjo.optString("vipcardno");
		
		int tot_integral = integral*tot_qty;
		if(tot_integral>vip_integral){
			logger.debug("use integral is error->tot_integral:"+integral+",vip_integral:"+vip_integral);
			vh.put("code","-1");
			vh.put("message","积分不足，请重新下单");
			return vh;
		}

		
		//2.判断使用积分与商品中的应付积分是否一致
		int need_integral =QueryEngine.getInstance().doQueryInt("select ag.pointprice from wx_brand_appendgoods ag where ag.id=? and ag.ad_client_id=?",new Object[] {brand_appendgoodsid,ad_client_id});
		
		if(need_integral!=integral) {
			logger.debug("use integral is error->tot_integral:"+integral+",need_integral:"+need_integral);
			vh.put("code","-1");
			vh.put("message","使用积分不正确，请重新下单");
			return vh;
		}
		
		
		//3.请求erp扣减积分
		String serverUrl="";
		String SKEY="";
		boolean isErp=false;
		JSONObject ifsjo = QueryEngine.getInstance().doQueryObject("select ifs.erpurl,ifs.username,ifs.iserp,wc.wxparam from WX_INTERFACESET ifs left join web_client wc on wc.ad_client_id=ifs.ad_client_id WHERE ifs.ad_client_id=?", new Object[]{ad_client_id}, false);
		if(ifsjo==JSONObject.NULL||ifsjo==null){
			logger.debug("not find WX_INTERFACESET");
			vh.put("code","-1");
			vh.put("message","数据维护异常，请联系商家");
			return vh;
		}else{
			serverUrl=ifsjo.optString("erpurl");
			SKEY=ifsjo.optString("wxparam");
			isErp="Y".equalsIgnoreCase(ifsjo.optString("iserp"));
			if(isErp && nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY)) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
				vh.put("code","-1");
				vh.put("message","数据维护异常，请联系商家");
				return vh;
			}
		}
/*		List all=null;

		String serverUrl="";
		String SKEY="";
		boolean isErp=false;
		
		all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,wc.wxparam from WX_INTERFACESET ifs left join web_client wc on wc.ad_client_id=ifs.ad_client_id WHERE ifs.ad_client_id="+ad_client_id);
		
		if(all!=null&&all.size()>0) {
			System.out.println("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			SKEY=(String)((List)all.get(0)).get(3);
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			if(isErp && nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY)) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
				vh.put("code","-1");
				vh.put("message","数据维护异常，请联系商家");
				return vh;
			}
		}else {
			logger.debug("not find WX_INTERFACESET");
			vh.put("code","-1");
			vh.put("message","数据维护异常，请联系商家");
			return vh;
		}*/
		
		if(!isErp) {
			logger.debug("未接通ERP");
			vh.put("code","-1");
			vh.put("message","未接通ERP");
			return vh;
		}

		//先生成订单
		JSONObject consumejo=new JSONObject();
		try {
				consumejo.put("vipid", vipid);
				consumejo.put("ad_client_id", ad_client_id);
				
				//consumejo.put("wx_address_id", addressid);
				
				consumejo.put("name", name);
				consumejo.put("phonenum", phonenum);
				consumejo.put("province", province);
				consumejo.put("city", city);
				consumejo.put("regionid", regionid);
				consumejo.put("address", address);

				

				consumejo.put("brand_appendgoodsid", brand_appendgoodsid);
				//consumejo.put("aliasid", alias_id);
				consumejo.put("tot_qty", tot_qty);
				consumejo.put("tot_integral",tot_integral);

		} catch (JSONException e) {
				e.printStackTrace();
				vh.put("code","-1");
				vh.put("message","数据维护异常");
				return vh;
		}
 		ArrayList params2=new ArrayList();
	    params2.add(ad_client_id);
		params2.add(consumejo.toString());
		
		ArrayList para = new ArrayList();

		para.add(java.sql.Clob.class);
		Collection list = QueryEngine.getInstance().executeFunction("wx_integralorder_create", params2, para);
		String resultStr = (String) list.iterator().next();
		JSONObject jo2;
		try {
			logger.debug("resultStr->" + resultStr);
			jo2 = new JSONObject(resultStr);
		} catch (JSONException e) {
			e.printStackTrace();
			logger.debug("积分订单生成异常");
			vh.put("code","-1");
			vh.put("message","库存不足");
			return vh;
		}
		if (jo2.optInt("code", -1) != 0) {
			logger.debug("积分订单生成异常");
			vh.put("code","-1");
			vh.put("message",jo2.optString("message"));
			return vh;
		} 
		String docno = jo2.optString("orderno");
		int orderid = jo2.optInt("orderid");

		
		
		String ts=String.valueOf(System.currentTimeMillis());
		String cardid=String.valueOf(ad_client_id);
    	
    	logger.debug("cardid=" + cardid + ",ts=" + ts + ",skey="+ SKEY);
		String Sign="";
		try {
			Sign = nds.util.MD5Sum.toCheckSumStr(cardid + ts+ SKEY);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("args[cardid]", cardid);
		params.put("args[cardno]", cardno);
		params.put("args[docno]", docno);
		params.put("args[integral]", String.valueOf(tot_integral));
		
		params.put("format","JSON");
		params.put("client","");
		params.put("ver","1.0");
		params.put("sig",Sign);
		params.put("ts",ts);
		params.put("method","integralExchange");
		
		ValueHolder vhr=null;
		try{
			vhr=RestUtils.sendRequest(serverUrl,params,"POST");
		} catch (Throwable tx) {
			logger.debug("IntegralOrderCreateCommand offline error->"+tx.getLocalizedMessage());
			tx.printStackTrace();
			vh.put("code","-1");
			vh.put("message","数据维护异常，请联系商家");
			return vh;
			//return;
		}
		String result=(String) vhr.get("message");
		logger.debug("IntegralOrderCreateCommand offline result->"+result);
		JSONObject jo=null;
		try {
			jo= new JSONObject(result);
		}catch(Exception e) {
			vh.put("code","-1");
			vh.put("message","线下会员积分兑换异常，请联系商家");
			return vh;
		}
		int recode=jo.optInt("errCode",-1);
		if(recode!=0) {
			logger.debug("IntegralOrderCreateCommand offline error->"+jo.optString("errMessage"));
			vh.put("code","-1");
			vh.put("message",jo.optString("errMessage","积分兑换异常，请联系商家"));
			QueryEngine.getInstance().executeUpdate("update wx_order t set t.isactive='N',t.ordermessage=? where t.id=? and t.ad_client_id=?", new Object[] {jo.optString("errMessage","积分兑换异常，请联系商家"),orderid,ad_client_id});
			
		}else{
			QueryEngine.getInstance().executeUpdate("update wx_order t set t.integralexchanged='Y',t.sale_status='1' where t.id=? and t.ad_client_id=?", new Object[] {orderid,ad_client_id});
			vh.put("code","0");
			vh.put("message","积分兑换成功");
		}
	

		return vh;
	}
	
	
	private boolean hasErrorParams(Object... objs){
		for(Object obj:objs){
			if(obj instanceof Integer){
				if( (Integer)obj<=0)return true;
			}else if(obj instanceof String){
				if(nds.util.Validator.isNull((String)obj))return true ;
			}
		}
		return false;
	}
}
