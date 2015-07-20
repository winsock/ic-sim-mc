package external.simulator;

public abstract class GraphicElm extends CircuitElm {

	public GraphicElm(int xx, int yy) {
		super(xx, yy);
	}

	public GraphicElm(int xa, int ya, int xb, int yb, int flags) {
		super(xa, ya, xb, yb, flags);
	}

	protected int getPostCount() {
		return 0;
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}

