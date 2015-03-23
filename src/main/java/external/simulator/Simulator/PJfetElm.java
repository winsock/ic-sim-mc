package external.simulator.Simulator;

class PJfetElm extends JfetElm {
    public PJfetElm(int xx, int yy) {
        super(xx, yy, true);
    }

    Class getDumpClass() {
        return JfetElm.class;
    }
}
