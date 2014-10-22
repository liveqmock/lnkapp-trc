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
import org.fbi.trc.tqc.helper.ProjectConfigManager;
import org.fbi.trc.tqc.repository.dao.TqcQuotaAcctMapper;
import org.fbi.trc.tqc.repository.dao.TqcQuotaMchtMapper;
import org.fbi.trc.tqc.repository.dao.TqcTxnAcctMapper;
import org.fbi.trc.tqc.repository.dao.TqcTxnMchtMapper;
import org.fbi.trc.tqc.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        String txnDate = request.getHeader("txnTime").substring(0, 8);
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

        List<HostBookInfo> bookInfos = null;
        try {
            bookInfos = readFileByLines(ProjectConfigManager.getInstance().getProperty("ftp_root_dir") + "/" + tia.getFileName());
        } catch (FileNotFoundException e) {
            response.setHeader("rtnCode", TxnRtnCode.FTP_FILE_NOTEXIST.getCode());
            return;
        }
        //FTP�ļ����  �ֺܷ˶� ������ˮ�ܽ��ȶԣ�


        //�Ƿ��ظ�����

        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
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

        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        BigDecimal mchtBookSuccAmt = new BigDecimal("0");
        BigDecimal mchtBookFailAmt = new BigDecimal("0");
        int mchtBookSuccCnt = 0;
        int mchtBookFailCnt = 0;
        int count = 0;

        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);

            //����ظ�����
            if (isRepeatTxn(session,tia,txnDate)) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.FTP_FILE_REPEAT);
                cbsRtnInfo.setRtnMsg("");
                return cbsRtnInfo;
            }

            for (HostBookInfo bookInfo : bookInfos) {
                count++;
                if ("1".equals(bookInfo.getBookStatus())) {//���˳ɹ�
                    mchtBookSuccAmt = mchtBookSuccAmt.add(bookInfo.getTxnAmt());
                    mchtBookSuccCnt++;
                    updateAcctQuota(session, tia, bookInfo);
                } else {
                    mchtBookFailAmt = mchtBookFailAmt.add(bookInfo.getTxnAmt());
                    mchtBookFailCnt++;
                }

                //ACCT��ˮ��   ��������+��ˮ�� Ψһȷ��һ��
                updateAcctTxn(session, bookInfo, sdf);
            }

            //����mcht quota����
            updateMchtQuota(session, tia, txnDate, mchtBookSuccAmt, mchtBookSuccCnt);

            //����mcht��ˮ����
            updateMchtTxn(session, tia, txnDate, mchtBookSuccAmt, mchtBookFailAmt, mchtBookSuccCnt, mchtBookFailCnt, count, sdf);

            //�鷵����Ϣ
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_SUCCESS);
            session.commit();
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

    private List<HostBookInfo> readFileByLines(String fileName) throws IOException {
        List<HostBookInfo> bookInfos = new ArrayList<>();
        if (fileName == null || "".equals(fileName)) {
            throw new IllegalArgumentException("�ļ�������Ϊ��.");
        }
        File file = new File(fileName);
        BufferedReader br = null;

        br = new BufferedReader(new FileReader(file));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\\|");
                HostBookInfo bookInfo = new HostBookInfo();
                bookInfo.setTxnDate(fields[0]);
                bookInfo.setTxnSn(fields[1]);
                bookInfo.setAcctNo(fields[2]);
                bookInfo.setTxnAmt(new BigDecimal(fields[3].trim()));
                bookInfo.setBookStatus(fields[4]);
                bookInfos.add(bookInfo);
            }
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.error("�ļ��������", e);
            }
        }
        return bookInfos;
    }


    private boolean isRepeatTxn(SqlSession session, CbsTia1030 tia, String txnDate){
        TqcTxnMchtMapper txnMchtMapper = session.getMapper(TqcTxnMchtMapper.class);
        TqcTxnMcht txnMcht;
        TqcTxnMchtExample txnMchtExample = new TqcTxnMchtExample();
        txnMchtExample.createCriteria().andTxnDateEqualTo(txnDate)
                .andVchSnEqualTo(tia.getVchSn())
                .andAcctNoEqualTo(tia.getAcctNo())
                .andMchtCodeEqualTo(tia.getMchtCode())
                .andPrjCodeEqualTo(tia.getPrjCode());
        List<TqcTxnMcht> txnMchts = txnMchtMapper.selectByExample(txnMchtExample);
        if (txnMchts.size() == 0) {
            throw new RuntimeException("δ�ҵ���Ӧ�ĵ�λ��ˮTqcTxnMcht��¼");
        } else {
            txnMcht = txnMchts.get(0);
            return "1".equals(txnMcht.getBookFlag());
        }
    }

    //�����˺��޶��ۼƱ�
    private void updateAcctQuota(SqlSession session, CbsTia1030 tia, HostBookInfo bookInfo) {
        TqcQuotaAcctMapper quotaAcctMapper = session.getMapper(TqcQuotaAcctMapper.class);
        TqcQuotaAcctKey quotaAcctKey = new TqcQuotaAcctKey();
        quotaAcctKey.setMchtCode(tia.getMchtCode());
        quotaAcctKey.setPrjCode(tia.getPrjCode());
        quotaAcctKey.setAcctNo(bookInfo.getAcctNo());
        TqcQuotaAcct quotaAcct = quotaAcctMapper.selectByPrimaryKey(quotaAcctKey);
        if (quotaAcct == null) {
            throw new RuntimeException("FTP ����ļ�����ϸ��¼���˺�����");
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
    }

    //�����˺���ˮ��
    private void updateAcctTxn(SqlSession session, HostBookInfo bookInfo, SimpleDateFormat sdf) {
        TqcTxnAcctMapper txnAcctMapper = session.getMapper(TqcTxnAcctMapper.class);
        TqcTxnAcctExample txnAcctExample = new TqcTxnAcctExample();
        txnAcctExample.createCriteria().andTxnDateEqualTo(bookInfo.getTxnDate()).andTxnSeqnoEqualTo(bookInfo.getTxnSn());
        List<TqcTxnAcct> txnAccts = txnAcctMapper.selectByExample(txnAcctExample);
        if (txnAccts.size() == 1) {
            TqcTxnAcct txn = txnAccts.get(0);
            if ("0".equals(txn.getChkStatus())) { //δ��֤ͨ��
                if ("1".equals(bookInfo.getBookStatus())) {//�������˳ɹ�
                    throw new RuntimeException("��ˮ��״̬��FTP�ļ�״̬��һ�¡�pkid=" + txn.getPkid());
                }
            } else { //ֻ����֤ͨ������ˮ����״̬����
                txn.setBookFlag(bookInfo.getBookStatus());
                txn.setBookTime(sdf.format(new Date()));
                txnAcctMapper.updateByPrimaryKey(txn);
            }
        } else {
            throw new RuntimeException("TqcTxnAcct���ݲ�Ψһ");
        }
    }

    //���� ��λ�޶��ۼƱ�
    private void updateMchtQuota(SqlSession session, CbsTia1030 tia, String txnDate, BigDecimal mchtBookSuccAmt, int mchtBookSuccCnt) {
        TqcQuotaMchtMapper quotaMchtMapper = session.getMapper(TqcQuotaMchtMapper.class);
        TqcQuotaMchtKey quotaMchtKey = new TqcQuotaMchtKey();
        quotaMchtKey.setMchtCode(tia.getMchtCode());
        quotaMchtKey.setPrjCode(tia.getPrjCode());
        TqcQuotaMcht quotaMcht = quotaMchtMapper.selectByPrimaryKey(quotaMchtKey);
        if (quotaMcht == null) {
            throw new RuntimeException("����mcht quota���ݴ���");
        } else {
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
            quotaMchtMapper.updateByPrimaryKey(quotaMcht);
        }
    }

    //���µ�λ��ˮ��
    private void updateMchtTxn(SqlSession session, CbsTia1030 tia, String txnDate,
                               BigDecimal mchtBookSuccAmt, BigDecimal mchtBookFailAmt,
                               int mchtBookSuccCnt, int mchtBookFailCnt,
                               int count,
                               SimpleDateFormat sdf) {
        TqcTxnMchtMapper txnMchtMapper = session.getMapper(TqcTxnMchtMapper.class);
        TqcTxnMcht txnMcht;
        TqcTxnMchtExample txnMchtExample = new TqcTxnMchtExample();
        txnMchtExample.createCriteria().andTxnDateEqualTo(txnDate)
                .andVchSnEqualTo(tia.getVchSn())
                .andAcctNoEqualTo(tia.getAcctNo())
                .andMchtCodeEqualTo(tia.getMchtCode())
                .andPrjCodeEqualTo(tia.getPrjCode());
        List<TqcTxnMcht> txnMchts = txnMchtMapper.selectByExample(txnMchtExample);
        if (txnMchts.size() == 0) {
            throw new RuntimeException("δ�ҵ���Ӧ�ĵ�λ��ˮTqcTxnMcht��¼");
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
    }

}
