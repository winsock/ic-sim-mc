package external.simulator.Simulator;

abstract class GraphicElm extends CircuitElm {

    GraphicElm(int xx, int yy) {
        super(xx, yy);
    }

    GraphicElm(int xa, int ya, int xb, int yb, int flags) {
        super(xa, ya, xb, yb, flags);
    }

    int getPostCount() {
        return 0;
    }

    @Override
    boolean isWire() {
        return false;
    }
}

