/* TODO: Variables and constants */
/* TODO: Extract parse-name and find-name */
/* TODO: Condition system */

import java.math.BigDecimal;
import java.nio.ByteBuffer;

public class Otter {
  class Symbol {
    public Symbol p;
    public String n;
    public int c;
    public Object v;
    public boolean hidden;
    public boolean immediate;
  }

  // The use of different stacks allows speed when needed,
  // and also, when implementing specific words to use the
  // long based stack as type/value, to have a common stack
  // like Factor does.
  public long[] d;
  public int dp;
  public Object[] o;
  public int op;
  public long[] r;
  public int rp;
  public int ip;
  public ByteBuffer c;
  public int cp;
  public Symbol l;
  public int state;
  public int cc;
  public String ibuf;
  
  public Otter() {
    d = new long[256];
    dp = 0;
    o = new Object[128];
    op = 0;
    r = new long[256];
    rp = 0;
    ip = 65536;
    l = null;
    c = ByteBuffer.allocateDirect(65536);
    state = 0;

    evaluate("\\: : $: \\;");
    evaluate(": ; $; \\;i");
    evaluate(": immediate $i ;");
    evaluate(": postpone $p ;");
    evaluate(": recurse $^ ; immediate");
    evaluate(": drop $_ ;");
    evaluate(": dup $d ;");
    evaluate(": over $o ;");
    evaluate(": swap $s ;");
    evaluate(": rot $r ;");
    evaluate(": + $+ ;");
    evaluate(": - $- ;");
    evaluate(": > $> ;");
    evaluate(": choose $? ;");
    evaluate(": times $t ;");
    evaluate(": fib 2 - 1 swap 1 swap [ swap over + ] times swap drop ;");
    evaluate(": 1- 1 - ;");
    evaluate(": rfib dup 1 > [ 1- dup 1- recurse swap recurse + ] [ ] choose ;");
  }

  /* Compilation */
  
  public void cnum() {
    long n = d[--dp];
    if (n == 0) {
      c.put(cp++, (byte)'0');
    } else if (n == 1) {
      c.put(cp++, (byte)'1');
    } else if (n > -128 && n < 128) {
      c.put(cp++, (byte)'#');
      c.put(cp++, (byte)n);
    } else if (n > -32768 && n < 32768) {
      c.put(cp++, (byte)'2');
      c.putShort(cp, (short)n);
      cp += 2;
    } else if (n > -2147483648L && n < 2147483648L) {
      c.put(cp++, (byte)'4');
      c.putInt(cp, (int)n);
      cp += 4;
    } else {
      c.put(cp++, (byte)'8');
      c.putLong(cp, n);
      cp += 8;
    }
  }

  public int code_size(int p) {
    int t = 1, n = 0;
    while (t > 0) {
      if (c.get(p + n) == '[') t++;
      if (c.get(p + n) == ']') t--;
      n++;
    }
    return n - 1;
  }
  
  public void cword(Symbol w, boolean inline) {
    System.out.printf("Compiling %s (Inlining %b)\n", w.n, inline);
    if (inline) {
      int cs = code_size(w.c);
      System.out.printf("Code size %d\n", cs);
      if (cs < 5) {
        for (int i = 0; i < cs; i++) {
          System.out.print("compiling ");
           System.out.print((char)c.get(w.c + i));
          System.out.println();
          c.put(cp++, c.get(w.c + i));
        }
        return;
      }
    }
    /* Don't inline */
    d[dp++] = w.c;
    cnum();
    c.put(cp++, (byte)'e');
  }

  /* Operations */

  public void drop() { dp--; }
  public void dup() { long l = d[dp - 1]; d[dp++] = l; }
  public void over() { long l = d[dp - 2]; d[dp++] = l; }
  public void swap() { long l = d[dp - 1]; d[dp - 1] = d[dp - 2]; d[dp - 2] = l; }
  public void rot() { long l = d[dp - 1]; d[dp - 1] = d[dp - 3]; d[dp - 3] = d[dp - 2]; d[dp - 2] = l; }
  
  public void add() { d[dp - 2] = d[dp - 2] + d[dp - 1]; dp--; }
  public void sub() { d[dp - 2] = d[dp - 2] - d[dp - 1]; dp--; }
  public void mul() { d[dp - 2] = d[dp - 2] * d[dp - 1]; dp--; }
  public void div() { d[dp - 2] = d[dp - 2] / d[dp - 1]; dp--; }
  public void mod() { d[dp - 2] = d[dp - 2] % d[dp - 1]; dp--; }

