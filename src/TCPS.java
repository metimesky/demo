
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Created by zhangxinwei on 16/7/16.
 */
public class TCPS {

    static Logger logger = Logger.getLogger(TCPS.class.getName());


    public static byte[] recv() {
        try {
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress("127.0.0.1", 8080));

            ByteBuffer buf = ByteBuffer.allocate(1024);

            while (true) {
                buf.clear();

                Socket s = ss.accept();
                InputStream is = s.getInputStream();

                int readCount = 0;
                while (true) {
                    int nextByte = is.read();
                    if (nextByte == -1) {
                        logger.info("已读至尾");
                        break;
                    }

                    buf.put((byte) nextByte);
                    readCount++;
                }

                buf.flip();
                byte[] recvData = new byte[readCount+1];
                System.arraycopy(buf.array(), 0, recvData, 0, readCount);
                logger.info(new String(recvData));
                logger.info("test".getBytes().length + "");

                return recvData;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param reqData 请求体
     * @param RetLen 体长
     * @param preReq 重发次数
     * @return
     */
    public static byte[] writeWithRet(byte[] reqData, int RetLen, int preReq) {

        try {
            Socket s = new Socket("127.0.0.1", 8080);
            s.setSoTimeout(3000);

            if (!s.isConnected()) {
                logger.info("连接失败");
                return null;
            }
            OutputStream os = s.getOutputStream();
            os.write(reqData);
            os.flush();

            InputStream is = s.getInputStream();
            ByteBuffer buf = ByteBuffer.allocate(RetLen);

            int failed = 0;
            int count = 0;
            while (count < RetLen && failed < preReq) {
                int nextByte = 0;
                try{
                   nextByte = is.read();
                }catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    failed++;
                    logger.info("未读取到数据,重发");
                    continue;
                }


                if (nextByte == -1) {
                    logger.info("已读至尾");
                    break;
                }

                buf.put((byte) nextByte);
            }

            s.close();
            return buf.array();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void write(byte[] data) {
        try {
            Socket s = new Socket("127.0.0.1", 8080);
            s.setSoTimeout(3000);

            if (!s.isConnected()) {
                logger.info("连接失败");
            }
            OutputStream os = s.getOutputStream();
            os.write(data);
            os.flush();
            os.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        TCPS.writeWithRet("tests".getBytes(), 10, 3);
    }
}
