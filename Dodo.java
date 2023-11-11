import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Dodo {
  public Consumer<Dodo>[] x = new Consumer[26];
  public ByteBuffer d = ByteBuffer.allocateDirect(64 * 1024 * 4);
  public int[] s = new int[256];
  public int sp = 0;
  public int[] r = new int[256];
  public int rp = 0;
  public int ip = d.capacity();
  public int err = 0;
  public boolean tr = false;
  public int ipos = 0;
  public int ilen = 0;
  public int state = 0;
  public int latest = -1;

  public static int ABUF = 0;
  public static int IBUF = 64;

  public Dodo() {
    d.position(IBUF + 84);
  }

  public int here() { return d.position(); }
  public void allot(int a) { d.position(d.position() + a); }
  public void align() { int h = here(); d.position((h + 3) & ~3); }

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

  public void add() { int a = pop(); int b = pop(); push(b + a); }
  public void sub() { int a = pop(); int b = pop(); push(b - a); }
  public void mul() { int a = pop(); int b = pop(); push(b * a); }
  public void div() { int a = pop(); int b = pop(); push(b / a); }
  public void mod() { int a = pop(); int b = pop(); push(b % a); }

  public void lt() { int a = pop(); int b = pop(); push(b < a ? -1 : 0); }
  public void eq() { int a = pop(); int b = pop(); push(b == a ? -1 : 0); }
  public void gt() { int a = pop(); int b = pop(); push(b > a ? -1 : 0); }
	public void zeq() { int n = pop(); push(n == 0 ? -1 : 0); }

  public void and() { int a = pop(); int b = pop(); push(b & a); }
  public void or() { int a = pop(); int b = pop(); push(a | b); }
  public void xor() { int a = pop(); int b = pop(); push(b ^ a); }
  public void invert() { int a = pop(); push(~a); }
  
  public boolean tail() { return ip >= d.capacity() || d.get(ip) == ']' || d.get(ip) == '}'; }

  public void execute() { int q = pop(); if (!tail()) r[rp++] = ip; ip = q; }
	public void jump() { int d = pop(); ip += d; }
	public void zjump() { int d = pop(); int b = pop(); if (b == 0) ip += d; }
  public void ret() { if (rp > 0) ip = r[--rp]; else ip = d == null ? 0 : d.capacity(); }
  public void eval(int q) { push(q); execute(); inner(); }
	public void quotation() { int d = pop(); push(ip); ip += d; }
  
	public void quit() { err = -256; }

  public void literal(int n) {
    if (n == 1) {
      d.put((byte)'1');
    } else if (n >= -128 && n <= 127) {
      d.put((byte)'\'');
      d.put((byte)n);
    } else if (n >= -32768 && n <= 32767) {
      d.put((byte)'2');
      d.putShort((short)n);
    } else {
      d.put((byte)'4');
      d.putInt(n);
    }
  }
  
  public byte peek() { return d.get(ip); }
  public byte token() { return d.get(ip++); }

  public void step() {
    int a, n;
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
					case '$': d.put(token()); break;
  	      case '1': push(1); break;
          case '\'': push((int)d.get(ip)); ip += 1; break;
          case '2': push((int)d.getShort(ip)); ip += 2; break;
          case '4': push(d.getInt(ip)); ip += 4; break;
  	      case '_': drop(); break;
  	      case 'd': dup(); break;
  	      case 'o': over(); break;
  	      case 's': swap(); break;
  	      case 'r': rot(); break;
  	      case 'n': nip(); break;
					case 'p': pick(); break;
					case '(': to_r(); break;
					case ')': from_r(); break;
          case 'f': fetch_r(); break;
  	      case '+': add(); break;
  	      case '-': sub(); break;
  	      case '*': mul(); break;
  	      case '/': div(); break;
  	      case '%': mod(); break;
  	      case '<': lt(); break;
  	      case '=': eq(); break;
  	      case '>': gt(); break;
					case '0': zeq(); break;
          case '&': and(); break;
  	      case '|': or(); break;
  	      case '^': xor(); break;
  	      case '~': invert(); break;
  	      case '!': a = pop(); n = pop(); d.putInt(a, n); break;
  	      case '@': a = pop(); push(d.getInt(a)); break;
  	      case ';': a = pop(); n = pop(); d.putShort(a, (short)n); break;
  	      case ':': a = pop(); push((int)d.getShort(a)); break;
  	      case ',': a = pop(); b = (byte)pop(); d.put(a, b); break;
  	      case '.': a = pop(); push(d.get(a)); break;
					case 'h': push(here()); break;
					case 'a': n = pop(); allot(n); break;
					case 'g': align(); break;
					case '[': quotation(); break;
          case ']': ret(); break;
  	      case 'i': execute(); break;
					case 'j': jump(); break;
					case 'z': zjump(); break;
					case 'c': push(4); break;
					case 'u': tr = false; break;
					case 'v': tr = true; break;
					case 'q': quit(); break;
          case 'e': push(latest); break;
          case 'w': compile(); break;
          case 'x': parse_name(); break;
          case 'y': find_name(); break;
          case 'l': n = pop(); literal(n); break;
        }
		}
  }

  public void inner() {
    int t = rp;
    while (t <= rp && ip < d.capacity()) {
      if (tr) { trace(); System.out.println(); }
      step();
      // Manage errors
    }
  }

  public void asm(String s) {
    int l = s.length();
    for (int i = 0; i < l; i++) { d.put(ABUF + i, (byte)s.charAt(i)); }
    d.put(ABUF + l, (byte)']');
    ip = ABUF;
    inner();
  }

  // Tracing
  public void dump_code(int c) {
    int t = 1;
		char k;
    while (t > 0 && c < d.capacity()) {
      switch (k = (char)d.get(c++)) {
        case '{': case '[': t++; System.out.print(k); break;
        case '}': case ']': t--; System.out.print(k); break;
        case '\'': System.out.printf("#%d", d.get(c++)); break;
        case '2': System.out.printf("#%d", d.getShort(c)); c += 2; break;
        case '4': System.out.printf("#%d", d.getInt(c)); c += 4; break;
        default: System.out.print(k); break;
      }
    }
  }
  
  public void trace() {
    System.out.printf("<%d> ", state);
    for (int i = 0; i < sp; i++) {
      System.out.printf("%d ", s[i]);
    }
		if (ip < d.capacity()) {
	    System.out.print(" : ");
	    dump_code(ip);
		}
    for (int i = rp - 1; i >= 0; i--) {
      System.out.print(" : ");
      dump_code((int)r[i]);
    }
  }

  // ----- OUTER INTERPRETER ------

  public static byte NO_FLAGS = 0;
  public static byte HIDDEN = 1;
  public static byte IMMEDIATE = 2;
  public static byte VARIABLE = 4;
  
  public static int wPREVIOUS = 0;
  public static int wCODE = 4;
  public static int wFLAGS = 8;
  public static int wNAMELEN = 9;
  public static int wNAME = 10;
  
  public void parse_name() {
    while (ipos < ilen && Character.isSpace((char)d.get(IBUF + ipos))) { ipos++; }
    push(IBUF + ipos);
    while (ipos < ilen && !Character.isSpace((char)d.get(IBUF + ipos))) { ipos++; }
    push(IBUF + ipos - T());
  }

  public boolean compareWithoutCase(int w, int t, int l) {
    if (d.get(w + wNAMELEN) != l) return false;
    for (int i = 0; i < l; i++) {
      byte wc = d.get(w + wNAME + i);
      byte tc = d.get(t + i);
      if (wc > 96 && wc < 123) wc -= 32;
      if (tc > 96 && tc < 123) tc -= 32;
      if (wc != tc) return false; 
    }
    return true;
  }
  
  public void find_name() {
    int l = pop();
    int t = pop();
    int w = latest;
    while (w != -1) {
      if (compareWithoutCase(w, t, l) && !is_hidden(w)) break;
      w = d.getInt(w + wPREVIOUS);
    }
    push(t);
    push(l);
    push(w);
  }

  public boolean is_hidden(int w) {
    return (d.get(w + wFLAGS) & HIDDEN) == HIDDEN;
  }
  
  public boolean is_immediate(int w) {
    return (d.get(w + wFLAGS) & IMMEDIATE) == IMMEDIATE;
  }

  public void do_interpret(int w) {
    int c = d.getInt(w + wCODE);
    eval(c);
  }

  public void do_compile(int w) {
    int c = d.getInt(w + wCODE);
    int t = 1;
    while (t > 0) {
      if (d.get(c) == '[') t++;
      if (d.get(c) == ']') t--;
      if (t > 0) d.put(d.get(c++));
    }
  }

  public void compile() {
    int w = pop();
    do_compile(w);
  }

  public boolean do_asm(int l, int t) {
    if (l < 2 || d.get(t) != (byte)'\\') return false;
    for (int i = 0; i < l - 1; i++) d.put(ABUF + i, d.get(t + i + 1));
    d.put(ABUF + l, (byte)']');
    eval(ABUF);
    return true;
  }

  public boolean do_casm(int l, int t) {
    if (l < 2 || d.get(t) != (byte)'$') return false;
    for (int i = 0; i < l - 1; i++) d.put(d.get(t + i + 1));
    return true;
  }

  public boolean do_colon(int l, int t) {
    if (l != 1 || d.get(t) != (byte)':') return false;
    parse_name();
    l = pop();
    t = pop();
    align();
    int w = here();
    d.putInt(latest);
    latest = w;
    d.putInt(0);
    d.put(HIDDEN);
    d.put((byte)l);
    for (int i = 0; i < l; i++) d.put(d.get(t + i));
    align();
    d.putInt(w + wCODE, here());
    state = 1;
    return true;
  }

  public boolean do_semicolon(int l, int t) {
    if (l != 1 || d.get(t) != (byte)';') return false; 
    d.put((byte)']');
    state = 0;
    int w = latest;
    d.put(w + wFLAGS, NO_FLAGS);
    return true;
  }

  public void do_number(int l, int t) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < l; i++) sb.append((char)d.get(t + i));
    try {
      int n = Integer.parseInt(sb.toString());
      if (state > 0) literal(n);
      else push(n);
    } catch (NumberFormatException e) {
      err = -13;
    }
  }
  
  public void evaluate(String s) {
    ipos = 0;
    ilen = s.length();
    for (int i = 0; i < s.length(); i++) { d.put(IBUF + i, (byte)s.charAt(i)); }
    while (ipos < ilen && err == 0) {
      parse_name();
      if (T() == 0) { drop(); drop(); return; }
      find_name();
      if (T() != -1) {
        short w = (short)pop();
        drop();
        drop();
        if (state == 0 || is_immediate(w)) do_interpret(w);
        else do_compile(w);
      } else {
        drop();
        int l = pop();
        int t = pop();
        if (!do_asm(l, t))
          if (!do_casm(l, t))
            if (!do_colon(l, t))
              if (!do_semicolon(l, t))
                do_number(l, t);
      }
    }
  }
}