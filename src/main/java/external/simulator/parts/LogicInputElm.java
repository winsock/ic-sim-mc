package external.simulator.parts;

import external.simulator.EditInfo;
import external.simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class LogicInputElm extends SwitchElm {
	private final int FLAG_TERNARY = 1;
	private final int FLAG_NUMERIC = 2;
	private double hiV;
	private double loV;

	public LogicInputElm(int xx, int yy) {
		super(xx, yy, false);
		hiV = 5;
		loV = 0;
	}

	public LogicInputElm(int xa, int ya, int xb, int yb, int f,
	                     StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		try {
			hiV = new Double(st.nextToken());
			loV = new Double(st.nextToken());
		} catch (Exception e) {
			hiV = 5;
			loV = 0;
		}
		if (isTernary())
			posCount = 3;
	}

	private boolean isTernary() {
		return (flags & FLAG_TERNARY) != 0;
	}

	private boolean isNumeric() {
		return (flags & (FLAG_TERNARY | FLAG_NUMERIC)) != 0;
	}

	public int getDumpType() {
		return 'L';
	}

	protected String dump() {
		return super.dump() + " " + hiV + " " + loV;
	}

	protected int getPostCount() {
		return 1;
	}

	protected void setPoints() {
		super.setPoints();
		lead1 = interpPoint(point1, point2, 1 - 12 / dn);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		Color c = needsHighlight() ? selectColor : whiteColor;
		String s = position == 0 ? "L" : "H";
		if (isNumeric())
			s = "" + position;
		setBbox(point1, lead1, 0);
		drawCenteredText(g, s, x2, y2, true, c);
		drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));
		updateDotCount();
		drawDots(g, point1, lead1, curcount);
		drawPosts(g, lightGrayColor);
	}

	protected void setCurrent(int vs, double c) {
		current = -c;
	}

	public void stamp() {
		double v = (position == 0) ? loV : hiV;
		if (isTernary())
			v = position * 2.5;
		sim.stampVoltageSource(0, nodes[0], voltSource, v);
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	public void getInfo(String arr[]) {
		arr[0] = "logic input";
		arr[1] = (position == 0) ? "low" : "high";
		if (isNumeric())
			arr[1] = "" + position;
		arr[1] += " (" + getVoltageText(volts[0]) + ")";
		arr[2] = "I = " + getCurrentText(getCurrent());
	}

	protected boolean hasGroundConnection(int n1) {
		return true;
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, 0, 0);
			ei.setCheckbox(new Toggleable("Momentary Switch", momentary));
			return ei;
		}
		if (n == 1)
			return new EditInfo("High Voltage", hiV, 10, -10);
		if (n == 2)
			return new EditInfo("Low Voltage", loV, 10, -10);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			momentary = ei.getCheckbox().getState();
		if (n == 1)
			hiV = ei.getValue();
		if (n == 2)
			loV = ei.getValue();
	}

	public int getShortcut() {
		return 'i';
	}
}
