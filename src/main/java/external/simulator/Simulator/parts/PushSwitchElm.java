package external.simulator.Simulator.parts;

public class PushSwitchElm extends SwitchElm {
    public PushSwitchElm(int xx, int yy) {
        super(xx, yy, true);
    }

    protected Class getDumpClass() {
        return SwitchElm.class;
    }

    public int getShortcut() {
        return 0;
    }
}
