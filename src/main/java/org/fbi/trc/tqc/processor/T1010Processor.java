package org.fbi.trc.tqc.processor;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.trc.tqc.domain.cbs.T1010Request.CbsTia1010;
import org.fbi.trc.tqc.domain.cbs.T1010Response.CbsToa1010;
import org.fbi.trc.tqc.enums.TxnRtnCode;
import org.fbi.trc.tqc.helper.MybatisFactory;
import org.fbi.trc.tqc.repository.dao.*;
import org.fbi.trc.tqc.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanrui on 2014-10-20.
 * 1071010收款单位限额验证
 */
public class T1010Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnDate = request.getHeader("txnDate");
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia1010 tia;
        try {
            tia = unmarshalCbsRequestMsg(request.getRequestBody());
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }

        //请求报文检查
        if (StringUtils.isEmpty(tia.getAcctNo())
                || StringUtils.isEmpty(tia.getMchtCode())
                || StringUtils.isEmpty(tia.getPrjCode())) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文信息不全.");
            throw new RuntimeException("请求报文信息不全");
        }

        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, txnDate, hostTxnsn);
            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = marshalCbsResponseMsg(tia.getAcctNo(), cbsRtnInfo.getRtnMsg());
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常");
        }

    }

    //解包生成CBS请求报文BEAN
    private CbsTia1010 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia1010 tia = new CbsTia1010();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia1010) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia1010");
        return tia;
    }

    //生成特色平台响应报文
    private String marshalCbsResponseMsg(String acctNo, String rtnMsg) {
        CbsToa1010 cbsToa = new CbsToa1010();
        cbsToa.setAcctNo(acctNo);
        cbsToa.setRtnMsg(rtnMsg);

        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("特色平台报文转换失败.", e);
        }
        return cbsRespMsg;
    }

    //限额处理 可能需要进行多表更新处理
    private CbsRtnInfo processTxn(CbsTia1010 tia, String txnDate, String hostTxnsn) {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        SqlSessionFactory sqlSessionFactory = null;
        SqlSession session = null;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);

            //获取限额定义表数据
            RuleInfo ruleInfo = getQuotaRuleInfo(session, tia);
            if (ruleInfo == null) {
                String msg = "[10]未定义公用限额规则.";
                cbsRtnInfo.setRtnCode(TxnRtnCode.QUOTA_CHK_ERR_PUB_RULE);
                cbsRtnInfo.setRtnMsg(msg);
                return cbsRtnInfo;
            }

            //获取当前累计表数据，不存在则新增一笔。
            TqcQuotaMcht quota = selectAndInitQuota(session, tia, txnDate);
            BigDecimal quotaDayAmt = quota.getTxnDayAmt();
            BigDecimal quotaMonthAmt = quota.getTxnMonthAmt();

            BigDecimal txnAmt = tia.getTxnAmt();
            if (!txnDate.equals(quota.getTxnDate())) { //日期不同
                quotaDayAmt = new BigDecimal("0");
                if (!txnDate.substring(0, 6).equals(quota.getTxnDate().substring(0, 6))) { //非同年同月
                    quotaMonthAmt = new BigDecimal("0");
                }
                //处理历史表
                insertHistoryRecord(session, quota);
            }

            //根据规则进行验证
            TxnRtnCode rtnCode = TxnRtnCode.TXN_ERR;
            String msg = "";
            if (txnAmt.compareTo(ruleInfo.getSingleLim()) == 1) {
                rtnCode = TxnRtnCode.QUOTA_CHK_ERR_SINGLE_AMT;
                msg = "[01]超单笔限额.";
            } else if (quotaDayAmt.add(txnAmt).compareTo(ruleInfo.getDayAmtLim()) == 1) {
                rtnCode = TxnRtnCode.QUOTA_CHK_ERR_DAY_AMT;
                msg = "[02]超日限额.";
            } else if (quotaMonthAmt.add(txnAmt).compareTo(ruleInfo.getMonthAmtLim()) == 1) {
                rtnCode = TxnRtnCode.QUOTA_CHK_ERR_MONTH_AMT;
                msg = "[03]超月限额.";
            } else {
                //检查通过  更新限额表!
                rtnCode = TxnRtnCode.TXN_SUCCESS;
                //updateQuota(session, quota, txnAmt, txnDate);
            }

            //组返回信息
            cbsRtnInfo.setRtnCode(rtnCode);
            cbsRtnInfo.setRtnMsg(msg);

            //记流水
            insertTxnRecord(session, tia, quota, txnDate, hostTxnsn, cbsRtnInfo);

            return cbsRtnInfo;
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            String msg = "[sn=" + hostTxnsn + "] " + "数据库处理失败。";
            logger.error(msg, e);
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_ERR);
            cbsRtnInfo.setRtnMsg(msg);
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private RuleInfo getQuotaRuleInfo(SqlSession session, CbsTia1010 tia) {
        RuleInfo ruleInfo = new RuleInfo();
        TqcRuleMcht rule = selectRule(session, tia);
        if (rule == null) {
            //使用公共规则
            TqcRulePub rulePub = selectPubRuleForMcht(session);
            if (rulePub == null) {
                return null;
            } else {
                ruleInfo.setSingleLim(rulePub.getSingleLim());
                ruleInfo.setDayAmtLim(rulePub.getDayAmtLim());
                ruleInfo.setMonthAmtLim(rulePub.getMonthAmtLim());
                ruleInfo.setDayCntLim(rulePub.getDayCntLim());
                ruleInfo.setMonthCntLim(rulePub.getMonthCntLim());
            }
        } else {
            ruleInfo.setSingleLim(rule.getSingleLim());
            ruleInfo.setDayAmtLim(rule.getDayAmtLim());
            ruleInfo.setMonthAmtLim(rule.getMonthAmtLim());
            ruleInfo.setDayCntLim(rule.getDayCntLim());
            ruleInfo.setMonthCntLim(rule.getMonthCntLim());
        }
        return ruleInfo;
    }

    private TqcRuleMcht selectRule(SqlSession session, CbsTia1010 tia) {
        TqcRuleMchtMapper ruleMapper = session.getMapper(TqcRuleMchtMapper.class);
        TqcRuleMchtKey ruleKey = new TqcRuleMchtKey();
        ruleKey.setMchtCode(tia.getMchtCode());
        ruleKey.setPrjCode(tia.getPrjCode());
        return ruleMapper.selectByPrimaryKey(ruleKey);
    }

    private TqcRulePub selectPubRuleForMcht(SqlSession session) {
        TqcRulePubMapper ruleMapper = session.getMapper(TqcRulePubMapper.class);
        return ruleMapper.selectByPrimaryKey("1");  //规则类型（1收款单位规则，2对公账号规则）
    }

    private TqcQuotaMcht selectAndInitQuota(SqlSession session, CbsTia1010 tia, String txnDate) {
        TqcQuotaMchtMapper quotaMapper = session.getMapper(TqcQuotaMchtMapper.class);
        TqcQuotaMchtKey key = new TqcQuotaMchtKey();
        key.setMchtCode(tia.getMchtCode());
        key.setPrjCode(tia.getPrjCode());
        TqcQuotaMcht quota = quotaMapper.selectByPrimaryKey(key);
        if (quota == null) { //第一笔
            quota = new TqcQuotaMcht();
            //初始化第一笔
            quota.setMchtCode(tia.getMchtCode());
            quota.setPrjCode(tia.getPrjCode());
            quota.setAcctNo(tia.getAcctNo());
            quota.setTxnDate(txnDate);
            quota.setTxnDayAmt(new BigDecimal("0"));
            quota.setTxnMonthAmt(new BigDecimal("0"));
            quota.setTxnDayCnt(0);
            quota.setTxnMonthCnt(0);
            quota.setVchSn(tia.getVchSn());
            quotaMapper.insert(quota);
        }
        return quota;
    }

    private void updateQuota(SqlSession session, TqcQuotaMcht quota, BigDecimal txnAmt, String txnDate) {
        quota.setTxnDayAmt(quota.getTxnDayAmt().add(txnAmt));
        quota.setTxnMonthAmt(quota.getTxnMonthAmt().add(txnAmt));
        quota.setTxnDayCnt(quota.getTxnDayCnt() + 1);
        quota.setTxnMonthCnt(quota.getTxnMonthCnt() + 1);
        if (!txnDate.equals(quota.getTxnDate())) { //日期相同
            quota.setTxnDate(txnDate);
        }
        session.getMapper(TqcQuotaMchtMapper.class).updateByPrimaryKey(quota);
    }

    //流水表处理
    private void insertTxnRecord(SqlSession session, CbsTia1010 tia, TqcQuotaMcht quota,
                                 String txnDate, String hostTxnsn, CbsRtnInfo cbsRtnInfo) {
        TqcTxnMcht txn = new TqcTxnMcht();
        txn.setTxnDate(txnDate);
        txn.setTxnSeqno(hostTxnsn);
        txn.setMchtCode(tia.getMchtCode());
        txn.setPrjCode(tia.getPrjCode());
        txn.setVchSn(tia.getVchSn());
        txn.setAcctNo(tia.getAcctNo());
        txn.setTxnAmt(tia.getTxnAmt());
        txn.setTxnCnt(1);     //TODO
        if (cbsRtnInfo.getRtnCode().equals(TxnRtnCode.TXN_SUCCESS)) {
            txn.setChkStatus("1");
        } else {
            txn.setChkStatus("0");
            txn.setTxnRejCode(cbsRtnInfo.getRtnCode().getCode());
            txn.setTxnRejReason(cbsRtnInfo.getRtnMsg());
        }
        txn.setChkTime(new SimpleDateFormat("HHmmss").format(new Date()));
        txn.setBookFlag("0");  //主机未记账
        txn.setBookTime("000000");
        txn.setTxnDayAmtPre(quota.getTxnDayAmt());
        txn.setTxnMonthAmtPre(quota.getTxnMonthAmt());
        txn.setVchSnPre(quota.getVchSn());

        txn.setTxnFailAmt(new BigDecimal("0"));
        txn.setTxnFailCnt(0);
        txn.setTxnSuccAmt(new BigDecimal("0"));
        txn.setTxnSuccCnt(0);
        txn.setRemark("");

        session.getMapper(TqcTxnMchtMapper.class).insert(txn);
    }

    //历史表处理
    private void insertHistoryRecord(SqlSession session, TqcQuotaMcht quota) {
        TqcQuotaMchtHis his = new TqcQuotaMchtHis();
        try {
            BeanUtils.copyProperties(his, quota);
            session.getMapper(TqcQuotaMchtHisMapper.class).insert(his);
        } catch (Exception e) {
            throw new RuntimeException("历史表处理失败", e);
        }
    }

}
