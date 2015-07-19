package external.simulator.Simulator.parts;

public class NTransistorElm extends TransistorElm {
    public NTransistorElm(int xx, int yy) {
        super(xx, yy, false);
    }

    protected Class getDumpClass() {
        return TransistorElm.class;
    }
}
