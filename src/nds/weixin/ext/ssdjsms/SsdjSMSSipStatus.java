package nds.weixin.ext.ssdjsms;

import java.util.Map;
import java.util.HashMap;

/**
 * Desc:
 * <p/>
 */
public enum SsdjSMSSipStatus
{
    BUSY("系统繁忙", "-1"),//服务请求失败
    SUCCESS("请求成功","1000"),
    USERERROR("用户不存在或密码出错","1001"),
    USERSTOP("用户被停用","1002"),
    LACKBALANCE("余额不足","1003"),
    OFTENREQUEST("请求频繁","1004"),
    SUPERCONTENT("内容超长","1005"),
    PHONEERROR("非法手机号码","1006"),
    KEYWORD("关键字过滤","1007"),
    MOREPHONE("接收号码数量过多","1008"),
    USEROVERITME("帐户过期","1009"),
    PARAMSERROR("参数格式错误","1010"),
    OTHERERROR("其它错误", 	"1011"),
    DATABASEERROR("数据库繁忙","1012"),
    SENDTIMEERROR("非法发送时间","1013");


   
    
    private String v;
    private String c;

    private static Map<String,SsdjSMSSipStatus> status;

    SsdjSMSSipStatus(String value, String code){
        v = value;
        c = code;
    }

    @Override
    public String toString() {
        return v;
    }

    public String getCode() {
        return c;
    }
    
    public static boolean hasCode(String code) {
    	if(status==null) {init();}
    	boolean isHasCode=false;
    	if(nds.util.Validator.isNull(code)) {return isHasCode;}
    	isHasCode=status.containsKey(code);
    	if(!isHasCode) {System.out.println("WeixinSipStatus don't have key->"+code);}
    	
    	return isHasCode;
    }
    
    public static SsdjSMSSipStatus getStatus(String code) {
    	if(status==null) {init();}
    	if(!hasCode(code)) {return status.get("-1");}
        return status.get(code);
    }
    
    private static synchronized void init() {
    	if(status == null) {
        	//System.out.println("status is null");
            status = new HashMap<String,SsdjSMSSipStatus>();
            status.put("-1",BUSY);
            status.put("1000",SUCCESS);
            status.put("1001",USERERROR);
            status.put("1002",USERSTOP);
            status.put("1003",LACKBALANCE);
            status.put("1004",OFTENREQUEST);
            status.put("1005",SUPERCONTENT);
            status.put("1006",PHONEERROR);
            status.put("1007",KEYWORD );
            status.put("1008",MOREPHONE);
            status.put("1009",USEROVERITME);
            status.put("1010",PARAMSERROR);
            status.put("1011",OTHERERROR);
            status.put("1012",DATABASEERROR);
            status.put("1013",SENDTIMEERROR);
        }
    }
}
