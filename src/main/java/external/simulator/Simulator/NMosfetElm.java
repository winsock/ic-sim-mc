package external.simulator.Simulator;

class NMosfetElm extends MosfetElm {
    public NMosfetElm(int xx, int yy) {
        super(xx, yy, false);
    }

    Class getDumpClass() {
        return MosfetElm.class;
    }
}
