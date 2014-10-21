package org.fbi.trc.tqc.processor;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.trc.tqc.domain.cbs.T1020Request.CbsTia1020;
import org.fbi.trc.tqc.domain.cbs.T1020Response.CbsToa1020;
import org.fbi.trc.tqc.enums.TxnRtnCode;
import org.fbi.trc.tqc.helper.FbiBeanUtils;
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
 * 1071020���˺Ž����޶���֤
 */
public class T1020Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnDate = request.getHeader("txnDate");
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia1020 tia;
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
    private CbsTia1020 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia1020 tia = new CbsTia1020();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia1020) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia1020");
        return tia;
    }

    //������ɫƽ̨��Ӧ����
    private String marshalCbsResponseMsg(String acctNo, String rtnMsg) {
        CbsToa1020 cbsToa = new CbsToa1020();
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
    private CbsRtnInfo processTxn(CbsTia1020 tia, String txnDate, String hostTxnsn) {
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
            TqcQuotaAcct quota = selectAndInitQuota(session, tia, txnDate);
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

    //��ȡ������ ��Ҫ��
    private RuleInfo getQuotaRuleInfo(SqlSession session, CbsTia1020 tia) {
        RuleInfo ruleInfo = new RuleInfo();
        //�Ƿ�Թ��˺�
        boolean isCorpAcct = isCorpAcct(tia.getAcctNo());
        //��ȡ�˺Ź������
        TqcRuleAcct rule = selectRule(session, tia, isCorpAcct);
        if (rule != null) {
            if (isCorpAcct) {
                FbiBeanUtils.copyProperties(rule, ruleInfo);
            } else {
                String useAreaFlag = rule.getAreaRuleFlag();
                if ("1".equals(useAreaFlag)) { //ʹ����ҵ����
                    TqcRuleArea ruleArea = selectAreaRule(session, rule.getAreaCode());
                    FbiBeanUtils.copyProperties(ruleArea, ruleInfo);
                } else {
                    FbiBeanUtils.copyProperties(rule, ruleInfo);
                }
            }
        } else {
            //���ҹ�������
            if (isCorpAcct) {//�Թ��˻�
                TqcRulePub rulePub = selectPubRule(session);
                if (rulePub == null) {
                    return null;
                } else {
                    FbiBeanUtils.copyProperties(rulePub, ruleInfo);
                }
            } else { //��˽�˻� ��������
                return null;
            }
        }
        return ruleInfo;
    }

    //�����˺ų��ȡ����������ж��Ǹ����˻����ǶԹ��˻����˺ų���Ϊ16λ/19λ�ģ��ж�Ϊ�����˻�������Ϊ�Թ��˻���
    private boolean isCorpAcct(String actNo) {
        int actLen = actNo.length();
        return !(actLen == 16 || actLen == 19);
    }
/*
    //�����˺ų��ȡ����������ж��Ǹ����˻����ǶԹ��˻����˺ų���Ϊ16λ/19λ�һ�������С��8���ַ��ģ��ж�Ϊ�����˻�������Ϊ�Թ��˻���
    private boolean isCorpAcct(String actNo, String actName) {
        if (StringUtils.isEmpty(actName)) {  //����Ϊ�� Ĭ��Ϊ��˽�˺� TODO
            return false;
        }

        int actLen = actNo.length();
        int nameLen = 0;
        try {
            nameLen = (actName.getBytes("GBK")).length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return !((actLen == 16 || actLen == 19) && nameLen < 8);
    }
*/

    private TqcRuleArea selectAreaRule(SqlSession session, String areaCode) {
        TqcRuleAreaMapper mapper = session.getMapper(TqcRuleAreaMapper.class);
        return mapper.selectByPrimaryKey(areaCode);
    }

    private TqcRuleAcct selectRule(SqlSession session, CbsTia1020 tia, boolean isCorpAcct) {
        TqcRuleAcctMapper ruleMapper = session.getMapper(TqcRuleAcctMapper.class);
        TqcRuleAcctKey ruleKey = new TqcRuleAcctKey();
        ruleKey.setMchtCode(tia.getMchtCode());
        ruleKey.setPrjCode(tia.getPrjCode());
        ruleKey.setAcctType(isCorpAcct ? "1" : "2");
        return ruleMapper.selectByPrimaryKey(ruleKey);
    }

    private TqcRulePub selectPubRule(SqlSession session) {
        TqcRulePubMapper ruleMapper = session.getMapper(TqcRulePubMapper.class);
        return ruleMapper.selectByPrimaryKey("2");  //�������ͣ�1�տλ����2�Թ��˺Ź���
    }

    private TqcQuotaAcct selectAndInitQuota(SqlSession session, CbsTia1020 tia, String txnDate) {
        TqcQuotaAcctMapper quotaMapper = session.getMapper(TqcQuotaAcctMapper.class);
        TqcQuotaAcctKey key = new TqcQuotaAcctKey();
        key.setMchtCode(tia.getMchtCode());
        key.setPrjCode(tia.getPrjCode());
        TqcQuotaAcct quota = quotaMapper.selectByPrimaryKey(key);
        if (quota == null) { //��һ��
            quota = new TqcQuotaAcct();
            //��ʼ����һ��
            quota.setMchtCode(tia.getMchtCode());
            quota.setPrjCode(tia.getPrjCode());
            quota.setAcctNo(tia.getAcctNo());
            quota.setVchSn(tia.getVchSn());
            quota.setTxnDate(txnDate);
            quota.setTxnDayAmt(new BigDecimal("0"));
            quota.setTxnMonthAmt(new BigDecimal("0"));
            quota.setTxnDayCnt(0);
            quota.setTxnMonthCnt(0);
            quotaMapper.insert(quota);
        }
        return quota;
    }

    private void updateQuota(SqlSession session, TqcQuotaAcct quota, BigDecimal txnAmt, String txnDate) {
        quota.setTxnDayAmt(quota.getTxnDayAmt().add(txnAmt));
        quota.setTxnMonthAmt(quota.getTxnMonthAmt().add(txnAmt));
        quota.setTxnDayCnt(quota.getTxnDayCnt() + 1);
        quota.setTxnMonthCnt(quota.getTxnMonthCnt() + 1);
        if (!txnDate.equals(quota.getTxnDate())) { //������ͬ
            quota.setTxnDate(txnDate);
        }
        session.getMapper(TqcQuotaAcctMapper.class).updateByPrimaryKey(quota);
    }

    //��ˮ����
    private void insertTxnRecord(SqlSession session, CbsTia1020 tia, TqcQuotaAcct quota,
                                 String txnDate, String hostTxnsn, CbsRtnInfo cbsRtnInfo) {
        TqcTxnAcct txn = new TqcTxnAcct();
        txn.setTxnDate(txnDate);
        txn.setTxnSeqno(hostTxnsn);
        txn.setMchtCode(tia.getMchtCode());
        txn.setPrjCode(tia.getPrjCode());
        txn.setAcctNo(tia.getAcctNo());
        txn.setTxnAmt(tia.getTxnAmt());
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
        txn.setRemark("");

        session.getMapper(TqcTxnAcctMapper.class).insert(txn);
    }

    //��ʷ����
    private void insertHistoryRecord(SqlSession session, TqcQuotaAcct quota) {
        TqcQuotaAcctHis his = new TqcQuotaAcctHis();
        try {
            BeanUtils.copyProperties(his, quota);
            session.getMapper(TqcQuotaAcctHisMapper.class).insert(his);
        } catch (Exception e) {
            throw new RuntimeException("��ʷ����ʧ��", e);
        }
    }

}
