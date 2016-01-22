package nds.weixinpublicparty.ext;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.control.web.binhandler.BinaryHandler;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicpart.api.GetAuthorizerAccessToken;
import nds.publicplatform.api.GetPublicInfo;
import nds.query.QueryEngine;
import nds.util.Tools;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.WeUtils;
import nds.weixin.ext.WeUtilsManager;

public class RestPublicAuthorizedCallback  implements BinaryHandler{
	private Logger logger= LoggerManager.getInstance().getLogger(RestPublicAuthorizedCallback.class.getName());
	private static long NETWORK_DELAY_SECONDS=1000*60*10;// 10 mininutes 
	
	@Override
	public void init(ServletContext context) {
		NETWORK_DELAY_SECONDS=(Tools.getInt(WebUtils.getProperty("rest.timewindow","10"), 10)) * 1000*60 ;
	}

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String[] tmp=null;
    	String params=null;
    	
		logger.debug("from online get param->"+request.getQueryString());
   	  	logger.debug("from online post param->");
		for(Iterator iter = request.getParameterMap().entrySet().iterator();iter.hasNext();){  
			Map.Entry element = (Map.Entry)iter.next();  
			tmp = (String[]) element.getValue(); 
			params=String.valueOf(element.getKey()+"::");
			if(tmp!=null&&tmp.length>0) {
				for (int i = 0; i < tmp.length; i++) {  
	                params+=tmp[i];
	            } 
			}
            logger.debug(params);
		}
		
		JSONObject jo=new JSONObject();
		//String wxappid=WebUtils.getProperty("currentpublicparty","wx73b758959e1ef0f2");
		WePublicparty wpp=WePublicpartyManger.getInstance().getWpc();
		String wxappid=wpp.getAppid();
		
		GetAuthorizerAccessToken gaat=GetAuthorizerAccessToken.getInstance(wxappid);
		JSONObject ppjo=gaat.getAuthAccessToken(request.getParameter("auth_code"));
		if(ppjo==null||!"0".equals(ppjo.optString("code","-1"))||!ppjo.has("data")) {
			response.sendRedirect("/html/nds/portal/index.jsp");
			return;
		}
		ppjo=ppjo.optJSONObject("data");
		JSONObject ppjoinfo=ppjo.optJSONObject("authorization_info");
		String pappid=ppjoinfo.optString("authorizer_appid");
		ppjoinfo.put("authorization_appid",wxappid);
		
		WeUtils wu=null;
		UserWebImpl userWeb =null;

		
		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));
		
		if(userWeb==null) {
			response.sendRedirect("/html/nds/portal/index.jsp");
			return;
		}else if(userWeb.getUserId()==1) {
			response.sendRedirect("/");
			return;
		}
		
		//判断是否有授权过
		wu=WeUtilsManager.getInstance().getByAppid(pappid);
		if(wu!=null&&wu.getAd_client_id()!=userWeb.getAdClientId()) {//
			String html="<html><body><script>alert('此公众号已在此平台授权过，不能重复授权');</script></body></html>";
			response.setContentType("text/html; charset=utf-8");
			response.setCharacterEncoding("utf-8");
			PrintWriter pw=response.getWriter();
			pw.write(html);
			pw.flush();
			pw.close();
			return;
		}
		
		logger.debug("found userWeb"+userWeb.getAdClientId()+","+userWeb.getUserId());
		wu=WeUtilsManager.getInstance().addWeUtils(pappid, wxappid, userWeb.getAdClientId());
		logger.debug("domain->"+wu.getDoMain());
		
		GetPublicInfo gpi=GetPublicInfo.getInstance(pappid);
		JSONObject pjo=gpi.getPublicInfo();
		if(pjo==null||!"0".equals(pjo.optString("code","-1;"))) {
			response.sendRedirect("/html/nds/portal/index.jsp");
			return;
		}
		pjo=pjo.optJSONObject("data");
		
		jo.put("authorization_info", ppjo);
		jo.put("authorizer_info", pjo);
		wu.updatePublicinfo(jo);
		
		response.sendRedirect("/html/nds/portal/index.jsp");
	}

}
