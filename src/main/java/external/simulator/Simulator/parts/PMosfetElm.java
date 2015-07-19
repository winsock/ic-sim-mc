package external.simulator.Simulator.parts;

public class PMosfetElm extends MosfetElm {
    public PMosfetElm(int xx, int yy) {
        super(xx, yy, true);
    }

    protected Class getDumpClass() {
        return MosfetElm.class;
    }
}
