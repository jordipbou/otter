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

  class Word {
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
		o.push(ipos);
    while (ipos < ilen && ibuf.charAt(ipos) != c) { ipos++; }
		ipos++;
		o.push(ipos - o.top());
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
      if (w.name.equalsIgnoreCase(name)) break;
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
      if (l == 0) { o.err = -16; return true; } // ZERO LENGTH NAME
      Word w = new Word();
      words.add(w);
      w.name = ibuf.substring((int)t, (int)(t + l));
      align(o);
      w.code = here;
      w.flags = HIDDEN;
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
      w.flags = NO_FLAGS;
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
        if (!state || (w.flags & IMMEDIATE) == IMMEDIATE) {
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
 
  public void immediate(Otter o) {
		words.get(words.size() - 1).flags |= IMMEDIATE;
	}

  public void accept(Otter o) {
    switch (o.token()) {
      case 'h': o.push(here); break;
			case 'i': immediate(o); break;
			case 'p': parse(o); break;
			case 'n': parse_name(o); break;
			case 'f': find_name(o); break;
    } 
  }

  public void trace(Otter o) {
    System.out.printf("[%b] ", state);
    o.trace();
    System.out.printf("<%s>\n", ibuf.substring(ipos, ilen));
  }
}
