import java.nio.ByteBuffer;
import java.util.function.Consumer;

/* ERROR MANAGEMENT */

public class Dodo {
  public Consumer<Dodo>[] x = new Consumer[26];

	public Dictionary d;

	// General registers
  public int[] s;
  public int sp;
  public int[] r;
  public int rp;
  public int ip;
  public int err;

	// Debugging registers
	public boolean tr;

	public Dodo(Dictionary dict) {
		d = dict;
		s = new int[256];
		sp = 0;
		r = new int[256];
		rp = 0;
		ip = d.capacity();
		err = 0;
		tr = false;
	}

	// Stack helpers
	public int T() { return s[sp - 1]; }
	public int N() { return s[sp - 2]; }
	public int NN() { return s[sp - 3]; }

  public void push(int a) { s[sp++] = a; }
  public int pop() { return s[--sp]; }
  public void drop() { sp--; }
	public void over() {push(s[sp - 2]); }

	// Execution helpers
  public byte peek() { return d.get(ip); }
  public byte token() { return d.get(ip++); }
	public boolean in_dict() { return ip >= 0 && ip < d.capacity(); }

  public boolean tail() {	return !in_dict() || peek() == ']' || peek() == '\\'; }
  public void execute() { int q = pop(); if (!tail()) r[rp++] = ip; ip = q; }
	public void jump() { int d = pop(); ip += d; }
	public void zjump() { int d = pop(); int b = pop(); if (b == 0) ip += d; }
  public void ret() { if (rp > 0) ip = r[--rp]; else ip = d.capacity(); }
  public void eval(int q) { push(q); execute(); inner(); }
	public void quotation() { int d = pop(); push(ip); ip += d; }
  
	public void quit() { err = -256; }

