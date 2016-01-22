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
			vh.put("message","�û������ڣ������µ�½->");
			return vh;
		}
		
		JSONObject jo = (JSONObject)event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (Exception e) {
			logger.debug("get params error->"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		//��ȡ��ӿ���ص���Ϣ����
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			logger.debug("massreply error->not find WeUtils WeUtilsManager.getByAdClientId("+user.adClientId+")");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}
		//�ж�APPID��APPSECRET�Ƿ�Ϊ��
		if(nds.util.Validator.isNull(wu.getAppId())) {
			logger.debug("massreply error->appid or appsecret is null[appid:"+wu.getAppId()+"][appsecret:"+wu.getAppSecret()+"]");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}
		
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		if(wc==null) {
			logger.debug("massreply error->not find WeControl WeControl.getInstance("+wu.getAppId()+")");
			vh.put("code","-1");
			vh.put("message","�뵽�˵���΢�š��ġ�΢�Žӿ����á�������APPID��APPSECRET�������ˢ��APP����ť");
			return vh;
		}

		logger.debug("getAllMaterial");
		
		//�ز�����
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
				vh.put("message","��ȡ�ز��б��쳣��");
			}
		} catch (Exception e) {
			vh.put("code","-1");
			vh.put("message",wu.getAppId()+" getMaterial error2->"+e.toString());
			e.printStackTrace();
		}


		return vh;
	}
	

}
