package nds.weixinpublicparty.ext;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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

import org.json.JSONException;
import org.json.JSONObject;

public class SendCouponsCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		JSONObject jo=null;
		try{
			jo = (JSONObject) event.getParameterValue("jsonObject");
			jo=jo.optJSONObject("params");
		}catch(Exception e){
			e.printStackTrace();
			vh.put("code", -1);
			vh.put("messgae", "�����Żݾ�ʧ��1");
			return vh;
		}
		if(jo==null||!jo.has("couponsid")){
			vh.put("code", "-1");
			vh.put("message", "�����Żݾ�ʧ��2");
			return vh;
		}
		int couponsid=jo.optInt("couponsid",-1);
		logger.error("couponsid-------->"+couponsid);
		if(couponsid<=0){
			logger.error("params error:companyid");
			vh.put("code", "-1");
			vh.put("message", "�����Żݾ�ʧ��3");
			return vh;
		}
		int companyid=jo.optInt("companyid",-1);
		WeUtils wu=WeUtilsManager.getByAdClientId(companyid);
		logger.error("wu-------->"+wu);
		if(wu==null) {
			vh.put("code", "-1");
			vh.put("message", "��ȡ�����û����ں�������Ϣ");
			return vh;
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		
		String templateCode = jo.optString("templateCode","");
		List<ArrayList> tli = QueryEngine.getInstance().doQueryList("select t.templateid,t.first,t.remark from wx_template t where t.ad_client_id=? and t.code=?", new Object[]{companyid,templateCode});
		if(tli==null||tli.size()!=1){
			vh.put("code", "-1");
			vh.put("message", "δ�ҵ�ģ����Ϣ! templateCode-->"+templateCode);
			logger.debug("δ�ҵ�ģ����Ϣ! templateCode-->"+templateCode);
			return vh;
		}
		String templateid = String.valueOf(tli.get(0).get(0));
		String firsts = String.valueOf(tli.get(0).get(1));
		String remarks = String.valueOf(tli.get(0).get(2));
		
		//----------------------------------------------------------SQL������(���Ƿָ���)
		List all=null;
		try{
			//name���Żݾ������  value����ֵ minimumcharge���������(����)
			all=QueryEngine.getInstance().doQueryList("select c.name,c.value,c.minimumcharge,to_number(to_char(decode(nvl(c.validay,0),0,nvl(c.starttime,sysdate), sysdate), 'YYYYMMDD')),to_number(to_char(decode(nvl(c.validay,0),0, nvl(c.endtime, add_months(nvl(c.starttime,sysdate), 1)),sysdate+c.validay), 'YYYYMMDD')),v.wechatno from wx_couponemploy ce join wx_coupon c on ce.wx_coupon_id=c.id join wx_vip v on v.id=ce.wx_vip_id where ce.id=?",new Object[] {couponsid});
		}catch(Exception e){
			e.printStackTrace();
			vh.put("code", -1);
			vh.put("message", "�����Żݾ�ʧ��4");
		}
		if(all==null||all.size()<0){
			logger.error("all-------->"+all);
			vh.put("code", -1);
			vh.put("message", "��ʱû�з�����Ϣ");
			return vh;
		}
		all=(List)all.get(0);
		String name=String.valueOf(all.get(0));
		String value=String.valueOf(all.get(1));
		String minimumcharge=String.valueOf(all.get(2));
		String starttime=String.valueOf(all.get(3));
		String endtime=String.valueOf(all.get(4));
		String openid=String.valueOf(all.get(5));

		//-----------------------------------------------------��������ݣ����Ƿָ��ߣ�
		JSONObject first=new JSONObject(); 
		JSONObject keynote1=new JSONObject();
		JSONObject keynote2=new JSONObject();
		JSONObject keynote3=new JSONObject();
		JSONObject keynote4=new JSONObject();
		JSONObject keynote5=new JSONObject();
		JSONObject remark=new JSONObject();
		try{
			//first.put("value", "���յ�һ��"+value+"��"+name+"���Żݾ�");
			first.put("value", firsts);
			first.put("color", "#173177");//------------------------first
			keynote1.put("value",name+"�Żݾ�");
			keynote1.put("color", "#173177");//---------------------keynote1
			keynote2.put("value",name+"�Żݾ�");
			keynote2.put("color", "#173177");//---------------------keynote2
			keynote3.put("value", value);
			keynote3.put("color", "#173177");//----------------------keynote3
			keynote4.put("value", "������"+minimumcharge);
			keynote4.put("color", "#173177");//----------------------keynote4
			keynote5.put("value", "��"+starttime+"��"+endtime);
			keynote5.put("color", "#173177");//----------------------keynote5
			//remark.put("value", "֧����ʽ����΢��ˢ��,��"+name+"ʹ��");
			remark.put("value", remarks);
			remark.put("color", "#173177");//-------------------------remark
		}catch(Exception e){
			e.printStackTrace();
		}
			JSONObject data=new JSONObject();
			try{
				data.put("first", first);
				data.put("keynote1", keynote1);
				data.put("keynote2", keynote2);
				data.put("keynote3", keynote3);
				data.put("keynote4", keynote4);
				data.put("keynote5", keynote5);
				data.put("remark", remark);
			}catch(Exception e){
				e.printStackTrace();
			}

			JSONObject sendcoupons=new JSONObject();
			try {
				sendcoupons.put("touser", openid);
				sendcoupons.put("template_id", templateid);
			    String mycoupon_url = AuthorizeUrl.getAuthorizeUrl(wu, "/html/nds/oto/webapp/coupon/index.vml");
				sendcoupons.put("url", mycoupon_url);
				sendcoupons.put("data", data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String cp=sendcoupons.toString();
			if(nds.util.Validator.isNull(cp)) {
				vh.put("code", "-1");
				vh.put("message", "����ģ������Ϊ��");
				return vh;
			}
			WeTemplate wt=WeTemplate.getInstance(wu.getAppId());
			JSONObject wnjo = wt.sendTemplate(wc, cp);
			
			if(wnjo!=null) {
				vh.put("code",wnjo.optInt("code"));
				vh.put("message",wnjo.optString("message"));
				vh.put("data", wnjo);
			}else {
				vh.put("code","-1");
				vh.put("message","ʧ�ܣ�");
			}
			
			
		return vh;
	}
	
}
