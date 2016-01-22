package nds.weixinpublicparty.ext;

import java.io.File;
import java.rmi.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeMedia;
import nds.publicweixin.ext.common.WxPublicControl;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class UploadImgCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		User user = this.helper.getOperator(event);
		int ad_client_id=user.adClientId;
		WeUtils wu=WeUtilsManager.getByAdClientId(ad_client_id);
		ValueHolder holder = new ValueHolder();
		if(wu==null) {
			holder.put("code", "-1");
			holder.put("message", "��ȡ�����û����ں�������Ϣ");
			return holder;
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		
		JSONObject resultjo=null;
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e) {
			logger.debug("parent is->"+jo.optString("params")+"not typeof JSONObject");
			holder.put("code", "-1");
			holder.put("message", "�����쳣");
			return holder;
		}
		String fildpath=jo.optString("filepath");
		logger.debug("uploadimg to weixin filepath------->"+fildpath);
		if(nds.util.Validator.isNull(fildpath)) {
			holder.put("code", "-1");
			holder.put("message", "�ļ�·��Ϊ��");
			return holder;
		}
		//�ж��ļ��Ƿ����
		File file =null;
		try{
			file = new File(fildpath);
			if(!file.exists()){
				holder.put("code", "-1");
				holder.put("message", "�ļ�������");
				return holder;
			}
		}catch(Throwable t){
			try {
				holder.put("code", -1);
				holder.put("message", "�ļ�������"+t.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return holder;
		}
		
		
		if(nds.util.Validator.isNull(wu.getAppId())) {
			holder.put("code", -1);
			holder.put("message", "������APPID��APPSECRET��");
		}else {
			WeMedia wm= WeMedia.getInstance(wu.getAppId());
			resultjo=wm.uploadImage(wc, fildpath);
			if(resultjo.optInt("code",-1)<0) {
				holder.put("code", -1);
				holder.put("message", resultjo.optString("message"));
			}else {
				holder.put("code", 0);
				holder.put("message", resultjo.optString("message"));
				holder.put("url", resultjo.optString("url"));
			}
		}

		return holder;
	}

}
