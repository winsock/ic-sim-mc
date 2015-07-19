package external.simulator.Simulator.parts;

import external.simulator.Simulator.parts.RailElm;

public class SquareRailElm extends RailElm {
    public SquareRailElm(int xx, int yy) {
        super(xx, yy, WF_SQUARE);
    }

    protected Class getDumpClass() {
        return RailElm.class;
    }

    public int getShortcut() {
        return 0;
    }
}
