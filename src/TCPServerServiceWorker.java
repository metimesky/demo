
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by zhangxinwei on 6/9/16.
 */
public class TCPServerServiceWorker implements Runnable{
    ServerSocket s;

    public TCPServerServiceWorker(int port) {
        try {
            s = new ServerSocket(port);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
//        try {
//            for (;;) {
//                Socket sc = s.accept();
//                if (sc == null) continue;
//
//                AGVManager.e.submit(new AsyncReceiveData(sc));
//            }
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    class AsyncReceiveData implements Runnable {
//        Socket sc;
//
//        public AsyncReceiveData(Socket sc) { sc = sc;}
//
//
//        @Override
//        public void run() {
//            try{
//                InputStream in = sc.getInputStream();
//                // 报文的第一个字节，标识消息的类型
//                byte[] buffer = new byte[1];
//                byte messageType;
//                int res;
//                // 分配四个字节存储数据长度
//                ByteBuffer bb = ByteBuffer.allocate(4);
//                int count = 0;
//
//                // 获得消息类型
//                while (true) {
//                    // 读到1个byte数据，数据为消息体的标识
//                    res = in.read(buffer);
//
//                    if (res != -1) {
//                        // 获得消息类型
//                        messageType = buffer[0];
//                        // 跳出循环，准备接受四个字节的数据体长度
//                        break;
//                    } else {
//                        // 如果没有消息输入，线程暂停0.1秒
////                    try {
////                        Thread.sleep(100);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
//                    }
//                }
//
//                // 获得消息长度
//                bb.clear();
//                count = 0;
//                while (true) {
//                    count++;
//                    res = in.read(buffer);
//                    if (res != -1) {
//                        bb.put(buffer);
//                    }
//                    if (count == 4) {
//                        break;
//                    }
//                }
//                // 将byte[]存储的数据长度转为int类型，并根据其构建一个ByteBuffer
//                int realData = ByteUtil.getInt(bb.array(), 0);
//                ByteBuffer rbb = ByteBuffer.allocate(realData);
//                count = 0;
//                // 读出指定长度的数据
//                while (true) {
//                    count++;
//                    res = in.read(buffer);
//                    if (res != -1) {
//                        rbb.put(buffer);
//                    }
//                    if (count == realData) {
//                        break;
//                    }
//                }
//
//                MessageHandler.parse(rbb.array(), messageType);
//            }catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
