package external.simulator.Simulator.parts;

public class PJfetElm extends JfetElm {
    public PJfetElm(int xx, int yy) {
        super(xx, yy, true);
    }

    protected Class getDumpClass() {
        return JfetElm.class;
    }
}
