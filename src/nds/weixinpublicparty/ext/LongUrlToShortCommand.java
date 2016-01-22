package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class LongUrlToShortCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		//获取用户
		User user = this.helper.getOperator(event);
		ValueHolder vh =new ValueHolder();
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","用户不存在，请重新登陆->");
			return vh;
		}
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		/*try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		logger.debug("jo->"+jo.toString());*/
		
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("publish menu error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		//判断APPID与APPSECRET是否为空
		if(nds.util.Validator.isNull(wu.getAppId())||nds.util.Validator.isNull(wu.getAppSecret())) {
			logger.debug("publish menu error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		
		
		

		return null;
	}

}
