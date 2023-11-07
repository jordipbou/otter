import java.util.function.Consumer;

public class Machine {
  public Dictionary dict;
  public Consumer<Otter>[] extensions;

  public Machine() {
    dict = new Dictionary(64 * 1024 * 4);
    extensions = new Consumer[26];
  }
}