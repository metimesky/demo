
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * @author zhangxinwei
 * 
 */

public class ByteReader {
	private byte[] bytes = null;
	private int index = 0;

	private InputStream is = null;

	public ByteReader(InputStream is) {
		this.is = is;
	}

	public ByteReader(byte[] bs) {
		this.bytes = bs;
	}

	public ByteReader(File f) {
		long flen = f.length();
		if (flen > Integer.MAX_VALUE) {
			System.out.println("file too long");
			return;
		}
		FileInputStream fis;
		try {

			fis = new FileInputStream(f);
			byte[] fileData = new byte[(int) flen];
			fis.read(fileData);
			this.bytes = fileData;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int curIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public char charAtIndex(int index) {
		return (char) bytes[index];
	}

	public byte[] peerLine() {
		if (index >= bytes.length)
			return null;

		int start = index;
		int end = index;

		int index_t = index;

		for (;;) {
			if (bytes[index_t] != '\r' && bytes[index_t] != '\n') {
				++index_t;
				if (index_t >= bytes.length) {
					end = index_t;
					break;
				}
			} else {
				end = index_t;

				if (bytes[index_t] == '\r') {
					++index_t;
					while (bytes[index_t] != '\n') {
						++index_t;
					}
					++index_t;
				}

				break;
			}

		}
		return Arrays.copyOfRange(bytes, start, end);
	}

	public byte[] readLine() {
		if (index >= bytes.length)
			return null;

		int from = index;
		int to = index;

		for (;;) {
			if (index >= bytes.length) {
				to = index;
				break;
			}

			if (bytes[index] == '\r' || bytes[index] == '\n') {
				to = index;
				break;
			} else {
				++index;
			}
		}

		++index;
		if (index < bytes.length) {
			if (bytes[index - 1] == '\r' && bytes[index] == '\n') {
				++index;
			}

		}

		return Arrays.copyOfRange(bytes, from, to);
	}

	public static int indexOf(byte[] self, byte b) {
		if (self == null)
			return -1;

		for (int i = 0; i < self.length; i++) {
			if (self[i] == b) {
				return i;
			}
		}

		return -1;
	}

	public static boolean endWith(byte[] self, byte b) {

		return false;
	}

	public static boolean startWith(byte[] self, byte b) {

		return false;
	}

	/**
	 * bytes position in self,same string indexOf
	 * 
	 * @param self
	 * @param bytes
	 * @return
	 */
	public static int indexOf(byte[] self, byte[] bytes) {
		if (self == null || bytes == null)
			return -1;

		for (int i = 0; i < self.length; i++) {
			for (int j = 0; j < bytes.length; j++) {
				if (i + j >= self.length)
					return -1;
				if (self[i + j] == bytes[j]) {
					if (j == bytes.length - 1) {
						return i;
					}
				} else {
					break;
				}
			}
		}
		return -1;
	}

	public static int indexOf(byte[] self, byte[] bytes, int from) {
		if (self == null || bytes == null)
			return -1;

		int selfLen = self.length;
		int theLen = bytes.length;
		for (int i = from; i <= selfLen - theLen; i++) {
			if (self[i] == bytes[0]) {
				for (int j = 1; j < theLen; j++) {
					if (self[i + j] != bytes[j]) {
						return -1;
					}
				}
				return i;
			}
		}

		return -1;
	}

	public static ArrayList indexOfTotal(byte[] self, byte[] bytes, int from) {
		if (self == null || bytes == null)
			return null;

		ArrayList al = new ArrayList<>();
		int selfLen = self.length;
		int theLen = bytes.length;
		label: for (int i = from; i <= selfLen - theLen; i++) {
			if (self[i] == bytes[0]) {
				for (int j = 1; j < theLen; j++) {
					if (self[i + j] != bytes[j]) {
						continue label;
					}
				}
				al.add(i);
			}
		}

		return al;
	}

	/**
	 * 
	 * @param self
	 * @param bytes
	 * @return
	 */
	public static boolean equal(byte[] self, byte[] bytes) {
		if (self == null || bytes == null)
			return false;

		if (self.length != bytes.length)
			return false;

		for (int i = 0; i < self.length; i++) {
			if (self[i] != bytes[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 分成俩串,滤空格\r\n
	 * 
	 * @param self
	 * @param c
	 * @return
	 */
	public static String[] kvSplit(byte[] self, char c) {
		int index = 0;
		for (int i = 0; i < self.length; i++) {
			if (c == self[i]) {
				index = i;
				break;
			}
		}

		String[] kv = new String[2];
		// key
		int from = 0;
		int to = 0;
		for (int i = 0; i < index; i++) {
			if (self[i] == ' ') {
			} else {
				from = i;
				break;
			}
		}
		for (int i = index - 1; i > from; i--) {
			if (self[i] == ' ') {

			} else {
				to = i + 1;
				break;
			}
		}
		kv[0] = new String(Arrays.copyOfRange(self, from, to));

		// val
		int from2 = 0;
		int to2 = 0;
		for (int i = index + 1; i < self.length; i++) {
			if (self[i] == ' ') {
			} else {
				from2 = i;
				break;
			}
		}
		for (int i = self.length - 1; i >= from2; i--) {
			if (self[i] == ' ') {

			} else {
				to2 = i + 1;
				break;
			}
		}
		kv[1] = new String(Arrays.copyOfRange(self, from2, to2));

		return kv;
	}

	/**
	 * 分成数组，滤空格\r\n
	 * 
	 * @param self
	 * @param c
	 * @return
	 */
	public static String[] split(byte[] self, char c) {
		ArrayList indexList = new ArrayList<>();

		for (int i = 0; i < self.length; i++) {
			if (c == self[i]) {
				indexList.add(i);
			}
		}

		int count = indexList.size();
		String[] ss = new String[count + 1];
		int from = 0;
		int to = 0;
		for (int i = 0; i <= count; i++) {
			if (i == count) {
				to = self.length;
			} else {
				to = (int) indexList.get(i);
			}

			// 去空格
			for (int j = from; j < to; j++) {
				if (self[j] == ' ') {
				} else {
					from = j;
					break;
				}
			}

			for (int j = to - 1; j >= from; j--) {
				if (self[j] == ' ') {

				} else {
					to = j;
					break;
				}
			}
			++to;

			byte[] ds = Arrays.copyOfRange(self, from, to);
			ss[i] = new String(ds);
			from = to + 1;
		}

		return ss;
	}

	public static String readWord(byte[] bytes, int index) {
		int to = index;
		for (int i = index; i < bytes.length; i++) {
			if (bytes[i] == ' ') {
				to = i;
				break;
			}
		}
		return new String(Arrays.copyOfRange(bytes, index, to));
	}

	/**
	 * 至包含最右字符的行及前些行
	 * 
	 * @param c
	 * @return
	 */
	public ArrayList<byte[]> nextLinesContainChar(char rightCh, char leftChar) {
		ArrayList<byte[]> as;
		if (index >= bytes.length)
			return null;
		else {
			as = new ArrayList<>();
		}
		byte[] lineData = readLine();
		as.add(lineData);
		while (!isContainRightChar(lineData, rightCh, leftChar)) {
			byte[] lined = readLine();
			as.add(lined);
		}
		return as;
	}

	public static boolean isContainRightChar(byte[] bs, char rightCh, char leftChar) {
		int leftCount = 0;
		for (int i = 0; i < bs.length; i++) {
			if (bs[i] == leftChar) {
				++leftCount;
			} else if (bs[i] == rightCh) {
				if (leftCount == 0)
					return true;
				else {
					--leftCount;
				}
			}
		}

		return false;
	}

	public static int lenWhenTrim(byte[] self) {

		// from
		int notSpaceLeft = 0;
		for (int i = 0; i < self.length; i++) {
			if (self[i] == ' ' || self[i] == '\t') {

			} else {
				break;
			}

			notSpaceLeft = i;
		}

		// to
		int notSpaceRight = notSpaceLeft + 1;
		if (notSpaceRight >= self.length) {
			return 0;
		}

		for (int i = self.length - 1; i > notSpaceRight; i--) {
			if (self[i] == ' ' || self[i] == '\t') {

			} else {
				notSpaceRight = i;
				break;
			}
		}

		return notSpaceRight - notSpaceLeft;
	}

	public static byte[] trim(byte[] self) {
		// from
		int notSpaceLeft = 0;
		for (int i = 0; i < self.length; i++) {
			if (self[i] == ' ' || self[i] == '\t') {

			} else {
				break;
			}

			notSpaceLeft = i;
		}

		// to
		int notSpaceRight = notSpaceLeft + 1;
		if (notSpaceRight >= self.length) {
			return null;
		}

		for (int i = self.length - 1; i > notSpaceRight; i--) {
			if (self[i] == ' ' || self[i] == '\t') {

			} else {
				notSpaceRight = i;
				break;
			}
		}

		return Arrays.copyOfRange(self, notSpaceLeft + 1, notSpaceRight + 1);
	}

	public static void main(String[] args) {
		// String s =
		// "fafafafafame--------safaffdsfdfame------fsdfdsfefamegg;ag------";
		// // int i = ByteReader.indexOf(s.getBytes(), "----".getBytes(), 0);
		// ArrayList al = ByteReader.indexOfTotal(s.getBytes(),
		// "fame".getBytes(), 0);
		// for (int i = 0; i < al.size(); i++) {
		// System.out.println(al.get(i));
		// }
		String s2 = "GET // HTTP/1.1";

		String[] kvs = ByteReader.split(s2.getBytes(), ' ');
		for (String string : kvs) {
			System.out.println(string);

			System.out.println(string.length());
		}

		// String s = "f ";
		// // System.out.println(ByteReader.lenWhenTrim(s.getBytes()));
		// System.out.println(new String(ByteReader.trim(" fff".getBytes())));

		// String s = "<!DOCTYPE html>\n<html>\n";
		// ByteReader br = new ByteReader(s.getBytes());
		// byte[] bs = br.readLine();
		// for(;;) {
		// if(bs == null) {
		// break;
		// }else {
		// System.out.println(new String(bs));
		// }
		//
		// bs = br.readLine();
		// }
	}

	public byte[] readLineByStream() {
		if(bytes != null) {
			
			byte[] bs = readLine();
			if(curIndex() == bytes.length - 1);
			bytes = null;
			return bs;
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		int count = 0;
		while (count == 0) {
			try {
				count = is.available();
				byte[] bs = new byte[count];
				while ((count = is.read(bs)) != 0) {
					bos.write(bs, 0, count);
					bs = new byte[is.available()];
				}

				bytes = bos.toByteArray();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return readLine();
	}

}
