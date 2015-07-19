package external.simulator.Simulator;

public class NJfetElm extends JfetElm {
    public NJfetElm(int xx, int yy) {
        super(xx, yy, false);
    }

    Class getDumpClass() {
        return JfetElm.class;
    }
}

