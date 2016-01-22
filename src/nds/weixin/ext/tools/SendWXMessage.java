package nds.weixin.ext.tools;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.weixin.ext.WePublicparty;
import nds.weixin.ext.WePublicpartyManger;
import nds.weixin.ext.dispose.TextDispose;

public class SendWXMessage {
	private static Logger logger= LoggerManager.getInstance().getLogger(SendWXMessage.class.getName());
	
	
	public static void sendWXMessage(HttpServletRequest request,HttpServletResponse response,String message){
		WePublicparty wpp=WePublicpartyManger.getInstance().getWpc();//.getByAppid(pappid);
		
		if(nds.util.Validator.isNull(message)){
			logger.error("message is null");
			try {
				PrintWriter pw=response.getWriter();
				pw.print("success");
				pw.flush();
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		if(wpp==null) {
			logger.error("not find wepublicpartyby appid:");
			try {
				PrintWriter pw=response.getWriter();
				pw.print("success");
				pw.flush();
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		logger.debug("result->"+message);
		try {
			WXBizMsgCrypt pc = new WXBizMsgCrypt(wpp.getToken(), wpp.getNewencodingaeskey(), wpp.getAppid());
			message=pc.encryptMsg(message, request.getParameter("timestamp"), request.getParameter("nonce"));
			logger.debug("resultStr:"+message+",timestamp:"+request.getParameter("timestamp")+",nonce:"+request.getParameter("nonce"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.debug("2result->"+message);
		try{
			PrintWriter pw=response.getWriter();
			pw.print(message);
			pw.flush();
			pw.close();
		}catch(Exception e){
			logger.debug("text error->"+e.getMessage());
			e.printStackTrace();
		}
	}
}
