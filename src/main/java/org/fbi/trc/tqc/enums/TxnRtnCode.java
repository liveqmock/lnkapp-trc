package org.fbi.trc.tqc.enums;

import java.util.Hashtable;

/**
 * 业务交易返回码
 */
public enum TxnRtnCode implements EnumApp {
    TXN_SUCCESS("0000", "处理完成,检查通过."),
    QUOTA_CHK_ERR_SINGLE_AMT("1001", "金额超单笔限额"),
    QUOTA_CHK_ERR_DAY_AMT("1002", "金额超日限额"),
    QUOTA_CHK_ERR_MONTH_AMT("1003", "金额超月限额"),
    QUOTA_CHK_ERR_PUB_RULE("1010", "未定义公用限额规则"),
    FTP_FILE_NOTEXIST("1011", "文件不存在"),
    FTP_FILE_ERR("1012", "文件处理失败"),
    FTP_FILE_REPEAT("1013", "交易重复,此凭证结果已处理."),
    TXN_ERR("9000", "交易处理失败");

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
