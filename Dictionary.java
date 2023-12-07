import java.nio.ByteBuffer;

public class Dictionary {
	public static int PAD_OFFSET = 176;
	public static int PAD_SIZE = 84;

	public ByteBuffer b;
	public int offset;

	public Dictionary(int size) {
		b = ByteBuffer.allocateDirect(size);
		here_to_there();
	}

	public int here() { return b.position(); }
	public int aligned() { return (here() + 3) & ~(3); }
	public void align() { b.position(aligned()); }
	public void allot(int n) { b.position(here() + n); }
	public int there() { return here() + offset; }

	public void here_to_there() { offset = here() + PAD_OFFSET + PAD_SIZE; }

	public void bcompile(byte v) { b.put(v); }
	public void wcompile(short v) { b.putShort(v); }
	public void ccompile(int v) { b.putInt(v); }

	public void btransient(byte v) { b.put(there(), v); offset += 1; }
	public void wtransient(short v) { b.putShort(there(), v); offset += 2; }
	public void ctransient(int v) { b.putInt(there(), v); offset += 4; }

	public int unused() { return b.capacity() - here(); }
	public int tunused() { return b.capacity() - there(); }

	public int capacity() { return b.capacity(); }

	public byte get(int p) { return b.get(p); }
	public short getShort(int p) { return b.getShort(p); }
	public int getInt(int p) { return b.getInt(p); }

	public void put(int p, byte v) { b.put(p, v); }
	public void putShort(int p, short v) { b.putShort(p, v); }
	public void putInt(int p, int v) { b.putInt(p, v); }

	public int string_to_transient(String s) {
		if (tunused() < s.length()) here_to_there();
		if (tunused() < s.length()) return -1;
		int t = there();
		for (int i = 0; i < s.length(); i++) { btransient((byte)s.charAt(i)); }
		return t;	
	}

	public String mem_to_string(int l, int s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < l; i++) sb.append((char)get(s + i));
		return sb.toString();
	}
}
