
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zhangxinwei on 16/7/1.
 */
public class UDPReceiveWorker implements Runnable{
    DatagramSocket s;

    public UDPReceiveWorker(int port) {
        try {
            s = new DatagramSocket(port);
        }catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {

            byte messageType;
            int len = 0;

            for (;;) {
                byte[] data = new byte[62];
                DatagramPacket p = new DatagramPacket(data, data.length);
                s.receive(p);

                //假设一次完毕
                if (p.getLength() > 0) {
                    len = ByteUtil.getInt(data, 1);
                    if (len > 0 && p.getLength() == 5 + len) {
//                        MessageHandler.parse(data, 5, data[0]);
                        break;
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