  public void and() { d[dp - 2] = d[dp - 2] & d[dp - 1]; dp--; }
  public void or() { d[dp - 2] = d[dp - 2] | d[dp - 1]; dp--; }
  public void not() { d[dp - 1] = ~d[dp - 1]; }

  public void eq() { d[dp - 2] = (d[dp - 2] == d[dp - 1]) ? -1 : 0; dp--; }
  public void lt() { d[dp - 2] = (d[dp - 2] < d[dp - 1]) ? - 1 : 0; dp--; }
  public void gt() { d[dp - 2] = (d[dp - 2] > d[dp - 1]) ? -1 : 0; dp--; }

  public void branch() { long f = d[--dp]; long t = d[--dp]; long b = d[--dp]; if (b != 0) eval((int)t); else eval((int)f); }
  public void times() { long q = d[--dp]; long n = d[--dp]; for (;n > 0;n--) { eval((int)q); } }

  public void postpone() {
    parse_name();
  }
  
  public void step() {
    switch (c.get(ip++)) {
      /* Literals */
      case '0': d[dp++] = 0L; break;
      case '1': d[dp++] = 1L; break;      
      case '#': d[dp++] = (long)c.get(ip++); break;
      case '2': d[dp++] = (long)c.getShort(ip); ip += 2; break;
      case '4': d[dp++] = (long)c.getInt(ip); ip += 4; break;
      case '8': d[dp++] = (long)c.getInt(ip); ip += 8; break;
      /* Execution */
      case 'e': call(); break;
      case '[': quotation(); break;
      case ']': ret(); break;
      case ':': colon(); break;
      case ';': semicolon(); break;
      case '^': recurse(); break;
      case 'i': immediate(); break;
      /* Stack */
      case '_': drop(); break;
      case 'd': dup(); break;
      case 'o': over(); break;
      case 's': swap(); break;
      case 'r': rot(); break;
      /* Arithmetics */
      case '+': add(); break;
      case '-': sub(); break;
      case '*': mul(); break;
      case '/': div(); break;
      case '%': mod(); break;
      /* Bitwise */
      case '&': and(); break;
      case '|': or(); break;
      case '!': not(); break;
      /* Comparison */
      case '=': eq(); break;
      case '<': lt(); break;
      case '>': gt(); break;
      /* Combinators */
      case '?': branch(); break;
      case 't': times(); break;

      case 'p': postpone(); break;
    }
  }

  public void quotation() { long q = d[--dp]; d[dp++] = ip; ip += q - 1; }
  public void call() { if (ip < 65536) r[rp++] = (long)ip; ip = (int)d[--dp]; }
  public void ret() { ip = rp > 0 ? ((int)r[--rp]) : 65536; }
  public void eval(int q) { d[dp++] = q; call(); inner(); }
  
  public void recurse() { cword(l, false); }
  public void immediate() { l.immediate = true; }
  public void colon() {
    parse_name();
    String name = (String)o[--op];
    if (name.length() == 0) {
      /* error */
      return;
    }
    Symbol w = new Symbol();
    w.n = name;
    w.hidden = true;
    w.immediate = false;
    w.v = null;
    w.c = cp;
    w.p = l;
    l = w;
    state = 1;
  }
  public void semicolon() {
    c.put(cp++, (byte)']');
    state = 0;
    l.hidden = false;
    System.out.println("--- COMPILED WORD");
    System.out.println(l.n);
    dump_code(l.c);
    System.out.println();
  }

  /* having step as a separate function allows better testing, but needs one more call per token */
  public void inner() {
    int t = rp;
    while (t <= rp && ip < 65536) {
      trace();
      step();
    }
  }

  /* Evaluation */

  public void parse_name() {
    while (ibuf.length() > 0 && Character.isSpace(ibuf.charAt(0))) {
      ibuf = ibuf.substring(1);
    }
    StringBuilder sb = new StringBuilder();
    while (ibuf.length() > 0 && !Character.isSpace(ibuf.charAt(0))) {
      sb.append(ibuf.charAt(0));
      ibuf = ibuf.substring(1);
    }
    o[op++] = sb.toString();
  }
  
