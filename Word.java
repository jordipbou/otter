public class Word extends DictItem {
  public CharSequence code;
  public Boolean immediate;
  public Boolean hidden;

  public static void create(String n, Environment e) {
    Word w = new Word();
    w.previous = e.latest;
    e.latest = w;
    w.name = n;
    w.immediate = false;
    w.hidden = false;
  }
}