import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * �Ƶ� ��˰
 * User: zhanrui
 */
public class SocketClient {

    private String ip;
    private int port;
    private String txnCode;


    public byte[] call(byte[] sendbuf) {
        Socket socket = null;
        OutputStream os = null;
        byte[] recvbuf = null;
        try {
            socket = new Socket(ip, port);
            socket.setSoTimeout(60000);

            os = socket.getOutputStream();
            os.write(sendbuf);
            os.flush();

            InputStream is = socket.getInputStream();
            recvbuf = new byte[6];
            int readNum = is.read(recvbuf);
            if (readNum < 6) {
                throw new RuntimeException("��ȡ���ĳ��ȳ���");
            }
            int msgLen = Integer.parseInt(new String(recvbuf).trim());
            recvbuf = new byte[msgLen];

            readNum = is.read(recvbuf);
            if (readNum != msgLen - 6) {
                throw new RuntimeException("��ȡ���ĳ��ȳ���");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert os != null;
                os.close();
                socket.close();
            } catch (IOException e) {
                //
            }
        }
        return recvbuf;
    }

    private String getTxnFile() {
        String txnFile = "txn" + this.txnCode +".txt";
        InputStream usageStream = getClass().getClassLoader().getResourceAsStream(txnFile);

        if (usageStream == null) {
            System.err.println("���ױ����ļ������ڣ�" + txnFile);
            System.exit(1);
        }

        StringBuffer  result = new StringBuffer();
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(usageStream));
            String line;

            while ((line = buf.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buf != null) {
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    //1561070 ���ʵǼ�Ԥ����

    private String getRequestMsg1010() {
        String header = "1.0" +
                new SimpleDateFormat("[yyyyMMdd--HHmmss]").format(new Date())+
                "0000" +
                "1701010" +
                "999999999" +
                "123456789012" +
                "LNK170" +  //userid 6
                "TRCTQC" +  //appid 6
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                "[MAC123456789012345678901234567]";

        String body = "PZH001|" +
                "ACCTNO001|" +
                "MCHT001|" +
                "PRJ001|" +
                "100|";
        return header + body;
    }

    private String getRequestMsg1020() {
        String header = "1.0" +
                new SimpleDateFormat("[yyyyMMdd--HHmmss]").format(new Date())+
                "0000" +
                "1701020" +
                "999999999" +
                "123456789012" +
                "LNK170" +  //userid 6
                "TRCTQC" +  //appid 6
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) +
                "[MAC123456789012345678901234567]";

        String body = "PZH001|" +
                "ACCTNO1234123456789|" +        //��˽
                "MCHT001|" +
                "PRJ001|" +
                "100|";
        return header + body;
    }


    public static void main(String... argv) throws UnsupportedEncodingException, InterruptedException {
        Thread.sleep(3000);
        SocketClient client = new SocketClient();
/*
        System.out.println("" + argv.length);
        if (argv.length < 3) {
            System.out.println("�������ʾ����\n" +
                    "    MockClient -ip=127.0.0.1 -port=60004 -txn=1010");
            System.exit(1);
        }

        for (String arg : argv) {
            if (arg.startsWith("-ip=")) {
                client.ip = arg.substring(4);
                continue;
            }
            if (arg.startsWith("-port=")) {
                client.port = Integer.parseInt(arg.substring(6));
                continue;
            }
            if (arg.startsWith("-txn=")) {
                client.txnCode = arg.substring(5);
                continue;
            }
        }

*/
        //client.getTxnFile();
        client.ip = "127.0.0.1";
        client.port = 60006;
        client.txnCode = "1000";

        String message = client.getRequestMsg1020();
        System.out.printf("===������:%s\n", message);

        int len = message.getBytes("GBK").length;
        String strLen = "" + (len + 6);
        for (int i = strLen.length(); i < 6; i++) {
            strLen += " ";
        }
        byte[] recvbuf = client.call((strLen + message).getBytes("GBK"));
        System.out.printf("===���������ر���:%s\n", new String(recvbuf, "GBK"));


/*
        message = client.getRequestMsg1070();

        len = message.getBytes("GBK").length;
        strLen = "" + (len + 6);
        for (int i = strLen.length(); i < 6; i++) {
            strLen += " ";
        }
        recvbuf = client.call((strLen + message).getBytes("GBK"));
        System.out.printf("���������ر��ģ�%s\n", new String(recvbuf, "GBK"));
*/
    }
}