  public void evaluate(String s) {
    ibuf = s;
    while (ibuf.length() > 0) {
      parse_name();
      String tk = (String)o[--op];
      if (tk.length() == 0) return;
      /* Find name */
      Symbol w = l;
      while (w != null) {
        if (w.n.equals(tk)) break;
        w = w.p;
      }
      if (w != null) {
        /* Interpret/compile word */
        if (state == 0 || w.immediate) {
          eval(w.c);
        } else {
          cword(w, true);
        }
      } else {
        if (tk.equals("[")) {
          if (state == 0) {
            state = 1; 
            d[dp++] = cp;
            cc = cp;
          } else {
            /* I use a number that will need 2 bytes */
            state++;
            d[dp++] = cp + 1;
            d[dp++] = 1024;
            cnum();
            c.put(cp++, (byte)'[');
          }
        } else if (tk.equals("]")) {
          if (state > 1) {
            c.put(cp++, (byte)']');
            long p = d[--dp];  
            c.putShort((int)p, (short)(cp - p - 2));
            state--;
          } else {
            c.put(cp++, (byte)']');
            state = 0;
            System.out.println("CODE COMPILED:");
            for (int i = cc; i < cp; i++) {
              char u = (char)c.get(i);
              if (u == '#') {
                System.out.print('#');
                System.out.print(c.get(++i));
              } else if (u == '2') {
                System.out.print('#');
                System.out.print(c.getShort(i + 1));
                i += 2;
              } else {
                System.out.print(u);
              }
            }
            System.out.println();
          }
          /*
        } else if (tk.equals(":")) {
          colon();
        } else if (tk.equals(";")) {
          semicolon();
          */
        } else if (tk.charAt(0) == '\\') {
          /* Interpret assembler */
          tk = tk.substring(1);
            for (int i = 0; i < tk.length(); i++) {
              c.put(65535 - tk.length() + i, (byte)tk.charAt(i));
            }
            eval(65535 - tk.length());
       } else if (tk.charAt(0) == '$') {
          /* Compile assembler */
          tk = tk.substring(1);
          for (int i = 0; i < tk.length(); i++) {
            c.put(cp++, (byte)tk.charAt(i));
          }   
        } else {
          try {
            BigDecimal n = new BigDecimal(tk);
            /* Long integer literal */
            if (state > 0) {
              d[dp++] = n.longValueExact();
              cnum();
            } else {
              d[dp++] = n.longValueExact(); 
            }
          } catch (NumberFormatException | ArithmeticException e) {
            System.out.println("WORD NOT FOUND");
            System.out.println(tk);
          }  
        }
      }
    }  
  }

  /* Tracing */

  public void dump_code(int p) {
    int t = 1;
    while (t > 0 && p < 65536) {
      if (c.get(p) == '[') t++;
      if (c.get(p) == ']') t--;
      if (c.get(p) == '#') {
        byte n = c.get(++p);
        p += 1;
        System.out.print('#');
        System.out.print(n);
      } else if (c.get(p) == '2') {
        short n = c.getShort(++p);
        p += 2;
        System.out.print('#');
        System.out.print(n);
      } else if (c.get(p) == '4') {
        int n = c.getInt(++p);
        p += 4;
        System.out.print('#');
        System.out.print(n);
      } else if (c.get(p) == '8') {
        long n = c.getLong(++p);
        p += 8;
        System.out.print('#');
        System.out.print(n);
      } else {
        System.out.print((char)c.get(p++));
      }
    }
  }
  
  public void dump_dstack() {
    for(int i = 0; i < dp; i++) {
      System.out.print(d[i]);
      System.out.print(" ");
    }
  }

  public void dump_ostack() {
    System.out.print("[ ");
    for (int i = 0; i < op; i++) {
      System.out.print(o[i]);
      System.out.print(" ");
    }
    System.out.print("] ");
  }

  public void dump_rstack() {
    System.out.print(": ");
    dump_code(ip);
    for(int i = rp; i > 0; i--) {
      System.out.print(": ");
      dump_code(((Long)r[i - 1]).intValue());
    }
  }

  public void trace() {
    dump_ostack();
    dump_dstack();
    dump_rstack();
    System.out.println();
  }

  public void test() {
    c.put((byte)'2');
    c.putShort((short)3121);
    c.put((byte)'#');
    c.put((byte)-121);
    c.put((byte)'+');
    c.put((byte)']');
    inner();
  }
}
