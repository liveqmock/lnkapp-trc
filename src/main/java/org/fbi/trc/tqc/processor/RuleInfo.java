package org.fbi.trc.tqc.processor;

import java.math.BigDecimal;

/**
 * Created by zhanrui on 2014/10/20.
 */
public class RuleInfo {
    private BigDecimal singleLim = new BigDecimal("0");
    private BigDecimal dayAmtLim = new BigDecimal("0");
    private BigDecimal monthAmtLim = new BigDecimal("0");
    private Integer dayCntLim = 0;
    private Integer monthCntLim = 0;

    public BigDecimal getSingleLim() {
        return singleLim;
    }

    public void setSingleLim(BigDecimal singleLim) {
        this.singleLim = singleLim;
    }

    public BigDecimal getDayAmtLim() {
        return dayAmtLim;
    }

    public void setDayAmtLim(BigDecimal dayAmtLim) {
        this.dayAmtLim = dayAmtLim;
    }

    public BigDecimal getMonthAmtLim() {
        return monthAmtLim;
    }

    public void setMonthAmtLim(BigDecimal monthAmtLim) {
        this.monthAmtLim = monthAmtLim;
    }

    public Integer getDayCntLim() {
        return dayCntLim;
    }

    public void setDayCntLim(Integer dayCntLim) {
        this.dayCntLim = dayCntLim;
    }

    public Integer getMonthCntLim() {
        return monthCntLim;
    }

    public void setMonthCntLim(Integer monthCntLim) {
        this.monthCntLim = monthCntLim;
    }

    @Override
    public String toString() {
        return "QuotaRuleInfo{" +
                "singleLim=" + singleLim +
                ", dayAmtLim=" + dayAmtLim +
                ", monthAmtLim=" + monthAmtLim +
                ", dayCntLim=" + dayCntLim +
                ", monthCntLim=" + monthCntLim +
                '}';
    }
}
