package external.simulator.Simulator.parts;

public class ClockElm extends RailElm {
    public ClockElm(int xx, int yy) {
        super(xx, yy, VoltageElm.WF_SQUARE);
        maxVoltage = 2.5;
        bias = 2.5;
        frequency = 100;
        flags |= FLAG_CLOCK;
    }

    protected Class getDumpClass() {
        return RailElm.class;
    }

    public int getShortcut() {
        return 0;
    }
}
