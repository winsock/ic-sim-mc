package external.simulator.Simulator;

class ACVoltageElm extends VoltageElm {
    public ACVoltageElm(int xx, int yy) {
        super(xx, yy, WF_AC);
    }

    Class getDumpClass() {
        return VoltageElm.class;
    }
}
