package external.simulator.parts;

public class PushSwitchElm extends SwitchElm {
	public PushSwitchElm(int xx, int yy) {
		super(xx, yy, true);
	}

	protected Class<SwitchElm> getDumpClass() {
		return SwitchElm.class;
	}

	public int getShortcut() {
		return 0;
	}
}
