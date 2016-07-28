
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Created by zhangxinwei on 6/9/16.
 */
public class UDPSender {

    static Logger log = Logger.getLogger(UDPSender.class.getName());

    static DatagramSocket s;

    static {
        try {
            s = new DatagramSocket(new InetSocketAddress("", 0));
        }catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void send(byte[] data) {
        log.info("发送udp数据, 长度: "+data.length+" 字节");
        DatagramPacket p = new DatagramPacket(data, data.length);
        try {
            s.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
