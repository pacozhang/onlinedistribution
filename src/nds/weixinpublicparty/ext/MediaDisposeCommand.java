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
import nds.query.QueryEngine;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class MediaDisposeCommand extends Command {

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,RemoteException {
		User user = this.helper.getOperator(event);
		int ad_client_id=user.adClientId;
		WeUtils wu=WeUtilsManager.getByAdClientId(ad_client_id);
		ValueHolder holder = new ValueHolder();
		if(wu==null) {
			holder.put("code", "-1");
			holder.put("message", "获取不到用户公众号配置信息");
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
			holder.put("message", "参数异常");
			return holder;
		}
		String fildpath=jo.optString("filepath");
		if(nds.util.Validator.isNull(fildpath)) {
			holder.put("code", "-1");
			holder.put("message", "文件路径为空");
			return holder;
		}
		//判断文件是否存在
		File file =null;
		try{
			file = new File(fildpath);
			if(!file.exists()){
				holder.put("code", "-1");
				holder.put("message", "文件不存在");
				return holder;
			}
		}catch(Throwable t){
			try {
				holder.put("code", -1);
				holder.put("message", "文件不存在"+t.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return holder;
		}
		
		String type=jo.optString("type");
		
		if(nds.util.Validator.isNull(wu.getAppId())) {
			holder.put("code", -1);
			holder.put("message", "请完善APPID与APPSECRET！");
		}else {
			WeMedia wm= WeMedia.getInstance(wu.getAppId());
			resultjo=wm.uploadFile(wc, jo);
			if(resultjo.optInt("code",-1)<0) {
				holder.put("code", -1);
				holder.put("message", resultjo.optString("message"));
			}else {
				int id=0;
				QueryEngine qe=QueryEngine.getInstance();
				id=qe.getSequence("WX_MEDIA");//get_Sequences('WX_MEDIA')
				String insertmidia="INSERT INTO WX_MEDIA(ID,AD_CLIENT_ID,AD_ORG_ID,NAME,MEDIA_ID,MTYPE,OWNERID,MODIFIERID,UPFILE) VALUES(?,?,?,?,?,?,?,?,?)";
				try {
					qe.executeUpdate(insertmidia, new Object[] {id,wu.getAd_client_id(),0,"未命名",resultjo.optString("media_id"),type,user.getId(),user.getId(),resultjo.optString("url")});
					holder.put("code", 0);
					holder.put("message", "执行成功");
					holder.put("media_id", resultjo.optString("media_id"));
					holder.put("url", resultjo.optString("url"));
					holder.put("id", id);
				}catch(Exception e) {
					holder.put("code", -1);
					holder.put("message", "新增媒体信息失败->"+e.getMessage());
					e.printStackTrace();
				}
			}
		}

		return holder;
	}

}
