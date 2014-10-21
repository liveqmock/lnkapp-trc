package org.fbi.linking.codec.dataformat.samples.staringmodel.T1000;


import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created with IntelliJ IDEA.
 * User: zhanrui
 * Date: 13-9-12
 * Time: ÏÂÎç1:39
 * To change this template use File | Settings | File Templates.
 */
@SeperatedTextMessage
public class Header {
    @DataField(seq = 1)
    private String txnCode;

    @DataField(seq = 2)
    private String txnDate;

    public String getTxnCode() {
        return txnCode;
    }

    public void setTxnCode(String txnCode) {
        this.txnCode = txnCode;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }
}
