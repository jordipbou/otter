import java.util.function.Consumer;

public class Terminal implements Consumer<Otter> {
	public static void emit(Otter o) {
		System.out.printf("%c", (byte)o.pop());
	}

	public static void key(Otter o) {
		// TODO
	}

	public static void write(Otter o) {
		int l = o.pop();
		int s = o.pop();
		for (int i = 0; i < l; i++) {
			System.out.printf("%c", (byte)o.block.get(s + i));
		}
	}

	public static void read(Otter o) {
		// TODO
	}

	public void accept(Otter o) {
		switch (o.token()) {
			case 'e': emit(o); break;
			case 'k': key(o); break;
			case 't': write(o); break;
			case 'a': read(o); break;
		}
	}
}
