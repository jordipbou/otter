import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class Otter {
	public static int pSIZE = 0;
	public static int pHERE = 4;
	public static int pFREEZE = 8;

  public ByteBuffer block;
  public Consumer<Otter>[] extensions;
  public int[] s;
  public int sp;
  public int[] r;
  public int rp;
  public int ip;
	public int err;
	public boolean tr;

  public Otter() {
    s = new int[256];
    sp = 0;
    r = new int[256];
    rp = 0;
    ip = 0;
    extensions = new Consumer[26];
		err = 0;
		tr = false;
  }

	public void init_block(int size) {
		block = ByteBuffer.allocateDirect(size);
		block.putInt(pSIZE, size);						// BLOCK SIZE
		block.putInt(pHERE, pFREEZE + 4);			// HERE
		block.putInt(pFREEZE, pFREEZE + 4);		// FREEZE POINT
		ip = block.capacity();
	}

	public int here() { return block.getInt(pHERE); }
	public void allot(int n) { block.putInt(pHERE, block.getInt(pHERE) + n); }
  public void align() { int h = block.getInt(pHERE); h = (h + 7) & ~(7); block.putInt(pHERE, h); }
 
  public int T() { return s[sp - 1]; }
  public int N() { return s[sp - 2]; }
  public int NN() { return s[sp - 3]; }

  public void push(int a) { s[sp++] = a; }
  public int pop() { return s[--sp]; }
  public void drop() { sp--; }
  public void dup() { int a = pop(); push(a); push(a); }
  public void over() { int a = pop(); int b = pop(); push(b); push(a); push(b); }
  public void swap() { int a = pop(); int b = pop(); push(a); push(b); }
  public void rot() { int a = pop(); int b = pop(); int c = pop(); push(b); push(a); push(c); }
  public void nip() { int a = pop(); int b = pop(); push(a); }
	public void pick() { int n = pop(); push(s[sp - 1 - n]); }

	public void to_r() { r[rp++] = s[--sp];	}
	public void from_r() { s[sp++] = r[--rp];	}
  
  public void add() { int a = pop(); int b = pop(); push(b + a); }
  public void sub() { int a = pop(); int b = pop(); push(b - a); }
  public void mul() { int a = pop(); int b = pop(); push(b * a); }
  public void div() { int a = pop(); int b = pop(); push(b / a); }
  public void mod() { int a = pop(); int b = pop(); push(b % a); }

  public void and() { int a = pop(); int b = pop(); push(b & a); }
  public void or() { int a = pop(); int b = pop(); push(a | b); }
  public void xor() { int a = pop(); int b = pop(); push(b ^ a); }
  public void invert() { int a = pop(); push(~a); }

  public void lt() { int a = pop(); int b = pop(); push(b < a ? -1 : 0); }
  public void eq() { int a = pop(); int b = pop(); push(b == a ? -1 : 0); }
  public void gt() { int a = pop(); int b = pop(); push(b > a ? -1 : 0); }
	public void zeq() { int n = pop(); push(n == 0 ? -1 : 0); }

  public void bfetch() { int a = pop(); push((int)block.get(a)); }
  public void bstore() { int a = pop(); int b = pop(); block.put(a, (byte)b); }
  public void wfetch() { int a = pop(); push((int)block.getShort(a)); }
  public void wstore() { int a = pop(); int b = pop(); block.putShort(a, (short)b); }
  public void lfetch() { int a = pop(); push(block.getInt(a)); }
  public void lstore() { int a = pop(); int b = pop(); block.putInt(a, b); }
	// If a 32 bit system, cfetch and cstore its equivalent to lfetch and lstore
  public void cfetch() { int a = pop(); push(block.getInt(a)); }
  public void cstore() { int a = pop(); int b = pop(); block.putInt(a, b); }

	// These ones don't need to be bytecodes (they're very easily replicated from here / allot
	// and store. But are practical for the API.
	// Not every function must be a bytecode  if it can be easily replicated !!
	public void bcompile(byte n) { block.put(here(), n); allot(1); }
	public void scompile(short n) { block.putShort(here(), n); allot(2); }
	public void ccompile(int n) { block.putInt(here(), n); allot(4); }

	public void literal(int n) {
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

  public boolean tail() {
    return
      ip >= block.capacity()
      || block.get(ip) == ']'
      || block.get(ip) == '}';
  }

  public void execute() { int q = pop(); if (!tail()) r[rp++] = ip; ip = q; }
	public void jump() { int d = pop(); ip += d; }
	public void zjump() { int d = pop(); int b = pop(); if (b == 0) ip += d; }
  public void ret() { if (rp > 0) ip = r[--rp]; else ip = block.capacity(); }
  public void eval(int q) { push(q); execute(); inner(); }
	public void quotation() { int d = pop(); push(ip); ip += d; }

	// public void fmark() { bcompile((byte)'2'); push(here()); scompile((short)0); }
  public void fresolve() { int a = pop(); int d = here() - a - 3; block.putShort(a, (short)d); }

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
    int l2 = pop();
    int s2 = pop();
    int l1 = pop();
    int s1 = pop();
    if (l1 != l2) push(0);
		else {
			for (int i = 0; i < l1; i++) {
				if (block.get(s1 + i) != block.get(s2 + i)) { push(0); return; }
			}
			push(-1);
		}
  }

	public void compareWithoutCase() {
		int l2 = pop();
		int s2 = pop();
	  int l1 = pop();
		int s1 = pop();
		if (l1 != l2) push(0);
		else {
			for (int i = 0; i < l1; i++) {
				byte a = block.get(s1 + i);
				byte b = block.get(s2 + i);
				if (a >= 97 && a <= 122) a -= 32;
				if (b >= 97 && b <= 122) b -= 32;
				if (a != b) { push(0); return; }
			}
			push(-1);
		}
	}

  public void copy() {
    int l = pop();
    int d = pop();
    int s = pop();
    for (int i = 0; i < l; i++) {
      block.put(d + i, block.get(s + i));
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
					case '$': bcompile(token()); break;

  	      case '1': push(1); break;
  	      case '#': number(); break;
  	      case '\'': push((int)block.get(ip++)); break;
  	      case '2': push((int)block.getShort(ip)); ip += 2; break;
  	      case '4': push(block.getInt(ip)); ip += 4; break;
  	        
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
					case '`': lstore(); break;
					case '\\': lfetch(); break;
  	      case ';': wstore(); break;
  	      case ':': wfetch(); break;
  	      case ',': bstore(); break;
  	      case '.': bfetch(); break;

					case 'h': push(block.getInt(pHERE)); break;
					case 'a': int n = pop(); allot((int)n); break;
					case 'g': align(); break;

					case 'l': n = pop(); literal(n); break;

					case '[': quotation(); break;
  	      case '{': block(); break;
  	      case '}': case ']': ret(); break;
  	      case 'i': execute(); break;
					case 'j': jump(); break;
					case 'z': zjump(); break;

					// case 'f': fmark(); break;
					case 'b': fresolve(); break;

  	      case '"': string(); break;
  	      case 'm': compare(); break;
					case 'w': compareWithoutCase(); break;
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
		char k;
    while (t > 0 && c >= 0 && c < block.capacity()) {
      switch (k = (char)block.get(c++)) {
        case '{': case '[': t++; System.out.print(k); break;
        case '}': case ']': t--; System.out.print(k); break;
				case '\'': System.out.printf("#%d", block.get(c++)); break;
				case '2': System.out.printf("#%d", block.getShort(c)); c += 2; break;
				case '4': System.out.printf("#%d", block.getInt(c)); c += 4; break;
				case 10: break;
				default: System.out.print(k);	break;
      }
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
