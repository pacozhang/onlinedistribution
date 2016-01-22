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
    BUSY( "ϵͳ��æ", "-1"),//��������ʧ��
    SUCCESS( "����ɹ�", 						"0"),//��������ɹ�
    TOKENERROR( "��ȡaccess_tokenʱAppSecret����,����access_token��Ч",     "40001"),//�ص��ӿ�ʧ�ܵ�Ĭ�ϴ���
    DOCUMENT_INVALID( "���Ϸ���ƾ֤����", 		"40002"),//���û�����д�û�����صĴ���
    OPENID_INVALID( "���Ϸ���OpenID", 					"40003"),//���û�����д�ֻ�����صĴ���
    MEDIA_INVALID( "���Ϸ���ý���ļ�����", 			"40004"),//���û�����д������صĴ���
    FILETYPE_INVALID( "���Ϸ����ļ�����", 				"40005"),//���û�����д���֤��صĴ���
    FILESIZE_INVALID( "���Ϸ����ļ���С", 					"40006"),//��ʵ�忨��������صĴ���
    MEDIAID_INVALID( "���Ϸ���ý���ļ�id", 				"40007"),//��ʵ�忨��֤����صĴ���
    MSGTYPE_INVALID( "���Ϸ�����Ϣ����", 						"40008"),//���û�����д������صĴ���
    PICSIZE_INVALID( "���Ϸ���ͼƬ�ļ���С", 			"40009"),//���û�����д�Ա���صĴ���
    VOICESIZE_INVALID( "���Ϸ��������ļ���С", 					"40010"),//ʵ�忨δ�ҵ����ѱ���
    VIDEOSIZE_ERROR( "���Ϸ�����Ƶ�ļ���С", 	"40011"),//�������δ����
    THUM_INVALID( "���Ϸ�������ͼ�ļ���С", 				"40012"),//�����ڲ�ִ�д���,��ϵͳ��æ
    APPID_INVAILD( "���Ϸ���APPID",				"40013"),//������Ч,���󷽷��Ĳ�������Ҫ��
    TOKEN_INVAILD( "���Ϸ���access_token",					"40014"),//sig У�����
    MENU_INVAILD( "���Ϸ��Ĳ˵�����",					"40015"),//û��Ȩ�޲���
    BUTTONS_ERROR( "���Ϸ��İ�ť����",					"40016"),//�߼��Դ���
    BUTTONSS_ERROR( "���Ϸ��İ�ť����",					"40017"),//�ظ�����
    BUTNAME_ERROR("���Ϸ��İ�ť���ֳ���","40018"),
    KEYLENGTH_ERROR("���Ϸ��İ�ťKEY����","40019"),
    URLENGTHS_ERROR("���Ϸ��İ�ťURL����","40020"),
    BUTTONTYPE_ERROR("���Ϸ��Ĳ˵��汾��","40021"),
    MENUSERIES_ERROR("���Ϸ����Ӳ˵�����","40022"),
    SUBMENU_ERROR("���Ϸ����Ӳ˵���ť����","40023"),
    SUBBUT_ERROR("���Ϸ����Ӳ˵���ť����","40024"),
    SUBNAME_ERROR("���Ϸ����Ӳ˵���ť���ֳ���","40025"),
    SUBKEY_ERROR("���Ϸ����Ӳ˵���ťKEY����","40026"),
    SUBURLENGTH_ERROR("���Ϸ����Ӳ˵���ťURL����","40027"),
    CUSMEUN_ERROR("���Ϸ����Զ���˵�ʹ���û�","40028"),
    OAUTH_ERROR("���Ϸ���oauth_code","40029"),
    REFTOKEN_ERROR("���Ϸ���refresh_token","40030"),
    OPENIDLIST_ERROR("���Ϸ���openid�б�","40031"),
    OPENIDLENGTH_ERROR("���Ϸ���openid�б���","40032"),
    UNCHARAT_ERROR("���Ϸ��������ַ�,���ܰ���xxxx��ʽ���ַ�","40033"),
    PARAMETER_ERROR("���Ϸ��Ĳ���","40035"),
    REQUEST_ERROR("���Ϸ��������ʽ","40038"),
    URLENGTH_INVALILD("���Ϸ���URL����","40039"),
    GROUPID_ERROR("���Ϸ��ķ���id","40050"),
    GROUPNAME_ERROR("�������ֲ��Ϸ�","40051"),
    ACTOKEN_MISS("ȱ��access_token����","41001"),
    APPID_MISS("ȱ��appid����","41002"),
    REFTOKEN_MISS("ȱ��refresh_token����","41003"),
    SECRET_MISS("ȱ��secret����","41004"),
    MEDIA_MISS("ȱ�ٶ�ý���ļ�����","41005"),
    MEDIAID_MISS("ȱ��media_id����","41006"),
    SUBMENU_MISS("ȱ���Ӳ˵�����","41007"),
    OAUTHCODE_MISS("ȱ��oauth code","41008"),
    OPENID_MISS("ȱ��openid","41009"),
    ACCTOKEN_TIMEOUT("access_token��ʱ","42001"),
    REFTOKEN_TIMEOUT("refresh_token��ʱ","42002"),
    OAUTHCODE_TIMEOUT("oauth_code��ʱ","42003"),
    GET_NEED("��ҪGET����","43001"),
    POST_NEED("��ҪPOST����","43002"),
    HTTPS_NEED("��ҪHTTPS����","43003"),
    RECIPIENT_NEED("��Ҫ�����߹�ע","43004"),
    FRIEND_NEED("��Ҫ���ѹ�ϵ","43005"),
    MEDIA_EMPTY("��ý���ļ�Ϊ��","44001"),
    POST_EMPTY("POST�����ݰ�Ϊ��","44002"),
    PIC_EMPTY("ͼ����Ϣ����Ϊ��","44003"),
    MSG_EMPTY("�ı���Ϣ����Ϊ��","44004"),
    MEDIA_OVER("��ý���ļ���С��������","45001"),
    MSG_OVER("��Ϣ���ݳ�������","45002"),
    TITLE_OVER("�����ֶγ�������","45003"),
    DESC_OVER("�����ֶγ�������","45004"),
    LINK_OVER("�����ֶγ�������","45005"),
    PICLINK_OVER("ͼƬ�����ֶγ�������","45006"),
    VOICE_OVER("��������ʱ�䳬������","45007"),
    PICMSG_OVER("ͼ����Ϣ��������","45008"),
    INFACE_LIMIT("�ӿڵ��ó�������","45009"),
    MENU_LIMIT("�����˵�������������","45010"),
    REBACK_LIMIT("�ظ�ʱ�䳬������","45015"),
    SYSGROUP_LIMIT("ϵͳ����,�������޸�","45016"),
    GROUPOVER_ERROR("�������ֹ���","45017"),
    GPLENTHOVER_ERROR("����������������","45018"),
    NOMEDIA_ERROR("������ý������","46001"),
    NOMENU_ERROR("�����ڵĲ˵��汾","46002"),
    NOMENUDATA_ERROR("�����ڵĲ˵�����","46003"),
    NOUSER_ERROR("�����ڵ��û�","46004"),
    PARSEJO_ERROR("����JSON/XML���ݴ���","47001"),
    UNAPI_ERROR("api����δ��Ȩ","48001"),
    USERAPI_ERROR("�û�δ��Ȩ��api","50001");

    /*
{"0000":"��������ʧ��","9999":"��������ɹ�","1001":"ǩ����Ч","1002":"�������","1003":"�û���ʧ��","1004":"��Ҫ���û�","1005":"/��Ҫ�ṩAppKey","1006":"��Ҫ�ṩ������","1007":"��Ҫ�ṩǩ��","1008":"��Ҫ�ṩʱ���","1009":"�û���֤ʧ��","1010":"��Ȩ���ʷ���","1011":"���񲻴���","1012":"��Ҫ�ṩSessionId","1013":"��Ҫ�ṩ�û���"}
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
