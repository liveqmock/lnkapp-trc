package org.fbi.trc.tqc.domain.cbs.T1030Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;

@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia1030 {
    @DataField(seq = 1)
    private String vchSn;
    @DataField(seq = 2)
    private String acctNo;
    @DataField(seq = 3)
    private String mchtCode;
    @DataField(seq = 4)
    private String prjCode;
    @DataField(seq = 5)
    private int txnCnt;
    @DataField(seq = 6)
    private BigDecimal txnAmt;
    @DataField(seq = 7)
    private String fileName;

    public String getAcctNo() {
        return acctNo;
    }

    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo;
    }

    public String getMchtCode() {
        return mchtCode;
    }

    public void setMchtCode(String mchtCode) {
        this.mchtCode = mchtCode;
    }

    public String getPrjCode() {
        return prjCode;
    }

    public void setPrjCode(String prjCode) {
        this.prjCode = prjCode;
    }

    public BigDecimal getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(BigDecimal txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getVchSn() {
        return vchSn;
    }

    public void setVchSn(String vchSn) {
        this.vchSn = vchSn;
    }

    public int getTxnCnt() {
        return txnCnt;
    }

    public void setTxnCnt(int txnCnt) {
        this.txnCnt = txnCnt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "CbsTia1030{" +
                "vchSn='" + vchSn + '\'' +
                ", acctNo='" + acctNo + '\'' +
                ", mchtCode='" + mchtCode + '\'' +
                ", prjCode='" + prjCode + '\'' +
                ", txnCnt=" + txnCnt +
                ", txnAmt=" + txnAmt +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}