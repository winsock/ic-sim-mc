package external.simulator.parts;

public class PTransistorElm extends TransistorElm {
	public PTransistorElm(int xx, int yy) {
		super(xx, yy, true);
	}

	protected Class<TransistorElm> getDumpClass() {
		return TransistorElm.class;
	}
}
