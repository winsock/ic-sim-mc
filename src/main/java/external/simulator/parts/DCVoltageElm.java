package external.simulator.parts;

public class DCVoltageElm extends VoltageElm {
	public DCVoltageElm(int xx, int yy) {
		super(xx, yy, WF_DC);
	}

	protected Class getDumpClass() {
		return VoltageElm.class;
	}

	public int getShortcut() {
		return 'v';
	}
}
