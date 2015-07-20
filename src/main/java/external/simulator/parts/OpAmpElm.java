package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class OpAmpElm extends CircuitElm {
	final int FLAG_SWAP = 1;
	private final int FLAG_SMALL = 2;
	private final int FLAG_LOWGAIN = 4;
	int opaddtext;
	boolean reset;
	private int opsize;
	private int opheight;
	private int opwidth;
	private double maxOut;
	private double minOut;
	private double gain;
	private double gbw;
	private Point[] in1p;
	private Point[] in2p;
	private Point[] textp;
	private Polygon triangle;
	private double lastvd;

	public OpAmpElm(int xx, int yy) {
		super(xx, yy);
		noDiagonal = true;
		maxOut = 15;
		minOut = -15;
		gbw = 1e6;
		setSize(1);
		setGain();
	}

	public OpAmpElm(int xa, int ya, int xb, int yb, int f,
	                StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		maxOut = 15;
		minOut = -15;
		// GBW has no effect in this version of the simulator, but we
		// retain it to keep the file format the same
		gbw = 1e6;
		try {
			maxOut = new Double(st.nextToken());
			minOut = new Double(st.nextToken());
			gbw = new Double(st.nextToken());
		} catch (Exception ignored) {
		}
		noDiagonal = true;
		setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
		setGain();
	}

	private void setGain() {
		// gain of 100000 breaks e-amp-dfdx.txt
		// gain was 1000, but it broke amp-schmitt.txt
		gain = ((flags & FLAG_LOWGAIN) != 0) ? 1000 : 100000;

	}

	protected String dump() {
		return super.dump() + " " + maxOut + " " + minOut + " " + gbw;
	}

	protected boolean nonLinear() {
		return true;
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(point1, point2, opheight * 2);
		drawThickLine(g, in1p[0], in1p[1], getVoltageColor(volts[0]));
		drawThickLine(g, in2p[0], in2p[1], getVoltageColor(volts[1]));
		Color color = needsHighlight() ? selectColor : lightGrayColor;
		drawThickPolygon(g, triangle, color);
		drawCenteredText(g, "-", textp[0].getX(), textp[0].getY() - 2, true, color);
		drawCenteredText(g, "+", textp[1].getX(), textp[1].getY(), true, color);
		drawThickLine(g, lead2, point2, getVoltageColor(volts[2]));
		curcount = updateDotCount(current, curcount);
		drawDots(g, point2, lead2, curcount);
		drawPosts(g, lightGrayColor);
	}

	protected double getPower() {
		return volts[2] * current;
	}

	private void setSize(int s) {
		opsize = s;
		opheight = 8 * s;
		opwidth = 13 * s;
		flags = (flags & ~FLAG_SMALL) | ((s == 1) ? FLAG_SMALL : 0);
	}

	protected void setPoints() {
		super.setPoints();
		if (dn > 150 && this == sim.getDragElm())
			setSize(2);
		int ww = opwidth;
		if (ww > dn / 2)
			ww = (int) (dn / 2);
		calcLeads(ww * 2);
		int hs = opheight * dsign;
		if ((flags & FLAG_SWAP) != 0)
			hs = -hs;
		in1p = newPointArray(2);
		in2p = newPointArray(2);
		textp = newPointArray(2);
		interpPoint2(point1, point2, in1p[0], in2p[0], 0, hs);
		interpPoint2(lead1, lead2, in1p[1], in2p[1], 0, hs);
		interpPoint2(lead1, lead2, textp[0], textp[1], .2, hs);
		Point tris[] = newPointArray(2);
		interpPoint2(lead1, lead2, tris[0], tris[1], 0, hs * 2);
		triangle = createPolygon(tris[0], tris[1], lead2);
	}

	protected int getPostCount() {
		return 3;
	}

	protected Point getPost(int n) {
		return (n == 0) ? in1p[0] : (n == 1) ? in2p[0] : point2;
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	public void getInfo(String arr[]) {
		arr[0] = "op-amp";
		arr[1] = "V+ = " + getVoltageText(volts[1]);
		arr[2] = "V- = " + getVoltageText(volts[0]);
		// sometimes the voltage goes slightly outside range, to make
		// convergence easier.  so we hide that here.
		double vo = Math.max(Math.min(volts[2], maxOut), minOut);
		arr[3] = "Vout = " + getVoltageText(vo);
		arr[4] = "Iout = " + getCurrentText(getCurrent());
		arr[5] = "range = " + getVoltageText(minOut) + " to " +
			getVoltageText(maxOut);
	}

	public void stamp() {
		int vn = sim.getNodeList().size() + voltSource;
		sim.stampNonLinear(vn);
		sim.stampMatrix(nodes[2], vn, 1);
	}

	public void doStep() {
		double vd = volts[1] - volts[0];
		if (Math.abs(lastvd - vd) > .1)
			sim.setConverged(false);
		else if (volts[2] > maxOut + .1 || volts[2] < minOut - .1)
			sim.setConverged(false);
		double x = 0;
		int vn = sim.getNodeList().size() + voltSource;
		double dx = 0;
		if (vd >= maxOut / gain && (lastvd >= 0 || sim.getrand(4) == 1)) {
			dx = 1e-4;
			x = maxOut - dx * maxOut / gain;
		} else if (vd <= minOut / gain && (lastvd <= 0 || sim.getrand(4) == 1)) {
			dx = 1e-4;
			x = minOut - dx * minOut / gain;
		} else
			dx = gain;
		//System.out.println("opamp " + vd + " " + volts[2] + " " + dx + " "  + x + " " + lastvd + " " + sim.converged);

		// newton-raphson
		sim.stampMatrix(vn, nodes[0], dx);
		sim.stampMatrix(vn, nodes[1], -dx);
		sim.stampMatrix(vn, nodes[2], 1);
		sim.stampRightSide(vn, x);

		lastvd = vd;
		/*if (sim.converged)
	      System.out.println((volts[1]-volts[0]) + " " + volts[2] + " " + initvd);*/
	}

	// there is no current path through the op-amp inputs, but there
	// is an indirect path through the output to ground.
	protected boolean getConnection(int n1, int n2) {
		return false;
	}

	protected boolean hasGroundConnection(int n1) {
		return (n1 == 2);
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	protected double getVoltageDiff() {
		return volts[2] - volts[1];
	}

	public int getDumpType() {
		return 'a';
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Max Output (V)", maxOut, 1, 20);
		if (n == 1)
			return new EditInfo("Min Output (V)", minOut, -20, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			maxOut = ei.getValue();
		if (n == 1)
			minOut = ei.getValue();
	}
}
