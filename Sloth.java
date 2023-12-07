import java.io.*;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Sloth extends Dodo implements Consumer<Dodo> {
	public static int wPREVIOUS = 0;
	public static int wXT = 4;
	public static int wFLAGS = 8;
	public static int wNAMELEN = 9;
	public static int wNAME = 10;

	public static byte NO_FLAGS = 0;
	public static byte HIDDEN = 1;
	public static byte EXECUTABLE = 2;
	public static byte IMMEDIATE = 4;

	public int ibuf;
	public int ipos;
	public int ilen;

	public int latest;

	public int state;

	public Sloth() {
		super(new Dictionary(1024 * 1024));
		x['S' - 'A'] = this;
		ibuf = 0;
		ipos = 0;
		ilen = 0;
		latest = 0;
		state = 0;
	}

	public void interpret(int nt) {
		int xt = d.getInt(nt + wXT);
		eval(xt);
	}

	public void compile(int nt) {
		int xt = d.getInt(nt + wXT);
		int t = 1;
		while (t > 0) {
			if (d.get(xt) == '[') t++;
			if (d.get(xt) == ']') t--;
			if (t > 0) d.bcompile(d.get(xt));
			xt++;
		}
	}

	public void evaluate(String s) {
		ibuf = d.string_to_transient(s);
		ilen = s.length();
		ipos = 0;
		// Check if there's a new interpreter in town...
		while (err == 0) {
			parse_name();
			if (T() == 0) { drop(); drop(); return; }
			over(); over();
			find_name();
			if (T() != 0) {
				int w = pop();
				drop(); drop();
				if (state == 0 || (d.get(w + wFLAGS) & IMMEDIATE) == IMMEDIATE) interpret(w);
				else compile(w);
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

	public void include(String f) {
		System.out.printf("Include file: %s\n", f);
		try {
	    BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
	    try {
				while (true) {
	    	  String l = r.readLine();
	    	  if (l == null) break;
					System.out.printf("--> %s\n", l);
	    	  evaluate(l);
	    	  if (err != 0) {
						System.out.printf("ERROR:%d\n", err);
						return;
	    	  }
	    	  trace();
	    	}
	    } catch(IOException e) {
				return;
			}
		} catch (FileNotFoundException e) { 
			return; 
		}

	}

	public void parse_name() {
    while (ipos < ilen && Character.isWhitespace((char)d.get(ibuf + ipos))) { ipos++; }
    push(ibuf + ipos);
    while (ipos < ilen && !Character.isWhitespace((char)d.get(ibuf + ipos))) { ipos++; }
    push(ibuf + ipos - T());
	}

	public boolean compare_without_case(int w, int l, int t) {
		if (d.get(w + wNAMELEN) != (byte)l) return false;
		for (int i = 0; i < l; i++) {
			int a = (int)d.get(w + wNAME + i);
			int b = (int)d.get(t + i);
			if (a >= 97 && a <= 122) a = a - 32;
			if (b >= 97 && b <= 122) b = b - 32;
			if (a != b) return false;
		}
		return true;
	}

	public void find_name() {
		int l = pop();
		int t = pop();
		int w = latest;
		while (w != 0) {
			if (!((d.get(w + wFLAGS) & HIDDEN) == HIDDEN) && compare_without_case(w, l, t)) break;
			w = d.getInt(w + wPREVIOUS);
		}
		push(w);
	}

	public boolean do_asm(int l, int t) {
		if (l > 1 && d.get(t) == '\\') {
			if (d.tunused() <= l) d.here_to_there();
			if (d.tunused() <= l) { err = -8; return true; }
			int q = d.there();
			for (int i = 1; i < l; i++) { d.btransient(d.get(t + i)); }
			d.btransient((byte)']');
			eval(q);
			return true;
		} else {
			return false;
		}
	}

	public boolean do_casm(int l, int t) {
		if (l > 1 && d.get(t) == '$') {
			if (d.unused() <= l) { err = -8; return true; }
			for (int i = 1; i < l; i++) { d.bcompile(d.get(t + i)); }
			return true;
		} else {
			return false;
		}
	}

	public void header(int l, int t) {
    d.align();
    int w = d.here();
    d.ccompile(latest);
    latest = w;
    d.ccompile(0);
    d.bcompile(NO_FLAGS);
    d.bcompile((byte)l);
    for (int i = 0; i < l; i++) d.bcompile(d.get(t + i));
    d.align();
		d.putInt(latest + wXT, d.here());
	}

	public void colon() {
		parse_name();
		int l = pop();
		int t = pop();
		if (l == 0) { err = -16; return; }
		header(l, t); if (err != 0) return;
		d.put(latest + wFLAGS, HIDDEN);
		state = 1;
	}

	public boolean do_colon(int l, int t) {
		if (l == 1 && d.get(t) == ':') {
			colon();
			return true;
		} else {
			return false;
		}
	}

	public void semicolon() {
		d.bcompile((byte)']');
		state = 0;
		d.put(latest + wFLAGS, EXECUTABLE);
	}

	public boolean do_semicolon(int l, int t) {
		if (l == 1 && d.get(t) == ';') {
			semicolon();
			return true;
		} else {
			return false;
		}
	}

	public void do_number(int l, int t) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < l; i++) sb.append((char)d.get(t + i));
    try {
      int n = Integer.parseInt(sb.toString());
      //if (d.getInt(STATE) > 0) literal(n);
      /*else*/ push(n);
    } catch (NumberFormatException e) {
      err = -13;
    }
  }

	public void name_to_string() {
		int nt = pop();
		push(nt + wNAME);
		push(d.get(nt + wNAMELEN));
	}

	public void accept(Dodo d) {
		switch (d.token()) {
			case ':': colon(); break;
			case ';': semicolon(); break;
			case 'f': find_name(); break;
			case 'i': int l = pop(); int t = pop(); include(d.d.mem_to_string(l, t)); break;
			case 'l': push(latest); break;
			case 'n': parse_name(); break;
			case 's': d.push(ibuf); d.push(ilen); break;
			case 'w':
				switch (d.token()) {
					case 's': name_to_string(); break;
				}
				break;
		}
	}
}
