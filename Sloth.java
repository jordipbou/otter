import java.math.BigDecimal;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Sloth implements Consumer<Otter> {
  public static byte NO_FLAGS = 0;
  public static byte VARIABLE = 1;
  public static byte HIDDEN = 2;
  public static byte IMMEDIATE = 4;

	public static int wPREVIOUS = 0;
	public static int wCODE = 4;
	public static int wCODELEN = 8;
	public static int wFLAGS = 10;
	public static int wNAMELEN = 11;
	public static int wNAME = 12;

	public static int pLATEST = Otter.pFREEZE + 4;
	public static int pSTATE = pLATEST + 4;
	public static int pIPOS = pSTATE + 4;
	public static int pILEN = pIPOS + 4;
	public static int pIBUF = pILEN + 4;
	public static int pABUF = pIBUF + 256;
	public static int pARET = pABUF + 64;

  public Sloth(Otter o, int size) {
		o.init_block(size);
		o.block.putInt(o.pHERE, pARET + 1);
		o.block.putInt(o.pFREEZE, pARET + 1);
		o.block.putInt(pLATEST, -1);
		o.block.put(pARET, (byte)']');
  }

  public void parse(Otter o) {
    char c = (char)o.pop();
		int p = o.block.getInt(pIPOS);
		int l = o.block.getInt(pILEN);
		p++;
		o.push(pIBUF + p);
    while (p < l  && (char)o.block.get(pIBUF + p) != c) { p++; }
		p++;
		o.push(pIBUF + p - o.T());
		o.block.putInt(pIPOS, p);
  }
  
  public void parse_name(Otter o) {
 		int p = o.block.getInt(pIPOS);
		int l = o.block.getInt(pILEN);
    while (p < l && Character.isWhitespace((char)o.block.get(pIBUF + p))) { p++; }
    o.push(pIBUF + p);
    while (p < l && !Character.isWhitespace((char)o.block.get(pIBUF + p))) { p++; }
    o.push(pIBUF + p - o.T());
		o.block.putInt(pIPOS, p);
  }

  public void find_name(Otter o) {
    int l = o.pop();
    int t = o.pop();
		int w = o.block.getInt(pLATEST);
    while (w >= 0) {
			o.push(t); o.push(l); o.push(w + wNAME); o.push((int)o.block.get(w + wNAMELEN));
			o.compareWithoutCase();
			if (o.pop() != 0) break;
      w = o.block.getInt(w + wPREVIOUS);
    }
    o.push(t);
    o.push(l);
    o.push(w);
  }
 
  public boolean asm(Otter o) {
    if (o.T() > 1 && (char)o.block.get(o.N()) == '\\') {
      int l = o.pop() - 1;
      int t = o.pop() + 1;
			o.push(t); o.push(pARET - l); o.push(l);
			o.copy();
      o.eval(pARET - l);
      return true; 
    } else return false;
  }

  public boolean casm(Otter o) {
    if (o.T() > 1 && (char)o.block.get(o.N()) == '$') {
      int l = o.pop() - 1;
      int t = o.pop() + 1;
			o.push(t); o.push(o.here()); o.push(l);
			o.copy();
			o.allot(l);
      return true; 
    } else return false;
  }

  public boolean colon(Otter o) {
    if (o.T() == 1 && (char)o.block.get(o.N()) == ':') {
      o.drop(); o.drop();
      parse_name(o);
      int l = o.pop();
      int t = o.pop();
      if (l == 0) { o.err = -16; return true; } // ZERO LENGTH NAME
			o.align();
			int w = o.here();
			o.ccompile(o.block.getInt(pLATEST));
			o.block.putInt(pLATEST, w);
			o.ccompile(0);
			o.scompile((short)0);
			o.bcompile(HIDDEN);
			o.bcompile((byte)l);
			o.push(t); o.push(o.here()); o.push(l);
			o.copy();
			o.allot(l);
			o.align();
			o.block.putInt(w + wCODE, o.here());
			o.block.putInt(pSTATE, 1);
      return true; 
    }  else return false;
  }

  public boolean semicolon(Otter o) {
    if (o.T() == 1 && (char)o.block.get(o.N()) == ';') {
      o.drop(); o.drop();
			o.bcompile((byte)']');
			int w = o.block.getInt(pLATEST);
			o.block.putShort(w + wCODELEN, (short)(o.here() - o.block.getInt(w + wCODE)));
			o.block.put(w + wFLAGS, NO_FLAGS);
			o.block.putInt(pSTATE, 0);
      return true; 
    } else return false;
  }

  public void number(Otter o) {
    int l = o.pop();
    int t = o.pop();
	  try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < l; i++) {
				sb.append((char)o.block.get(t + i));
			}
			
      BigDecimal n = new BigDecimal(sb.toString());
      if (o.block.getInt(pSTATE) > 0) o.literal(n.intValueExact());
			else o.push(n.intValueExact());
    } catch (NumberFormatException | ArithmeticException e) {
      o.err = -13; // Undefined word
    }
  }
 
  public void compile(Otter o, int w) {
		int code = o.block.getInt(w + wCODE);
		int code_len = o.block.getShort(w + wCODELEN);
		o.push(code); o.push(o.here()); o.push(code_len - 1);
		o.copy();
		o.allot(code_len - 1);
		/*
    if (w.codelen < 4) {
      for (int i = 0; i < w.codelen - 1; i++) {
        o.block.put(here++, o.block.get(w.code + i));
      }
    } else {
      o.literal(w.code);
      o.block.put(here++, (byte)'i');
    }
		*/
  }
 
  public void evaluate(Otter o, String s) {
		for (int i = 0; i < s.length(); i++) {
			o.block.put(pIBUF + i, (byte)s.charAt(i));
		}
		o.block.putInt(pIPOS, 0);
		o.block.putInt(pILEN, s.length());
    while (o.block.getInt(pIPOS) < o.block.getInt(pILEN)) {
      parse_name(o);
      if (o.T() == 0) { o.drop(); o.drop(); return; }
      find_name(o);
      if (o.T() != -1) {
        int w = o.pop();
        o.drop(); o.drop();
        if (o.block.getInt(pSTATE) == 0 || (o.block.get(w + wFLAGS) & IMMEDIATE) == IMMEDIATE) {
          o.eval(o.block.getInt(w + wCODE));
        } else {
          compile(o, w);
        }
      } else {
        o.drop();
        if (!asm(o)) 
          if (!casm(o)) 
            if (!colon(o)) 
              if (!semicolon(o)) 
                number(o);
      }
    }
  }
 
  public void immediate(Otter o) {
		int l = o.block.getInt(pLATEST);
		o.block.put(l + wFLAGS, (byte)(((byte)(o.block.get(l + wFLAGS)) | ((byte)(IMMEDIATE)))));
	}

	public void recurse(Otter o) {
		int l = o.block.getInt(pLATEST);
		int c = o.block.getInt(l + wCODE);
		o.literal(c); 
		o.bcompile((byte)'i');
	}

  public void accept(Otter o) {
    switch (o.token()) {
      //case 'h': o.push(here); break;
			case 'i': immediate(o); break;
			case 'p': parse(o); break;
			case 'n': parse_name(o); break;
			case 'f': find_name(o); break;
			case 'r': recurse(o); break;
    } 
  }

/*
  public void trace(Otter o) {
    System.out.printf("[%b] ", state);
    o.trace();
    System.out.printf("<%s>\n", ibuf.substring(ipos, ilen));
  }
*/
}
