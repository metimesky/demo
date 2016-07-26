
import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteUtil {
	public static String getString(byte[] bytes, String charsetName) {
		return new String(bytes, Charset.forName(charsetName));
	}

	public static String getString(byte[] b, int start, int end) {
		return getString(Arrays.copyOfRange(b, start, end), "UTF-8");
	}

	public static boolean getBoolean(byte[] b, int off) {
		return b[off] != 0;
	}

	public static char getChar(byte[] b, int off) {
		return (char) ((b[off + 1] & 0xFF) + (b[off] << 8));
	}

	public static short getShort(byte[] b, int off) {
		return (short) ((b[off + 1] & 0xFF) + (b[off] << 8));
	}

	public static int getInt(byte[] b, int off) {
		return ((b[off + 3] & 0xFF)) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off]) << 24);
	}

	public static float getFloat(byte[] b, int off) {
		return Float.intBitsToFloat(getInt(b, off));
	}

	public static long getLong(byte[] b, int off) {
		return ((b[off + 7] & 0xFFL)) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16)
				+ ((b[off + 4] & 0xFFL) << 24) + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40)
				+ ((b[off + 1] & 0xFFL) << 48) + (((long) b[off]) << 56);
	}

	public static double getDouble(byte[] b, int off) {
		return Double.longBitsToDouble(getLong(b, off));
	}

	/*
	 * Methods for packing primitive values into byte arrays starting at given
	 * offsets.
	 */

	public static void putBoolean(byte[] b, int off, boolean val) {
		b[off] = (byte) (val ? 1 : 0);
	}

	public static void putChar(byte[] b, int off, char val) {
		b[off + 1] = (byte) (val);
		b[off] = (byte) (val >>> 8);
	}

	public static void putShort(byte[] b, int off, short val) {
		b[off + 1] = (byte) (val);
		b[off] = (byte) (val >>> 8);
	}

	public static void putInt(byte[] b, int off, int val) {
		b[off + 3] = (byte) (val);
		b[off + 2] = (byte) (val >>> 8);
		b[off + 1] = (byte) (val >>> 16);
		b[off] = (byte) (val >>> 24);
	}

	public static void putFloat(byte[] b, int off, float val) {
		putInt(b, off, Float.floatToIntBits(val));
	}

	public static void putLong(byte[] b, int off, long val) {
		b[off + 7] = (byte) (val);
		b[off + 6] = (byte) (val >>> 8);
		b[off + 5] = (byte) (val >>> 16);
		b[off + 4] = (byte) (val >>> 24);
		b[off + 3] = (byte) (val >>> 32);
		b[off + 2] = (byte) (val >>> 40);
		b[off + 1] = (byte) (val >>> 48);
		b[off] = (byte) (val >>> 56);
	}

	public static void putDouble(byte[] b, int off, double val) {
		putLong(b, off, Double.doubleToLongBits(val));
	}

	/*
	 * public static double getDouble(byte[] bytes) { long l = getLong(bytes,0);
	 * return Double.longBitsToDouble(l); }
	 * 
	 * public static byte[] getBytes(String data) { return getBytes(data,
	 * "UTF-8"); }
	 * 
	 * public static byte[] getBytes(float data) { int intBits =
	 * Float.floatToIntBits(data); return getBytes(intBits); }
	 * 
	 * public static byte[] getBytes(double data) { long intBits =
	 * Double.doubleToLongBits(data); return getBytes(intBits); }
	 * 
	 * public static byte[] getBytes(String data, String charsetName) { Charset
	 * charset = Charset.forName(charsetName); return data.getBytes(charset); }
	 * 
	 * public static byte[] getBytes(int data) { byte[] bytes = new byte[4];
	 * putInt(bytes, 0, data); return bytes; }
	 * 
	 * public static byte[] getBytes(long data) { byte[] bytes = new byte[8];
	 * putLong(bytes, 0, data); return bytes; }
	 * 
	 * public static byte[] getBytes(short data) { byte[] bytes = new byte[2];
	 * bytes[0] = (byte) (data & 0xff); bytes[1] = (byte) ((data & 0xff00) >>
	 * 8); return bytes; }
	 * 
	 * public static byte[] getBytes(char data) { byte[] bytes = new byte[2];
	 * bytes[0] = (byte) (data); bytes[1] = (byte) (data >> 8); return bytes; }
	 */

}
