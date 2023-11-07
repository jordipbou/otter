import java.util.function.Consumer;
import java.io.IOException;

public class Terminal implements Consumer<Otter> {
  /*
	public static void dot(Otter o) {
		System.out.printf("%d ", o.pop());
	}

	public static void emit(Otter o) {
		System.out.printf("%c", (byte)o.pop());
	}

	public static void key(Otter o) {
		try {
			o.push(System.in.read());
		} catch(IOException e) {
			o.push(0);
		}
	}

	public static void write(Otter o) {
		int l = o.pop();
		int s = o.pop();
		for (int i = 0; i < l; i++) {
			System.out.printf("%c", (byte)o.block.get(s + i));
		}
	}

	public static void read(Otter o) {
		int l = o.pop();
		int s = o.pop();
		byte[] buf = new byte[255];
		try {
			int n = System.in.read(buf);
			for (int i = 0; i < n; i++) {
				o.block.put(s + i, buf[i]);
			}
			o.push(n);
		} catch (IOException e) {
			o.push(0);
		}
	}
*/
	public void accept(Otter o) {
		switch (o.token()) {
			// case '.': dot(o); break;
			// case 'e': emit(o); break;
			// case 'k': key(o); break;
			// case 't': write(o); break;
			// case 'a': read(o); break;
		}
	}
}
