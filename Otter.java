import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class Otter {
  // VCPU's own memory mapping
  private static int ABUF = -65;
  private static int IBUF = ABUF - 80;
  private static int PAD = IBUF - 84;
  private static int CSTRINGS = PAD - 33;
  private static int NUMOUT = CSTRINGS - 18;
  private static int LIMIT = NUMOUT - 18;

  // VCPU mem
  private static int MSZ = 1024; 
  public ByteBuffer m;
 
  // VCPU registers 
  public int sp;
  public int rp;
  public int ip;
	public int err;
	public boolean tr; 
  public int ipos;
  public int ilen; 

  // VCPU stacks
  public int[] s;
  public int[] r;

  // Dictionary (shared memory between VCPUs)
  public ByteBuffer d;
  public static int LATEST = 64 * 1024 * 4;

  // Extensions (shared between VCPUs)
  public Consumer<Otter>[] x; 

  // Initialization
  public Otter() {
    m = ByteBuffer.allocateDirect(1024);
    ipos = 0;
    ilen = 0;
    s = new int[256];
    sp = 0;
    r = new int[256];
    rp = 0;
    ip = 0;
		err = 0;
		tr = false;  
  }

  public Consumer<Otter>[] init_extensions() {
    return new Consumer[26];
  }

  public ByteBuffer init_dictionary(int size) {
    ByteBuffer d = ByteBuffer.allocateDirect(size);
    d.position(LATEST + 4);
    return d;
  }

  // Stack operations (don't depend on VCPU memory or on dictionary)
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
  public void fetch_r() { s[sp++] = r[rp - 1]; }

  // Arithmetic operations (don't depend on VCPU memory or on dictionary)
  public void add() { int a = pop(); int b = pop(); push(b + a); }
  public void sub() { int a = pop(); int b = pop(); push(b - a); }
  public void mul() { int a = pop(); int b = pop(); push(b * a); }
  public void div() { int a = pop(); int b = pop(); push(b / a); }
  public void mod() { int a = pop(); int b = pop(); push(b % a); }

  // Bitwise operations (don't depend on VCPU memory on on dictionary)
  public void and() { int a = pop(); int b = pop(); push(b & a); }
  public void or() { int a = pop(); int b = pop(); push(a | b); }
  public void xor() { int a = pop(); int b = pop(); push(b ^ a); }
  public void invert() { int a = pop(); push(~a); }

  public void lt() { int a = pop(); int b = pop(); push(b < a ? -1 : 0); }
  public void eq() { int a = pop(); int b = pop(); push(b == a ? -1 : 0); }
  public void gt() { int a = pop(); int b = pop(); push(b > a ? -1 : 0); }
	public void zeq() { int n = pop(); push(n == 0 ? -1 : 0); }

  // Memory fetch operations (from VCPU memory and from dictionary -if defined-)
  public byte fbyte(int a) { return (a < 0) ? m.get(MSZ + a) : d.get(a); }
  public short fshort(int a) { return (a < 0) ? m.getShort(MSZ + a) : d.getShort(a); }
  public int fcell(int a) { return (a < 0) ? m.getInt(MSZ + a) :  d.getInt(a); }

  // Memory store operations (on VCPU memory or on dictionary -if defined-)
  public void sbyte(int a, byte b) { if (a < 0) m.put(1024 + a, b); else d.put(a, b); }
  public void sshort(int a, short s) { if (a < 0) m.putShort(1024 + a, s); else d.putShort(a, s); }
  public void scell(int a, int c) { if (a < 0) m.putInt(1024 + a, c); else d.putInt(a, c); }

  // Compile into dictionary
  public void cbyte(byte b) { d.put(b); }
  public void cshort(short s) { d.putShort(s); }
  public void ccell(int c) { d.putInt(c); }

  public int here() { return d.position(); }
  public void allot(int a) { d.position(d.position() + a); }
  public void align() { int h = here(); d.position((h + 3) & ~3); }

 	public void literal(int n) {
    if (n == 1) {
      d.put((byte)'1');
    } else if (n >= -128 && n <= 127) {
      d.put((byte)'\\');
      d.put((byte)n);
    } else if (n >= -32768 && n <= 32767) {
      d.put((byte)'2');
      d.putShort((short)n);
    } else {
      d.put((byte)'4');
      d.putInt(n);
    }
	}

  // Execution operations (depend on fetch memory operations)
  public boolean tail() {
    return
      ip >= d.capacity()
      || d.get(ip) == ']'
      || d.get(ip) == '}';
  }

  public void execute() { int q = pop(); if (!tail()) r[rp++] = ip; ip = q; }
	public void jump() { int d = pop(); ip += d; }
	public void zjump() { int d = pop(); int b = pop(); if (b == 0) ip += d; }
  public void ret() { if (rp > 0) ip = r[--rp]; else ip = d == null ? 0 : d.capacity(); }
  public void eval(int q) { push(q); execute(); inner(); }
	public void quotation() { int d = pop(); push(ip); ip += d; }
  
	public void quit() { err = -256; }

  // Literals, don't really needed except for testing
  public void block() { 
	  push(ip);
    int t = 1;
    while (t > 0 && ip < d.capacity()) {
      switch (token()) {
        case '{': case '[': t++; break;
        case '}': case ']': t--; break;
      }
    }
  }

  public void number() {
    int n = 0;
    while (ip < d.capacity()) {
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
    while (ip < d.capacity() && token() != '"') { c++; }
    push(c); 
  }

  // String operations (need memory -VCPU and if defined dictionary-)
  public void compare(boolean withoutCase) {
    int l2 = pop();
    int s2 = pop();
    int l1 = pop();
    int s1 = pop();
    if (l1 != l2) push(0);
		else {
			for (int i = 0; i < l1; i++) {
				byte a = fbyte(s1 + i);
				byte b = fbyte(s2 + i);
				if (withoutCase && a >= 97 && a <= 122) a -= 32;
				if (withoutCase && b >= 97 && b <= 122) b -= 32;
				if (a != b) { push(0); return; }			}
			push(-1);
		}
  }

  public void copy() {
    int l = pop();
    int d = pop();
    int s = pop();
    for (int i = 0; i < l; i++) {
      sbyte(d + i, fbyte(s + i));
    }
  }

  public void type() {
    int l = pop();
    int s = pop();
    for (int i = 0; i < l; i++) {
      push(fbyte(s + i));
      x['E' - 'A'].accept(this);
    }
  }

  // Inner interpreter
  
  public byte peek() { return fbyte(ip); }
  public byte token() { return fbyte(ip++); }

  public void step() {
    int a, n;
    short s;
    byte b;
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
  	    x[(char)token() - 'A'].accept(this);
  	    break;
  	  default:
  	    switch (token()) {
					case '$': cbyte(token()); break;

  	      case '1': push(1); break;
  	      case '#': number(); break;

          case '\'': push((int)fbyte(ip++)); break;
          case '2': push((int)fshort(ip)); ip += 2; break;
          case '4': push(fcell(ip)); ip += 4; break;
  	        
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

  	      case '!': a = pop(); n = pop(); scell(a, n); break;
  	      case '@': a = pop(); push(fcell(a)); break;
  	      case ';': a = pop(); s = (short)pop(); sshort(a, s); break;
  	      case ':': a = pop(); push(fshort(a)); break;
  	      case ',': a = pop(); b = (byte)pop(); sbyte(a, b); break;
  	      case '.': a = pop(); push(fbyte(a)); break;

					case 'h': push(here()); break;
					case 'a': n = pop(); allot(n); break;
					case 'g': align(); break;

					case 'l': n = pop(); literal(n); break;

					case '[': quotation(); break;
  	      case '{': block(); break;
  	      case '}': case ']': ret(); break;
  	      case 'i': execute(); break;
					case 'j': jump(); break;
					case 'z': zjump(); break;

  	      case '"': string(); break;
  	      case 'm': compare(false); break;
					case 'w': compare(true); break;
  	      case 'y': copy(); break;

					case 'c': push(4); break;

					case 'u': tr = false; break;
					case 'v': tr = true; break;

					case 'q': quit(); break;

          case 'x':
            switch (token()) {
              case 'e':
                switch (token()) {
                  case '@': push(err); break;
                  case '!': err = pop(); break;
                }
                break;
              case 'i':
                switch (token()) {
                  case '@': push(ip); break;
                  case '!': ip = pop(); break;
                }
                break;
              case 's':
                switch (token()) {
                  case '@': push(sp); break;
                  case '!': sp = pop(); break;
                }
                break;
              case 'r':
                switch (token()) {
                  case '@': push(rp); break;
                  case '!': rp = pop(); break;
                }
                break;
            }
            break;
  	    }
		}
  }

  public boolean valid(int p) {
    if (d == null) {
      return p >= (-MSZ) && p < 0;
    } else {
      return p >= (-MSZ) && p < d.capacity(); 
    }
  }
  
  public void inner() {
    int t = rp;
    while (t <= rp && valid(ip)) {
      if (tr) { trace(); System.out.println(); }
      step();
      // Manage errors
    }
  }

  // Isolated execution (without evaluation)
  public void asm(String s) {
    int l = s.length();
    for (int i = 0; i < l; i++) { sbyte(ABUF + i, (byte)s.charAt(i)); }
    sbyte(ABUF + l, (byte)']');
    ip = ABUF;
    inner();
  }

  // Tracing
  public void dump_code(int c) {
    int t = 1;
		char k;
    while (t > 0 && valid(c)) {
      switch (k = (char)fbyte(c++)) {
        case '{': case '[': t++; System.out.print(k); break;
        case '}': case ']': t--; System.out.print(k); break;
        case '\'': System.out.printf("#%d", fbyte(c++)); break;
        case '2': System.out.printf("#%d", fshort(c)); c += 2; break;
        case '4': System.out.printf("#%d", fcell(c)); c += 4; break;
      }
    }
  }
  
  public void trace() {
    for (int i = 0; i < sp; i++) {
      System.out.printf("%d ", s[i]);
    }
		if (valid(ip)) {
	    System.out.print(" : ");
	    dump_code(ip);
		}
    for (int i = rp - 1; i >= 0; i--) {
      System.out.print(" : ");
      dump_code((int)r[i]);
    }
  }

  // Evaluation (names)
  public void parse_name() {
    while (ipos < ilen && Character.isSpace((char)fbyte(IBUF + ipos))) { ipos++; }
    push(IBUF + ipos);
    int l = ipos;
    while (ipos < ilen && !Character.isSpace((char)fbyte(IBUF + ipos))) { ipos++; }
    push(ipos - l);
  }

  public void find_name() {
    int l = pop();
    int s = pop();
    // define starting word from latest
    //while () {
    //  
    //}
  }
  
  public void evaluate(String s) {
    ipos = 0;
    ilen = s.length();
    for (int i = 0; i < ilen; i++) { sbyte(IBUF + i, (byte)s.charAt(i)); }
    while (ipos < ilen) {
      parse_name();
      find_name();
    }
  }

  // Dictionary
  public void create() {
    parse_name();
    if (T() == 0) { drop(); drop(); err = -13; return; }
    
  }
}
