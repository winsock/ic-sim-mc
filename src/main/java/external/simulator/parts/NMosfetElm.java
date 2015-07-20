package external.simulator.parts;

public class NMosfetElm extends MosfetElm {
	public NMosfetElm(int xx, int yy) {
		super(xx, yy, false);
	}

	protected Class<MosfetElm> getDumpClass() {
		return MosfetElm.class;
	}
}
