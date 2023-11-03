import java.io.*;

class Main {
  public static void main(String[] args) throws IOException {
    
    Otter o = new Otter();
    Sloth s = new Sloth(o, 65536);
    
    // o.extensions['H' - 'A'] = x -> { System.out.println("Hello world!"); };
    o.extensions['C' - 'A'] = new Combinators();
    o.extensions['S' - 'A'] = s;

    BufferedReader f =
      new BufferedReader(
        new InputStreamReader(
          new FileInputStream("ans.fth")));

    try {
    while (true) {
      String l = f.readLine();
      if (l == null) break;
			System.out.printf("--> %s\n", l);
      s.evaluate(o, l);
 			if (o.err != 0) {
				System.out.printf("ERROR: %d\n", o.err);
				System.out.printf("IBUF: [%s]\n", s.ibuf.substring(s.ipos, s.ilen));
				o.err = 0;
				o.sp = 0;
				o.rp = 0;
				o.ip = o.block.capacity();
			}
   }
    } catch(IOException e) {}
    
      BufferedReader obj = 
        new BufferedReader(
          new InputStreamReader(
            System.in)); 
    
    while (true) {
      System.out.print("> ");
      //o.isolated(obj.readLine());
      s.evaluate(o, obj.readLine());
			if (o.err != 0) {
				if (o.err == -256) System.exit(0);
				System.out.printf("ERROR: %d\n", o.err);
				o.err = 0;
				o.sp = 0;
				o.rp = 0;
				o.ip = o.block.capacity();
			} else {
				o.trace();
				System.out.println("Ok");
			}
    } 
  }
}
