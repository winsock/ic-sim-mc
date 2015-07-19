package external.simulator.Simulator;

import java.util.Vector;

class CircuitNode {
    int x, y;
    final Vector<CircuitNodeLink> links;
    boolean internal;

    CircuitNode() {
        links = new Vector<CircuitNodeLink>();
    }
}
