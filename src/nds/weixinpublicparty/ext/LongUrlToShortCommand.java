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
		//��ȡ�û�
		User user = this.helper.getOperator(event);
		ValueHolder vh =new ValueHolder();
		if(user==null) {
			logger.debug("publish menu error->user logout");
			vh.put("code","-1");
			vh.put("message","�û������ڣ������µ�½->");
			return vh;
		}
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		/*try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		logger.debug("jo->"+jo.toString());*/
		
		//��ȡ��ӿ���ص���Ϣ����
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("publish menu error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}
		//�ж�APPID��APPSECRET�Ƿ�Ϊ��
		if(nds.util.Validator.isNull(wu.getAppId())||nds.util.Validator.isNull(wu.getAppSecret())) {
			logger.debug("publish menu error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}
		
		
		

		return null;
	}

}
