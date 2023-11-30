import java.io.*;
import java.util.function.Consumer;

class Main {
  public static void main(String[] args) throws IOException {
    Dodo d = new Dodo();
    d.x['E' - 'A'] = x -> { int n = x.pop(); System.out.print((char)n); };
    d.x['D' - 'A'] = new Debug();

    BufferedReader f =
      new BufferedReader(
        new InputStreamReader(
          //new FileInputStream("ans.fth")));
					new FileInputStream("system.fth")));

    try {
    while (true) {
      String l = f.readLine();
      if (l == null) break;
      System.out.printf("--> %s\n", l);
      d.evaluate(l);
      if (d.err != 0) {
				System.out.printf("ERROR:%d\n", d.err);
				System.exit(-1);
      }
      d.trace();
    }
    } catch(IOException e) {}

    BufferedReader obj = 
      new BufferedReader(
        new InputStreamReader(
          System.in)); 
   
		d.tr = true;

    while (true) {
      System.out.print("> ");
      //d.asm(obj.readLine());
      d.evaluate(obj.readLine());
			if (d.err != 0) {
				if (d.err == -256) System.exit(0);
				System.out.printf("ERROR: %d\n", d.err);
				d.err = 0;
				d.sp = 0;
				d.rp = 0;
				d.ip = d.d.capacity();
      } else {
				d.trace();
				System.out.println("Ok");
      }
    }     
    //Otter o = new Otter();
    
    //o.x = new Consumer[26];
    //o.x['E' - 'A'] = x -> { int c = x.pop(); System.out.print((char)c); };
/*
    Otter o = new Otter();
    Sloth s = new Sloth(o, 65536);
    
    // o.extensions['H' - 'A'] = x -> { System.out.println("Hello world!"); };
    o.extensions['C' - 'A'] = new Combinators();
    o.extensions['S' - 'A'] = s;
		o.extensions['T' - 'A'] = new Terminal();
    
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
				o.err = 0;
				o.sp = 0;
				o.rp = 0;
				o.ip = o.block.capacity();
			}
			o.trace();
    }
    } catch(IOException e) {}
    */
    //BufferedReader obj = 
    //  new BufferedReader(
    //    new InputStreamReader(
    //      System.in)); 
   //
		//o.tr = true;
//
    //while (true) {
    //  System.out.print("> ");
    //  //o.asm(obj.readLine());
    //  o.evaluate(obj.readLine());
			//if (o.err != 0) {
				//if (o.err == -256) System.exit(0);
				//System.out.printf("ERROR: %d\n", o.err);
				//o.err = 0;
				//o.sp = 0;
				//o.rp = 0;
				//o.ip = o.d.capacity();
      //}else {
				//o.trace();
				//System.out.println("Ok");
      //}
    //} 
  }
}
