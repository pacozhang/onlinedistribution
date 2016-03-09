package nds.weixin.ext.dispose;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.publicweixin.ext.common.WxPublicControl;

public class LocationDispose implements IMessageDispose{
	private static Logger logger= LoggerManager.getInstance().getLogger(LocationDispose.class.getName());
	
	public void dispose(HttpServletRequest request,HttpServletResponse response, WxPublicControl wpc,JSONObject jo) {
		try{
			PrintWriter pw=response.getWriter();
			pw.print("s");
			pw.flush();
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
