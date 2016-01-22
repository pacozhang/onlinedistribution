package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.CheckSendMessage;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

import org.json.JSONObject;

public class CheckSendMessageCommand extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		User user = this.helper.getOperator(event);
		JSONObject jo = (JSONObject)event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
			logger.debug("jo.toString()"+jo.toString());
		} catch (Exception e) {
			logger.debug("get params error->"+e.getLocalizedMessage());
			e.printStackTrace();
		}		
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","用户不存在，请重新登陆->");
			return vh;
		}
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("massreply error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		
		CheckSendMessage bgm=CheckSendMessage.getInstance(wu.getAppId());//调用与微信服务器交互类的方法
		JSONObject tempjo=null;
		try {		
			 tempjo=bgm.checkmessage(wc, jo);//与微信服务器进行数据交互的类的方法中的参数
			if(tempjo!=null) {
				vh.put("code",tempjo.optInt("code",-1));
				vh.put("message",tempjo.optString("message"));
			}else {
				vh.put("code","-1");
				vh.put("message","查询消息发送状态失败！");
			}
		} catch (Exception e) {
			vh.put("code","-1");
			vh.put("message",wu.getAppId()+" getMaterial error2->"+e.toString());
			e.printStackTrace();
		}
		
		return vh;
	}

}
