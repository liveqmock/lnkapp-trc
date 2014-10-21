package org.fbi.trc.tqc.domain.cbs.T1020Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;

@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia1020 {
    @DataField(seq = 1)
    private String vchSn;
    @DataField(seq = 2)
    private String acctNo;
    @DataField(seq = 3)
    private String mchtCode;
    @DataField(seq = 4)
    private String prjCode;
    @DataField(seq = 5)
    private BigDecimal txnAmt;

    public String getVchSn() {
        return vchSn;
    }

    public void setVchSn(String vchSn) {
        this.vchSn = vchSn;
    }

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

    @Override
    public String toString() {
        return "CbsTia1020{" +
                "vchSn='" + vchSn + '\'' +
                ", acctNo='" + acctNo + '\'' +
                ", mchtCode='" + mchtCode + '\'' +
                ", prjCode='" + prjCode + '\'' +
                ", txnAmt=" + txnAmt +
                '}';
    }
}