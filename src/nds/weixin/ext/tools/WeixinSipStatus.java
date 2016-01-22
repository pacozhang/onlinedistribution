package nds.weixin.ext.tools;

import java.util.Map;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Desc:
 * <p/>
 */
public enum WeixinSipStatus
{
    BUSY( "系统繁忙", "-1"),//服务请求失败
    SUCCESS( "请求成功", 						"0"),//服务请求成功
    TOKENERROR( "获取access_token时AppSecret错误,或者access_token无效",     "40001"),//回调接口失败的默认错误
    DOCUMENT_INVALID( "不合法的凭证类型", 		"40002"),//与用户所填写用户名相关的错误
    OPENID_INVALID( "不合法的OpenID", 					"40003"),//与用户所填写手机号相关的错误
    MEDIA_INVALID( "不合法的媒体文件类型", 			"40004"),//与用户所填写邮箱相关的错误
    FILETYPE_INVALID( "不合法的文件类型", 				"40005"),//与用户所填写身份证相关的错误
    FILESIZE_INVALID( "不合法的文件大小", 					"40006"),//绑卡实体卡号密码相关的错误
    MEDIAID_INVALID( "不合法的媒体文件id", 				"40007"),//绑卡实体卡验证码相关的错误
    MSGTYPE_INVALID( "不合法的消息类型", 						"40008"),//与用户所填写生日相关的错误
    PICSIZE_INVALID( "不合法的图片文件大小", 			"40009"),//与用户所填写性别相关的错误
    VOICESIZE_INVALID( "不合法的语音文件大小", 					"40010"),//实体卡未找到或已被绑定
    VIDEOSIZE_ERROR( "不合法的视频文件大小", 	"40011"),//必需参数未传递
    THUM_INVALID( "不合法的缩略图文件大小", 				"40012"),//方法内部执行错误,如系统繁忙
    APPID_INVAILD( "不合法的APPID",				"40013"),//参数无效,请求方法的参数不合要求
    TOKEN_INVAILD( "不合法的access_token",					"40014"),//sig 校验出错
    MENU_INVAILD( "不合法的菜单类型",					"40015"),//没有权限操作
    BUTTONS_ERROR( "不合法的按钮个数",					"40016"),//逻辑性错误
    BUTTONSS_ERROR( "不合法的按钮个数",					"40017"),//重复操作
    BUTNAME_ERROR("不合法的按钮名字长度","40018"),
    KEYLENGTH_ERROR("不合法的按钮KEY长度","40019"),
    URLENGTHS_ERROR("不合法的按钮URL长度","40020"),
    BUTTONTYPE_ERROR("不合法的菜单版本号","40021"),
    MENUSERIES_ERROR("不合法的子菜单级数","40022"),
    SUBMENU_ERROR("不合法的子菜单按钮个数","40023"),
    SUBBUT_ERROR("不合法的子菜单按钮类型","40024"),
    SUBNAME_ERROR("不合法的子菜单按钮名字长度","40025"),
    SUBKEY_ERROR("不合法的子菜单按钮KEY长度","40026"),
    SUBURLENGTH_ERROR("不合法的子菜单按钮URL长度","40027"),
    CUSMEUN_ERROR("不合法的自定义菜单使用用户","40028"),
    OAUTH_ERROR("不合法的oauth_code","40029"),
    REFTOKEN_ERROR("不合法的refresh_token","40030"),
    OPENIDLIST_ERROR("不合法的openid列表","40031"),
    OPENIDLENGTH_ERROR("不合法的openid列表长度","40032"),
    UNCHARAT_ERROR("不合法的请求字符,不能包含xxxx格式的字符","40033"),
    PARAMETER_ERROR("不合法的参数","40035"),
    REQUEST_ERROR("不合法的请求格式","40038"),
    URLENGTH_INVALILD("不合法的URL长度","40039"),
    GROUPID_ERROR("不合法的分组id","40050"),
    GROUPNAME_ERROR("分组名字不合法","40051"),
    ACTOKEN_MISS("缺少access_token参数","41001"),
    APPID_MISS("缺少appid参数","41002"),
    REFTOKEN_MISS("缺少refresh_token参数","41003"),
    SECRET_MISS("缺少secret参数","41004"),
    MEDIA_MISS("缺少多媒体文件数据","41005"),
    MEDIAID_MISS("缺少media_id参数","41006"),
    SUBMENU_MISS("缺少子菜单数据","41007"),
    OAUTHCODE_MISS("缺少oauth code","41008"),
    OPENID_MISS("缺少openid","41009"),
    ACCTOKEN_TIMEOUT("access_token超时","42001"),
    REFTOKEN_TIMEOUT("refresh_token超时","42002"),
    OAUTHCODE_TIMEOUT("oauth_code超时","42003"),
    GET_NEED("需要GET请求","43001"),
    POST_NEED("需要POST请求","43002"),
    HTTPS_NEED("需要HTTPS请求","43003"),
    RECIPIENT_NEED("需要接收者关注","43004"),
    FRIEND_NEED("需要好友关系","43005"),
    MEDIA_EMPTY("多媒体文件为空","44001"),
    POST_EMPTY("POST的数据包为空","44002"),
    PIC_EMPTY("图文消息内容为空","44003"),
    MSG_EMPTY("文本消息内容为空","44004"),
    MEDIA_OVER("多媒体文件大小超过限制","45001"),
    MSG_OVER("消息内容超过限制","45002"),
    TITLE_OVER("标题字段超过限制","45003"),
    DESC_OVER("描述字段超过限制","45004"),
    LINK_OVER("链接字段超过限制","45005"),
    PICLINK_OVER("图片链接字段超过限制","45006"),
    VOICE_OVER("语音播放时间超过限制","45007"),
    PICMSG_OVER("图文消息超过限制","45008"),
    INFACE_LIMIT("接口调用超过限制","45009"),
    MENU_LIMIT("创建菜单个数超过限制","45010"),
    REBACK_LIMIT("回复时间超过限制","45015"),
    SYSGROUP_LIMIT("系统分组,不允许修改","45016"),
    GROUPOVER_ERROR("分组名字过长","45017"),
    GPLENTHOVER_ERROR("分组数量超过上限","45018"),
    NOMEDIA_ERROR("不存在媒体数据","46001"),
    NOMENU_ERROR("不存在的菜单版本","46002"),
    NOMENUDATA_ERROR("不存在的菜单数据","46003"),
    NOUSER_ERROR("不存在的用户","46004"),
    PARSEJO_ERROR("解析JSON/XML内容错误","47001"),
    UNAPI_ERROR("api功能未授权","48001"),
    USERAPI_ERROR("用户未授权该api","50001");

