package external.simulator.parts;

public class NTransistorElm extends TransistorElm {
	public NTransistorElm(int xx, int yy) {
		super(xx, yy, false);
	}

	protected Class<TransistorElm> getDumpClass() {
		return TransistorElm.class;
	}
}
