package nds.weixinpublicparty.ext;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.Cookie;

import org.directwebremoting.WebContext;
import org.json.JSONObject;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.control.web.SessionContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.security.User;
import nds.util.NDSException;
import nds.util.WebKeys;

public class PseudoLoginCommand extends Command{
	private static Logger logger= LoggerManager.getInstance().getLogger(PseudoLoginCommand.class.getName());
	  
	private static final String GET_USER_BY_MAIL="select u.name, u.passwordhash, u.isactive, u.description, c.domain, u.ad_client_id, u.email from users u , ad_client c where u.email=? and c.id=u.ad_client_id";


	@Override
	public ValueHolder execute(DefaultWebEvent event) throws NDSException,
			RemoteException {
		ValueHolder vh=new ValueHolder();
		JSONObject jo= event.getJSONObject();
		try {
			WebContext ctx = (WebContext)jo.get("org.directwebremoting.WebContext");
			
			User user=getUser("nea@burgeon.com.cn");
	        //logger.debug("user"+user.email);
	        WebUtils.loginSSOUser(user,ctx.getHttpServletRequest() , ctx.getHttpServletResponse());
	        //com.liferay.portal.action.LoginAction.login(ctx.getHttpServletRequest(), ctx.getHttpServletResponse(),user.email,user.passwordHash, false);
	        Cookie cookie=new Cookie("name",user.name);
			cookie.setMaxAge(1000000);
			ctx.getHttpServletResponse().addCookie(cookie);
	        SessionContextManager scmanager= WebUtils.getSessionContextManager(ctx.getSession());
	        UserWebImpl usr=(UserWebImpl)scmanager.getActor(WebKeys.USER);
	        
	        logger.debug("user is->"+(usr==null?"is null":usr.getUserId()));
	        if(usr==null) {
	        	vh.put("code", "-1");
	        	vh.put("message", "login error");
	        }else {
	        	vh.put("code", "0");
	        	vh.put("message", "login success");
	        }
		}catch(Exception e) {
			vh.put("code", "-1");
        	vh.put("message", e.getLocalizedMessage());
        	e.printStackTrace();
		}
		return vh;
	}
	
	private User getUser(String email) throws NDSException {
        User usr=null;
        Connection con=null;
        PreparedStatement pstmt= null;
        ResultSet rs= null;
        try {
            con= QueryEngine.getInstance().getConnection();
            pstmt= con.prepareStatement(GET_USER_BY_MAIL);
            pstmt.setString(1,email);
            rs= pstmt.executeQuery();
            if( rs.next()){
                usr= new User();
                usr.name=(rs.getString(1));
                usr.passwordHash=(rs.getString(2));
                usr.isActive= "Y".equals( rs.getString(3));
                usr.description=(rs.getString(4));
                usr.clientDomain=(rs.getString(5));
                usr.adClientId = rs.getInt(6);
                usr.email= rs.getString(7);
                logger.debug("get users");
            }
        }
        catch(Exception e) {
            logger.debug("Errors found when trying to get operator from event.",e);
            throw new NDSException("@exception@", e);
            
        }finally{
            if( rs!=null){try{rs.close();}catch(Exception e){}}
            if( pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            if( con!=null){try{con.close();}catch(Exception e){}}
        }
        return usr;
		
	} 

}