    /*
{"0000":"服务请求失败","9999":"服务请求成功","1001":"签名无效","1002":"请求过期","1003":"用户绑定失败","1004":"需要绑定用户","1005":"/需要提供AppKey","1006":"需要提供服务名","1007":"需要提供签名","1008":"需要提供时间戳","1009":"用户认证失败","1010":"无权访问服务","1011":"服务不存在","1012":"需要提供SessionId","1013":"需要提供用户名"}
     */
    
    private String v;
    private String c;

    private static Map<String,WeixinSipStatus> status;

    WeixinSipStatus(String value, String code){
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
    
    public static WeixinSipStatus getStatus(String code) {
    	if(status==null) {init();}
    	if(!hasCode(code)) {return status.get("-1");}
        return status.get(code);
    }
    
    private static synchronized void init() {
    	if(status == null) {
        	//System.out.println("status is null");
            status = new HashMap<String,WeixinSipStatus>();
            status.put("-1",BUSY);
            status.put("0",SUCCESS);
            status.put("40001",TOKENERROR);
            status.put("40002",DOCUMENT_INVALID);
            status.put("40003",OPENID_INVALID);
            status.put("40004",MEDIA_INVALID);
            status.put("40005",FILETYPE_INVALID);
            status.put("40006",FILESIZE_INVALID);
            status.put("40007",MEDIAID_INVALID );
            status.put("40008",MSGTYPE_INVALID);
            status.put("40009",PICSIZE_INVALID);
            status.put("40010",VOICESIZE_INVALID);
            status.put("40011",VIDEOSIZE_ERROR);
            status.put("40012",THUM_INVALID);
            status.put("40013",APPID_INVAILD);
            status.put("40014",TOKEN_INVAILD);
            status.put("40015",MENU_INVAILD);
            status.put("40016",BUTTONS_ERROR);
            status.put("40017",BUTTONSS_ERROR);
            status.put("40018",BUTNAME_ERROR);
            status.put("40019",KEYLENGTH_ERROR);
            status.put("40020",URLENGTHS_ERROR);
            status.put("40021",BUTTONTYPE_ERROR);
            status.put("40022",MENUSERIES_ERROR);
            status.put("40023",SUBMENU_ERROR);
            status.put("40024",SUBBUT_ERROR);
            status.put("40025",SUBNAME_ERROR);
            status.put("40026",SUBKEY_ERROR);
            status.put("40027",SUBURLENGTH_ERROR);
            status.put("40028",CUSMEUN_ERROR);
            status.put("40029",OAUTH_ERROR);
            status.put("40030",REFTOKEN_ERROR);
            status.put("40031",OPENIDLIST_ERROR);
            status.put("40032",OPENIDLENGTH_ERROR);
            status.put("40033",UNCHARAT_ERROR);
            status.put("40035",PARAMETER_ERROR);
            status.put("40038",REQUEST_ERROR);
            status.put("40039",URLENGTH_INVALILD);
            status.put("40050",GROUPID_ERROR);
            status.put("40051",GROUPNAME_ERROR);
            status.put("41001",ACTOKEN_MISS);
            status.put("41002",APPID_MISS);
            status.put("41003",REFTOKEN_MISS);
            status.put("41004",SECRET_MISS);
            status.put("41005",MEDIA_MISS);
            status.put("41006",MEDIAID_MISS);
            status.put("41007",SUBMENU_MISS);
            status.put("41008",OAUTHCODE_MISS);
            status.put("41009",OPENID_MISS);
            status.put("42001",ACCTOKEN_TIMEOUT);
            status.put("42002",REFTOKEN_TIMEOUT);
            status.put("42003",OAUTHCODE_TIMEOUT);
            status.put("43001",GET_NEED);
            status.put("43002",POST_NEED);
            status.put("43003",HTTPS_NEED);
            status.put("43004",RECIPIENT_NEED);
            status.put("43005",FRIEND_NEED);
            status.put("44001",MEDIA_EMPTY);
            status.put("44002",POST_EMPTY);
            status.put("44003",PIC_EMPTY);
            status.put("44004",MSG_EMPTY);
            status.put("45001",MEDIA_OVER);
            status.put("45002",MSG_OVER);
            status.put("45003",TITLE_OVER);
            status.put("45004",DESC_OVER);
            status.put("45005",LINK_OVER);
            status.put("45006",PICLINK_OVER);
            status.put("45007",VOICE_OVER);
            status.put("45008",PICMSG_OVER);
            status.put("45009",INFACE_LIMIT);
            status.put("45010",MENU_LIMIT);
            status.put("45015",REBACK_LIMIT);
            status.put("45016",SYSGROUP_LIMIT);
            status.put("45017",GROUPOVER_ERROR);
            status.put("45018",GPLENTHOVER_ERROR);
            status.put("46001",NOMEDIA_ERROR);
            status.put("46002",NOMENU_ERROR);
            status.put("46003",NOMENUDATA_ERROR);
            status.put("46004",NOUSER_ERROR);
            status.put("47001",PARSEJO_ERROR);
            status.put("48001",UNAPI_ERROR);
            status.put("50001",USERAPI_ERROR);
        }
    }
}
