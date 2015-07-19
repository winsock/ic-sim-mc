package external.simulator.Simulator.parts;

public class ACVoltageElm extends VoltageElm {
    public ACVoltageElm(int xx, int yy) {
        super(xx, yy, WF_AC);
    }

    protected Class getDumpClass() {
        return VoltageElm.class;
    }
}
