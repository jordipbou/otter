import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class Otter {
  public ByteBuffer block;
  public Consumer<Otter>[] extensions;
  public long[] s;
  public int sp;
  public long[] r;
  public int rp;
  public int ip;
	public int err;
	public boolean tr;

  public Otter() {
    s = new long[256];
    sp = 0;
    r = new long[256];
    rp = 0;
    ip = 0;
    extensions = new Consumer[26];
		err = 0;
		tr = false;
  }

	public void init_block(int size) {
		block = ByteBuffer.allocateDirect(size);
		block.putInt(0, size);	// BLOCK SIZE
		block.putInt(4, 8);			// HERE
	}

	public int here() { return block.getInt(4); }
	public void allot(int n) { block.putInt(4, block.getInt(4) + n); }

  public long T() { return s[sp - 1]; }
  public long N() { return s[sp - 2]; }
  public long NN() { return s[sp - 3]; }

  public void push(long a) { s[sp++] = a; }
  public long pop() { return s[--sp]; }
  public void drop() { sp--; }
  public void dup() { long a = pop(); push(a); push(a); }
  public void over() { long a = pop(); long b = pop(); push(b); push(a); push(b); }
  public void swap() { long a = pop(); long b = pop(); push(a); push(b); }
  public void rot() { long a = pop(); long b = pop(); long c = pop(); push(b); push(a); push(c); }
  public void nip() { long a = pop(); long b = pop(); push(a); }
	public void pick() { long n = pop(); push(s[sp - 1 - (int)n]); }

	public void to_r() { r[rp++] = s[--sp];	}
	public void from_r() { s[sp++] = r[--rp];	}
  
  public void add() { long a = pop(); long b = pop(); push(b + a); }
  public void sub() { long a = pop(); long b = pop(); push(b - a); }
  public void mul() { long a = pop(); long b = pop(); push(b * a); }
  public void div() { long a = pop(); long b = pop(); push(b / a); }
  public void mod() { long a = pop(); long b = pop(); push(b % a); }

  public void and() { long a = pop(); long b = pop(); push(b & a); }
  public void or() { long a = pop(); long b = pop(); push(a | b); }
  public void xor() { long a = pop(); long b = pop(); push(b ^ a); }
  public void invert() { long a = pop(); push(~a); }

  public void lt() { long a = pop(); long b = pop(); push(b < a ? -1L : 0L); }
  public void eq() { long a = pop(); long b = pop(); push(b == a ? -1L : 0L); }
  public void gt() { long a = pop(); long b = pop(); push(b > a ? -1L : 0L); }
	public void zeq() { long n = pop(); push(n == 0 ? -1 : 0); }

  public void bfetch() { long a = pop(); push((long)block.get((int)a)); }
  public void bstore() { long a = pop(); long b = pop(); block.put((int)a, (byte)b); }

  public void sfetch() { long a = pop(); push((long)block.getShort((int)a)); }
  public void sstore() { long a = pop(); long b = pop(); block.putShort((int)a, (short)b); }

  public void ifetch() { long a = pop(); push((long)block.getInt((int)a)); }
  public void istore() { long a = pop(); long b = pop(); block.putInt((int)a, (int)b); }

  public void cfetch() { long a = pop(); push(block.getLong((int)a)); }
  public void cstore() { long a = pop(); long b = pop(); block.putLong((int)a, b); }

	public void literal(long n) {
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
    } else if (n >= -2147483648 && n <= 2147483647) {
      block.put(here(), (byte)'4');
			allot(1);
      block.putInt(here(), (int)n);
      allot(4);
    } else {
      block.put(here(), (byte)'8');
			allot(1);
      block.putLong(here(), (long)n);
      allot(8);
    }
	}

  public boolean tail() {
    return
      ip >= block.capacity()
      || block.get(ip) == ']'
      || block.get(ip) == '}';
  }

  public void execute() { long q = pop(); if (!tail()) r[rp++] = ip; ip = (int)q; }
  public void ret() { if (rp > 0) ip = (int)r[--rp]; else ip = block.capacity(); }
  public void eval(long q) { push(q); execute(); inner(); }
	public void quotation() { long d = pop(); push(ip); ip += d; }
	public void quit() { err = -256; }

  public void block() { 
	  push(ip);
    int t = 1;
    while (t > 0 && ip < block.capacity()) {
      switch (token()) {
        case '{': case '[': t++; break;
        case '}': case ']': t--; break;
      }
    }
  }

  public void number() {
    int n = 0;
    while (ip < block.capacity()) {
      byte c = peek();
      if (c >= '0' && c <= '9') {
        n = n * 10 + c - '0';
        ip++;
      } else break;
    }
    push(n);
  }

  public void string() {
    int c = 0;
    push(ip);
    while (ip < block.capacity() && token() != '"') { c++; }
    push(c); 
  }

  public void compare() {
    long l2 = pop();
    long s2 = pop();
    long l1 = pop();
    long s1 = pop();
    // TODO
  }

  public void copy() {
    long l = pop();
    long d = pop();
    long s = pop();
    for (int i = 0; i < l; i++) {
      block.put((int)d + i, block.get((int)s + i));
    }
  }

  public byte peek() { return block.get(ip); }
  public byte token() { return block.get(ip++); }

  public void step() {
		switch (peek()) {
  	  case 'A': case 'B':
  	  case 'C': case 'D':
  	  case 'E': case 'F':
  	  case 'G': case 'H':
  	  case 'I': case 'J':
  	  case 'K': case 'L':
  	  case 'M': case 'N':
  	  case 'O': case 'P':
  	  case 'Q': case 'R':
  	  case 'S': case 'T':
  	  case 'U': case 'V':
  	  case 'W': case 'X':
  	  case 'Y': case 'Z':
  	    extensions[(char)token() - 'A'].accept(this);
  	    break;
  	  default:
  	    switch (token()) {
  	      case '1': push(1L); break;
  	      case '#': number(); break;
  	      case '\'': push((long)block.get(ip++)); break;
  	      case '2': push((long)block.getShort(ip)); ip += 2; break;
  	      case '4': push((long)block.getInt(ip)); ip += 4; break;
  	      case '8': push((long)block.getLong(ip)); ip += 8; break;
  	        
  	      case '_': drop(); break;
  	      case 'd': dup(); break;
  	      case 'o': over(); break;
  	      case 's': swap(); break;
  	      case 'r': rot(); break;
  	      case 'n': nip(); break;
					case 'p': pick(); break;

					case '(': to_r(); break;
					case ')': from_r(); break;

  	      case '+': add(); break;
  	      case '-': sub(); break;
  	      case '*': mul(); break;
  	      case '/': div(); break;
  	      case '%': mod(); break;

  	      case '&': and(); break;
  	      case '|': or(); break;
  	      case '~': invert(); break;
  	      case '^': xor(); break;

  	      case '<': lt(); break;
  	      case '=': eq(); break;
  	      case '>': gt(); break;
					case '0': zeq(); break;

  	      case '!': cstore(); break;
  	      case '@': cfetch(); break;
  	        /*
  	      case '`': istore(); break;
  	      case '\'': ifetch(); break;
  	      */
  	      case ',': sstore(); break;
  	      case '.': sfetch(); break;
  	      case ';': bstore(); break;
  	      case ':': bfetch(); break;

					case 'l': long n = pop(); literal(n); break;

					case '[': quotation(); break;
  	      case '{': block(); break;
  	      case '}': case ']': ret(); break;
  	      case 'x': execute(); break;

  	      case '"': string(); break;
  	      case 'm': compare(); break;
  	      case 'y': copy(); break;

					case 'c': push(8); break;

					case 'u': tr = false; break;
					case 'v': tr = true; break;

					case 'q': quit(); break;
  	    }
		}
  }
  
  public void inner() {
    int t = rp;
    while (t <= rp && ip < block.capacity()) {
      if (tr) { trace(); System.out.println(); }
      step();
      // Manage errors
    }
  }
  
  public void isolated(String s) {
    block = ByteBuffer.wrap(s.getBytes(Charset.forName("UTF-8")));
    ip = 0;
    inner();
  }

  public void dump_code(int c) {
    int t = 1;
    while (t > 0 && c >= 0 && c < block.capacity()) {
      switch (block.get(c)) {
        case '{': case '[': t++; break;
        case '}': case ']': t--; break;
      }
      System.out.print((char)block.get(c++));
    }
  }
  
  public void trace() {
    for (int i = 0; i < sp; i++) {
      System.out.printf("%d ", s[i]);
    }
		if (ip < block.capacity()) {
	    System.out.print(" : ");
	    dump_code(ip);
		}
    for (int i = rp - 1; i >= 0; i--) {
      System.out.print(" : ");
      dump_code((int)r[i]);
    }
  }
}
