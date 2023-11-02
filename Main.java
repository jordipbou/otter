import java.io.*;

class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader obj = 
      new BufferedReader(
        new InputStreamReader(
          System.in)); 
    Otter o = new Otter();
    Sloth s = new Sloth(o, 65536);
    
    // o.extensions['H' - 'A'] = x -> { System.out.println("Hello world!"); };
    o.extensions['C' - 'A'] = new Combinators();
    o.extensions['S' - 'A'] = s;
  
    while (true) {
      System.out.print("> ");
      //o.isolated(obj.readLine());
      s.evaluate(o, obj.readLine());
      s.trace(o);
			System.out.println("Ok");
    } 
  }
}
