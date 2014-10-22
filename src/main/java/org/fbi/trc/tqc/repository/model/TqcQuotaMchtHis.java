package org.fbi.trc.tqc.repository.model;

import java.math.BigDecimal;

public class TqcQuotaMchtHis {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.PKID
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String pkid;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.MCHT_CODE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String mchtCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.PRJ_CODE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String prjCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.ACCT_NO
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String acctNo;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.VCH_SN
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String vchSn;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DATE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String txnDate;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_AMT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private BigDecimal txnDayAmt;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_AMT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private BigDecimal txnMonthAmt;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_CNT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private Integer txnDayCnt;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_CNT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private Integer txnMonthCnt;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_QUOTA_MCHT_HIS.REMARK
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    private String remark;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.PKID
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.PKID
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getPkid() {
        return pkid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.PKID
     *
     * @param pkid the value for TRC.TQC_QUOTA_MCHT_HIS.PKID
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setPkid(String pkid) {
        this.pkid = pkid == null ? null : pkid.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.MCHT_CODE
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.MCHT_CODE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getMchtCode() {
        return mchtCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.MCHT_CODE
     *
     * @param mchtCode the value for TRC.TQC_QUOTA_MCHT_HIS.MCHT_CODE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setMchtCode(String mchtCode) {
        this.mchtCode = mchtCode == null ? null : mchtCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.PRJ_CODE
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.PRJ_CODE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getPrjCode() {
        return prjCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.PRJ_CODE
     *
     * @param prjCode the value for TRC.TQC_QUOTA_MCHT_HIS.PRJ_CODE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setPrjCode(String prjCode) {
        this.prjCode = prjCode == null ? null : prjCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.ACCT_NO
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.ACCT_NO
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getAcctNo() {
        return acctNo;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.ACCT_NO
     *
     * @param acctNo the value for TRC.TQC_QUOTA_MCHT_HIS.ACCT_NO
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo == null ? null : acctNo.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.VCH_SN
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.VCH_SN
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getVchSn() {
        return vchSn;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.VCH_SN
     *
     * @param vchSn the value for TRC.TQC_QUOTA_MCHT_HIS.VCH_SN
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setVchSn(String vchSn) {
        this.vchSn = vchSn == null ? null : vchSn.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DATE
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.TXN_DATE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getTxnDate() {
        return txnDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DATE
     *
     * @param txnDate the value for TRC.TQC_QUOTA_MCHT_HIS.TXN_DATE
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate == null ? null : txnDate.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_AMT
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_AMT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public BigDecimal getTxnDayAmt() {
        return txnDayAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_AMT
     *
     * @param txnDayAmt the value for TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_AMT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setTxnDayAmt(BigDecimal txnDayAmt) {
        this.txnDayAmt = txnDayAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_AMT
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_AMT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public BigDecimal getTxnMonthAmt() {
        return txnMonthAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_AMT
     *
     * @param txnMonthAmt the value for TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_AMT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setTxnMonthAmt(BigDecimal txnMonthAmt) {
        this.txnMonthAmt = txnMonthAmt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_CNT
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_CNT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public Integer getTxnDayCnt() {
        return txnDayCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_CNT
     *
     * @param txnDayCnt the value for TRC.TQC_QUOTA_MCHT_HIS.TXN_DAY_CNT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setTxnDayCnt(Integer txnDayCnt) {
        this.txnDayCnt = txnDayCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_CNT
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_CNT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public Integer getTxnMonthCnt() {
        return txnMonthCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_CNT
     *
     * @param txnMonthCnt the value for TRC.TQC_QUOTA_MCHT_HIS.TXN_MONTH_CNT
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setTxnMonthCnt(Integer txnMonthCnt) {
        this.txnMonthCnt = txnMonthCnt;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_QUOTA_MCHT_HIS.REMARK
     *
     * @return the value of TRC.TQC_QUOTA_MCHT_HIS.REMARK
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_QUOTA_MCHT_HIS.REMARK
     *
     * @param remark the value for TRC.TQC_QUOTA_MCHT_HIS.REMARK
     *
     * @mbggenerated Wed Oct 22 11:35:56 CST 2014
     */
    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}