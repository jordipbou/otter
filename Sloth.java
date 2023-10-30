import java.math.BigDecimal;
import java.util.function.Consumer;
import java.nio.ByteBuffer;

public class Sloth implements Consumer<Otter> {
  public static int pSIZE = 0;
  public static int pLATEST = 4;
  public static int pHERE = 8;
  public static int pILEN = 12;
  public static int pIPOS = 16;
  public static int pIBUF = 20;
  public static int pABUF;

  public static int wPREV = 0;
  public static int wCODE = 4;
  public static int wFLAGS = 8;
  public static int wNLEN = 9;
  public static int wNAME = 10;

  public Sloth(Otter o, int size) {
    o.block = ByteBuffer.allocateDirect(size);
    o.block.putInt(pSIZE, size);
    o.block.putInt(pLATEST, 0);
    o.block.putInt(pHERE, pIBUF + 256);
    o.block.putInt(pILEN, 0);
    o.block.putInt(pIPOS, 0);
    pABUF = size - 64;
    o.ip = size;
    o.extensions['S' - 'A'] = this;
  }

  public int getLatest(Otter o) { return o.block.getInt(pLATEST); }
  public void setLatest(Otter o, int l) { o.block.putInt(pLATEST, l); }
  
  public int getIlen(Otter o) { return o.block.getInt(pILEN); }
  public void setIlen(Otter o, int n) { o.block.putInt(pILEN, n); }
  
  public int getIpos(Otter o) { return o.block.getInt(pIBUF + pIPOS); }
  public void setIpos(Otter o, int n) { o.block.putInt(pIBUF + pIPOS, n); }
  public void incIpos(Otter o) { setIpos(o, getIpos(o) + 1); }

  public void fillIbuf(Otter o, String s) {
    for (int i = 0; i < s.length(); i++) {
      o.block.put(pIBUF + i, (byte)s.charAt(i));
    }
    setIlen(o, s.length());
    setIpos(o, 0);
  }

  public byte peekIbuf(Otter o) { return o.block.get(pIBUF + getIpos(o)); }
  
  public void parse_spaces(Otter o) {
    while (getIpos(o) < getIlen(o) && Character.isWhitespace(peekIbuf(o))) { incIpos(o); }
  }
  
  public void parse_non_spaces(Otter o) {
    while (getIpos(o) < getIlen(o) && !Character.isWhitespace((char)peekIbuf(o))) { incIpos(o); }
  }

  public void parse(Otter o) {
    byte c = (byte)o.pop();
    o.push(pIBUF + getIpos(o));
    while (getIpos(o) < getIlen(o) && peekIbuf(o) != c) { incIpos(o); }
    o.push(pIBUF + getIpos(o) - o.top());
  }

  public void parse_name(Otter o) {
    parse_spaces(o);
    o.push(pIBUF + getIpos(o));
    parse_non_spaces(o);
    o.push(pIBUF + getIpos(o) - o.top());
  }

  public int getPrevious(Otter o, int w) {
    return o.block.getInt(w + wPREV);
  }

  public void setPrevious(Otter o, int w, int p) {
    o.block.putInt(w + wPREV, p);
  }

  public int getCode(Otter o, int w) {
    return o.block.getInt(w + wCODE);
  }

  public void setCode(Otter o, int w, int c) {
    o.block.putInt(w + wCODE, c);
  }

  public byte getFlags(Otter o, int w) {
    return o.block.get(w + wFLAGS);
  }

  public void setFlags(Otter o, int w, byte f) {
    o.block.put(w + wFLAGS, f);
  }

  public byte getNLen(Otter o, int w) {
    return o.block.get(w + wNLEN);
  }

  public void setNLen(Otter o, int w, byte l) {
    o.block.put(w + wNLEN, l);
  }

  public void nt_to_name(Otter o) {
    int w = (int)o.pop();
    o.push(w + wNAME);
    o.push(o.block.get(w + wNLEN));
  }
    
  public void find_name(Otter o) { // ( c-addr n -- w )
    long w = getLatest(o);
    while (w != 0) {
      o.over(); o.over(); 
      nt_to_name(o);
      o.compare();
      if (o.pop() == 0) break;
      w = getPrevious(o, (int)w);
    }
    o.push(w);
  }
    
  public void evaluate(Otter o, String s) {
    fillIbuf(o, s);
    while (getIpos(o) < getIlen(o)) {
      parse_name(o);
      if (o.top() == 0) { o.drop(); o.drop(); return; }
      find_name(o);
      long w = o.pop();
      if (w != 0) {
        // eval 
      } else {
        long l = o.pop();
        long t = o.pop();
        if (l == 1 && o.block.get((int)t) == ':') {
        } else if (l == 1 && o.block.get((int)t) == ';') {
        } else if (o.block.get((int)t) == '\\') {
          o.push(t + 1);
          o.push(pABUF - l + 1);
          o.push(l - 1);
          o.copy();
          o.eval(pABUF - l + 1);
        } else if (o.block.get((int)t) == '$') {
          o.push(t + 1);
          here(o);
          o.push(l - 1);
          o.dup();
          allot(o);
          o.copy();
        } else {
 					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < l; i++) {
						sb.append((char)o.block.get((int)t + i));
					}
					try {
				    BigDecimal n = new BigDecimal(sb.toString());
						o.push(n.longValueExact());
						// TODO: Compile
          } catch (NumberFormatException | ArithmeticException e) {
            System.out.println("WORD NOT FOUND");
            System.out.println(sb.toString());
          }  
        }
      }
    }
  }

  public void allot(Otter o) {
    int n = (int)o.pop();
    o.block.putInt(pHERE, o.block.getInt(pHERE) + n);
  }
  
  public void here(Otter o) {
    o.push((long)o.block.getInt(pHERE));
  }
  
  public void accept(Otter o) {
    switch (o.token()) {
    case 'a': allot(o); break;
    case 'f': find_name(o); break;
    case 'h': here(o); break;
    case 'n': parse_name(o); break;
    case 'p': parse(o); break;
    }
  }
}