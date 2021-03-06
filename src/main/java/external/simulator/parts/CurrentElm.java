package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class CurrentElm extends CircuitElm {
	private Polygon arrow;
	private double currentValue;
	private Point ashaft1;
	private Point ashaft2;
	private Point center;

	public CurrentElm(int xx, int yy) {
		super(xx, yy);
		currentValue = .01;
	}

	public CurrentElm(int xa, int ya, int xb, int yb, int f,
	                  StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		try {
			currentValue = new Double(st.nextToken());
		} catch (Exception e) {
			currentValue = .01;
		}
	}

	protected String dump() {
		return super.dump() + " " + currentValue;
	}

	public int getDumpType() {
		return 'i';
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(26);
		ashaft1 = interpPoint(lead1, lead2, .25);
		ashaft2 = interpPoint(lead1, lead2, .6);
		center = interpPoint(lead1, lead2, .5);
		Point p2 = interpPoint(lead1, lead2, .75);
		arrow = calcArrow(center, p2, 4, 4);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		int cr = 12;
		draw2Leads(g, CircuitElm.lightGrayColor);
		getVoltageColor((volts[0] + volts[1]) / 2);

		drawThickCircle(g, center.getX(), center.getY(), cr, Color.YELLOW);
		drawThickLine(g, ashaft1, ashaft2, CircuitElm.whiteColor);

		drawPolygon(g, arrow, Color.YELLOW);
		setBbox(point1, point2, cr);
		doDots(g);
/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(currentValue, "A");
            if (dx == 0 || dy == 0)
                drawValues(g, s, cr);
        }*/
		drawPosts(g, Color.lightGray);
	}

	public void stamp() {
		current = currentValue;
		sim.stampCurrentSource(nodes[0], nodes[1], current);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Current (A)", currentValue, 0, .1);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		currentValue = ei.getValue();
	}

	@Override
	protected boolean isWire() {
		return false;
	}

	public void getInfo(String arr[]) {
		arr[0] = "current source";
		getBasicInfo(arr);
	}

	protected double getVoltageDiff() {
		return volts[1] - volts[0];
	}
}
