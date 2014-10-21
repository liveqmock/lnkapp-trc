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
 * 1071030扣款结果处理
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

        //FTP文件检查  总分核对 并与流水总金额比对！

        //是否重复交易

        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            List<HostBookInfo> bookInfos = null;
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
                if ("1".equals(bookInfo.getBookStatus())) {//记账成功
                    mchtBookSuccAmt = mchtBookSuccAmt.add(bookInfo.getTxnAmt());
                    mchtBookSuccCnt++;
                    //ACCT quota表
                    TqcQuotaAcctKey quotaAcctKey = new TqcQuotaAcctKey();
                    quotaAcctKey.setMchtCode(tia.getMchtCode());
                    quotaAcctKey.setPrjCode(tia.getPrjCode());
                    TqcQuotaAcct quotaAcct = quotaAcctMapper.selectByPrimaryKey(quotaAcctKey);
                    if (quotaAcct == null) {
                        //TODO
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
                } else {
                    mchtBookFailAmt = mchtBookFailAmt.add(bookInfo.getTxnAmt());
                    mchtBookFailCnt++;
                }

                //ACCT流水表   交易日期+流水号 唯一确定一笔
                TqcTxnAcct txn = new TqcTxnAcct();
                TqcTxnAcctExample txnAcctExample = new TqcTxnAcctExample();
                txnAcctExample.createCriteria().andTxnDateEqualTo(bookInfo.getTxnDate()).andTxnSeqnoEqualTo(bookInfo.getTxnSn());
                List<TqcTxnAcct> txnAccts = txnAcctMapper.selectByExample(txnAcctExample);
                if (txnAccts.size() == 1) {
                    txn = txnAccts.get(0);
                    if ("0".equals(txn.getChkStatus())) { //未验证通过
                        if ("1".equals(bookInfo.getBookStatus())) {//主机记账成功
                            logger.error("流水表状态与FTP文件状态不一致。pkid=" + txn.getPkid());
                            //TODO
                        }
                    } else { //只对验证通过的流水进行状态更新
                        txn.setBookFlag(bookInfo.getBookStatus());
                        txn.setBookTime(sdf.format(new Date()));
                        txnAcctMapper.updateByPrimaryKey(txn);
                    }
                } else {
                    //TODO
                }
            }//end for

            //更新mcht quota数据
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
                if (!quotaMcht.getTxnDate().substring(0, 6).equals(txnDate.substring(0, 6))) { //非同年同月
                    quotaMcht.setTxnMonthAmt(mchtBookSuccAmt);
                    quotaMcht.setTxnMonthCnt(mchtBookSuccCnt);
                } else {
                    quotaMcht.setTxnMonthAmt(quotaMcht.getTxnMonthAmt().add(mchtBookSuccAmt));
                    quotaMcht.setTxnMonthCnt(quotaMcht.getTxnMonthCnt() + mchtBookSuccCnt);
                }
            }

            //更新mcht流水数据
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

            //组返回信息
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_SUCCESS);
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
}
