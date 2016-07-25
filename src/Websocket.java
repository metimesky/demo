package ws;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64EncoderStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by zhangxinwei on 7/23/16.
 * websocket协议实现
 * 摘于https://github.com/blinkdog/websocket
 */
public class Websocket {


    public void start(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);

            for (;;) {
                new Worker(ss.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Websocket().start(8080);
    }
}

class Worker extends Thread {

    boolean isHandshake;
    Socket s;
    String requestUri;
    boolean finished;

    public Worker(Socket clientSocket) {
        s = clientSocket;
    }

    @Override
    public void run() {
        try {
            handshake();
            recv();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handshake() throws IOException {
        String line = readLine();
        String[] requestLine = line.split(" ");
        if (requestLine.length < 2)
            throw new IOException("Wrong Request-Line format: " + line);
        requestUri = requestLine[1];
        Boolean upgrade = false, connection = false;
        Long[] keys = new Long[1];

        Map<String, String> kv = new HashMap<>();
        while (!(line = readLine()).equals("")) {
            String[] parts = line.split(": ", 2);
            if (parts.length != 2)
                throw new IOException("Wrong field format: " + line);
            String name = parts[0];
            String value = parts[1];

            kv.put(name, value);
        }

        isHandshake = checkStartsWith(requestLine[0], "GET")
                && checkContains(requestLine[2], "HTTP/")
                && kv.get("Host") != null
                && checkContains(kv.get("Upgrade"), "websocket")
                && checkContains(kv.get("Connection"), "Upgrade")
                && "13".equals(kv.get("Sec-WebSocket-Version"))
                && kv.get("Sec-WebSocket-Key") != null;
        String nonce = kv.get("Sec-WebSocket-Key");
        String acceptKey = sha1ForWS(nonce);

        // if we have met all the requirements
        if (isHandshake) {
            OutputStream os = s.getOutputStream();
            os.write("HTTP/1.1 101 Switching Protocols\r\n".getBytes());
            os.write("Upgrade: websocket\r\n".getBytes());
            os.write("Connection: upgrade\r\n".getBytes());
            os.write("Sec-WebSocket-Accept: ".getBytes());
            os.write(acceptKey.getBytes());
            os.write("\r\n\r\n".getBytes());
            os.flush();
        }

        return;
    }

    public static String sha1ForWS(String nonce) {
        try {
            MessageDigest md= MessageDigest.getInstance("SHA-1");

            byte[] b1 = nonce.getBytes();
            md.update(b1, 0, b1.length);

            byte[] b2 = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11".getBytes();
            md.update(b2, 0, b2.length);

            byte[] b4 = new byte[b1.length];
            b4 = md.digest();

            return new String(BASE64EncoderStream.encode(b4));
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void recv() throws IOException {
        long payloadLength = 0;
        int[] maskingBytes = new int[4];
        int maskingIndex = 0;

        for (;;) {
            InputStream in = s.getInputStream();
            // byte 0: flags and opcode
            int flagOps = in.read();
            if ((flagOps & 0x70) != 0x00) {
                return;
            }
            int opcode = flagOps & 0x0f;
            if (opcode >= 0x03 && opcode <= 0x07) {
                return;
            }
            if (opcode >= 0x0b) {
                return;
            }
            boolean finalFragment = (flagOps & 0x80) == 0x80;
            boolean controlOpcode = (flagOps & 0x08) == 0x08;
            if (controlOpcode && !finalFragment) {
                return;
            }
            // byte 1: masking and payload length
            int maskPayload = in.read();
            boolean masked = (maskPayload & 0x80) == 0x80;
            if (!masked) {
                return;
            }
            int payloadSize = maskPayload & 0x7f;
            // byte 2-9: extended payload length, if specified
            if (payloadSize == 0x7e) {
                if (controlOpcode) {
                    return;
                }
                payloadLength = (in.read() << 8) | (in.read());
                if (payloadLength < 126) {
                    return;
                }
            } else if (payloadSize == 0x7f) {
                if (controlOpcode) {
                    return;
                }
                payloadLength = 0L;
                for (int i = 0; i < 8; i++) {
                    payloadLength |= in.read() << (8 - 1 - i) * 8;
                }
                if (payloadLength < 0x10000) {
                    return;
                }
            } else {
                payloadLength = payloadSize;
            }
            // byte 10-13: masking key
            for (int i = 0; i < 4; i++) {
                maskingBytes[i] = in.read();
            }
            maskingIndex = 0;
            // if this is a control opcode; handle the control frame
            if (opcode == 0x08) {
//                handleCloseFrame();
//                System.out.println("close");
                in.close();
            }
            if (opcode == 0x09) {
//                handlePingFrame();
                byte[] payload = new byte[(int) payloadLength];
                int count = 0;
                while (payloadLength > 0L) {
                    payload[count] = (byte) in.read();
                    count++;
                }
                writePong(payload);
            }
            if (opcode == 0x0a) {
//                handlePongFrame();
                byte[] payload = new byte[(int) payloadLength];
                int count = 0;
                while (payloadLength > 0L) {
                    payload[count] = (byte) in.read();
                    count++;
                }
            }


            byte[] data = new byte[(int)payloadLength];
//            System.out.println(payloadLength);
            for (int i = (int)(payloadLength - 1); i >= 0; i--) {
                int byteData = in.read() ^ maskingBytes[maskingIndex];
                maskingIndex++;
                maskingIndex &= 0x03;
                data[(int)(payloadLength - 1 - i)] = (byte) byteData;
            }

            send(data);
        }
    }

    /**
     * Write a Close control frame to the WebSocket.
     * @throws IOException if an I/O error occurs
     */
    public final void writeClose() throws IOException {
        OutputStream outputStream = s.getOutputStream();
        outputStream.write(new byte[] {
                (byte) 0x88, (byte) 0x00
        });
    }

    /**
     * Write a Close control frame to the WebSocket.
     * @param statusCode status code indicating the reason for the closure
     *                   of the WebSocket; constants defined in RFC 6455
     * @throws IOException if an I/O error occurs
     */
    public final void writeClose(final int statusCode) throws IOException {
        OutputStream outputStream = s.getOutputStream();

        outputStream.write(new byte[] {
                (byte) 0x88, (byte) 0x02,
                (byte) ((statusCode & 0x0000ff00) >> 8),
                (byte) (statusCode & 0x000000ff)
        });
    }

    /**
     * Write a Pong control frame to the WebSocket. Uses the provided data
     * as the payload data of the control frame.
     * @param pongPayload byte array containing payload data for the pong frame
     * @throws IOException if an I/O error occurs
     */
    public final void writePong(final byte[] pongPayload) throws IOException {
        OutputStream outputStream = s.getOutputStream();

        outputStream.write(new byte[] {
                (byte) 0x8a, (byte) (pongPayload.length)
        });
        outputStream.write(pongPayload);
    }

    public void send(byte[] data) {
        int utfLength = data.length;
        OutputStream outputStream = null;
        try {
            outputStream = s.getOutputStream();
            outputStream.write(0x81); // final text-frame
            if (utfLength < 126) {
                outputStream.write(utfLength); // small payload length
            } else if (utfLength < 0x10000) {
                outputStream.write(0x7e); // medium payload flag
                outputStream.write((utfLength & 0x0000ff00) >> 8);
                outputStream.write(utfLength & 0x000000ff);
            } else {
                outputStream.write(0x7f); // large payload flag
                outputStream.write(0x00); // upper bytes
                outputStream.write(0x00); // upper bytes
                outputStream.write(0x00); // upper bytes
                outputStream.write(0x00); // upper bytes
                outputStream.write((utfLength & 0x7f000000) >> 24);
                outputStream.write((utfLength & 0x00ff0000) >> 16);
                outputStream.write((utfLength & 0x0000ff00) >> 8);
                outputStream.write(utfLength & 0x000000ff);
            }
            outputStream.write(data); // text payload
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkContains(final String s1, final String s2) {
        if (s1 == null) {
            return false;
        }
        return s1.contains(s2);
    }

    public static boolean checkStartsWith(final String s1, final String s2) {
        if (s1 == null) {
            return false;
        }
        return s1.startsWith(s2);
    }

    /**
     * Reads line (terminated by "\r\n" - 0x0D 0x0A) from the socket.
     *
     * @return line that was read
     * @throws IOException
     *             thrown when an error occurs while reading
     */
    protected String readLine() throws IOException {
        Vector<Byte> line = new Vector<Byte>();

        InputStream in = s.getInputStream();
        Integer last = in.read();
        if (last.equals(-1))
            throw new IOException("End of stream");
        Integer current = in.read();
        while (!((last.equals(0x0D)) && (current.equals(0x0A)))) {
            if (current.equals(-1))
                throw new IOException("End of stream");
            line.add(last.byteValue());
            last = current;
            current = in.read();
        }
        return byteCollectionToString(line);
    }

    /**
     * Creates a string from given byte collection.
     *
     * @param collection
     *            collection to be converted
     * @return string made from the byte collection
     */
    private String byteCollectionToString(Collection<Byte> collection) {
        byte[] byteArray = new byte[collection.size()];
        Integer i = 0;
        for (Iterator<Byte> iterator = collection.iterator(); iterator
                .hasNext();) {
            byteArray[i++] = iterator.next();
        }
        return new String(byteArray, Charset.forName("UTF-8"));
    }
}