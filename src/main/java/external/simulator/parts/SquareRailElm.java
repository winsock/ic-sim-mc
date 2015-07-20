package external.simulator.parts;

public class SquareRailElm extends RailElm {
	public SquareRailElm(int xx, int yy) {
		super(xx, yy, WF_SQUARE);
	}

	protected Class<RailElm> getDumpClass() {
		return RailElm.class;
	}

	public int getShortcut() {
		return 0;
	}
}
