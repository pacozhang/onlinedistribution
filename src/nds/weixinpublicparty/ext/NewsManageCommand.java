package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeNews;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class NewsManageCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		//获取用户
		User user = this.helper.getOperator(event);
		ValueHolder vh =new ValueHolder();
		if(user==null) {
			logger.debug("NewsManageCommand error->user logout");
			vh.put("code","-1");
			vh.put("message","用户不存在，请重新登陆->");
			return vh;
		}
		
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			vh.put("code", "-1");
			vh.put("message", "获取不到用户公众号配置信息");
			return vh;
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e) {
			logger.debug("parent is->"+jo.optString("params")+"not typeof JSONObject");
			vh.put("code", "-1");
			vh.put("message", "参数异常");
			return vh;
		}
		String news=jo.optString("handlemsg");
		if(nds.util.Validator.isNull(news)) {
			vh.put("code", "-1");
			vh.put("message", "图文参数为空");
			return vh;
		}

		WeNews wns=WeNews.getInstance(wu.getAppId());
		JSONObject wnjo;
		if("uploadNews".equals(jo.optString("handleType"))){
			wnjo=wns.uploadNews(wc, news);
		}else if("editNews".equals(jo.optString("handleType"))){
			wnjo=wns.editNews(wc, news);
		}else if("removemedia".equals(jo.optString("handleType"))){
			wnjo=wns.removeMedia(wc, news);
		}else{
			vh.put("code", "-1");
			vh.put("message", "handleType参数异常,只能是uploadNews或editNews");
			return vh;
		}
		

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
