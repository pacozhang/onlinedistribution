package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeTemplate;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;
import nds.weixin.ext.tools.AuthorizeUrl;

/**
 * 订单提交成功通知
 * @author kunlun
 *
 */
public class SubmitSuccessCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		// TODO Auto-generated method stub
		ValueHolder vh =new ValueHolder();
		JSONObject jo=null;
		
		try{
			jo=(JSONObject) event.getParameterValue("jsonObject");
			jo=jo.optJSONObject("params");
		}catch(Exception e){
			e.printStackTrace();
			vh.put("code", -1);
			vh.put("message", "提交订单失败1");
		}
		logger.debug("params--"+jo.toString());
		if(jo==null||!jo.has("orderid")||!jo.has("companyid")||!jo.has("templateCode")){
			vh.put("code", -1);
			vh.put("message", "没有找到相应参数");
			return vh;
		}
		//传入的参数
		int orderid=jo.optInt("orderid",-1);
		int companyid=jo.optInt("companyid",-1);
		String code=jo.optString("templateCode");
		
		if(companyid<=0){
			logger.error("params error:companyid");
			vh.put("code", "-1");
			vh.put("message", "提交订单失败2");
			return vh;
		}
		
		WeUtils wu=WeUtilsManager.getByAdClientId(companyid);
		
		if(wu==null) {
			vh.put("code", "-1");
			vh.put("message", "提交订单失败3");
			return vh;
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		
		if(orderid<=0){
			logger.error("params error:orderid:"+orderid);
			vh.put("code", "-1");
			vh.put("message", "提交订单失败4");
			return vh;
		}
		if(code==null){
			logger.error("params error:templateCode:"+code);
			vh.put("code", "-1");
			vh.put("message", "提交订单失败5");
			return vh;
		}
		
		//---------------------------------------------数据库部分
		List all = null;
		try{
			all=QueryEngine.getInstance().doQueryList("select o.docno,o.tot_amt,v.wechatno,o.creationdate,o.payment from wx_order o join wx_vip v on o.wx_vip_id=v.id where o.id=?",new Object[] {orderid});
			
		}catch(Exception e){
			e.printStackTrace();
			vh.put("code", -1);
			vh.put("message", "提交订单失败6");
			return vh;
		}
		if(all==null||all.size()<=0){
			vh.put("code", -1);
			vh.put("message", "提交订单失败7");
		}
		all=(List)all.get(0);
		//docno--->te_num 为订单号  tit_amt----->total为金额   wechatno------>openid为发送的人物
		String te_num=String.valueOf(all.get(0));
		String total=String.valueOf(all.get(1));
		String openid=String.valueOf(all.get(2));
		String creationdate=String.valueOf(all.get(3));
		String payment=String.valueOf("微支付");
		
		if(openid==null){
			vh.put("code", -1);
			vh.put("message", "发送失败！");
			return vh;
		}

		List allfr = null;
		try {
			allfr=QueryEngine.getInstance().doQueryList("select t.templateid,t.first,t.remark from wx_template t where t.ad_client_id=? and t.code=?",new Object[]{companyid,code});
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			vh.put("code", -1);
			vh.put("message", "提交订单失败8");
			return vh;
		}
		allfr=(List)allfr.get(0);
		
		String templateid=String.valueOf(allfr.get(0));
		String first=String.valueOf(allfr.get(1));
		String remark=String.valueOf(allfr.get(2));
		if(templateid==null){
			vh.put("code", -1);
			vh.put("message", "没有获取到模板！");
			return vh;
		}
		
		//---------------------------------------------发给微信的json数据
		JSONObject first1=new JSONObject();
		JSONObject te_num1=new JSONObject();
		JSONObject total1=new JSONObject();
		JSONObject creationdate1=new JSONObject();
		JSONObject payment1=new JSONObject();
		JSONObject remark1=new JSONObject();
		try{
			first1.put("value", first);
			first1.put("color", "#173177");
			te_num1.put("value", te_num);
			te_num1.put("color", "#173177");
			total1.put("value", total);
			total1.put("color", "#173177");
			creationdate1.put("value", creationdate);
			creationdate1.put("color", "#173177");
			payment1.put("value", payment);
			payment1.put("color", "#173177");
			remark1.put("value", remark);
			remark1.put("color", "#173177");
		}catch(Exception e){
			e.printStackTrace();
		}
		JSONObject data=new JSONObject();
		try {
			data.put("first", first1);
			data.put("keyword1", te_num1);
			data.put("keyword2", creationdate1);
			data.put("keyword3", total1);
			data.put("keyword4", payment1);
			data.put("remark", remark1);
		} catch (Exception e) {
			// TODO: handle exception
		}
		String url = AuthorizeUrl.getAuthorizeUrl(wu, "/html/nds/oto/webapp/order/index.vml?id="+orderid);
		JSONObject submitsuccess=new JSONObject();
		try {
			submitsuccess.put("touser", openid);
			submitsuccess.put("template_id", templateid);
			submitsuccess.put("url", url);
			submitsuccess.put("data", data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String sb=submitsuccess.toString();
		logger.debug("submitparams---"+sb);
		if(nds.util.Validator.isNull(sb)) {
			vh.put("code", "-1");
			vh.put("message", "发送模板内容为空");
			return vh;
		}
		
		WeTemplate wt=WeTemplate.getInstance(wu.getAppId());
		JSONObject wnjo = wt.sendTemplate(wc, sb);
		
		if(wnjo!=null) {
			vh.put("code",wnjo.optInt("code"));
			vh.put("message",wnjo.optString("message"));
			vh.put("data", wnjo);
		}else {
			vh.put("code","-1");
			vh.put("message","失败！");
		}
		
		return vh;
	}

}
