import java.math.BigDecimal;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Sloth implements Consumer<Otter> {
  class Word {
    public static byte NO_FLAGS = 0;
    public static byte VARIABLE = 1;
    public static byte HIDDEN = 2;
    public static byte IMMEDIATE = 4;
    
    public int code;
    public int codelen;
    public String name;
    public byte flags;
  }
  
  public String ibuf;
  public int ipos;
  public int ilen;
  public boolean state;
  public ArrayList<Word> words;
  public int here;
  
  public Sloth(Otter o, int size) {
    o.block = ByteBuffer.allocateDirect(size);   
    words = new ArrayList<Word>();
  }

  public void parse(Otter o) {
    char c = (char)o.pop();
    ipos++;
    while (ipos < ilen && ibuf.charAt(ipos) != c) { ipos++; }
  }
  
  public void parse_name(Otter o) {
    while (ipos < ilen && Character.isWhitespace(ibuf.charAt(ipos))) { ipos++; }
    o.push(ipos);
    while (ipos < ilen && !Character.isWhitespace(ibuf.charAt(ipos))) { ipos++; }
    o.push(ipos - o.top());
  }

  public void find_name(Otter o) {
    long l = o.pop();
    long t = o.pop();
    String name = ibuf.substring((int)t, (int)(t + l));
    int i = words.size() - 1;
    while (i >= 0) {
      Word w = words.get(i);
      if (w.name.equals(name)) break;
      i--;
    }
    o.push(t);
    o.push(l);
    o.push(i);
  }

  public void align(Otter o) {
    here = (here + 7) & ~(7); 
  }
  
  public boolean asm(Otter o) {
    if (o.top() > 1 && ibuf.charAt((int)o.next()) == '\\') {
      long l = o.pop() - 1;
      long t = o.pop() + 1;
      for (int i = 0; i < l; i++) {
        o.block.put((int)(o.block.capacity() - l + i), (byte)ibuf.charAt((int)(t + i)));
      }
      o.eval(o.block.capacity() - l);
      return true; 
    } else return false;
  }

  public boolean casm(Otter o) {
    if (o.top() > 1 && ibuf.charAt((int)o.next()) == '$') {
      long l = o.pop() - 1;
      long t = o.pop() + 1;
      for (int i = 0; i < l; i++) {
        o.block.put(here++, (byte)ibuf.charAt((int)(t + i)));
      }
      return true; 
    } else return false;
  }

  public boolean colon(Otter o) {
    if (o.top() == 1 && ibuf.charAt((int)o.next()) == ':') {
      o.drop(); o.drop();
      parse_name(o);
      long l = o.pop();
      long t = o.pop();
      System.out.printf("Creating word: [%s]\n", ibuf.substring((int)t, (int)(t + l)));
      if (l == 0) { o.err = -16; return true; } // ZERO LENGTH NAME
      Word w = new Word();
      words.add(w);
      w.name = ibuf.substring((int)t, (int)(t + l));
      align(o);
      w.code = here;
      w.flags = Word.HIDDEN;
      state = true;
      return true; 
    }  else return false;
  }

  public boolean semicolon(Otter o) {
    if (o.top() == 1 && ibuf.charAt((int)o.next()) == ';') {
      o.drop(); o.drop();
      o.block.put(here++, (byte)']');
      Word w = words.get(words.size() - 1);
      state = false; 
      w.flags = Word.NO_FLAGS;
      w.codelen = here - w.code;
      return true; 
    } else return false;
  }

  public void literal(Otter o) {
    long n = o.pop();
    if (n == 1) {
      o.block.put(here++, (byte)'1');
    } else if (n >= -128 && n <= 127) {
      o.block.put(here++, (byte)'\'');
      o.block.put(here++, (byte)n);
    } else if (n >= -32768 && n <= 32767) {
      o.block.put(here++, (byte)'2');
      o.block.putShort(here, (short)n);
      here += 2;
    } else if (n >= -2147483648 && n <= 2147483647) {
      o.block.put(here++, (byte)'4');
      o.block.putInt(here, (int)n);
      here += 4;
    } else {
      o.block.put(here++, (byte)'8');
      o.block.putLong(here, (long)n);
      here += 8;
    }
  }
  
  public void number(Otter o) {
    long l = o.pop();
    long t = o.pop();
	  try {
      BigDecimal n = new BigDecimal(ibuf.substring((int)t, (int)(t + l)));
		  o.push(n.longValueExact());
      if (state)
        literal(o);
    } catch (NumberFormatException | ArithmeticException e) {
      o.err = -13; // Undefined word
    }
  }
 
  public void compile(Otter o, Word w) {
    // This if should depend on the size of the literal for this
    // word...
    if (w.codelen < 4) {
      for (int i = 0; i < w.codelen - 1; i++) {
        o.block.put(here++, o.block.get(w.code + i));
      }
    } else {
      o.push(w.code);
      literal(o);
      o.block.put(here++, (byte)'x');
    }
  }
  
  public void evaluate(Otter o, String s) {
    ibuf = s;
    ipos = 0;
    ilen = s.length();
    while (ipos < ilen) {
      parse_name(o);
      if (o.top() == 0) { o.drop(); o.drop(); return; }
      find_name(o);
      if (o.top() != -1) {
        Word w = words.get((int)o.pop());
        o.drop(); o.drop();
        if (!state || (w.flags & Word.IMMEDIATE) == Word.IMMEDIATE) {
          o.eval(w.code);
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
  
  public void accept(Otter o) {
    switch (o.token()) {
      case 'h': o.push(here); break;
      // Does this make any sense?
        /*
      case ':': colon(o); break;
      case ';': semicolon(o); break;
      case 'n': parse_name(o); break;
      case 'p': parse(o); break;
      case ']': state = false; break;
      case '[': state = true; break;
        */
    } 
  }

  public void trace(Otter o) {
    System.out.printf("[%b] ", state);
    o.trace();
    System.out.printf("<%s>\n", ibuf.substring(ipos, ilen));
  }
   /*
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
  */
}