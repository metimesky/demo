import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by zhangxinwei on 7/25/2016.
 * 字符串实体存储实现
 * offset + a + len + a + isdelete
 */
public class Database {

    public String dbdirPath = "";
    static Database db = null;

    RandomAccessFile rcf = null;
    RandomAccessFile rmf = null;

    long[] lens = null;
    long[] offsets = null;

    Object mlock = new Object();

    Database() {
        fileCheck();
    }

    public static void main(String[] args) {
        Database db = Database.getInstance();

        String key = db.insert("发发发发发发ifaipfa");

        System.out.println(key);

        String c = db.find("0a25a0");
        System.out.println(c);

        String newkey = db.update("50a25a0", "发发发发发发ifaipfxfaxxxxxxxxxx");
        System.out.println(newkey);
    }

   void loadIsDelete() {
       try {
           rmf.seek(0);
           int count = (int) (rmf.length() / 8);

           if (count == 0)
               return;

           lens = new long[count];
           offsets = new long[count];

           for (int i = 0; i < count; i++) {
               offsets[i] = rmf.readLong();
               lens[i] = rmf.readLong();
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    void test() {
        try {
            rmf.seek(5);
            rmf.writeInt(200);
            rmf.writeByte(1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void test2() {
        try {
            int len = rmf.readInt();
            byte b = rmf.readByte();

            System.out.println(len);
            System.out.println(b);

            rmf.seek(5);

            int len2 = rmf.readInt();
            byte b2 = rmf.readByte();

            System.out.println(len2);
            System.out.println(b2);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Database getInstance() {
        if (db == null) {
            db = new Database();
        }
        return db;
    }



    void fileCheck() {
        File file = new File("C:\\Users\\zhangxinwei\\Documents\\htagv\\dbdir");
        System.out.println(File.separator);
        if (!file.exists()) {
            System.out.println("no exist");
            file.mkdirs();

            try {
                File cf = new File(file.getPath() + File.separator +"content");
                cf.createNewFile();
                rcf = new RandomAccessFile(cf, "rw");

                File mf = new File(file.getPath() + File.separator + "meta");
                mf.createNewFile();
                rmf = new RandomAccessFile(mf, "rw");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                String[] fs = file.list();
                List<String> list = Arrays.asList(fs);
                File cf = new File(file.getPath() + File.separator +"content");
                rcf = new RandomAccessFile(cf, "rw");

                if (!list.contains("content")) {
                    cf.createNewFile();
                }

                File mf = new File(file.getPath() + File.separator + "meta");
                rmf = new RandomAccessFile(mf, "rw");

                if (!list.contains("meta")) {
                    mf.createNewFile();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String insert(String str) {
        String key = null;
        long offset = 0;
        long len = 0;
        byte[] data = str.getBytes();
        if (data.length == 0) {
            return null;
        }

        try {
            offset = checkOld(data.length);
            if (offset == 0) {
                offset = rcf.length();
                rcf.seek(rcf.length());
            }else {
                rcf.seek(offset);
            }

            //write
            rcf.write(data);

        }catch (IOException e) {
            e.printStackTrace();
        }

        return offset + "a" + data.length + "a"+ 0;
    }

    long checkOld(long len) {
        loadIsDelete();

        if (lens == null) {
            return 0;
        }

        for (int i = 0; i < lens.length; i++) {
            if (lens[i] == len) {
                return offsets[i];
            }
        }

        return 0;
    }

    /**
     *
     * @param key offset+a+len+a + isdelete
     * @return
     */
    public String find(String key) {
        if (key == null)
            return null;
        String[] strs = key.split("a");
        if (strs.length != 3) {
            return null;
        }

        int isdelete = Integer.parseInt(strs[2]);
        if (isdelete == 1) {
            System.out.println("delete");
            return null;
        }
        long offset = Long.parseLong(strs[0]);
        long len = Long.parseLong(strs[1]);



        byte[] data = null;
        try {
            if (offset > rcf.length())
                return null;
             data = new byte[(int)len];
            rcf.seek(offset);
            rcf.read(data);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new String(data);
    }

    public String update(String key, String newData) {
        if (key == null)
            return null;
        String[] strs = key.split("a");
        if (strs.length != 3) {
            return null;
        }

        int isdelete = Integer.parseInt(strs[2]);
        if (isdelete == 1) {
            System.out.println("delete");
            return null;
        }
        long offset = Long.parseLong(strs[0]);
        long len = Long.parseLong(strs[1]);

        byte[] newByte = newData.getBytes();
        if (len >= newByte.length) {
            try {
                rcf.seek(offset);
                rcf.write(newByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                offset = rcf.length();
                rcf.seek(offset);
                rcf.write(newByte);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //add isdelete file
            try {
                if (offset + len> rcf.length()) {

                }else {
                    addDeleteFile(offset, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return offset + "a" + newByte.length + "a" + 0;
    }

     void addDeleteFile(long offset, long len) {
         try {
             rmf.seek(rmf.length());
             rmf.writeLong(offset);
             rmf.writeLong(len);
         } catch (IOException e) {
             e.printStackTrace();
         }
    }

    public void delete(String str) {

    }
}