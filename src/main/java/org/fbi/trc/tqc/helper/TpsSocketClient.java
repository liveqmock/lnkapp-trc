package org.fbi.trc.tqc.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * ������������ client
 * User: zhanrui
 * Date: 13-11-27
 */
public class TpsSocketClient {
    private String ip;
    private int port;
    private int timeout = 30000; //Ĭ�ϳ�ʱʱ�䣺ms  ���ӳ�ʱ�����ʱͳһ
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public TpsSocketClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * @param sendbuf:����3�ֽڳ����ֶ� �� 1�ֽڵļ��ܱ�ʶ
     * @return ��Ӧ���Ĳ���3�ֽڵĳ����ֶ�
     * @throws Exception ���У�SocketTimeoutExceptionΪ��ʱ�쳣
     */
    public byte[] call(byte[] sendbuf) throws Exception {
        byte[] recvbuf = null;

        InetAddress addr = InetAddress.getByName(ip);
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(addr, port), timeout);
            socket.setSoTimeout(timeout);

            OutputStream os = socket.getOutputStream();
            os.write(sendbuf);
            os.flush();

            InputStream is = socket.getInputStream();
            recvbuf = new byte[3];
            int readNum = is.read(recvbuf);
            if (readNum == -1) {
                throw new RuntimeException("�����������ѹر�!");
            }
            if (readNum < 3) {
                throw new RuntimeException("��ȡ����ͷ���Ȳ��ִ���...");
            }
            int msgLen = Integer.parseInt(new String(recvbuf).trim());
            logger.info("����ͷָʾ����:" + msgLen);
            recvbuf = new byte[msgLen - 3];

            //TODO���в������������Ӳ��ȶ� ����ʱһ��ʱ��
            Thread.sleep(500);

            readNum = is.read(recvbuf);   //������
            if (readNum != msgLen - 3) {
                throw new RuntimeException("���ĳ��ȴ���,����ͷָʾ����:[" + msgLen + "], ʵ�ʻ�ȡ����:[" + readNum + "]");
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                //
            }
        }
        return recvbuf;
    }


    public static void main(String... argv) throws UnsupportedEncodingException {
        TpsSocketClient mock = new TpsSocketClient("127.0.0.1", 2308);

        String msg = "..........";

        String strLen = null;
        strLen = "" + (msg.getBytes("GBK").length + 4);
        String lpad = "";
        for (int i = 0; i < 3 - strLen.length(); i++) {
            lpad += "0";
        }
        strLen = lpad + strLen;


        byte[] recvbuf = new byte[0];
        try {
            recvbuf = mock.call(("0" + strLen + msg).getBytes("GBK"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.printf("���������أ�%s\n", new String(recvbuf, "GBK"));
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
