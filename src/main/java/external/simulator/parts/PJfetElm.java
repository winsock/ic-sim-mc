package external.simulator.parts;

public class PJfetElm extends JfetElm {
	public PJfetElm(int xx, int yy) {
		super(xx, yy, true);
	}

	protected Class<JfetElm> getDumpClass() {
		return JfetElm.class;
	}
}
