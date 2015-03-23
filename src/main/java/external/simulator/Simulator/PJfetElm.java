package external.simulator.Simulator;

/**
 * Created by winsock on 3/22/15.
 */
class PJfetElm extends JfetElm {
    public PJfetElm(int xx, int yy) {
        super(xx, yy, true);
    }

    Class getDumpClass() {
        return JfetElm.class;
    }
}
