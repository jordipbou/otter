import java.util.function.Consumer;

public class Debug implements Consumer<Dodo> {
  public void see(Dodo d) {
    int w = d.pop();
    System.out.print(": ");
    for (int i = 0; i < d.d.get(w + d.wNAMELEN); i++) {
      System.out.printf("%c", (char)d.d.get(w + d.wNAME + i));
    }
    System.out.print(" ");
    d.dump_code(d.d.getInt(w + d.wCODE));
    System.out.println(";");
  }

  public void tick(Dodo d) {
    d.parse_name();
    d.find_name();
    d.nip(); d.nip();
  }
  
  public void accept(Dodo d) {
    switch (d.token()) {
      case 's': see(d); break;
      case '\'': tick(d); break;
    }
  }
}