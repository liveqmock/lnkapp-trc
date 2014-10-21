package org.fbi.trc.tqc.repository.model;

public class TqcRuleAcctKey {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_RULE_ACCT.MCHT_CODE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    private String mchtCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_RULE_ACCT.PRJ_CODE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    private String prjCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column TRC.TQC_RULE_ACCT.ACCT_TYPE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    private String acctType;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_RULE_ACCT.MCHT_CODE
     *
     * @return the value of TRC.TQC_RULE_ACCT.MCHT_CODE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public String getMchtCode() {
        return mchtCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_RULE_ACCT.MCHT_CODE
     *
     * @param mchtCode the value for TRC.TQC_RULE_ACCT.MCHT_CODE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void setMchtCode(String mchtCode) {
        this.mchtCode = mchtCode == null ? null : mchtCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_RULE_ACCT.PRJ_CODE
     *
     * @return the value of TRC.TQC_RULE_ACCT.PRJ_CODE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public String getPrjCode() {
        return prjCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_RULE_ACCT.PRJ_CODE
     *
     * @param prjCode the value for TRC.TQC_RULE_ACCT.PRJ_CODE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void setPrjCode(String prjCode) {
        this.prjCode = prjCode == null ? null : prjCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column TRC.TQC_RULE_ACCT.ACCT_TYPE
     *
     * @return the value of TRC.TQC_RULE_ACCT.ACCT_TYPE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public String getAcctType() {
        return acctType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column TRC.TQC_RULE_ACCT.ACCT_TYPE
     *
     * @param acctType the value for TRC.TQC_RULE_ACCT.ACCT_TYPE
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void setAcctType(String acctType) {
        this.acctType = acctType == null ? null : acctType.trim();
    }
}