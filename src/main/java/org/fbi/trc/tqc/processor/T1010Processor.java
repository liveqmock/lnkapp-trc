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
 * 1071010�տλ�޶���֤
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
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }

        //�����ļ��
        if (StringUtils.isEmpty(tia.getAcctNo())
                || StringUtils.isEmpty(tia.getMchtCode())
                || StringUtils.isEmpty(tia.getPrjCode())) {
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨��������Ϣ��ȫ.");
            throw new RuntimeException("��������Ϣ��ȫ");
        }

        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, txnDate, hostTxnsn);
            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = marshalCbsResponseMsg(tia.getAcctNo(), cbsRtnInfo.getRtnMsg());
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣");
        }

    }

    //�������CBS������BEAN
    private CbsTia1010 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia1010 tia = new CbsTia1010();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia1010) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia1010");
        return tia;
    }

    //������ɫƽ̨��Ӧ����
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
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
        }
        return cbsRespMsg;
    }

    //�޶�� ������Ҫ���ж����´���
    private CbsRtnInfo processTxn(CbsTia1010 tia, String txnDate, String hostTxnsn) {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        SqlSessionFactory sqlSessionFactory = null;
        SqlSession session = null;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);

            //��ȡ�޶�������
            RuleInfo ruleInfo = getQuotaRuleInfo(session, tia);
            if (ruleInfo == null) {
                String msg = "[10]δ���幫���޶����.";
                cbsRtnInfo.setRtnCode(TxnRtnCode.QUOTA_CHK_ERR_PUB_RULE);
                cbsRtnInfo.setRtnMsg(msg);
                return cbsRtnInfo;
            }

            //��ȡ��ǰ�ۼƱ����ݣ�������������һ�ʡ�
            TqcQuotaMcht quota = selectAndInitQuota(session, tia, txnDate);
            BigDecimal quotaDayAmt = quota.getTxnDayAmt();
            BigDecimal quotaMonthAmt = quota.getTxnMonthAmt();

            BigDecimal txnAmt = tia.getTxnAmt();
            if (!txnDate.equals(quota.getTxnDate())) { //���ڲ�ͬ
                quotaDayAmt = new BigDecimal("0");
                if (!txnDate.substring(0, 6).equals(quota.getTxnDate().substring(0, 6))) { //��ͬ��ͬ��
                    quotaMonthAmt = new BigDecimal("0");
                }
                //������ʷ��
                insertHistoryRecord(session, quota);
            }

            //���ݹ��������֤
            TxnRtnCode rtnCode = TxnRtnCode.TXN_ERR;
            String msg = "";
            if (txnAmt.compareTo(ruleInfo.getSingleLim()) == 1) {
                rtnCode = TxnRtnCode.QUOTA_CHK_ERR_SINGLE_AMT;
                msg = "[01]�������޶�.";
            } else if (quotaDayAmt.add(txnAmt).compareTo(ruleInfo.getDayAmtLim()) == 1) {
                rtnCode = TxnRtnCode.QUOTA_CHK_ERR_DAY_AMT;
                msg = "[02]�����޶�.";
            } else if (quotaMonthAmt.add(txnAmt).compareTo(ruleInfo.getMonthAmtLim()) == 1) {
                rtnCode = TxnRtnCode.QUOTA_CHK_ERR_MONTH_AMT;
                msg = "[03]�����޶�.";
            } else {
                //���ͨ��  �����޶��!
                rtnCode = TxnRtnCode.TXN_SUCCESS;
                //updateQuota(session, quota, txnAmt, txnDate);
            }

            //�鷵����Ϣ
            cbsRtnInfo.setRtnCode(rtnCode);
            cbsRtnInfo.setRtnMsg(msg);

            //����ˮ
            insertTxnRecord(session, tia, quota, txnDate, hostTxnsn, cbsRtnInfo);

            return cbsRtnInfo;
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            String msg = "[sn=" + hostTxnsn + "] " + "���ݿ⴦��ʧ�ܡ�";
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
            //ʹ�ù�������
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
        return ruleMapper.selectByPrimaryKey("1");  //�������ͣ�1�տλ����2�Թ��˺Ź���
    }

    private TqcQuotaMcht selectAndInitQuota(SqlSession session, CbsTia1010 tia, String txnDate) {
        TqcQuotaMchtMapper quotaMapper = session.getMapper(TqcQuotaMchtMapper.class);
        TqcQuotaMchtKey key = new TqcQuotaMchtKey();
        key.setMchtCode(tia.getMchtCode());
        key.setPrjCode(tia.getPrjCode());
        TqcQuotaMcht quota = quotaMapper.selectByPrimaryKey(key);
        if (quota == null) { //��һ��
            quota = new TqcQuotaMcht();
            //��ʼ����һ��
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
        if (!txnDate.equals(quota.getTxnDate())) { //������ͬ
            quota.setTxnDate(txnDate);
        }
        session.getMapper(TqcQuotaMchtMapper.class).updateByPrimaryKey(quota);
    }

    //��ˮ����
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
        txn.setBookFlag("0");  //����δ����
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

    //��ʷ����
    private void insertHistoryRecord(SqlSession session, TqcQuotaMcht quota) {
        TqcQuotaMchtHis his = new TqcQuotaMchtHis();
        try {
            BeanUtils.copyProperties(his, quota);
            session.getMapper(TqcQuotaMchtHisMapper.class).insert(his);
        } catch (Exception e) {
            throw new RuntimeException("��ʷ����ʧ��", e);
        }
    }

}
