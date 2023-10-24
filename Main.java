import java.io.*;

class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader obj = 
      new BufferedReader(
        new InputStreamReader(
          System.in)); 
    Otter o = new Otter();
  
    while (true) {
      System.out.print("> ");
      o.evaluate(obj.readLine());
      o.m.trace();
			System.out.println("Ok");
    } 
  }
}
