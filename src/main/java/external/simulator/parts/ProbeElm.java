package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class ProbeElm extends CircuitElm {
	private static final int FLAG_SHOWVOLTAGE = 1;
	private Point center;

	public ProbeElm(int xx, int yy) {
		super(xx, yy);
	}

	public ProbeElm(int xa, int ya, int xb, int yb, int f,
	                StringTokenizer st) {
		super(xa, ya, xb, yb, f);
	}

	public int getDumpType() {
		return 'p';
	}

	protected void setPoints() {
		super.setPoints();
		// swap points so that we subtract higher from lower
		if (point2.getY() < point1.getY()) {
			Point x = point1;
			point1 = point2;
			point2 = x;
		}
		center = interpPoint(point1, point2, .5);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int hs = 8;
		setBbox(point1, point2, hs);
		boolean selected = (needsHighlight() || sim.getPlotYElm() == this);
		double len = (selected || sim.getDragElm() == this) ? 16 : dn - 32;
		calcLeads((int) len);
		Color color = getVoltageColor(volts[0]);
		drawThickLine(g, point1, lead1, color);
		color = getVoltageColor(volts[1]);
		drawThickLine(g, lead2, point2, color);
		if (this == sim.getPlotXElm())
			drawCenteredText(g, "X", center.getX(), center.getY(), true, whiteColor);
		if (this == sim.getPlotYElm())
			drawCenteredText(g, "Y", center.getX(), center.getY(), true, whiteColor);
		if (mustShowVoltage()) {
			String s = getShortUnitText(volts[0], "V");
			drawValues(g, s, 4, whiteColor);
		}
		drawPosts(g, lightGrayColor);
	}

	private boolean mustShowVoltage() {
		return (flags & FLAG_SHOWVOLTAGE) != 0;
	}

	public void getInfo(String arr[]) {
		arr[0] = "scope probe";
		arr[1] = "Vd = " + getVoltageText(getVoltageDiff());
	}

	protected boolean getConnection(int n1, int n2) {
		return false;
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Show Voltage", mustShowVoltage()));
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0) {
			if (ei.getCheckbox().getState())
				flags = FLAG_SHOWVOLTAGE;
			else
				flags &= ~FLAG_SHOWVOLTAGE;
		}
	}
}

