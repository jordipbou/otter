import java.math.BigDecimal;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

public class Sloth implements Consumer<Otter> {
	public static int pSIZE = 0;
	public static int pHERE = 4;
	public static int pLATEST = 8;
	public static int pSTATE = 12;
	public static int pILEN = 16;
	public static int pIPOS = 20;
	public static int pIBUF = 24;
	public static int IBUF_SIZE = 256;
	public static int pABUF = pIBUF + IBUF_SIZE;
	public static int ABUF_SIZE = 64;
	public static int pABUF_RETURN = pABUF + ABUF_SIZE;
	public static int pMIN_HERE = pABUF_RETURN + 1;

	public Sloth(Otter o, int size) {
    o.block = ByteBuffer.allocateDirect(size);
		o.block.putInt(pSIZE, size);
		o.block.putInt(pLATEST, -1);
		o.block.putInt(pSTATE, 0);
		o.block.putInt(pILEN, 0);
		o.block.putInt(pIPOS, 0);
		o.block.putInt(pHERE, pMIN_HERE);
    o.block.put(pABUF_RETURN, (byte)']');
    o.ip = size;
    o.extensions['S' - 'A'] = this;
	}

	public void align(Otter o) {
		o.block.putInt(pHERE, (o.block.getInt(pHERE) + 7) & ~7);
	}

	public boolean in_buffer(Otter o) { return o.block.getInt(pIPOS) < o.block.getInt(pILEN); }
	public byte token(Otter o) { return o.block.get(pIBUF + o.block.getInt(pIPOS)); }
	public void next(Otter o) { o.block.putInt(pIPOS, o.block.getInt(pIPOS) + 1); }

  public void parse(Otter o) {
    byte c = (byte)o.pop();
    o.push(pIBUF + o.block.getInt(pIPOS));
    while (in_buffer(o) && token(o) != c) { next(o); }
    o.push(pIBUF + o.block.getInt(pIPOS) - o.top());
		next(o);
  }

  public void parse_name(Otter o) {
    while (in_buffer(o) && Character.isWhitespace(token(o))) { next(o); }
    o.push(pIBUF + o.block.getInt(pIPOS));
    while (in_buffer(o) && !Character.isWhitespace((char)token(o))) { next(o); }
    o.push(pIBUF + o.block.getInt(pIPOS) - o.top());
  }

	public static int wPREVIOUS = 0;
	public static int wCODE = 4;
	public static int wFLAGS = 8;
	public static int wNAMELEN = 9;
	public static int wNAME = 10;

	public static int wSIZE = 10; // + name length

	public static byte NO_FLAGS = 0;
	public static byte VARIABLE = 1;
	public static byte HIDDEN = 2;
	public static byte IMMEDIATE = 4;

	public void create(Otter o) {
		parse_name(o);
		long l = o.pop();
		long t = o.pop();
		if (l == 0) { o.err = -16; return; } // Zero length string as a name
		align(o);
		int w = o.block.getInt(pHERE);
		o.block.putInt(pHERE, o.block.getInt(pHERE) + wSIZE + (int)l);
		align(o);
		o.block.putInt(w + wPREVIOUS, o.block.getInt(pLATEST));
    o.block.putInt(pLATEST, w);
		o.block.putInt(w + wCODE, o.block.getInt(pHERE));
		o.block.put(w + wFLAGS, VARIABLE);
		o.block.put(w + wNAMELEN, (byte)l);
		for (int i = 0; i < l; i++) {
			o.block.put(w + wNAME + i, o.block.get((int)t + i));
		}
	}

	public void colon(Otter o) {
		create(o);
		o.block.put(o.block.getInt(pLATEST) + wFLAGS, HIDDEN);
		o.block.putInt(pSTATE, 1);
	}

	public void semicolon(Otter o) {
		o.block.put(o.block.getInt(pHERE), (byte)']');
    o.block.putInt(pHERE, o.block.getInt(pHERE) + 1);
		o.block.putInt(pSTATE, 0);
		o.block.put(o.block.getInt(pLATEST) + wFLAGS, NO_FLAGS);
	}

	public boolean compare_names(Otter o, int t1, byte l1, int t2, byte l2) {
		if (l1 != l2) return false;
		else {
			for (int i = 0; i < l1; i++) {
				if (o.block.get(t1 + i) != o.block.get(t2 + i)) return false;	
			}
			return true;
		}
	}

	public void find_name(Otter o) {
		long l = o.pop();
		long t = o.pop();
		long w = o.block.getInt(pLATEST);
		while (w != -1) {
			if (compare_names(o, (int)t, (byte)l, (int)w + wNAME, o.block.get((int)w + wNAMELEN)))
				break;
			w = o.block.getInt((int)w + wPREVIOUS);
		}
		o.push(t);
		o.push(l);
		o.push(w);
	}

