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
		//��ȡ�û�
		User user = this.helper.getOperator(event);
		ValueHolder vh =new ValueHolder();
		if(user==null) {
			logger.debug("NewsManageCommand error->user logout");
			vh.put("code","-1");
			vh.put("message","�û������ڣ������µ�½->");
			return vh;
		}
		
		//��ȡ��ӿ���ص���Ϣ����
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		if(wu==null) {
			vh.put("code", "-1");
			vh.put("message", "��ȡ�����û����ں�������Ϣ");
			return vh;
		}
		WxPublicControl wc=WxPublicControl.getInstance(wu.getAppId());
		JSONObject jo = (JSONObject) event.getParameterValue("jsonObject");
		try {
			jo=new JSONObject(jo.optString("params"));
		} catch (JSONException e) {
			logger.debug("parent is->"+jo.optString("params")+"not typeof JSONObject");
			vh.put("code", "-1");
			vh.put("message", "�����쳣");
			return vh;
		}
		String news=jo.optString("handlemsg");
		if(nds.util.Validator.isNull(news)) {
			vh.put("code", "-1");
			vh.put("message", "ͼ�Ĳ���Ϊ��");
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
			vh.put("message", "handleType�����쳣,ֻ����uploadNews��editNews");
			return vh;
		}
		

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
