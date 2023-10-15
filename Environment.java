public class Environment extends DictItem {
  public Environment parent;
  public DictItem latest;

  public DictItem findName(String name) {
    Environment e = this;
    while (e != null) {
      DictItem i = e.latest;
      while (i != null) {
        if (i.name.equals(name)) {
          return i;
        }
        i = i.previous;
      }
      e = e.parent;
    }
    return null;
  }
}