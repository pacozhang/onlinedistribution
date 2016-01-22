package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeUser;
import nds.query.QueryEngine;
import nds.util.NDSException;
import nds.util.Tools;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class WeUpdateVipCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();
		int tvipid=-1;
		int compaynid=-1;
		String openid=null;
		int vipid=Tools.getInt(event.getParameterValue("vipid"), -1);
		if(vipid<=0) {
			vh.put("code","-1");
			vh.put("message", "会员ID不对");
			return vh;
		}
		List vip=null;
		String updatetime=null;
		String currentdate=null;
		String searchvip="select vi.id,vi.ad_client_id,vi.wechatno,vi.updatedate from wx_vip_inqury vi where vi.wx_vip_id=?";
		String updatetvip="update wx_vip_inqury vi set vi.PHOTO=?,vi.NAME=?,vi.UNIONID=?,vi.COUNTRY=?,vi.PROVINCE=?,vi.CITY=?,vi.GENDER=?,vi.CONTACTADDRESS=?,vi.ISSUBSCRIBE=?,vi.updatedate=to_char(sysdate,'yyyyMMdd')"+
			  " where vi.id=?";
		try {
			vip=QueryEngine.getInstance().doQueryList(searchvip,new Object[] {vipid});
		}catch(Exception e) {
			logger.debug("update vip search vipinfo error->"+e.getLocalizedMessage());
		}
		if(vip==null||vip.size()<=0) {
			vh.put("code","-1");
			vh.put("message", "会员ID："+vipid+",不存在");
			return vh;
		}
		tvipid=Tools.getInt(((List)vip.get(0)).get(0), -1);
		compaynid=Tools.getInt(((List)vip.get(0)).get(1), -1);
		updatetime=String.valueOf(((List)vip.get(0)).get(3));
		openid=String.valueOf(((List)vip.get(0)).get(2));
		if(nds.util.Validator.isNull(openid)||tvipid<=0 || compaynid<=0) {
			vh.put("code","-1");
			vh.put("message", "会员ID："+vipid+",信息不正确");
			return vh;
		}
		
		Date d = new Date(); 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		currentdate=formatter.format(d);
		
		if(nds.util.Validator.isNull(updatetvip)&&currentdate.equalsIgnoreCase(updatetime)) {
			vh.put("code","0");
			vh.put("message", "今天已更新");
			return vh;
		}


		WeUtils wu=WeUtilsManager.getByAdClientId(compaynid);
		if(wu==null) {
			logger.debug("wu is null");
			vh.put("code","-1");
			vh.put("message", "公司："+compaynid+",wu is not find");
			return vh;
		}
		
		WeUser gua=WeUser.getInstance(wu.getAppId());
		
		JSONObject userjo=null;
		userjo=gua.getUser(openid);
		
		if(userjo==null||!userjo.has("openid")) {
			logger.debug("userjo is null");
			vh.put("code","-1");
			vh.put("message", "微信会员："+openid+"not find");
			return vh;
		}
		StringBuffer address=new StringBuffer();
		address.append(userjo.optString("country"));
		address.append(userjo.optString("province"));
		address.append(userjo.optString("city"));
		
		try {
			QueryEngine.getInstance().executeUpdate(updatetvip, new Object[] {userjo.optString("headimgurl",""),userjo.optString("nickname",""),userjo.optString("unionid",""),userjo.optString("country",""),userjo.optString("province",""),userjo.optString("city",""),userjo.optString("sex"),(address.length()<=0?"":address.toString()),(0==userjo.optInt("subscribe",0)?"N":"Y"),tvipid});
			QueryEngine.getInstance().executeUpdate("update wx_vip v set v.photo=?,v.name=? where v.id=?",new Object[] {userjo.optString("headimgurl",""),userjo.optString("nickname",""),vipid});
		}catch(Exception e) {
			logger.debug("update tempvip error->"+e.getLocalizedMessage());
			vh.put("code","-1");
			vh.put("message", "更新微信会员错误："+e.getLocalizedMessage());
			return vh;
		}
		vh.put("code","0");
		vh.put("message", "更新微信会员成功");
		
		return vh;
	}

}
