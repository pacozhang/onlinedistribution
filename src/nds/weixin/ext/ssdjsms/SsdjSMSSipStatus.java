package nds.weixin.ext.ssdjsms;

import java.util.Map;
import java.util.HashMap;

/**
 * Desc:
 * <p/>
 */
public enum SsdjSMSSipStatus
{
    BUSY("ϵͳ��æ", "-1"),//��������ʧ��
    SUCCESS("����ɹ�","1000"),
    USERERROR("�û������ڻ��������","1001"),
    USERSTOP("�û���ͣ��","1002"),
    LACKBALANCE("����","1003"),
    OFTENREQUEST("����Ƶ��","1004"),
    SUPERCONTENT("���ݳ���","1005"),
    PHONEERROR("�Ƿ��ֻ�����","1006"),
    KEYWORD("�ؼ��ֹ���","1007"),
    MOREPHONE("���պ�����������","1008"),
    USEROVERITME("�ʻ�����","1009"),
    PARAMSERROR("������ʽ����","1010"),
    OTHERERROR("��������", 	"1011"),
    DATABASEERROR("���ݿⷱæ","1012"),
    SENDTIMEERROR("�Ƿ�����ʱ��","1013");


   
    
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
