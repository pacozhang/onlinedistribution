package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.publicplatform.api.WeMeun;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.security.User;
import nds.util.NDSException;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class CreateMenuCommand extends Command {

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
		
		//获取与接口相关的信息对象
		WeUtils wu=WeUtilsManager.getByAdClientId(user.adClientId);
		
		
		ArrayList params=new ArrayList();
		params.add(wu.getAd_client_id());
		ArrayList para=new ArrayList();
		//para.add(java.sql.Types.VARCHAR);
		para.add( java.sql.Clob.class);
		String resultStr="";
		
		try {
			Collection list=QueryEngine.getInstance().executeFunction("wx_menu_create",params,para);
			resultStr=(String)list.iterator().next();
			logger.debug("get menu->"+resultStr);
		}catch (QueryException e1) {
			logger.debug("get menu erroe->"+e1.getMessage());
			e1.printStackTrace();
			vh.put("code", "-1");
			vh.put("message", "获取菜单数据异常->"+e1.getMessage());
			return vh;
		}
		WeMeun wmm=WeMeun.getInstance(wu.getAppId());
		JSONObject wm=wmm.createMenu(resultStr);
		
		//logger.debug("create menu result->"+wm.toString());
		if(wm!=null) {
			vh.put("code",0);
			vh.put("message",wm.optString("message"));
			vh.put("data", wm);
		}else {
			vh.put("code","-1");
			vh.put("message","创建菜单失败！");
		}
		return vh;
	}

}
