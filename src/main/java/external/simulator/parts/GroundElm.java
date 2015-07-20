package external.simulator.parts;

import external.simulator.CircuitElm;
import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class GroundElm extends CircuitElm {
	public GroundElm(int xx, int yy) {
		super(xx, yy);
	}

	public GroundElm(int xa, int ya, int xb, int yb, int f,
	                 StringTokenizer st) {
		super(xa, ya, xb, yb, f);
	}

	public int getDumpType() {
		return 'g';
	}

	protected int getPostCount() {
		return 1;
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		Color color = getVoltageColor(0);
		drawThickLine(g, point1, point2, color);
		int i;
		for (i = 0; i != 3; i++) {
			int a = 10 - i * 4;
			int b = i * 5; // -10;
			interpPoint2(point1, point2, ps1, ps2, 1 + b / dn, a);
			drawThickLine(g, ps1, ps2, color);
		}
		doDots(g);
		interpPoint(point1, point2, ps2, 1 + 11. / dn);
		setBbox(point1, ps2, 11);
		drawPost(g, x, y, nodes[0], color);
	}

	protected void setCurrent(int x, double c) {
		current = -c;
	}

	public void stamp() {
		sim.stampVoltageSource(0, nodes[0], voltSource, 0);
	}

	protected double getVoltageDiff() {
		return 0;
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	public void getInfo(String arr[]) {
		arr[0] = "ground";
		arr[1] = "I = " + getCurrentText(getCurrent());
	}

	protected boolean hasGroundConnection(int n1) {
		return true;
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public int getShortcut() {
		return 'g';
	}
}
