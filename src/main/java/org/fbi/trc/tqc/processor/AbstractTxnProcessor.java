package org.fbi.trc.tqc.processor;

import org.apache.commons.lang.StringUtils;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10Processor;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.trc.tqc.enums.TxnRtnCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * User: zhanrui
 * Date: 2014-10-19
 */
public abstract class AbstractTxnProcessor extends Stdp10Processor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnCode = request.getHeader("txnCode");
        String tellerId = request.getHeader("tellerId");
        String hostTxnsn = request.getHeader("serialNo");

        if (StringUtils.isEmpty(tellerId)) {
            tellerId = "TELLERID";
        }

        try {
            MDC.put("txnCode", txnCode);
            MDC.put("tellerId", tellerId);
            logger.info("CBS Request:" + "[sn=" + hostTxnsn + "]\n" + request.toString());
            doRequest(request, response);
            logger.info("CBS Response:"+ "[sn=" + hostTxnsn + "]\n"  + response.toString());
        }catch (Exception e){
            response.setHeader("rtnCode", TxnRtnCode.TXN_ERR.getCode());
            throw new RuntimeException(e);
        } finally {
            MDC.remove("txnCode");
            MDC.remove("tellerId");
        }
    }

    abstract protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException;

    //打包cbs响应报文
    protected void marshalCbsResponse(TxnRtnCode txnRtnCode, String errMsg, Stdp10ProcessorResponse response) {
        if (StringUtils.isEmpty(errMsg)) {
            errMsg = txnRtnCode.getTitle();
        }
        response.setHeader("rtnCode", txnRtnCode.getCode());
        try {
            response.setResponseBody(errMsg.getBytes(response.getCharacterEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("编码错误", e);
        }
    }
}
