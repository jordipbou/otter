import java.util.function.Consumer;

public class Combinators implements Consumer<Otter> {
  public void ifte(Otter o) {
    long a = o.pop();
    long b = o.pop();
    if (o.pop() == 0) {
      o.eval(a);
    } else {
      o.eval(b);
    }
  }

  public void bi(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    o.push(c);
    o.eval(b);
    o.push(c);
    o.eval(a);
  }

  public void bi2(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    long d = o.pop();
    o.push(d);
    o.push(c);
    o.eval(b);
    o.push(d);
    o.push(c);
    o.eval(a);
  }

  public void bi3(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    long d = o.pop();
    long e = o.pop();
    o.push(e);
    o.push(d);
    o.push(c);
    o.eval(b);
    o.push(e);
    o.push(d);
    o.push(c);
    o.eval(a);
  }
  
  public void dip(Otter o) {
    long a = o.pop();
    long b = o.pop();
    o.eval(a);
    o.push(b);
  }

  public void dip2(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    o.eval(a);
    o.push(c);
    o.push(b);
  }

  public void dip3(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    long d = o.pop();
    o.eval(a);
    o.push(d);
    o.push(c);
    o.push(b);
  }
  
  public void dip4(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    long d = o.pop();
    long e = o.pop();
    o.eval(a);
    o.push(e);
    o.push(d);
    o.push(c);
    o.push(b);
  }

  public void keep(Otter o) {
    long a = o.pop();
    long b = o.pop();
    o.push(b);
    o.eval(a);
    o.push(b);
  }

  public void keep2(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    o.push(c);
    o.push(b);
    o.eval(a);
    o.push(c);
    o.push(b);
  }

  public void keep3(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    long d = o.pop();
    o.push(d);
    o.push(c);
    o.push(b);
    o.eval(a);
    o.push(d);
    o.push(c);
    o.push(b);
  }

  public void times(Otter o) {
    long q = o.pop();
    long n = o.pop();
    while (n-- > 0) {
      o.eval(q);
    }
  }

  public void _binrec(Otter o, long a, long b, long c, long d) {
    o.eval(a);
    if (o.pop() != 0) {
      o.eval(b);
    } else {
      o.eval(c);
      _binrec(o, a, b, c, d);
      o.swap();
      _binrec(o, a, b, c, d);
      o.eval(d);
    }
  }

  public void binrec(Otter o) {
    long d = o.pop();
    long c = o.pop();
    long b = o.pop();
    long a = o.pop();
    _binrec(o, a, b, c, d);
  }

  public void _linrec(Otter o, long a, long b, long c, long d) {
    o.eval(d);
    if (o.pop() != 0) {
      o.eval(c);
    } else {
      o.eval(b);
      _linrec(o, a, b, c, d);
      o.eval(a);
    }
  }

  public void linrec(Otter o) {
    long a = o.pop();
    long b = o.pop();
    long c = o.pop();
    long d = o.pop();
    _linrec(o, a, b, c, d);
  }
  
  public void _primrec(Otter o, long a, long b) {
    long n = o.pop();
    if (n == 0) {
      o.eval(b);
    } else {
      o.push(n);
      o.push(n - 1L);
      _primrec(o, a, b);
      o.eval(a);
    }
  }
  
  public void primrec(Otter o) {
    long a = o.pop();
    long b = o.pop();
    _primrec(o, a, b);
  }

  public void until(Otter o) {
    long a = o.pop();
    do {
      o.eval(a);
      if (o.pop() == 0) break;
    } while(true);
  }

  public void _while(Otter o) {
    long a = o.pop();
    do {
      o.eval(a);
      if (o.pop() != 0) break;
    } while(true);
  }
  
  public void accept(Otter o) {
    switch (o.token()) {
      case '?': ifte(o); break;
      case 'b':
        switch (o.token()) {
          case '1': bi(o); break;
          case '2': bi2(o); break;
          case '3': bi3(o); break;
        }
        break;
      case 'd': 
        switch (o.token()) {
          case '1': dip(o); break;
          case '2': dip2(o); break;
          case '3': dip3(o); break;
          case '4': dip4(o); break;
        }
        break;
      case 'k':
        switch (o.token()) {
          case '1': keep(o); break;
          case '2': keep2(o); break;
          case '3': keep3(o); break;
        }
        break;
      case 't': times(o); break;
      case 'r':
        switch (o.token()) {
          case 'b': binrec(o); break;
          case 'l': linrec(o); break;
          case 'p': primrec(o); break;
        }
        break;
      case 'u': until(o); break;
      case 'w': _while(o); break;    
    }
  }
}