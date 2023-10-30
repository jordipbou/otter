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

  public Otter() {
    s = new long[256];
    sp = 0;
    r = new long[256];
    rp = 0;
    ip = 0;
    extensions = new Consumer[26];
  }

  public void push(long a) {
    s[sp++] = a;
  }

  public long pop() {
    return s[--sp];
  }

  public void drop() {
    sp--;
  }

  public void dup() {
    long a = pop();
    push(a);
    push(a);
  }

  public void over() {
    long a = pop();
    long b = pop();
    push(b);
    push(a);
    push(b);
  }
  
  public void swap() {
    long a = pop();
    long b = pop();
    push(a);
    push(b);
  }

  public void rot() {
    long a = pop();
    long b = pop();
    long c = pop();
    push(b);
    push(a);
    push(c);
  }

  public void nip() {
    long a = pop();
    long b = pop();
    push(a);
  }
  
  public void add() {
    long a = pop();
    long b = pop();
    push(b + a);
  }

  public void sub() {
    long a = pop();
    long b = pop();
    push(b - a);
  }
  
  public void mul() {
    long a = pop();
    long b = pop();
    push(b * a);
  }

  public void div() {
    long a = pop();
    long b = pop();
    push(b / a);
  }

  public void mod() {
    long a = pop();
    long b = pop();
    push(b % a);
  }

  public void and() {
    long a = pop();
    long b = pop();
    push(b & a);
  }

  public void or() {
    long a = pop();
    long b = pop();
    push(a | b);
  }

  public void xor() {
    long a = pop();
    long b = pop();
    push(b ^ a);
  }

  public void invert() {
    long a = pop();
    push(~a);
  }

  public void lt() {
    long a = pop();
    long b = pop();
    push(b < a ? -1L : 0L);
  }

  public void eq() {
    long a = pop();
    long b = pop();
    push(b == a ? -1L : 0L);
  }

  public void gt() {
    long a = pop();
    long b = pop();
    push(b > a ? -1L : 0L);
  }

  public void bfetch() {
    long a = pop();
    push((long)block.get((int)a));
  }

  public void bstore() {
    long a = pop();
    long b = pop();
    block.put((int)a, (byte)b);
  }

  public void sfetch() {
    long a = pop();
    push((long)block.getShort((int)a));
  }

  public void sstore() {
    long a = pop();
    long b = pop();
    block.putShort((int)a, (short)b);
  }

  public void ifetch() {
    long a = pop();
    push((long)block.getInt((int)a));
  }

  public void istore() {
    long a = pop();
    long b = pop();
    block.putInt((int)a, (int)b);
  }

  public void cfetch() {
    long a = pop();
    push(block.getLong((int)a));
  }

  public void cstore() {
    long a = pop();
    long b = pop();
    block.putLong((int)a, b);
  }

  public boolean tail() {
    return
      ip >= block.capacity()
      || block.get(ip) == ']'
      || block.get(ip) == '}';
  }

  public void execute() {
    long q = pop();
    if (!tail())
      r[rp++] = ip;
    ip = (int)q;
  }

  public void eval(long q) {
    push(q);
    execute();
    inner();
  }

  public byte peek() { 
    return block.get(ip); 
  }

  public byte token() {
    return block.get(ip++);
  }

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

  public void ret() {
    if (rp > 0) 
      ip = (int)r[--rp];
    else
      ip = block.capacity();
  }

  public void string() {
    int c = 0;
    push(ip);
    while (token() != '"') { c++; }
    push(c); 
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
  
  public void emit() {
    char c = (char)pop();
    System.out.print(c);
  }

  public void key() {
    try {
      push((long)System.in.read());   
    } catch (java.io.IOException e) {
      // put correct error code in err
    }
  }
  
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
          case '0': push(0L); break;
          case '1': push(1L); break;
          case '#': number(); break;
            
          case '_': drop(); break;
          case 'd': dup(); break;
          case 'o': over(); break;
          case 's': swap(); break;
          case 'r': rot(); break;
          case 'n': nip(); break;

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

          case '!': cstore(); break;
          case '@': cfetch(); break;
          case '`': istore(); break;
          case '\'': ifetch(); break;
          case ',': sstore(); break;
          case '.': sfetch(); break;
          case ';': bstore(); break;
          case ':': bfetch(); break;

          case '{': block(); break;
          case '}': ret(); break;

          case '"': string(); break;

          case 'e': emit(); break;
          case 'k': key(); break;
        }
    }
  }
  
  public void inner() {
    int t = rp;
    while (t <= rp && ip < block.capacity()) {
      //trace();
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
    System.out.print(" : ");
    dump_code(ip);
    for (int i = rp - 1; i >= 0; i--) {
      System.out.print(" : ");
      dump_code((int)r[i]);
    }
    System.out.println();
  }
/*
	public static int DICT_SIZE = 65536;

	public static byte NO_FLAGS = 0;
	public static byte VARIABLE = 1;
	public static byte HIDDEN = 2;
	public static byte IMMEDIATE = 3;

	class Dictionary {
		private ByteBuffer data;

		private int HEADER_SIZE = 12;

		public Dictionary(int size) { 
			data = ByteBuffer.allocateDirect(size);
			putInt(size); // DICT_SIZE
			putInt(0); // LATEST
			putInt(12); // HERE
		}

		public byte getByteAt(int pos) { return data.get(pos); }
		public void putByte(byte value) { data.put(value); }
		public void putByteAt(int pos, byte value) { data.put(pos, value); }
		// TODO: I don't like these two names
		public void putBytesFrom(int len, int from) {
			for (int i = 0; i < len; i++) {
				putByte(getByteAt(from + i));
			}
		}
		public void putBytesAt(int pos, byte[] bytes) {
			for (int i = 0; i < bytes.length; i++) {
				putByteAt(pos + i, bytes[i]);
			}
		}

		public short getShortAt(int pos) { return data.getShort(pos); }
		public void putShort(short value) { data.putShort(value); }
		public void putShortAt(int pos, short value) { data.putShort(pos, value); }

		public int getIntAt(int pos) { return data.getInt(pos); }
		public void putInt(int value) { data.putInt(value); }
		public void putIntAt(int pos, int value) { data.putInt(pos, value); }

		public long getLongAt(int pos) { return data.getLong(pos); }
		public void putLong(long value) { data.putLong(value); }
		public void putLongAt(int pos, long value) { data.putLong(pos, value); }

		public int size() { return getIntAt(0); }
		public int here() { return data.position(); }
		public int allot(int n) {
			int h = here();
			if (n == 0) { return h; }
			else if (n < 0) { 
				if (h + n < 12) { return - 1; }
				else {
					data.position(data.position() + n);
					putIntAt(8, data.position());
					return h;
				}
			} else {
				if (h + n > size()) { return -1; }
				else {
					data.position(data.position() + n);
					putIntAt(8, data.position());	// Save on HERE in dict as its not used in ByteBuffer
					return h;
				}
			}
		}

		public void align() {
			int unaligned = here();
			int aligned = (unaligned + 7) & ~(7);
			allot(aligned - unaligned);
		}

		public void setLatest(int pos) { putIntAt(4, pos); }
		public int getLatest() { return getIntAt(4); }

		public int putHeader(byte nlen, int npos) {
			int header = here();
			putInt(getLatest());
			setLatest(header);
			putByte(VARIABLE);
			putByte(nlen);
			putBytesFrom(npos, nlen);
			align();
			return header;
		}

		public int getPrevious(int header) { return getIntAt(header); }

		public void setFlags(int header, byte flags) { putByteAt(header + 4, flags); }
		public byte getFlags(int header) { return getByteAt(header + 4); }
		public boolean hasFlag(int header, byte flag) { return (getFlags(header) & flag) == flag; }

		public int getNameLength(int header) { return (int)getByteAt(header + 5); }
		public int getName(int header) { return header + 6; }
	}

	interface Extension {
		void run(Machine m);
	}

	class Extensions {
		private Extension extensions[];

		public Extensions() {
			extensions = new Extension[26];
		}

		public void addExtension(char letter, Extension e) { extensions[letter - 'A'] = e; }
		public Extension getExtension(char letter) { return extensions[letter - 'A']; }
	}

	class Machine {
		private long[] d;
		private int dp;
		private long[] r;
		private int rp;
		private int ip;

		private Dictionary dict;
		private Extensions ext;

		public Machine(Dictionary dictionary, Extensions extensions) {
			d = new long[256];
			dp = 0;
			r = new long[256];
			rp = 0;
			ip = dictionary.size();;
			dict = dictionary;
			ext = extensions;
		}

		public void push(long l) { d[dp++] = l; }
		public long pop() { return d[--dp]; }
		public void drop() { dp--; }
	
		public long T() { return d[dp - 1]; }
		public long N() { return d[dp - 2]; }
		public long NN() { return d[dp - 3]; }
	
	  public void dup() { push(T()); }
	  public void over() { push(N()); }
	  public void swap() { long a = pop(); long b = pop(); push(a); push(b); }
	  public void rot() { long a = pop(); long b = pop(); long c = pop(); push(b); push(a); push(c); }
	  
	  public void add() { long a = pop(); long b = pop(); push(b + a); }
	  public void sub() { long a = pop(); long b = pop(); push(b - a); }
	  public void mul() { long a = pop(); long b = pop(); push(b * a); }
	  public void div() { long a = pop(); long b = pop(); push(b / a); }
	  public void mod() { long a = pop(); long b = pop(); push(b % a); }
	
	  public void and() { long a = pop(); long b = pop(); push (b & a); }
	  public void or() { long a = pop(); long b = pop(); push(b | a); }
	  public void invert() { long a = pop(); push(~a); }
	
	  public void lt() { long a = pop(); long b = pop(); push(a < b ? -1L : 0L); }
	  public void eq() { long a = pop(); long b = pop(); push(a == b ? -1L : 0L); }
	  public void gt() { long a = pop(); long b = pop(); push(a > b ? -1L : 0L); }

		public boolean tail() { return ip == dict.size() || dict.getByteAt(ip) == ']'; }
	  public void call() { if (!tail()) r[rp++] = (long)ip; ip = (int)pop(); }
	  public void ret() { ip = rp > 0 ? ((int)r[--rp]) : dict.size(); }
	  public void eval(int q) { push(q); call(); inner(); }

		public void bfetch() { long a = pop(); push(dict.getByteAt((int)a)); }
		public void bstore() { long a = pop(); long b = pop(); dict.putByteAt((int)a, (byte)b); }
		public void cfetch() { long a = pop(); push(dict.getLongAt((int)a)); }
		public void cstore() { long a = pop(); long b = pop(); dict.putLongAt((int)a, b); }
	
	 	public char PEEK() { return (char)dict.getByteAt(ip); }
		public char TOKEN() { return (char)dict.getByteAt(ip++); }
	
		public void step() {
			switch (PEEK()) {
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
			case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
			case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
			case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
			case 'Y': case 'Z':
				ext.getExtension(TOKEN()).run(this);
				break;
			default:
				switch (TOKEN()) {
				case 'e': call(); break;
				case ']': ret(); break;
	
				case '_': drop(); break;
				case 'd': dup(); break;
				case 'o': over(); break;
				case 's': swap(); break;
				case 'r': rot(); break;
	
				case '+': add(); break;
				case '-': sub(); break;
				case '*': mul(); break;
				case '/': div(); break;

				case ':': bfetch(); break;
				case ';': bstore(); break;
				case '.': cfetch(); break;
				case ',': cstore(); break;

				// TODO: Should all this be an extensions...
				// ...they're not basic for making the machine work, and need access to the
				// dictionary, and tokens collide.
				case 'h': push(dict.here()); break;
				case 'a': dict.allot((int)pop()); break;

				case '"': colon(); break;
				case '\'': semicolon(); break;
				}
			}
		}

		public void inner() {
  	  int t = rp;
  	  while (t <= rp && ip < dict.size()) {
				trace();
  	    step();
  	  }
		}
	
		public void trace_code(int pos) {
			int t = 1;
			while (t > 0 && pos < dict.size()) {
				if (dict.getByteAt(pos) == '[') t++;
				if (dict.getByteAt(pos) == ']') t--;
				System.out.printf("%c", dict.getByteAt(pos));
				pos++;
			}
		}

		public void trace() {
			for (int i = 0; i < dp; i++) {
				System.out.printf("%d ", d[i]);	
			}
			System.out.printf(": [%d] ", ip);
			trace_code(ip);
			System.out.println();
		}
	}

	private Dictionary dict;
	private Extensions ext;
	public Machine m;

	public int state;

	public static int IBUF_SIZE = 256;
	public static int IBUF;
	public int ipos;
	public int ilen;

	public static int ABUF_SIZE = 256;
	public static int ABUF;

	public Otter() {
		dict = new Dictionary(DICT_SIZE);	
		ext = new Extensions();
		m = new Machine(dict, ext);
		ipos = 0;
		ilen = 0;
		ABUF = dict.size() - ABUF_SIZE;
		IBUF = ABUF - IBUF_SIZE;
	}

  public void parse_name() {
    while (ipos < ilen && Character.isSpace((char)dict.getByteAt(IBUF + ipos))) ipos++;
		m.push(IBUF + ipos);
    while (ipos < ilen && !Character.isSpace((char)dict.getByteAt(IBUF + ipos))) ipos++;
		m.push((IBUF + ipos) - m.T());
  }

	public void create() {
		parse_name();
		long nlen = m.pop();
		long npos = m.pop();
		int header = dict.putHeader((byte)nlen, (int)npos);
	}

	public void colon() {
		create();
		dict.setFlags(dict.getLatest(), HIDDEN);
		state = 1;
	}

	public void semicolon() {
		dict.putByte((byte)']');
		state = 0;
		dict.setFlags(dict.getLatest(), NO_FLAGS);
	}

	public boolean compare(int l1, int s1, int l2, int s2) {
		if (l1 != l2) return false;
		for (int i = 0; i < l1; i++) {
			if (dict.getByteAt(s1 + i) != dict.getByteAt(s2 + i)) return false;
		}
		return true;
	}

	public void find_name() {
		long l = m.pop();
		long t = m.pop();
		int w = dict.getLatest();
		while (w != 0) {
			System.out.printf("FIND-NAME::WORD:%d\n", w);
			if (compare(dict.getNameLength(w), dict.getName(w), (int)l, (int)t)) break;
			w = dict.getPrevious(w);
		}
		m.push(t);
		m.push(l);
		m.push((long)w);
	}

 	public void evaluate(String s) {
		dict.putBytesAt(IBUF, s.getBytes());
    for (int i = 0; i < s.length(); i++) { dict.putByteAt(IBUF + i, (byte)s.charAt(i)); }
		ipos = 0;
		ilen = s.length();
    while (ipos < ilen) {
			m.trace();
      parse_name();
      if (m.T() == 0) { m.drop(); m.drop(); return; }
			find_name();
			if (m.T() != 0) {
			} else {
				int w = (int)m.pop();
				int l = (int)m.pop();
				int t = (int)m.pop();
				if (dict.getByteAt(t) == '\\') {
					l = l - 1;
					t = t + 1;
					for (int i = 0; i < l; i++) {
						dict.putByteAt(dict.size() - l + i, dict.getByteAt(t + i));
					}
					m.eval(dict.size() - l);
				} else if (dict.getByteAt(t) == '$') {
					l = l - 1;
					t = t + 1;
					for (int i = 0; i < l; i++) {
						dict.putByte(dict.getByteAt(t + i));
					}
				} else { 
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < l; i++) {
						sb.append((char)dict.getByteAt(t + i));
					}
					try {
				    BigDecimal n = new BigDecimal(sb.toString());
						m.push(n.longValueExact());
						// TODO: Compile
          } catch (NumberFormatException | ArithmeticException e) {
            System.out.println("WORD NOT FOUND");
            System.out.println(sb.toString());
          }  
	
				}
			}
		}
	}
*/
}
