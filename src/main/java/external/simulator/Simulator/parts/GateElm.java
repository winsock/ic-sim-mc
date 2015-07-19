package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public abstract class GateElm extends CircuitElm {
	private final int FLAG_SMALL = 1;
	int inputCount = 2;
	int hs2;
	int ww;
	Polygon gatePoly;
	Point pcircle, linePoints[];
	private boolean lastOutput;
	private int gwidth;
	private int gwidth2;
	private int gheight;
	private Point[] inPosts;
	private Point[] inGates;

	public GateElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
		inputCount = 2;
	}

	public GateElm(int xa, int ya, int xb, int yb, int f,
	        StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		inputCount = new Integer(st.nextToken());
		lastOutput = new Double(st.nextToken()) > 2.5;
		noDiagonal = true;
		setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
	}

	boolean isInverting() {
		return false;
	}

	private void setSize(int s) {
		gwidth = 7 * s;
		gwidth2 = 14 * s;
		gheight = 8 * s;
		flags = (s == 1) ? FLAG_SMALL : 0;
	}

	protected String dump() {
		return super.dump() + " " + inputCount + " " + volts[inputCount];
	}

	protected void setPoints() {
		super.setPoints();
		if (dn > 150 && this == sim.getDragElm())
			setSize(2);
		int hs = gheight;
		int i;
		ww = gwidth2; // was 24
		if (ww > dn / 2)
			ww = (int) (dn / 2);
		if (isInverting() && ww + 8 > dn / 2)
			ww = (int) (dn / 2 - 8);
		calcLeads(ww * 2);
		inPosts = new Point[inputCount];
		inGates = new Point[inputCount];
		allocNodes();
		int i0 = -inputCount / 2;
		for (i = 0; i != inputCount; i++, i0++) {
			if (i0 == 0 && (inputCount & 1) == 0)
				i0++;
			inPosts[i] = interpPoint(point1, point2, 0, hs * i0);
			inGates[i] = interpPoint(lead1, lead2, 0, hs * i0);
			volts[i] = (lastOutput ^ isInverting()) ? 5 : 0;
		}
		hs2 = gwidth * (inputCount / 2 + 1);
		setBbox(point1, point2, hs2);
	}

	public void draw(CircuitGUI.ClientCircuitGui screen, int mouseX, int mouseY, float partialTicks) {
		int i;
		for (i = 0; i != inputCount; i++) {
			drawThickLine(screen, inPosts[i], inGates[i], getVoltageColor(volts[i]));
		}
		Color mainColor = (needsHighlight() ? selectColor : lightGrayColor);
		drawThickLine(screen, lead2, point2, getVoltageColor(volts[inputCount]));
		drawThickPolygon(screen, gatePoly, mainColor);
		if (linePoints != null)
			for (i = 0; i != linePoints.length - 1; i++)
				drawThickLine(screen, linePoints[i], linePoints[i + 1], mainColor);
		if (isInverting())
			drawThickCircle(screen, pcircle.getX(), pcircle.getY(), 3, mainColor);
		curcount = updateDotCount(current, curcount);
		drawDots(screen, lead2, point2, curcount);
		drawPosts(screen, mainColor);
	}

	protected int getPostCount() {
		return inputCount + 1;
	}

	protected Point getPost(int n) {
		if (n == inputCount)
			return point2;
		return inPosts[n];
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	public abstract String getGateName();

	public void getInfo(String arr[]) {
		arr[0] = getGateName();
		arr[1] = "Vout = " + getVoltageText(volts[inputCount]);
		arr[2] = "Iout = " + getCurrentText(getCurrent());
	}

	public void stamp() {
		sim.stampVoltageSource(0, nodes[inputCount], voltSource);
	}

	public boolean getInput(int x) {
		return volts[x] > 2.5;
	}

	public abstract boolean calcFunction();

	public void doStep() {
		int i;
		boolean f = calcFunction();
		if (isInverting())
			f = !f;
		lastOutput = f;
		double res = f ? 5 : 0;
		sim.updateVoltageSource(0, nodes[inputCount], voltSource, res);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("# of Inputs", inputCount, 1, 8).
				setDimensionless();
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		inputCount = (int) ei.getValue();
		setPoints();
	}

	// there is no current path through the gate inputs, but there
	// is an indirect path through the output to ground.
	protected boolean getConnection(int n1, int n2) {
		return false;
	}

	protected boolean hasGroundConnection(int n1) {
		return (n1 == inputCount);
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}

