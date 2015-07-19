package external.simulator.Simulator.parts;

public class ACRailElm extends RailElm {
    public ACRailElm(int xx, int yy) {
        super(xx, yy, VoltageElm.WF_AC);
    }

    protected Class getDumpClass() {
        return RailElm.class;
    }

    public int getShortcut() {
        return 0;
    }
}
