package org.fbi.trc.tqc.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.trc.tqc.domain.cbs.T1030Request.CbsTia1030;
import org.fbi.trc.tqc.enums.TxnRtnCode;
import org.fbi.trc.tqc.helper.MybatisFactory;
import org.fbi.trc.tqc.repository.dao.TqcQuotaAcctMapper;
import org.fbi.trc.tqc.repository.dao.TqcQuotaMchtMapper;
import org.fbi.trc.tqc.repository.dao.TqcTxnAcctMapper;
import org.fbi.trc.tqc.repository.dao.TqcTxnMchtMapper;
import org.fbi.trc.tqc.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhanrui on 2014-10-20.
 * 1071030�ۿ�������
 */
public class T1030Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnDate = request.getHeader("txnDate");
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia1030 tia;
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

        //FTP�ļ����  �ֺܷ˶� ������ˮ�ܽ��ȶԣ�

        //�Ƿ��ظ�����

        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            List<HostBookInfo> bookInfos = null;
            cbsRtnInfo = processTxn(tia, txnDate, hostTxnsn, bookInfos);
            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣");
        }

    }

    //�������CBS������BEAN
    private CbsTia1030 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia1030 tia = new CbsTia1030();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia1030) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia1030");
        return tia;
    }

    /*
            ����Acct Quota��
            ����Mcht Quota��
            ����Acct Txn��
            ����Mcht Txn��
     */
    private CbsRtnInfo processTxn(CbsTia1030 tia, String txnDate, String hostTxnsn, List<HostBookInfo> bookInfos) {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        SqlSessionFactory sqlSessionFactory = null;
        SqlSession session = null;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);


            TqcQuotaMchtMapper quotaMchtMapper = session.getMapper(TqcQuotaMchtMapper.class);
            TqcQuotaAcctMapper quotaAcctMapper = session.getMapper(TqcQuotaAcctMapper.class);
            TqcTxnAcctMapper txnAcctMapper = session.getMapper(TqcTxnAcctMapper.class);

            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
            BigDecimal mchtBookSuccAmt = new BigDecimal("0");
            BigDecimal mchtBookFailAmt = new BigDecimal("0");
            int  mchtBookSuccCnt = 0;
            int  mchtBookFailCnt = 0;
            int count = 0;
            for (HostBookInfo bookInfo : bookInfos) {
                count++;
                if ("1".equals(bookInfo.getBookStatus())) {//���˳ɹ�
                    mchtBookSuccAmt = mchtBookSuccAmt.add(bookInfo.getTxnAmt());
                    mchtBookSuccCnt++;
                    //ACCT quota��
                    TqcQuotaAcctKey quotaAcctKey = new TqcQuotaAcctKey();
                    quotaAcctKey.setMchtCode(tia.getMchtCode());
                    quotaAcctKey.setPrjCode(tia.getPrjCode());
                    TqcQuotaAcct quotaAcct = quotaAcctMapper.selectByPrimaryKey(quotaAcctKey);
                    if (quotaAcct == null) {
                        //TODO
                    } else {
                        String bookDate = bookInfo.getTxnDate();
                        if (quotaAcct.getTxnDate().equals(bookDate)) { //ͬһ��
                            quotaAcct.setTxnDayAmt(quotaAcct.getTxnDayAmt().add(bookInfo.getTxnAmt()));
                            quotaAcct.setTxnMonthAmt(quotaAcct.getTxnMonthAmt().add(bookInfo.getTxnAmt()));
                            quotaAcct.setTxnDayCnt(quotaAcct.getTxnDayCnt() + 1);
                            quotaAcct.setTxnMonthCnt(quotaAcct.getTxnMonthCnt() + 1);
                        } else {
                            quotaAcct.setTxnDayAmt(bookInfo.getTxnAmt());
                            quotaAcct.setTxnDayCnt(1);
                            if (!quotaAcct.getTxnDate().substring(0, 6).equals(bookDate.substring(0, 6))) { //��ͬ��ͬ��
                                quotaAcct.setTxnMonthAmt(bookInfo.getTxnAmt());
                                quotaAcct.setTxnMonthCnt(1);
                            } else {
                                quotaAcct.setTxnMonthAmt(quotaAcct.getTxnMonthAmt().add(bookInfo.getTxnAmt()));
                                quotaAcct.setTxnMonthCnt(quotaAcct.getTxnMonthCnt() + 1);
                            }
                        }
                    }
                    quotaAcctMapper.updateByPrimaryKey(quotaAcct);
                } else {
                    mchtBookFailAmt = mchtBookFailAmt.add(bookInfo.getTxnAmt());
                    mchtBookFailCnt++;
                }

                //ACCT��ˮ��   ��������+��ˮ�� Ψһȷ��һ��
                TqcTxnAcct txn = new TqcTxnAcct();
                TqcTxnAcctExample txnAcctExample = new TqcTxnAcctExample();
                txnAcctExample.createCriteria().andTxnDateEqualTo(bookInfo.getTxnDate()).andTxnSeqnoEqualTo(bookInfo.getTxnSn());
                List<TqcTxnAcct> txnAccts = txnAcctMapper.selectByExample(txnAcctExample);
                if (txnAccts.size() == 1) {
                    txn = txnAccts.get(0);
                    if ("0".equals(txn.getChkStatus())) { //δ��֤ͨ��
                        if ("1".equals(bookInfo.getBookStatus())) {//�������˳ɹ�
                            logger.error("��ˮ��״̬��FTP�ļ�״̬��һ�¡�pkid=" + txn.getPkid());
                            //TODO
                        }
                    } else { //ֻ����֤ͨ������ˮ����״̬����
                        txn.setBookFlag(bookInfo.getBookStatus());
                        txn.setBookTime(sdf.format(new Date()));
                        txnAcctMapper.updateByPrimaryKey(txn);
                    }
                } else {
                    //TODO
                }
            }//end for

            //����mcht quota����
            TqcQuotaMchtKey quotaMchtKey = new TqcQuotaMchtKey();
            quotaMchtKey.setMchtCode(tia.getMchtCode());
            quotaMchtKey.setPrjCode(tia.getPrjCode());
            TqcQuotaMcht quotaMcht = quotaMchtMapper.selectByPrimaryKey(quotaMchtKey);
            if (txnDate.equals(quotaMcht.getTxnDate())) {
                quotaMcht.setTxnDayAmt(quotaMcht.getTxnDayAmt().add(mchtBookSuccAmt));
                quotaMcht.setTxnMonthAmt(quotaMcht.getTxnMonthAmt().add(mchtBookSuccAmt));
                quotaMcht.setTxnDayCnt(quotaMcht.getTxnDayCnt() + mchtBookSuccCnt);
                quotaMcht.setTxnMonthCnt(quotaMcht.getTxnMonthCnt() + mchtBookSuccCnt);
            } else {
                quotaMcht.setTxnDayAmt(mchtBookSuccAmt);
                quotaMcht.setTxnDayCnt(mchtBookSuccCnt);
                if (!quotaMcht.getTxnDate().substring(0, 6).equals(txnDate.substring(0, 6))) { //��ͬ��ͬ��
                    quotaMcht.setTxnMonthAmt(mchtBookSuccAmt);
                    quotaMcht.setTxnMonthCnt(mchtBookSuccCnt);
                } else {
                    quotaMcht.setTxnMonthAmt(quotaMcht.getTxnMonthAmt().add(mchtBookSuccAmt));
                    quotaMcht.setTxnMonthCnt(quotaMcht.getTxnMonthCnt() + mchtBookSuccCnt);
                }
            }

            //����mcht��ˮ����
            TqcTxnMchtMapper txnMchtMapper = session.getMapper(TqcTxnMchtMapper.class);
            TqcTxnMcht txnMcht = new TqcTxnMcht();
            TqcTxnMchtExample txnMchtExample = new TqcTxnMchtExample();
            txnMchtExample.createCriteria().andTxnDateEqualTo(txnDate)
                    .andVchSnEqualTo(tia.getVchSn())
                    .andAcctNoEqualTo(tia.getAcctNo())
                    .andMchtCodeEqualTo(tia.getMchtCode())
                    .andPrjCodeEqualTo(tia.getPrjCode());
            List<TqcTxnMcht> txnMchts = txnMchtMapper.selectByExample(txnMchtExample);
            if (txnMchts == null) {
                //TODO
            } else {
                txnMcht = txnMchts.get(0);
                txnMcht.setTxnCnt(count);
                txnMcht.setBookTime(sdf.format(new Date()));
                txnMcht.setBookFlag("1");
                txnMcht.setTxnSuccAmt(mchtBookSuccAmt);
                txnMcht.setTxnSuccCnt(mchtBookSuccCnt);
                txnMcht.setTxnFailAmt(mchtBookFailAmt);
                txnMcht.setTxnFailCnt(mchtBookFailCnt);
                txnMchtMapper.updateByPrimaryKey(txnMcht);
            }

            //�鷵����Ϣ
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_SUCCESS);
            return cbsRtnInfo;
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            String msg = "[sn=" + hostTxnsn + "] " + "���ݿ⴦��ʧ�ܡ�";
            logger.error(msg, e);
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_ERR);
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
