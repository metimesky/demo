
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by zhangxinwei on 16/6/20.
 *
 * 解析post content
 */
public class RequestContent {
    private Map<String, Object> params = new HashMap<>();
    private ByteReader br;

    public RequestContent(HttpExchange exchange) throws IOException {
        InputStream is;
        String c_type;
        is = exchange.getRequestBody();
        c_type = exchange.getRequestHeaders().get("Content-Type").get(0);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes = null;

        for (;;) {
            byte[] bs = new byte[1024];
            int count = is.read(bs);
            bos.write(bs);
            if (count == -1) {
                break;
            }
        }

        bytes = bos.toByteArray();
        br = new ByteReader(bytes);

        //parse
        int index = c_type.indexOf("boundary=");
        if (index == -1) {
            byte[] b = br.readLine();
            if (b != null && b.length > 0) {
                String content = new String(b);
                if (content != null && content.length() > 0) {
                    String[] kvs = content.split("&");
                    for (String string : kvs) {
                        String[] kv = string.split("=");
                        if (kv.length == 1) {
                            ArrayList<String> al = (ArrayList<String>)params.get(kv[0]);
                            if(al == null) {
                                al = new ArrayList<>(3);
                                al.add(null);
                                params.put(kv[0], al);
                            }else {
                                al.add(null);
                            }
                        } else if (kv.length == 2) {
                            ArrayList<String> al = (ArrayList<String>)params.get(kv[0]);
                            if(al == null) {
                                al = new ArrayList<>(3);
                                al.add(kv[1]);
                                params.put(kv[0], al);
                            }else {
                                al.add(kv[1]);
                            }
                        }
                    }
                }
            }
        } else {
            /**
             * 过滤boundary下形如 Content-Disposition: form-data; name="bin";
             * filename="12.pdf" Content-Type: application/octet-stream
             * Content-Transfer-Encoding: binary 的字符串
             */
            index += "boundary=".length();
            String boundary = "--" + c_type.substring(index);
            String lastboundary = boundary + "--";

            byte[] bs = br.readLine();

            // 循环解析文件
            // --OCqxMF6-JxtxoMDHmoG5W5eY9MGRsTBp
            // Content-Disposition: form-data; name="lng"
            // Content-Type: text/plain; charset=UTF-8
            // Content-Transfer-Encoding: 8bit
            // 116.361545

            String name_val = null;
            ArrayList al = null;
            int n = 0;
            while (bs != null) {
                if (ByteReader.equal(lastboundary.getBytes(), bs)) {
                    break;
                }

                // name filename
                bs = br.readLine();
                String[] kvs = ByteReader.split(bs, ';');
                for (String string : kvs) {
                    String[] kv = string.split("=");
                    if (kv[0].equals("name")) {
                        name_val = kv[1].replaceAll("\"", "");
                    } else if (kv[0].equals("filename")) {
                        String fileNameKey = name_val + "_filename";
                        params.put(fileNameKey, kv[1].replaceAll("\"", ""));
                    }
                }

                //new
                //\r\n
                bs = br.readLine();
                for (;;) {
                    if ((bs == null) || (bs != null && bs.length == 0)) {
                        break;
                    } else {
                        bs = br.readLine();
                    }
                }
                if(al == null) {
                    al = ByteReader.indexOfTotal(bytes, boundary.getBytes(), br.curIndex());
//                    System.out.println("-----"+al + bytes.length);
//                    for (int i = 0; i < al.size(); i++) {
//                        //1114126
//                        System.out.println("bound:="+al.get(i));
//                    }
                }
                int index_b = (int)al.get(n++);
                bs = Arrays.copyOfRange(bytes, br.curIndex(), index_b - 2);
                params.put(name_val, bs);
//                System.out.println(bs.length+" len");
                br.setIndex(index_b);
                bs = br.readLine();
            }
        }

    }

    public byte[] getUploadDataByName(String string) {
        return (byte[])params.get(string);
    }

    public String getUploadFileName(String s) {
        return (String)params.get(s);
    }

    public String getParameter(String string) {
        ArrayList al = (ArrayList) params.get(string);
        al = (ArrayList) params.get(string);
        if(al == null) {
            return null;
        }
        return (String)al.get(0);
    }

    public String[] getParameterValues(String string) {
        ArrayList al = (ArrayList) params.get(string);
        al = (ArrayList) params.get(string);
        if (al == null) {
            return null;
        }
        return (String[]) al.toArray();
    }
}
