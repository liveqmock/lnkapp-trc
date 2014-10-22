package org.fbi.trc.tqc.enums;

import java.util.Hashtable;

/**
 * ҵ���׷�����
 */
public enum TxnRtnCode implements EnumApp {
    TXN_SUCCESS("0000", "�������,���ͨ��."),
    QUOTA_CHK_ERR_SINGLE_AMT("1001", "�������޶�"),
    QUOTA_CHK_ERR_DAY_AMT("1002", "�����޶�"),
    QUOTA_CHK_ERR_MONTH_AMT("1003", "�����޶�"),
    QUOTA_CHK_ERR_PUB_RULE("1010", "δ���幫���޶����"),
    FTP_FILE_NOTEXIST("1011", "�ļ�������"),
    FTP_FILE_ERR("1012", "�ļ�����ʧ��"),
    FTP_FILE_REPEAT("1013", "�����ظ�,��ƾ֤����Ѵ���."),
    TXN_ERR("9000", "���״���ʧ��");

    private String code = null;
    private String title = null;
    private static Hashtable<String, TxnRtnCode> aliasEnums;

    TxnRtnCode(String code, String title) {
        this.init(code, title);
    }

    @SuppressWarnings("unchecked")
    private void init(String code, String title) {
        this.code = code;
        this.title = title;
        synchronized (this.getClass()) {
            if (aliasEnums == null) {
                aliasEnums = new Hashtable();
            }
        }
        aliasEnums.put(code, this);
        aliasEnums.put(title, this);
    }

    public static TxnRtnCode valueOfAlias(String alias) {
        return aliasEnums.get(alias);
    }

    public String getCode() {
        return this.code;
    }

    public String getTitle() {
        return this.title;
    }

    public String toRtnMsg() {
        return this.code + "|" + this.title;
    }
}