  public void literal(int n) {
    if (n == 1) { d.bcompile((byte)'1'); } 
		else if (n >= -128 && n <= 127) { d.bcompile((byte)'\''); d.bcompile((byte)n); } 
		else if (n >= -32768 && n <= 32767) { d.bcompile((byte)'2'); d.wcompile((short)n); } 
		else { d.bcompile((byte)'4'); d.ccompile(n); }
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

	// Literal helpers
	public void string() { push(ip); while (token() != '"') { }	push(ip - T() - 1); }
	public void number() { 
		int n = 0;
		for (byte k = token(); k >= 48 && k <= 57; k = token()) { n = 10*n + (k - 48); }
		ip--;
		push(n);
	}
  public void block() { 
		push(ip); 
		for (int t = 1; t > 0;) { 
			switch (token()) { 
				case '{': t++; 
				case '}': t--; 
			} 
		} 
	}

	// Basic combinators
	public void times() { int q = pop(); int l = pop(); for (;l > 0; l--) { eval(q); } }
	public void choice() { int f = pop(); int t = pop(); if (pop() != 0) eval(t); else eval(f); }

	// String helpers
	public void type() { 
		int l = pop(); 
		int s = pop(); 
		for (int i = 0; i < l; i++) { 
			push(d.get(s + i)); 
			x['E' - 'A'].accept(this); 
		} 
	}

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
					case '"': string(); break;
					case '#': number(); break;
					case '?': choice(); break;
					case 't': times(); break;
					case 'y': type(); break;

					case '$': d.bcompile(token()); break;

  	      case '1': push(1); break;
          case '\'': push((int)d.get(ip)); ip += 1; break;
          case '2': push((int)d.getShort(ip)); ip += 2; break;
          case '4': push(d.getInt(ip)); ip += 4; break;
  	      case '_': drop(); break;

  	      case 'd': push(s[sp - 1]); break;
  	      case 'o': push(s[sp - 2]); break;
  	      case 's': a = pop(); b = pop(); push(a); push(b); break;
  	      case 'r': a = pop(); b = pop(); c = pop(); push(b); push(a); push(c); break;
					/*
  	      case 'n': s[sp - 2] = s[sp - 1]; sp--; break;
					case 'p': a = pop(); push(s[a - 1]); break;
					*/
					case '(': r[rp++] = s[--sp]; break;
					case ')': s[sp++] = r[--rp]; break;
					case 'f': s[sp++] = r[rp - 1]; break;

  	      case '+': s[sp - 2] = s[sp - 2] + s[sp - 1]; sp--; break;
  	      case '-': s[sp - 2] = s[sp - 2] - s[sp - 1]; sp--; break;
  	      case '*': s[sp - 2] = s[sp - 2] * s[sp - 1]; sp--; break;
  	      case '/': s[sp - 2] = s[sp - 2] / s[sp - 1]; sp--; break;
  	      case '%': s[sp - 2] = s[sp - 2] % s[sp - 1]; sp--; break;

  	      case '<': s[sp - 2] = s[sp - 2] < s[sp - 1] ? -1 : 0; sp--; break;
  	      case '=': s[sp - 2] = s[sp - 2] == s[sp - 1] ? -1 : 0; sp--; break;
  	      case '>': s[sp - 2] = s[sp - 2] > s[sp - 1] ? -1 : 0; sp--; break;
					case '0': s[sp - 1] = s[sp - 1] == 0 ? -1 : 0; break;

					/*
					case 'u':
						switch (token()) {
							case '<': a = pop(); b = pop(); push(Integer.toUnsignedLong(b) < Integer.toUnsignedLong(a) ? -1 : 0); break;
						}
						break;
					*/

          case '&': s[sp - 2] = s[sp - 2] & s[sp - 1]; sp--; break;
  	      case '|': s[sp - 2] = s[sp - 2] | s[sp - 1]; sp--; break;
  	      case '^': s[sp - 2] = s[sp - 2] ^ s[sp - 1]; sp--; break;
  	      case '~': s[sp - 1] = ~s[sp - 1]; break;
					/*
					case '(': s[sp - 2] = s[sp - 2] << s[sp - 1]; sp--; break;
					case ')': s[sp - 2] = s[sp - 2] >> s[sp - 1]; sp--; break;
					*/

  	      case '!': a = pop(); n = pop(); d.putInt(a, n); break;
  	      case '@': a = pop(); push(d.getInt(a)); break;
  	      case ';': a = pop(); n = pop(); d.putShort(a, (short)n); break;
  	      case ':': a = pop(); push((int)d.getShort(a)); break;
  	      case ',': a = pop(); b = (byte)pop(); d.put(a, (byte)b); break;
  	      case '.': a = pop(); push(d.get(a)); break;

					case '[': quotation(); break;
					case '}': case ']': ret(); break;
					case '{': block(); break;

  	      case 'i': execute(); break;
					case 'j': jump(); break;
					case 'z': zjump(); break;

					case 'c': push(4); break;
					case 'b': push(0); break;
					case 'e': err = s[sp - 1]; sp--; break;
					case 'q': quit(); break;

					case '`': dump(); break;

					case 'h': push(d.here()); break;
					case 'a': d.allot(pop()); break;
					case 'g': d.align(); break;

					case 'v': tr = pop() != 0; break;
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
		if (d.tunused() <= s.length()) d.here_to_there();
		ip = d.there();
		if (d.tunused() <= s.length()) { err = -256; return; }
    for (int i = 0; i < s.length(); i++) { d.btransient((byte)s.charAt(i)); }
		d.btransient((byte)']');
		inner();
  }

  // Tracing
  public void dump_code(int c) {
    int t = 1;
		char k;
    while (t > 0 && c < d.capacity()) {
      switch (k = (char)d.get(c++)) {
				case '#': 
					System.out.printf("#");
					while ((k = (char)d.get(c++)) >= 48 && k <= 57) { 
						System.out.printf("%c", k); 
					}; 
					c--; 
					break;
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
}
