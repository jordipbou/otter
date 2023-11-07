import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class Otter {
  public Machine machine;
  public int[] s;
  public int sp;
  public int[] r;
  public int rp;
  public int ip;
	public int err;
	public boolean tr;
  
  public Otter(Machine m) {
    machine = m;
    s = new int[256];
    sp = 0;
    r = new int[256];
    rp = 0;
    ip = 0;
		err = 0;
		tr = false;  
  }
  
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
  
  public boolean tail() {
    return
      ip >= machine.dict.size()
      || machine.dict.fetch_byte(ip) == ']'
      || machine.dict.fetch_byte(ip) == '}';
  }

  public void execute() { int q = pop(); if (!tail()) r[rp++] = ip; ip = q; }
	public void jump() { int d = pop(); ip += d; }
	public void zjump() { int d = pop(); int b = pop(); if (b == 0) ip += d; }
  public void ret() { if (rp > 0) ip = r[--rp]; else ip = machine.dict.size(); }
  public void eval(int q) { push(q); execute(); inner(); }
	public void quotation() { int d = pop(); push(ip); ip += d; }
  
	public void quit() { err = -256; }

  public void block() { 
	  push(ip);
    int t = 1;
    while (t > 0 && ip < machine.dict.size()) {
      switch (token()) {
        case '{': case '[': t++; break;
        case '}': case ']': t--; break;
      }
    }
  }

  public void number() {
    int n = 0;
    while (ip < machine.dict.size()) {
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
    while (ip < machine.dict.size() && token() != '"') { c++; }
    push(c); 
  }

  public void compare(boolean withoutCase) {
    int l2 = pop();
    int s2 = pop();
    int l1 = pop();
    int s1 = pop();
    if (l1 != l2) push(0);
		else {
			for (int i = 0; i < l1; i++) {
				byte a = machine.dict.fetch_byte(s1 + i);
				byte b = machine.dict.fetch_byte(s2 + i);
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
      machine.dict.store_byte(d + i, machine.dict.fetch_byte(s + i));
    }
  }
  
  public byte peek() { return machine.dict.fetch_byte(ip); }
  public byte token() { return machine.dict.fetch_byte(ip++); }

  public void step() {
    int a, n;
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
  	    machine.extensions[(char)token() - 'A'].accept(this);
  	    break;
  	  default:
  	    switch (token()) {
					case '$': machine.dict.compile_byte(token()); break;

  	      case '1': push(1); break;
  	      case '#': number(); break;
  	      case '\'': push((int)machine.dict.fetch_byte(ip++)); break;
  	      case '2': push((int)machine.dict.fetch_short(ip)); ip += 2; break;
  	      case '4': push(machine.dict.fetch_int(ip)); ip += 4; break;
  	        
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

  	      case '!': a = pop(); n = pop(); machine.dict.store_int(a, n); break;
  	      case '@': a = pop(); push(machine.dict.fetch_int(a)); break;
					case '`': a = pop(); n = pop(); machine.dict.store_int(a, n); break;
					case '\\': a = pop(); push(machine.dict.fetch_int(a)); break;
  	      case ';': a = pop(); n = pop(); machine.dict.store_short(a, (short)n); break;
  	      case ':': a = pop(); push(machine.dict.fetch_short(a)); break;
  	      case ',': a = pop(); n = pop(); machine.dict.store_byte(a, (byte)n); break;
  	      case '.': a = pop(); push(machine.dict.fetch_byte(a)); break;

					case 'h': push(machine.dict.here()); break;
					case 'a': n = pop(); machine.dict.allot((int)n); break;
					case 'g': machine.dict.align(); break;

					case 'l': n = pop(); machine.dict.store_literal(n); break;

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
  
  public void inner() {
    int t = rp;
    while (t <= rp && ip < machine.dict.size()) {
      if (tr) { trace(); System.out.println(); }
      step();
      // Manage errors
    }
  }
  
  public void asm(String s) {
    int l = s.length();
    for (int i = 0; i < l; i++) {
      machine.dict.store_byte(machine.dict.size() - l + i, (byte)s.charAt(i));
    }
    ip = machine.dict.size() - l;
    inner();
  }

  public void dump_code(int c) {
    int t = 1;
		char k;
    while (t > 0 && c >= 0 && c < machine.dict.size()) {
      switch (k = (char)machine.dict.fetch_byte(c++)) {
        case '{': case '[': t++; System.out.print(k); break;
        case '}': case ']': t--; System.out.print(k); break;
				case '\'': System.out.printf("#%d", machine.dict.fetch_byte(c++)); break;
				case '2': System.out.printf("#%d", machine.dict.fetch_short(c)); c += 2; break;
				case '4': System.out.printf("#%d", machine.dict.fetch_int(c)); c += 4; break;
				case 10: break;
				default: System.out.print(k);	break;
      }
    }
  }
  
  public void trace() {
    for (int i = 0; i < sp; i++) {
      System.out.printf("%d ", s[i]);
    }
		if (ip < machine.dict.size()) {
	    System.out.print(" : ");
	    dump_code(ip);
		}
    for (int i = rp - 1; i >= 0; i--) {
      System.out.print(" : ");
      dump_code((int)r[i]);
    }
  }
}
