package external.simulator.Simulator.parts;

import external.simulator.Simulator.CircuitElm;
import external.simulator.Simulator.EditInfo;
import external.simulator.Simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;

import java.awt.*;
import java.util.StringTokenizer;

public class WireElm extends CircuitElm {
	private static final int FLAG_SHOWCURRENT = 1;
	private static final int FLAG_SHOWVOLTAGE = 2;

	public WireElm(int xx, int yy) {
		super(xx, yy);
	}

	public WireElm(int xa, int ya, int xb, int yb, int f,
	               StringTokenizer st) {
		super(xa, ya, xb, yb, f);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		Color color = getVoltageColor(volts[0]);
		drawThickLine(g, point1, point2, color);
		doDots(g);
		setBbox(point1, point2, 3);
		if (mustShowCurrent()) {
			String s = getShortUnitText(Math.abs(getCurrent()), "A");
			drawValues(g, s, 4, color);
		} else if (mustShowVoltage()) {
			String s = getShortUnitText(volts[0], "V");
			drawValues(g, s, 4, color);
		}
		drawPosts(g, lightGrayColor);
	}

	public void stamp() {
		sim.stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
	}

	private boolean mustShowCurrent() {
		return (flags & FLAG_SHOWCURRENT) != 0;
	}

	private boolean mustShowVoltage() {
		return (flags & FLAG_SHOWVOLTAGE) != 0;
	}

	protected int getVoltageSourceCount() {
		return 1;
	}

	public void getInfo(String arr[]) {
		arr[0] = "wire";
		arr[1] = "I = " + getCurrentDText(getCurrent());
		arr[2] = "V = " + getVoltageText(volts[0]);
	}

	public int getDumpType() {
		return 'w';
	}

	protected double getPower() {
		return 0;
	}

	protected double getVoltageDiff() {
		return volts[0];
	}

	protected boolean isWire() {
		return true;
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Show Current", mustShowCurrent()));
			return ei;
		}
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Show Voltage", mustShowVoltage()));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0) {
			if (ei.getCheckbox().getState())
				flags = FLAG_SHOWCURRENT;
			else
				flags &= ~FLAG_SHOWCURRENT;
		}
		if (n == 1) {
			if (ei.getCheckbox().getState())
				flags = FLAG_SHOWVOLTAGE;
			else
				flags &= ~FLAG_SHOWVOLTAGE;
		}
	}

	public int getShortcut() {
		return 'w';
	}
}
