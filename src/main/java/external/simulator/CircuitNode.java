package external.simulator;

import java.util.Vector;

class CircuitNode {
	final Vector<CircuitNodeLink> links;
	int x, y;
	boolean internal;

	CircuitNode() {
		links = new Vector<CircuitNodeLink>();
	}
}
