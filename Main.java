import java.io.*;

class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader obj = 
      new BufferedReader(
        new InputStreamReader(
          System.in)); 
    Otter x = new Otter();
  
    while (true) {
      System.out.print("> ");
      x.evaluate(obj.readLine());
      x.trace();
    } 
  }
}