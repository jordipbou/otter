import java.nio.ByteBuffer;

public class Dictionary {
  private ByteBuffer block;
  private int bottom;

  public Dictionary(int size) {
    block = ByteBuffer.allocateDirect(size);
    bottom = 0;
  }

  public int size() { return block.capacity(); }
  public int bottom() { return bottom; }
  public void bottom(int b) { 
    bottom = b; 
    if (block.position() < bottom) block.position(bottom); 
  }
  public int top() { return block.limit(); }
  public void top(int t) { block.limit(t); }

  public void compile_byte(byte b) { block.put(b); }
  public void compile_short(short s) { block.putShort(s); }
  public void compile_int(int i) { block.putInt(i); }
  public void compile_long(long l) { block.putLong(l); }

  public void store_byte(int pos, byte b) { block.put(pos, b); }
  public void store_short(int pos, short s) { block.putShort(pos, s); }
  public void store_int(int pos, int i) { block.putInt(pos, i); }
  public void store_long(int pos, long l) { block.putLong(pos, l); }

  public byte fetch_byte(int pos) { return block.get(pos); }
  public short fetch_short(int pos) { return block.getShort(pos); }
  public int fetch_int(int pos) { return block.getInt(pos); }
  public long fetch_long(int pos) { return block.getLong(pos); }

 	public void store_literal(int n) {
    if (n == 1) {
      block.put(here(), (byte)'1');
			allot(1);
    } else if (n >= -128 && n <= 127) {
      block.put(here(), (byte)'\'');
			allot(1);
      block.put(here(), (byte)n);
			allot(1);
    } else if (n >= -32768 && n <= 32767) {
      block.put(here(), (byte)'2');
			allot(1);
      block.putShort(here(), (short)n);
      allot(2);
    } else {
      block.put(here(), (byte)'4');
			allot(1);
      block.putInt(here(), n);
      allot(4);
    }
	}
 
  public int here() { return block.position(); }
  public void allot(int n) { 
    int h = here();
    if ((h + n) < bottom) block.position(bottom);
    else if ((h + n) > block.limit()) block.position(block.limit());
    else block.position(h + n);
  }
  public void align() { int h = here(); int a = (h + 3) + ~(3); allot(a - h); }
}