package external.simulator.parts;

public class ACRailElm extends RailElm {
	public ACRailElm(int xx, int yy) {
		super(xx, yy, VoltageElm.WF_AC);
	}

	protected Class<RailElm> getDumpClass() {
		return RailElm.class;
	}

	public int getShortcut() {
		return 0;
	}
}
