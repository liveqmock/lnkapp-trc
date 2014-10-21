package org.fbi.trc.tqc.repository.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TqcRulePubExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public TqcRulePubExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andRuleTypeIsNull() {
            addCriterion("RULE_TYPE is null");
            return (Criteria) this;
        }

        public Criteria andRuleTypeIsNotNull() {
            addCriterion("RULE_TYPE is not null");
            return (Criteria) this;
        }

        public Criteria andRuleTypeEqualTo(String value) {
            addCriterion("RULE_TYPE =", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotEqualTo(String value) {
            addCriterion("RULE_TYPE <>", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeGreaterThan(String value) {
            addCriterion("RULE_TYPE >", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeGreaterThanOrEqualTo(String value) {
            addCriterion("RULE_TYPE >=", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeLessThan(String value) {
            addCriterion("RULE_TYPE <", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeLessThanOrEqualTo(String value) {
            addCriterion("RULE_TYPE <=", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeLike(String value) {
            addCriterion("RULE_TYPE like", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotLike(String value) {
            addCriterion("RULE_TYPE not like", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeIn(List<String> values) {
            addCriterion("RULE_TYPE in", values, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotIn(List<String> values) {
            addCriterion("RULE_TYPE not in", values, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeBetween(String value1, String value2) {
            addCriterion("RULE_TYPE between", value1, value2, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotBetween(String value1, String value2) {
            addCriterion("RULE_TYPE not between", value1, value2, "ruleType");
            return (Criteria) this;
        }

        public Criteria andSingleLimIsNull() {
            addCriterion("SINGLE_LIM is null");
            return (Criteria) this;
        }

        public Criteria andSingleLimIsNotNull() {
            addCriterion("SINGLE_LIM is not null");
            return (Criteria) this;
        }

        public Criteria andSingleLimEqualTo(BigDecimal value) {
            addCriterion("SINGLE_LIM =", value, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimNotEqualTo(BigDecimal value) {
            addCriterion("SINGLE_LIM <>", value, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimGreaterThan(BigDecimal value) {
            addCriterion("SINGLE_LIM >", value, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("SINGLE_LIM >=", value, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimLessThan(BigDecimal value) {
            addCriterion("SINGLE_LIM <", value, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimLessThanOrEqualTo(BigDecimal value) {
            addCriterion("SINGLE_LIM <=", value, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimIn(List<BigDecimal> values) {
            addCriterion("SINGLE_LIM in", values, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimNotIn(List<BigDecimal> values) {
            addCriterion("SINGLE_LIM not in", values, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("SINGLE_LIM between", value1, value2, "singleLim");
            return (Criteria) this;
        }

        public Criteria andSingleLimNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("SINGLE_LIM not between", value1, value2, "singleLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimIsNull() {
            addCriterion("DAY_AMT_LIM is null");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimIsNotNull() {
            addCriterion("DAY_AMT_LIM is not null");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimEqualTo(BigDecimal value) {
            addCriterion("DAY_AMT_LIM =", value, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimNotEqualTo(BigDecimal value) {
            addCriterion("DAY_AMT_LIM <>", value, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimGreaterThan(BigDecimal value) {
            addCriterion("DAY_AMT_LIM >", value, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("DAY_AMT_LIM >=", value, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimLessThan(BigDecimal value) {
            addCriterion("DAY_AMT_LIM <", value, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimLessThanOrEqualTo(BigDecimal value) {
            addCriterion("DAY_AMT_LIM <=", value, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimIn(List<BigDecimal> values) {
            addCriterion("DAY_AMT_LIM in", values, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimNotIn(List<BigDecimal> values) {
            addCriterion("DAY_AMT_LIM not in", values, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("DAY_AMT_LIM between", value1, value2, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayAmtLimNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("DAY_AMT_LIM not between", value1, value2, "dayAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimIsNull() {
            addCriterion("MONTH_AMT_LIM is null");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimIsNotNull() {
            addCriterion("MONTH_AMT_LIM is not null");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimEqualTo(BigDecimal value) {
            addCriterion("MONTH_AMT_LIM =", value, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimNotEqualTo(BigDecimal value) {
            addCriterion("MONTH_AMT_LIM <>", value, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimGreaterThan(BigDecimal value) {
            addCriterion("MONTH_AMT_LIM >", value, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("MONTH_AMT_LIM >=", value, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimLessThan(BigDecimal value) {
            addCriterion("MONTH_AMT_LIM <", value, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimLessThanOrEqualTo(BigDecimal value) {
            addCriterion("MONTH_AMT_LIM <=", value, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimIn(List<BigDecimal> values) {
            addCriterion("MONTH_AMT_LIM in", values, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimNotIn(List<BigDecimal> values) {
            addCriterion("MONTH_AMT_LIM not in", values, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("MONTH_AMT_LIM between", value1, value2, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andMonthAmtLimNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("MONTH_AMT_LIM not between", value1, value2, "monthAmtLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimIsNull() {
            addCriterion("DAY_CNT_LIM is null");
            return (Criteria) this;
        }

        public Criteria andDayCntLimIsNotNull() {
            addCriterion("DAY_CNT_LIM is not null");
            return (Criteria) this;
        }

        public Criteria andDayCntLimEqualTo(Integer value) {
            addCriterion("DAY_CNT_LIM =", value, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimNotEqualTo(Integer value) {
            addCriterion("DAY_CNT_LIM <>", value, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimGreaterThan(Integer value) {
            addCriterion("DAY_CNT_LIM >", value, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimGreaterThanOrEqualTo(Integer value) {
            addCriterion("DAY_CNT_LIM >=", value, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimLessThan(Integer value) {
            addCriterion("DAY_CNT_LIM <", value, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimLessThanOrEqualTo(Integer value) {
            addCriterion("DAY_CNT_LIM <=", value, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimIn(List<Integer> values) {
            addCriterion("DAY_CNT_LIM in", values, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimNotIn(List<Integer> values) {
            addCriterion("DAY_CNT_LIM not in", values, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimBetween(Integer value1, Integer value2) {
            addCriterion("DAY_CNT_LIM between", value1, value2, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andDayCntLimNotBetween(Integer value1, Integer value2) {
            addCriterion("DAY_CNT_LIM not between", value1, value2, "dayCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimIsNull() {
            addCriterion("MONTH_CNT_LIM is null");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimIsNotNull() {
            addCriterion("MONTH_CNT_LIM is not null");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimEqualTo(Integer value) {
            addCriterion("MONTH_CNT_LIM =", value, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimNotEqualTo(Integer value) {
            addCriterion("MONTH_CNT_LIM <>", value, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimGreaterThan(Integer value) {
            addCriterion("MONTH_CNT_LIM >", value, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimGreaterThanOrEqualTo(Integer value) {
            addCriterion("MONTH_CNT_LIM >=", value, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimLessThan(Integer value) {
            addCriterion("MONTH_CNT_LIM <", value, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimLessThanOrEqualTo(Integer value) {
            addCriterion("MONTH_CNT_LIM <=", value, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimIn(List<Integer> values) {
            addCriterion("MONTH_CNT_LIM in", values, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimNotIn(List<Integer> values) {
            addCriterion("MONTH_CNT_LIM not in", values, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimBetween(Integer value1, Integer value2) {
            addCriterion("MONTH_CNT_LIM between", value1, value2, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andMonthCntLimNotBetween(Integer value1, Integer value2) {
            addCriterion("MONTH_CNT_LIM not between", value1, value2, "monthCntLim");
            return (Criteria) this;
        }

        public Criteria andRecverIsNull() {
            addCriterion("RECVER is null");
            return (Criteria) this;
        }

        public Criteria andRecverIsNotNull() {
            addCriterion("RECVER is not null");
            return (Criteria) this;
        }

        public Criteria andRecverEqualTo(Integer value) {
            addCriterion("RECVER =", value, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverNotEqualTo(Integer value) {
            addCriterion("RECVER <>", value, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverGreaterThan(Integer value) {
            addCriterion("RECVER >", value, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverGreaterThanOrEqualTo(Integer value) {
            addCriterion("RECVER >=", value, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverLessThan(Integer value) {
            addCriterion("RECVER <", value, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverLessThanOrEqualTo(Integer value) {
            addCriterion("RECVER <=", value, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverIn(List<Integer> values) {
            addCriterion("RECVER in", values, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverNotIn(List<Integer> values) {
            addCriterion("RECVER not in", values, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverBetween(Integer value1, Integer value2) {
            addCriterion("RECVER between", value1, value2, "recver");
            return (Criteria) this;
        }

        public Criteria andRecverNotBetween(Integer value1, Integer value2) {
            addCriterion("RECVER not between", value1, value2, "recver");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("REMARK is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("REMARK is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("REMARK =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("REMARK <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("REMARK >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("REMARK >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("REMARK <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("REMARK <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("REMARK like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("REMARK not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("REMARK in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("REMARK not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("REMARK between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("REMARK not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andOperIdIsNull() {
            addCriterion("OPER_ID is null");
            return (Criteria) this;
        }

        public Criteria andOperIdIsNotNull() {
            addCriterion("OPER_ID is not null");
            return (Criteria) this;
        }

        public Criteria andOperIdEqualTo(String value) {
            addCriterion("OPER_ID =", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdNotEqualTo(String value) {
            addCriterion("OPER_ID <>", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdGreaterThan(String value) {
            addCriterion("OPER_ID >", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdGreaterThanOrEqualTo(String value) {
            addCriterion("OPER_ID >=", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdLessThan(String value) {
            addCriterion("OPER_ID <", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdLessThanOrEqualTo(String value) {
            addCriterion("OPER_ID <=", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdLike(String value) {
            addCriterion("OPER_ID like", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdNotLike(String value) {
            addCriterion("OPER_ID not like", value, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdIn(List<String> values) {
            addCriterion("OPER_ID in", values, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdNotIn(List<String> values) {
            addCriterion("OPER_ID not in", values, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdBetween(String value1, String value2) {
            addCriterion("OPER_ID between", value1, value2, "operId");
            return (Criteria) this;
        }

        public Criteria andOperIdNotBetween(String value1, String value2) {
            addCriterion("OPER_ID not between", value1, value2, "operId");
            return (Criteria) this;
        }

        public Criteria andOperDateIsNull() {
            addCriterion("OPER_DATE is null");
            return (Criteria) this;
        }

        public Criteria andOperDateIsNotNull() {
            addCriterion("OPER_DATE is not null");
            return (Criteria) this;
        }

        public Criteria andOperDateEqualTo(String value) {
            addCriterion("OPER_DATE =", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateNotEqualTo(String value) {
            addCriterion("OPER_DATE <>", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateGreaterThan(String value) {
            addCriterion("OPER_DATE >", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateGreaterThanOrEqualTo(String value) {
            addCriterion("OPER_DATE >=", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateLessThan(String value) {
            addCriterion("OPER_DATE <", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateLessThanOrEqualTo(String value) {
            addCriterion("OPER_DATE <=", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateLike(String value) {
            addCriterion("OPER_DATE like", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateNotLike(String value) {
            addCriterion("OPER_DATE not like", value, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateIn(List<String> values) {
            addCriterion("OPER_DATE in", values, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateNotIn(List<String> values) {
            addCriterion("OPER_DATE not in", values, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateBetween(String value1, String value2) {
            addCriterion("OPER_DATE between", value1, value2, "operDate");
            return (Criteria) this;
        }

        public Criteria andOperDateNotBetween(String value1, String value2) {
            addCriterion("OPER_DATE not between", value1, value2, "operDate");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated do_not_delete_during_merge Tue Oct 21 10:59:00 CST 2014
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table TRC.TQC_RULE_PUB
     *
     * @mbggenerated Tue Oct 21 10:59:00 CST 2014
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}