	public void fill_input_buffer(Otter o, String s) {
		for (int i = 0; i < s.length(); i++) {
			o.block.put(pIBUF + i, (byte)s.charAt(i));
		}
		o.block.putInt(pIPOS, (int)0);
		o.block.putInt(pILEN, (int)s.length());
	}

	public void evaluate(Otter o, String s) {
		fill_input_buffer(o, s);
		while (in_buffer(o)) {
      trace(o);
			parse_name(o);
			if (o.top() == 0) { o.drop(); o.drop(); return; }
			find_name(o);
			long w = o.pop();
			if (w != -1) {
				o.drop(); o.drop();
				if (o.block.getInt(pSTATE) == 0 || (o.block.get((int)w + wFLAGS) & IMMEDIATE) == IMMEDIATE) {
					o.eval(o.block.getInt((int)w + wCODE));
				} else {
          // Compile!
				}
			} else {
        long l = o.pop();
        long t = o.pop();
				if (l == 1 && o.block.get((int)t) == ':') {
          colon(o);
				} else if (l == 1 && o.block.get((int)t) == ';') {
          semicolon(o);
				} else if (o.block.get((int)t) == (byte)'\\') {
          o.push(t + 1);
          o.push(pABUF_RETURN - l + 1);
          o.push(l - 1);
          o.copy();
          o.eval(pABUF_RETURN - l + 1);
				} else if (o.block.get((int)t) == '$') {
					o.push(t + 1);
          o.push(o.block.getInt(pHERE));
          o.push(l - 1);
          o.copy();
          o.block.putInt(pHERE, o.block.getInt(pHERE) + (int)l - 1);
				} else {
 					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < l; i++) {
						sb.append((char)o.block.get((int)t + i));
					}
          System.out.println(sb.toString());
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

	public void accept(Otter o) {
    switch (o.token()) {
      case 'h': o.push(o.block.getInt(pHERE)); break;
      case 'n': parse_name(o); break;
      case 'p': parse(o); break;
    }
	}

  public void trace(Otter o) {
    System.out.printf("[%d] ", o.block.getInt(pSTATE));
    o.trace();
    System.out.print("<");
    for (int i = o.block.getInt(pIPOS); i < o.block.getInt(pILEN); i++) {
      System.out.printf("%c", o.block.get(pIBUF + i));
    }
    System.out.println(">");
  }

/*
  public static int pSIZE = 0;
  public static int pLATEST = 4;
  public static int pHERE = 8;
	public static int pSTATE = 12;
  public static int pILEN = 16;
  public static int pIPOS = 20;
  public static int pIBUF = 24;
  public static int pABUF;

  public static int wPREV = 0;
  public static int wCODE = 4;
  public static int wFLAGS = 8;
  public static int wNLEN = 9;
  public static int wNAME = 10;

	public static byte VARIABLE = 1;
	public static byte HIDDEN = 2;
	public static byte IMMEDIATE = 4;

  public Sloth(Otter o, int size) {
    o.block = ByteBuffer.allocateDirect(size);
		setSize(o, size);
		setLatest(o, 0);
		setHere(o, pIBUF + 256);
		setIlen(o, 0);
		setIpos(o, 0);
		setState(o, 0);
    pABUF = size - 64;
    o.ip = size;
    o.extensions['S' - 'A'] = this;
  }

	public int getSize(Otter o) { return o.block.getInt(pSIZE); }
	public void setSize(Otter o, int s) { o.block.putInt(pSIZE, s); }

  public int getLatest(Otter o) { return o.block.getInt(pLATEST); }
  public void setLatest(Otter o, int l) { o.block.putInt(pLATEST, l); }

	public int getHere(Otter o) { return o.block.getInt(pHERE); }
	public void setHere(Otter o, int h) { o.block.putInt(pHERE, h); }

	public int getState(Otter o) { return o.block.getInt(pSTATE); }
	public void setState(Otter o, int s) { o.block.putInt(pSTATE, s); }
  
  public int getIlen(Otter o) { return o.block.getInt(pILEN); }
  public void setIlen(Otter o, int n) { o.block.putInt(pILEN, n); }
  
  public int getIpos(Otter o) { return o.block.getInt(pIPOS); }
  public void setIpos(Otter o, int n) { o.block.putInt(pIPOS, n); }
  public void incIpos(Otter o) { setIpos(o, getIpos(o) + 1); }

  public void fillIbuf(Otter o, String s) {
		byte[] b = s.getBytes(StandardCharsets.US_ASCII);
  	for (int i = 0; i < s.length(); i++) {
  	  o.block.put(pIBUF + i, b[i]);
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
		incIpos(o);
  }

  public void parse_name(Otter o) {
    parse_spaces(o);
    o.push(pIBUF + getIpos(o));
    parse_non_spaces(o);
    o.push(pIBUF + getIpos(o) - o.top());
  }

  public int getPrevious(Otter o, int w) { return o.block.getInt(w + wPREV); }
  public void setPrevious(Otter o, int w, int p) { o.block.putInt(w + wPREV, p); }
  public int getCode(Otter o, int w) { return o.block.getInt(w + wCODE); }
  public void setCode(Otter o, int w, int c) { o.block.putInt(w + wCODE, c); }
  public byte getFlags(Otter o, int w) { return o.block.get(w + wFLAGS); }
  public void setFlags(Otter o, int w, byte f) { o.block.put(w + wFLAGS, f); }
  public byte getNLen(Otter o, int w) { return o.block.get(w + wNLEN); }
  public void setNLen(Otter o, int w, byte l) { o.block.put(w + wNLEN, l); }
  public void nt_to_name(Otter o) {
    int w = (int)o.pop();
    o.push(w + wNAME);
    o.push(o.block.get(w + wNLEN));
  }
    
  public void find_name(Otter o) { // ( c-addr n -- w )
    long w = getLatest(o);
    while (w != 0) {
      o.over(); o.over(); // ( c-addr1 n1 c-addr1 n1 -- )
			o.push(w);					// ( c-addr1 n1 c-addr1 n1 w -- )
      nt_to_name(o);			// ( c-addr1 n1 c-addr1 n1 c-addr2 n2 -- )
			if (o.nnext() == o.top()) {
				o.drop(); o.swap();
	      o.compare();
	      if (o.pop() == 0) break;
			}
      w = getPrevious(o, (int)w);
    }
    o.push(w);
  }
  
	public void align(Otter o) {
		int h = o.block.getInt(pHERE);
		o.block.putInt(pHERE, (h + 7) & ~(7));
	}

	public void trace(Otter o) {
		System.out.printf("[%d] ", getState(o));
		o.trace();
		System.out.print("<");
		for (int i = getIpos(o); i < getIlen(o); i++) {
			System.out.printf("%c", o.block.get(pIBUF + i));
		}
		System.out.println(">");
	}

  public void evaluate(Otter o, String s) {
    fillIbuf(o, s);
    while (getIpos(o) < getIlen(o)) {
			trace(o);
      parse_name(o);
			System.out.println("AFTER PARSE NAME:");
			trace(o);
      if (o.top() == 0) { o.drop(); o.drop(); return; }
      find_name(o);
			System.out.println("AFTER FIND NAME:");
			trace(o);
      long w = o.pop();
      if (w != 0) {
        // eval 
      } else {
        long l = o.pop();
        long t = o.pop();
        if (l == 1 && o.block.get((int)t) == ':') {
					parse_name(o);
					l = o.pop();
					t = o.pop();
					align(o);
				  int h = o.block.getInt(pHERE);
					setPrevious(o, h, getLatest(o));
					setLatest(o, h);
					setFlags(o, h, HIDDEN);
					setNLen(o, h, (byte)l);
					o.push(t);
					o.push(h + wNAME);
					o.push(l);
					o.copy();
					o.push(wNAME + l);
					allot(o);
					align(o);
					setCode(o, h, getHere(o));
					setState(o, 1);
        } else if (l == 1 && o.block.get((int)t) == ';') {
					o.block.put(getHere(o), (byte)']');
					o.push(1L);
					allot(o);
					setState(o, 0);
					setFlags(o, getLatest(o), (byte)0);
        } else if (o.block.get((int)t) == '\\') {
          o.push(t + 1);
          o.push(o.block.getInt(pSIZE) - l + 1);
          o.push(l - 1);
          o.copy();
          o.eval(o.block.getInt(pSIZE) - l + 1);
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

	public void compilation(Otter o) { o.block.putInt(pSTATE, 1); }
	public void interpretation(Otter o) { o.block.putInt(pSTATE, 0); }
  
  public void accept(Otter o) {
    switch (o.token()) {
    case 'a': allot(o); break;
		//case 'c': create(o); break;
    case 'f': find_name(o); break;
    case 'h': here(o); break;
    case 'n': parse_name(o); break;
    case 'p': parse(o); break;
		case ']': setState(o, 1); break;
		case '[': setState(o, 0); break;
    }
  }
*/
}
