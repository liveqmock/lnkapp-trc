package org.fbi.trc.tqc.processor;

import java.math.BigDecimal;

/**
 * Created by zhanrui on 2014/10/21.
 * 主机账务处理结果
 */
public class HostBookInfo {
    private String txnDate;
    private String txnSn;
    private String acctNo;
    private BigDecimal txnAmt;
    private String  bookStatus;  //0代表失败，1代表成功

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getTxnSn() {
        return txnSn;
    }

    public void setTxnSn(String txnSn) {
        this.txnSn = txnSn;
    }

    public String getAcctNo() {
        return acctNo;
    }

    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo;
    }

    public BigDecimal getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(BigDecimal txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }
}
