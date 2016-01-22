package nds.weixin.ext.tools;

import java.util.Map;
import java.util.HashMap;

/**
 * Desc:
 * <p/>
 */
public enum SipStatus
{

    ERROR( "error", 			"0000"),//��������ʧ��
    SUCCESS( "success", 						"0"),//��������ɹ�
    INERFACEERROR( "inerfaceerror", 		"1000"),//�ص��ӿ�ʧ�ܵ�Ĭ�ϴ���
    USER_INVALID( "userinvalid", 		"1001"),//���û�����д�û�����صĴ���
    PHONE_INVALID( "phoneinvalid", 					"1002"),//���û�����д�ֻ�����صĴ���
    EMAIL_INVALID( "emailinvalid", 			"1003"),//���û�����д������صĴ���
    USERID_INVALID( "useridinvalid", 				"1004"),//���û�����д���֤��صĴ���
    PWD_INVALID( "pwdinvalid", 					"1005"),//��ʵ�忨��������صĴ���
    VCODE_INVALID( "vcodeinvalid", 				"1006"),//��ʵ�忨��֤����صĴ���
    BRTH_INVALID( "brthinvalid", 						"1007"),//���û�����д������صĴ���
    SEX_INVALID( "sexinvalid", 			"1008"),//���û�����д�Ա���صĴ���
    CARD_FAILD( "cardfaild", 					"1009"),//ʵ�忨δ�ҵ����ѱ���
  
    PARAM_ERROR( "paramerror", 	"1"),//�������δ����
    SERVICE_NOTEXIST( "servicenot", 				"2"),//�����ڲ�ִ�д���,��ϵͳ��æ
    PARAM_NONEED( "paramnoneed",				"3"),//������Ч,���󷽷��Ĳ�������Ҫ��
    SIGN_ERROR( "signerror",					"4"),//sig У�����
    AUTH_FAILD( "authfaild",					"5"),//û��Ȩ�޲���
    PROCESS_ERROR( "processerror",					"6"),//�߼��Դ���
    REPAIT_ERROR( "repaiterror",					"7");//�ظ�����

    /*
{"0000":"��������ʧ��","9999":"��������ɹ�","1001":"ǩ����Ч","1002":"�������","1003":"�û���ʧ��","1004":"��Ҫ���û�","1005":"/��Ҫ�ṩAppKey","1006":"��Ҫ�ṩ������","1007":"��Ҫ�ṩǩ��","1008":"��Ҫ�ṩʱ���","1009":"�û���֤ʧ��","1010":"��Ȩ���ʷ���","1011":"���񲻴���","1012":"��Ҫ�ṩSessionId","1013":"��Ҫ�ṩ�û���"}
     */
    
    private String v;
    private String c;

    private static Map<String,SipStatus> status ;

    SipStatus(String value, String code)
    {
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

    public static SipStatus getStatus(String code) {
        if(status == null) {
            status = new HashMap<String,SipStatus>();
            status.put("0000",ERROR);
            status.put("0",SUCCESS);
            status.put("1000",INERFACEERROR);
            status.put("1001",USER_INVALID);
            status.put("1002",PHONE_INVALID);
            status.put("1003",EMAIL_INVALID);
            status.put("1004",USERID_INVALID);
            status.put("1005",PWD_INVALID);
            status.put("1006",VCODE_INVALID);
            status.put("1007",BRTH_INVALID);
            status.put("1008",SEX_INVALID);
            status.put("1009",CARD_FAILD);
            status.put("1",PARAM_ERROR);
            status.put("2",SERVICE_NOTEXIST);
            status.put("3",PARAM_NONEED);
            status.put("4",SIGN_ERROR);
            status.put("5",AUTH_FAILD);
            status.put("6",PROCESS_ERROR);
            status.put("7",REPAIT_ERROR);
        }

        return status.get(code);
    }
}
