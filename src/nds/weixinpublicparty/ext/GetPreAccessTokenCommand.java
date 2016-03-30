package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;

import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.WebUtils;
import nds.publicweixin.ext.common.WxPublicpartyControl;
import nds.util.NDSException;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;

public class GetPreAccessTokenCommand  extends Command{

	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();
		
		//String wxappid=WebUtils.getProperty("currentpublicparty","wx73b758959e1ef0f2");
		WePublicparty wpp=WePublicpartyManger.getInstance().getWpc();
		String wxappid=wpp.getAppid();
		
		
		WxPublicpartyControl wppc=WxPublicpartyControl.getInstance(wxappid);
		JSONObject patjo=wppc.getPreAccessToken();
		
		//判断PREACCESSTOKEN是否获取成功
		if(patjo==null||!"0".equals(patjo.optString("code"))) {
			vh.put("code", "-1");
			vh.put("message", "获取失败");
			return vh;
		}
		
		String pac=patjo.optJSONObject("data").optString("pre_auth_code");
		if(nds.util.Validator.isNull(pac)) {
			vh.put("code", "-1");
			vh.put("message", "获取失败");
		}else {
			vh.put("code", "0");
			vh.put("message", "获取成功");
			vh.put("data", pac);
		}
		
		return vh;
	}

}
