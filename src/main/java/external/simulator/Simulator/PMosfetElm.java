package external.simulator.Simulator;

public class PMosfetElm extends MosfetElm {
    public PMosfetElm(int xx, int yy) {
        super(xx, yy, true);
    }

    Class getDumpClass() {
        return MosfetElm.class;
    }
}
