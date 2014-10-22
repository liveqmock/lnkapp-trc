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
 * 1071030扣款结果处理
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

        List<HostBookInfo> bookInfos = null;
        try {
            bookInfos = readFileByLines(ProjectConfigManager.getInstance().getProperty("ftp_root_dir") + "/" + tia.getFileName());
        } catch (FileNotFoundException e) {
            response.setHeader("rtnCode", TxnRtnCode.FTP_FILE_NOTEXIST.getCode());
            return;
        }
        //FTP文件检查  总分核对 并与流水总金额比对！


        //是否重复交易

        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, txnDate, hostTxnsn, bookInfos);
            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常");
        }

    }

    //解包生成CBS请求报文BEAN
    private CbsTia1030 unmarshalCbsRequestMsg(byte[] body) throws Exception {
        CbsTia1030 tia = new CbsTia1030();
        SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
        tia = (CbsTia1030) dataFormat.fromMessage(new String(body, "GBK"), "CbsTia1030");
        return tia;
    }

    /*
    处理Acct Quota表
    处理Mcht Quota表
    处理Acct Txn表
    处理Mcht Txn表
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

            //检查重复交易
            if (isRepeatTxn(session,tia,txnDate)) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.FTP_FILE_REPEAT);
                cbsRtnInfo.setRtnMsg("");
                return cbsRtnInfo;
            }

            for (HostBookInfo bookInfo : bookInfos) {
                count++;
                if ("1".equals(bookInfo.getBookStatus())) {//记账成功
                    mchtBookSuccAmt = mchtBookSuccAmt.add(bookInfo.getTxnAmt());
                    mchtBookSuccCnt++;
                    updateAcctQuota(session, tia, bookInfo);
                } else {
                    mchtBookFailAmt = mchtBookFailAmt.add(bookInfo.getTxnAmt());
                    mchtBookFailCnt++;
                }

                //ACCT流水表   交易日期+流水号 唯一确定一笔
                updateAcctTxn(session, bookInfo, sdf);
            }

            //更新mcht quota数据
            updateMchtQuota(session, tia, txnDate, mchtBookSuccAmt, mchtBookSuccCnt);

            //更新mcht流水数据
            updateMchtTxn(session, tia, txnDate, mchtBookSuccAmt, mchtBookFailAmt, mchtBookSuccCnt, mchtBookFailCnt, count, sdf);

            //组返回信息
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_SUCCESS);
            session.commit();
            return cbsRtnInfo;
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            String msg = "[sn=" + hostTxnsn + "] " + "数据库处理失败。";
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
            throw new IllegalArgumentException("文件名不能为空.");
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
                logger.error("文件处理错误", e);
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
            throw new RuntimeException("未找到对应的单位流水TqcTxnMcht记录");
        } else {
            txnMcht = txnMchts.get(0);
            return "1".equals(txnMcht.getBookFlag());
        }
    }

    //更新账号限额累计表
    private void updateAcctQuota(SqlSession session, CbsTia1030 tia, HostBookInfo bookInfo) {
        TqcQuotaAcctMapper quotaAcctMapper = session.getMapper(TqcQuotaAcctMapper.class);
        TqcQuotaAcctKey quotaAcctKey = new TqcQuotaAcctKey();
        quotaAcctKey.setMchtCode(tia.getMchtCode());
        quotaAcctKey.setPrjCode(tia.getPrjCode());
        quotaAcctKey.setAcctNo(bookInfo.getAcctNo());
        TqcQuotaAcct quotaAcct = quotaAcctMapper.selectByPrimaryKey(quotaAcctKey);
        if (quotaAcct == null) {
            throw new RuntimeException("FTP 结果文件中明细记录的账号有误");
        } else {
            String bookDate = bookInfo.getTxnDate();
            if (quotaAcct.getTxnDate().equals(bookDate)) { //同一天
                quotaAcct.setTxnDayAmt(quotaAcct.getTxnDayAmt().add(bookInfo.getTxnAmt()));
                quotaAcct.setTxnMonthAmt(quotaAcct.getTxnMonthAmt().add(bookInfo.getTxnAmt()));
                quotaAcct.setTxnDayCnt(quotaAcct.getTxnDayCnt() + 1);
                quotaAcct.setTxnMonthCnt(quotaAcct.getTxnMonthCnt() + 1);
            } else {
                quotaAcct.setTxnDayAmt(bookInfo.getTxnAmt());
                quotaAcct.setTxnDayCnt(1);
                if (!quotaAcct.getTxnDate().substring(0, 6).equals(bookDate.substring(0, 6))) { //非同年同月
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

    //更新账号流水表
    private void updateAcctTxn(SqlSession session, HostBookInfo bookInfo, SimpleDateFormat sdf) {
        TqcTxnAcctMapper txnAcctMapper = session.getMapper(TqcTxnAcctMapper.class);
        TqcTxnAcctExample txnAcctExample = new TqcTxnAcctExample();
        txnAcctExample.createCriteria().andTxnDateEqualTo(bookInfo.getTxnDate()).andTxnSeqnoEqualTo(bookInfo.getTxnSn());
        List<TqcTxnAcct> txnAccts = txnAcctMapper.selectByExample(txnAcctExample);
        if (txnAccts.size() == 1) {
            TqcTxnAcct txn = txnAccts.get(0);
            if ("0".equals(txn.getChkStatus())) { //未验证通过
                if ("1".equals(bookInfo.getBookStatus())) {//主机记账成功
                    throw new RuntimeException("流水表状态与FTP文件状态不一致。pkid=" + txn.getPkid());
                }
            } else { //只对验证通过的流水进行状态更新
                txn.setBookFlag(bookInfo.getBookStatus());
                txn.setBookTime(sdf.format(new Date()));
                txnAcctMapper.updateByPrimaryKey(txn);
            }
        } else {
            throw new RuntimeException("TqcTxnAcct数据不唯一");
        }
    }

    //更新 单位限额累计表
    private void updateMchtQuota(SqlSession session, CbsTia1030 tia, String txnDate, BigDecimal mchtBookSuccAmt, int mchtBookSuccCnt) {
        TqcQuotaMchtMapper quotaMchtMapper = session.getMapper(TqcQuotaMchtMapper.class);
        TqcQuotaMchtKey quotaMchtKey = new TqcQuotaMchtKey();
        quotaMchtKey.setMchtCode(tia.getMchtCode());
        quotaMchtKey.setPrjCode(tia.getPrjCode());
        TqcQuotaMcht quotaMcht = quotaMchtMapper.selectByPrimaryKey(quotaMchtKey);
        if (quotaMcht == null) {
            throw new RuntimeException("更新mcht quota数据错误");
        } else {
            if (txnDate.equals(quotaMcht.getTxnDate())) {
                quotaMcht.setTxnDayAmt(quotaMcht.getTxnDayAmt().add(mchtBookSuccAmt));
                quotaMcht.setTxnMonthAmt(quotaMcht.getTxnMonthAmt().add(mchtBookSuccAmt));
                quotaMcht.setTxnDayCnt(quotaMcht.getTxnDayCnt() + mchtBookSuccCnt);
                quotaMcht.setTxnMonthCnt(quotaMcht.getTxnMonthCnt() + mchtBookSuccCnt);
            } else {
                quotaMcht.setTxnDayAmt(mchtBookSuccAmt);
                quotaMcht.setTxnDayCnt(mchtBookSuccCnt);
                if (!quotaMcht.getTxnDate().substring(0, 6).equals(txnDate.substring(0, 6))) { //非同年同月
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

    //更新单位流水表
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
            throw new RuntimeException("未找到对应的单位流水TqcTxnMcht记录");
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
