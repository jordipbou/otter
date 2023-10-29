import java.util.function.Consumer;

public class Combinators implements Consumer<Otter> {
  public void times(Otter o) {
    long q = o.pop();
    long n = o.pop();
    while (n-- > 0) {
      o.eval(q);
    }
  }
  
  public void accept(Otter o) {
    switch (o.token()) {
      case 't': times(o); break;
    }
  }
}