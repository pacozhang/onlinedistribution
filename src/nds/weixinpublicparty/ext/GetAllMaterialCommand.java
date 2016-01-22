package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeBatchGetMaterial;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class GetAllMaterialCommand extends Command{
	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh =new ValueHolder();
		User user = this.helper.getOperator(event);
		
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","用户不存在，请重新登陆->");
			return vh;
		}
		
		JSONObject jo = (JSONObject)event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			logger.debug("get params error->"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("massreply error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		//判断APPID与APPSECRET是否为空
		if(nds.util.Validator.isNull(wu.getAppId())) {
			logger.debug("massreply error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}
		
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			logger.debug("massreply error->not find WeControl WeControl.getInstance("+wu.getAppId()+")");
			vh.put("code","-1");
			vh.put("message","请到菜单【微信】的【微信接口配置】中设置APPID与APPSECRET并点击【刷新APP】按钮");
			return vh;
		}

		logger.debug("getAllMaterial");
		
		//素材类型
		String type = jo.optString("type");//"news"
		
		WeBatchGetMaterial bgm=WeBatchGetMaterial.getInstance(wu.getAppId());
		JSONObject tempjo=null;
		try {		
			 tempjo=bgm.getAllMaterials(wc,user, type);
			if(tempjo!=null) {
				vh.put("code",tempjo.optInt("code",-1));
				vh.put("message",tempjo.optString("message"));
			}else {
				vh.put("code","-1");
				vh.put("message","获取素材列表异常！");
			}
		} catch (Exception e) {
			vh.put("code","-1");
			vh.put("message",wu.getAppId()+" getMaterial error2->"+e.toString());
			e.printStackTrace();
		}


		return vh;
	}
	

}
