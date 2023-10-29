import java.io.*;

class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader obj = 
      new BufferedReader(
        new InputStreamReader(
          System.in)); 
    Otter o = new Otter();
    o.extensions['H' - 'A'] = x -> { System.out.println("Hello world!"); };
    o.extensions['C' - 'A'] = new Combinators();
  
    while (true) {
      System.out.print("> ");
      o.isolated(obj.readLine());
      o.trace();
			System.out.println("Ok");
    } 
  }
}
