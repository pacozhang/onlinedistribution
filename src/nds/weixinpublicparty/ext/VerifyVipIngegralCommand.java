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
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class VerifyVipIngegralCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		boolean isErp=false;
		String serverUrl=null;
		String SKEY=null;
		String Sign=null;
		String careid=null;
		String ts=null;
		//获取用户
		
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int ad_client_id=jo.optInt("ad_client_id");
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(ad_client_id);
		if(wu==null) {
			logger.debug("wu is null->getByAdClientId("+ad_client_id+")");
			vh.put("code","-1");
			vh.put("message","数据异常请重试");
			return vh;
		}
		int vipid=jo.optInt("vipid",-1);
		if(vipid<=0) {
			logger.debug("vipid is error->"+vipid);
			vh.put("code","-1");
			vh.put("message","会员数据异常请重试");
			return vh;
		}
		
		
		
		
		//查询接口相关信息 url,skey
		List all=QueryEngine.getInstance().doQueryList("select ifs.erpurl,ifs.username,ifs.iserp,wc.wxparam,nvl(ifs.ismesauth,'N') from WX_INTERFACESET ifs join web_client wc on ifs.ad_client_id=wc.ad_client_id WHERE ifs.ad_client_id="+ad_client_id);
		
		
		if(all!=null&&all.size()>0) {
			logger.debug("WX_INTERFACESET size->"+all.size());
			serverUrl=(String)((List)all.get(0)).get(0);
			isErp="Y".equalsIgnoreCase((String)((List)all.get(0)).get(2));
			SKEY=(String)((List)all.get(0)).get(3);
			if(isErp&&(nds.util.Validator.isNull(serverUrl)||nds.util.Validator.isNull(SKEY))) {
				logger.debug("SERVERuRL OR SKEY IS NULL");
			}
		}else {
			logger.debug("not find WX_INTERFACESET ad_client_id->"+ad_client_id);
		}
		careid=String.valueOf(ad_client_id);
		ts=String.valueOf(System.currentTimeMillis());
		try {
			Sign = nds.util.MD5Sum.toCheckSumStr(careid + ts+ SKEY);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(isErp) {
			Object o=QueryEngine.getInstance().doQueryOne("select v.vipcardno from wx_vip v where v.id=?", new Object[] {vipid});
			String vipno=String.valueOf(o);
			if(nds.util.Validator.isNull(vipno)) {
				vh.put("code","-1");
				vh.put("message","会员信息异常，请重试");
				return vh;
			}
			HashMap<String, String> params =new HashMap<String, String>();
			params.put("args[cardid]",careid);
			
			params.put("args[vipno]",vipno);
			
			String integral=jo.optString("integral");
			if(nds.util.Validator.isNotNull(integral)){
				params.put("args[integral]",integral);
			}
			
			
			String ticketno = jo.optString("ticketno");

			if(nds.util.Validator.isNotNull(ticketno)){
				params.put("args[ticketno]",ticketno);
			}
			
			params.put("format","JSON");
			params.put("client","");
			params.put("ver","1.0");
			params.put("ts",ts);
			params.put("sig",Sign);
			params.put("method","verifyintegral");
			try{
				vh=RestUtils.sendRequest(serverUrl,params,"POST");
				//String url=serverUrl+"?"+RestUtils.delimit(params.entrySet(),true);
				//vh=RestUtils.sendRequest(url,null,"GET");
			} catch (Throwable tx) {
				logger.debug("verifyintegral offline error->"+tx.getLocalizedMessage());
				tx.printStackTrace();
			}
			
			String result=(String) vh.get("message");
			logger.debug("verifyintegral offline result->"+result);
			JSONObject rjo=null;
			try {
				rjo= new JSONObject(result);
			}catch(Exception e) {
				logger.debug("verifyintegral offline error->"+e.getLocalizedMessage());
				e.printStackTrace();
				vh.put("code","-1");
				vh.put("message","ERP验证积分或优惠券异常，请重试");
				return vh;
			}
			vh.put("code", rjo.optInt("errCode"));
			vh.put("message", rjo.optString("errMessage"));
			vh.put("data", rjo.optString("data"));
		}else {
			vh.put("code",0);
			vh.put("message","操作成功");
		}
		return vh;
	}
}
