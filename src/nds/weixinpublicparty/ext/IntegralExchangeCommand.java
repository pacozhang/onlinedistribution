package nds.weixinpublicparty.ext;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.rest.RestUtils;
import nds.util.NDSException;
import nds.util.Tools;

import org.json.JSONObject;

public class IntegralExchangeCommand extends Command{
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
		if(tjo==null||!tjo.has("cardno")||!tjo.has("docno")||!tjo.has("integral")||tjo.optInt("ad_client_id",-1)<=0) {
			logger.debug("integralExchange params error->"+tjo.toString());
			vh.put("code","-1");
			vh.put("message","integralExchange params error");
			return vh;
		}
		
		List all=null;
		int ad_client_id=tjo.optInt("ad_client_id");
		String careid=String.valueOf(ad_client_id);
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
		}
		
		if(!isErp) {
			logger.debug("未接通ERP");
			vh.put("code","-1");
			vh.put("message","未接通ERP");
			return vh;
		}
		
		//判断使用积分只能大于0
		int useintegral=tjo.optInt("integral",0);
		if (useintegral<=0) {
			logger.debug("use integral is error->cardno:"+tjo.optString("cardno")+",useintegral:"+useintegral);
			vh.put("code","-1");
			vh.put("message","使用积分不正确，请重新下单");
			return vh;
		}
		String orderno=tjo.optString("docno");
		if (nds.util.Validator.isNull(orderno)) {
			logger.debug("orderno error->orderno:"+orderno+",ad_client_id:"+ad_client_id);
			vh.put("code","-1");
			vh.put("message","订单数据异常，请重新下单");
			return vh;
		}
		all=QueryEngine.getInstance().doQueryList("select o.ordertype,o.amt_integral from wx_order o where o.docno=? and o.ad_client_id=?",new Object[] {orderno,ad_client_id});
		if(all==null||all.size()<=0) {
			logger.debug("not find order->orderno:"+orderno+",ad_client_id:"+ad_client_id);
			vh.put("code","-1");
			vh.put("message","订单数据异常，请重新下单");
			return vh;
		}
		all=(List)all.get(0);
		int orderintegral=Tools.getInt(all.get(1), -1);
		if(orderintegral<=0) {
			logger.debug("use integral is error->cardno:"+tjo.optString("cardno")+",orderintegral:"+orderintegral);
			vh.put("code","-1");
			vh.put("message","使用积分不正确，请重新下单");
			return vh;
		}
/*		int ordertype=Tools.getInt(all.get(0), -1);
		if(ordertype!=3) {
			logger.debug("not integral order->orderno:"+orderno+",ad_client_id:"+ad_client_id);
			throw new Exception("非积分订单不能使用积分兑换");
		}*/
		
		//判断使用积分与订单中的应付积分是否一致
		if(orderintegral!=useintegral) {
			logger.debug("use integral is error->orderintegral:"+orderintegral+",useintegral:"+useintegral);
			vh.put("code","-1");
			vh.put("message","使用积分不正确，请重新下单");
			return vh;
		}
		String ts=String.valueOf(System.currentTimeMillis());
    	
    	
    	logger.debug("cardid=" + careid + ",ts=" + ts + ",skey="+ SKEY);
		String Sign="";
		try {
			Sign = nds.util.MD5Sum.toCheckSumStr(careid + ts+ SKEY);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		HashMap<String, String> params =new HashMap<String, String>();
		params.put("args[cardid]", careid);
		params.put("args[cardno]", tjo.optString("cardno"));
		params.put("args[docno]", tjo.optString("docno"));
		params.put("args[integral]", tjo.optString("integral"));
		
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
			logger.debug("integralExchange offline error->"+tx.getLocalizedMessage());
			tx.printStackTrace();
			vh.put("code","-1");
			vh.put("message","数据维护异常，请联系商家");
			return vh;
			//return;
		}
		String result=(String) vhr.get("message");
		logger.debug("integralExchange offline result->"+result);
		JSONObject jo=null;
		try {
			jo= new JSONObject(result);
		}catch(Exception e) {
			vh.put("code","-1");
			vh.put("message","线下会员兑换异常，请联系商家");
			return vh;
		}
		int recode=jo.optInt("errCode");
		if(recode!=0) {
			logger.debug("integralExchange offline error->"+jo.optString("errMessage"));
			vh.put("code","-1");
			vh.put("message",jo.optString("errMessage","积分兑换异常，请联系商家"));
			return vh;
		}else{
			QueryEngine.getInstance().executeUpdate("update wx_order t set t.integralexchanged='Y' where t.docno=? and t.ad_client_id=?", new Object[] {orderno,ad_client_id});
		}
		vh.put("code","0");
		vh.put("message","积分扣减成功");
		return vh;
	}

}
