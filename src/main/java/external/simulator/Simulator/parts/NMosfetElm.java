package external.simulator.Simulator.parts;

public class NMosfetElm extends MosfetElm {
    public NMosfetElm(int xx, int yy) {
        super(xx, yy, false);
    }

    protected Class getDumpClass() {
        return MosfetElm.class;
    }
}
