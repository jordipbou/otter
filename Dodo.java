import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Dodo {
  public Consumer<Dodo>[] x = new Consumer[26];
  public ByteBuffer d = ByteBuffer.allocateDirect(1024 * 1024);
  public int[] s = new int[256];
  public int sp = 0;
  public int[] r = new int[256];
  public int rp = 0;
  public int ip = d.capacity();
  public int err = 0;
  public boolean tr = false;

	public int here() { return d.position(); }
	public void allot(int n) { d.position(d.position() + n); }
	public void align() { int h = here(); d.position((h + 3) & ~(3)); }

	public int T() { return s[sp - 1]; }
	public int N() { return s[sp - 2]; }
	public int NN() { return s[sp - 3]; }

  public void push(int a) { s[sp++] = a; }
  public int pop() { return s[--sp]; }
  public void drop() { sp--; }

  public boolean tail() {
		return !(ip >= 0 && ip < d.capacity()) 
			|| d.get(ip) == ']' 
			|| d.get(ip) == '\\'; 
	}

  public void execute() { int q = pop(); if (!tail()) r[rp++] = ip; ip = q; }
	public void jump() { int d = pop(); ip += d; }
	public void zjump() { int d = pop(); int b = pop(); if (b == 0) ip += d; }
  public void ret() { if (rp > 0) ip = r[--rp]; else ip = d.capacity(); }
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
	
  public void block() {
		int t = 1;
		push(ip);
		while (t > 0) {
			switch (token()) {
			  case '{': t++;
				case '}': t--;
			}
		}
	}

	public void dump() {
		int n = pop();
		int a = pop();
		while (n > 0) {
			System.out.println();
			System.out.printf("%010d  ", a);
			for (int i = 0; i < 8; i++) { System.out.printf("%02X ", d.get(a + i)); }
			System.out.print(" ");
			for (int i = 0; i < 8; i++) { System.out.printf("%02X ", d.get(a + 8 + i)); }
			System.out.print("   ");
			for (int i = 0; i < 16; i++) { 
				char k = (char)d.get(a + i);
				if (k >= 32 && k < 127)	System.out.printf("%c", d.get(a + i));
				else System.out.printf(".");
			}
			a = a + 16;
			n = n - 16;
		}
		System.out.println();
	}

  public byte peek() { return d.get(ip); }
  public byte token() { return d.get(ip++); }

  public void step() {
    int a, b, c, n;
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

  	      case 'd': push(s[sp - 1]); break;
  	      case 'o': push(s[sp - 2]); break;
  	      case 's': a = pop(); b = pop(); push(a); push(b); break;
  	      case 'r': a = pop(); b = pop(); c = pop(); push(b); push(a); push(c); break;
  	      case 'n': s[sp - 2] = s[sp - 1]; sp--; break;
					case 'p': a = pop(); push(s[a - 1]); break;

					case 't': r[rp++] = s[--sp]; break;
					case 'f': s[sp++] = r[--rp]; break;
					/*
          case 'u': push(r[rp - 1]); break;
					case 'v': push(r[rp - 2]); break;
					case 'w': push(r[rp - 3]); break;
					case 'x': push(r[rp - 4]); break;
					case 'y': push(r[rp - 5]); break;
					case 'z': push(r[rp - 6]); break;
					*/

  	      case '+': s[sp - 2] = s[sp - 2] + s[sp - 1]; sp--; break;
  	      case '-': s[sp - 2] = s[sp - 2] - s[sp - 1]; sp--; break;
  	      case '*': s[sp - 2] = s[sp - 2] * s[sp - 1]; sp--; break;
  	      case '/': s[sp - 2] = s[sp - 2] / s[sp - 1]; sp--; break;
  	      case '%': s[sp - 2] = s[sp - 2] % s[sp - 1]; sp--; break;

  	      case '<': s[sp - 2] = s[sp - 2] < s[sp - 1] ? -1 : 0; sp--; break;
  	      case '=': s[sp - 2] = s[sp - 2] == s[sp - 1] ? -1 : 0; sp--; break;
  	      case '>': s[sp - 2] = s[sp - 2] > s[sp - 1] ? -1 : 0; sp--; break;
					case '0': s[sp - 1] = s[sp - 1] == 0 ? -1 : 0; break;

					case 'u':
						switch (token()) {
							case '<': a = pop(); b = pop(); push(Integer.toUnsignedLong(b) < Integer.toUnsignedLong(a) ? -1 : 0); break;
						}
						break;

          case '&': s[sp - 2] = s[sp - 2] & s[sp - 1]; sp--; break;
  	      case '|': s[sp - 2] = s[sp - 2] | s[sp - 1]; sp--; break;
  	      case '^': s[sp - 2] = s[sp - 2] ^ s[sp - 1]; sp--; break;
  	      case '~': s[sp - 1] = ~s[sp - 1]; break;
					case '(': s[sp - 2] = s[sp - 2] << s[sp - 1]; sp--; break;
					case ')': s[sp - 2] = s[sp - 2] >> s[sp - 1]; sp--; break;

  	      case '!': a = pop(); n = pop(); d.putInt(a, n); break;
  	      case '@': a = pop(); push(d.getInt(a)); break;
  	      case ';': a = pop(); n = pop(); d.putShort(a, (short)n); break;
  	      case ':': a = pop(); push((int)d.getShort(a)); break;
  	      case ',': a = pop(); b = (byte)pop(); d.put(a, (byte)b); break;
  	      case '.': a = pop(); push(d.get(a)); break;

					case '[': quotation(); break;
					case '\\': case '}': case ']': ret(); break;
					case '{': block(); break;

  	      case 'i': execute(); break;
					case 'j': jump(); break;
					case '?': zjump(); break;
					case 'c': push(4); break;
					case 'b': push(0); break;
					case 'e': err = s[sp - 1]; sp--; break;
					case 'q': quit(); break;

					case '`': dump(); break;

					case 'h': push(here()); break;
					case 'a': allot(pop()); break;
					case 'g': align(); break;
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
    ip = d.capacity() - l;
    for (int i = 0; i < l; i++) { d.put(ip + i, (byte)s.charAt(i)); }
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

	public static int SIZE = 0;
	public static int LATEST = SIZE + 4;
	public static int INTERPRETER = LATEST + 4;
	public static int STATE = INTERPRETER + 4;
	public static int IBUF = STATE + 4;
	public static int IPOS = IBUF + 256;
	public static int ILEN = IPOS + 4;

	public static byte HIDDEN = 4;
	public static byte IMMEDIATE = 2;
	public static byte VARIABLE = 1;
	public static byte NO_FLAGS = 0;

	public static int wFLAGS = 0;
	public static int wPREVIOUS = wFLAGS + 1;
	public static int wCODE = wPREVIOUS + 4;
	public static int wNAMELEN = wCODE + 4;
	public static int wNAME = wNAMELEN + 1;

	public void string_to_TIB(String s) {
		d.putInt(IPOS, 0);
		d.putInt(ILEN, s.length());
		for (int i = 0; i < s.length(); i++) d.put(IBUF + i, (byte)s.charAt(i));
	}

	public void parse_name() {
		int p = d.getInt(IPOS);
		int l = d.getInt(ILEN);
		while (p < l && Character.isSpace((char)d.get(IBUF + p))) { p++; }
		push(p);
		while (p < l && !Character.isSpace((char)d.get(IBUF + p))) { p++; }
		push(p - T());
		d.putInt(IPOS, p);
	}

	public boolean compare_without_case(int w, int t, int l) {
		if (d.get(w + wNAMELEN) != (byte)l) return false;
		for (int i = 0; i < l; i++) {
			int a = (int)d.get(w + wNAME + i);
			int b = (int)d.get(IBUF + t + i);
			if (a >= 97 && a <= 122) a = a - 32;
			if (b >= 97 && b <= 122) b = b - 32;
			if (a != b) return false;
		}
		return true;
	}

	public void find_name() {
		int l = pop();
		int t = pop();
		int w = d.getInt(LATEST);
		while (w < d.capacity()) {
			if (compare_without_case(w, t, l)) break;
			w = d.getInt(w + wPREVIOUS);
		}
		push(t);
		push(l);
		push(w);
	}

  public boolean do_asm(int l, int t) {
    if (l < 2 || d.get(IBUF + t) != (byte)'\\') return false;
    for (int i = 0; i < l - 1; i++) d.put(d.capacity() - l + i, d.get(IBUF + t + i + 1));
    d.put(d.capacity() - 1, (byte)']');
    eval(d.capacity() - l);
    return true;
  }

  public boolean do_casm(int l, int t) {
    if (l < 2 || d.get(IBUF + t) != (byte)'$') return false;
    for (int i = 0; i < l - 1; i++) d.put(d.get(IBUF + t + i + 1));
    return true;
  }

  public boolean do_colon(int l, int t) {
    if (l != 1 || d.get(IBUF + t) != (byte)':') return false;
    parse_name();
    l = pop();
    t = pop();
    align();
    int w = here();
    d.put(HIDDEN);
    d.putInt(d.getInt(LATEST));
    d.putInt(LATEST, w);
    d.putInt(0);
    d.put((byte)l);
    for (int i = 0; i < l; i++) d.put(d.get(IBUF + t + i));
    align();
    d.putInt(w + wCODE, here());
    d.putInt(STATE, 1);
    return true;
  }

  public boolean do_semicolon(int l, int t) {
    if (l != 1 || d.get(IBUF + t) != (byte)';') return false; 
    d.put((byte)']');
    d.putInt(STATE, 0);
    int w = d.getInt(LATEST);
    d.put(w + wFLAGS, NO_FLAGS);
    return true;
  }

  public void do_number(int l, int t) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < l; i++) sb.append((char)d.get(IBUF + t + i));
    try {
      int n = Integer.parseInt(sb.toString());
      if (d.getInt(STATE) > 0) literal(n);
      else push(n);
    } catch (NumberFormatException e) {
      err = -13;
    }
  }

	public void do_interpret(int w) {	eval(d.getInt(w + wCODE)); }

	public void do_compile(int w) {
		int t = 1;
		int c = d.getInt(w + wCODE);
		while (t > 0) {
			byte k = d.get(c);
			if (k == '[' || k == '{') t++;
			if (k == ']' || k == '}') t--;
			if (t > 0) d.put(k);
			c++;
		}
	}

 	public void interpret() {
		int i = d.getInt(INTERPRETER);
		if (i < d.capacity()) { eval(i); }
		else {
			while (err == 0) {
				parse_name();
				if (T() == 0) { drop(); drop(); return; }
				find_name();
				if (T() < d.capacity()) {
					int w = pop();
					drop(); drop();
					if (d.getInt(STATE) == 0 || (d.get(w + wFLAGS) & IMMEDIATE) == IMMEDIATE) {
						do_interpret(w);
					} else {
						do_compile(w);
					}
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

	public void evaluate(String s) {
		string_to_TIB(s);
		interpret();
	}

  // ----- INITIALIZATION -----

	public Dodo() {
		d.putInt(SIZE, d.capacity());
		d.putInt(LATEST, d.capacity());
		d.putInt(INTERPRETER, d.capacity());	
		d.position(ILEN + 4);
	}
}
