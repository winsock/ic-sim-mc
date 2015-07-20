package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class AnalogSwitchElm extends CircuitElm {
	final int FLAG_INVERT = 1;
	double r_on;
	double r_off;
	boolean open;
	private double resistance;
	private Point ps;
	private Point point3;
	private Point lead3;

	public AnalogSwitchElm(int xx, int yy) {
		super(xx, yy);
		r_on = 20;
		r_off = 1e10;
	}

	public AnalogSwitchElm(int xa, int ya, int xb, int yb, int f,
	                       StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		r_on = 20;
		r_off = 1e10;
		try {
			r_on = new Double(st.nextToken());
			r_off = new Double(st.nextToken());
		} catch (Exception ignored) {
		}

	}

	protected String dump() {
		return super.dump() + " " + r_on + " " + r_off;
	}

	public int getDumpType() {
		return 159;
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(32);
		ps = new Point();
		int openhs = 16;
		point3 = interpPoint(point1, point2, .5, -openhs);
		lead3 = interpPoint(point1, point2, .5, -openhs / 2);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int openhs = 16;
		int hs = (open) ? openhs : 0;
		setBbox(point1, point2, openhs);

		draw2Leads(g, Color.YELLOW);

		interpPoint(lead1, lead2, ps, 1, hs);
		drawThickLine(g, lead1, ps, lightGrayColor);

		drawThickLine(g, point3, lead3, getVoltageColor(volts[2]));

		if (!open)
			doDots(g);
		drawPosts(g, lightGrayColor);
	}

	protected void calculateCurrent() {
		current = (volts[0] - volts[1]) / resistance;
	}

	// we need this to be able to change the matrix for each step
	protected boolean nonLinear() {
		return true;
	}

	public void stamp() {
		sim.stampNonLinear(nodes[0]);
		sim.stampNonLinear(nodes[1]);
	}

	public void doStep() {
		open = (volts[2] < 2.5);
		if ((flags & FLAG_INVERT) != 0)
			open = !open;
		resistance = (open) ? r_off : r_on;
		sim.stampResistor(nodes[0], nodes[1], resistance);
	}

	protected void drag(int xx, int yy) {
		xx = sim.snapGrid(xx);
		yy = sim.snapGrid(yy);
		if (abs(x - xx) < abs(y - yy))
			xx = x;
		else
			yy = y;
		int q1 = abs(x - xx) + abs(y - yy);
		int q2 = (q1 / 2) % sim.getGridSize();
		if (q2 != 0)
			return;
		x2 = xx;
		y2 = yy;
		setPoints();
	}

	protected int getPostCount() {
		return 3;
	}

	protected Point getPost(int n) {
		return (n == 0) ? point1 : (n == 1) ? point2 : point3;
	}

	public void getInfo(String arr[]) {
		arr[0] = "analog switch";
		arr[1] = open ? "open" : "closed";
		arr[2] = "Vd = " + getVoltageDText(getVoltageDiff());
		arr[3] = "I = " + getCurrentDText(getCurrent());
		arr[4] = "Vc = " + getVoltageText(volts[2]);
	}

	// we have to just assume current will flow either way, even though that
	// might cause singular matrix errors
	protected boolean getConnection(int n1, int n2) {
		return !(n1 == 2 || n2 == 2);
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Normally closed",
				(flags & FLAG_INVERT) != 0));
			return ei;
		}
		if (n == 1)
			return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
		if (n == 2)
			return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			flags = (ei.getCheckbox().getState()) ?
			        (flags | FLAG_INVERT) :
			        (flags & ~FLAG_INVERT);
		if (n == 1 && ei.getValue() > 0)
			r_on = ei.getValue();
		if (n == 2 && ei.getValue() > 0)
			r_off = ei.getValue();
	}
}

