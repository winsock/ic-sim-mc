package external.simulator.parts;

import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;
import org.lwjgl.util.Point;

import java.util.StringTokenizer;

public class Switch2Elm extends SwitchElm {
	private static final int FLAG_CENTER_OFF = 1;
	private final int openhs = 16;
	private int link;
	private Point[] swposts;
	private Point[] swpoles;

	public Switch2Elm(int xx, int yy) {
		super(xx, yy, false);
		noDiagonal = true;
	}

	Switch2Elm(int xx, int yy, boolean mm) {
		super(xx, yy, mm);
		noDiagonal = true;
	}

	public Switch2Elm(int xa, int ya, int xb, int yb, int f,
	                  StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		link = new Integer(st.nextToken());
		noDiagonal = true;
	}

	public int getDumpType() {
		return 'S';
	}

	protected String dump() {
		return super.dump() + " " + link;
	}

	protected void setPoints() {
		super.setPoints();
		calcLeads(32);
		swposts = newPointArray(2);
		swpoles = newPointArray(3);
		interpPoint2(lead1, lead2, swpoles[0], swpoles[1], 1, openhs);
		swpoles[2] = lead2;
		interpPoint2(point1, point2, swposts[0], swposts[1], 1, openhs);
		posCount = hasCenterOff() ? 3 : 2;
	}

/*    void draw(CircuitGUI g) {
        setBbox(point1, point2, openhs);

        // draw first lead
        getVoltageColor(g, volts[0]);
        drawThickLine(g, point1, lead1);

        // draw second lead
        getVoltageColor(g, volts[1]);
        drawThickLine(g, swpoles[0], swposts[0]);

        // draw third lead
        getVoltageColor(g, volts[2]);
        drawThickLine(g, swpoles[1], swposts[1]);

        // draw switch
        if (!needsHighlight())
            g.setColor(whiteColor);
        drawThickLine(g, lead1, swpoles[position]);

        updateDotCount();
        drawDots(g, point1, lead1, curcount);
        if (position != 2)
            drawDots(g, swpoles[position], swposts[position], curcount);
        drawPosts(g);
    }*/

	protected Point getPost(int n) {
		return (n == 0) ? point1 : swposts[n - 1];
	}

	protected int getPostCount() {
		return 3;
	}

	protected void calculateCurrent() {
		if (position == 2)
			current = 0;
	}

	public void stamp() {
		if (position == 2) // in center?
			return;
		CircuitElm.sim.stampVoltageSource(nodes[0], nodes[position + 1], voltSource, 0);
	}

	protected int getVoltageSourceCount() {
		return (position == 2) ? 0 : 1;
	}

	public void toggle() {
		super.toggle();
		if (link != 0) {
			int i;
			for (i = 0; i != CircuitElm.sim.elmList.size(); i++) {
				Object o = CircuitElm.sim.elmList.elementAt(i);
				if (o instanceof Switch2Elm) {
					Switch2Elm s2 = (Switch2Elm) o;
					if (s2.link == link)
						s2.position = position;
				}
			}
		}
	}

	protected boolean getConnection(int n1, int n2) {
		return position != 2 && comparePair(n1, n2, 0, 1 + position);
	}

	public void getInfo(String arr[]) {
		arr[0] = (link == 0) ? "switch (SPDT)" : "switch (DPDT)";
		arr[1] = "I = " + CircuitElm.getCurrentDText(getCurrent());
	}

	public EditInfo getEditInfo(int n) {
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Center Off", hasCenterOff()));
			return ei;
		}
		return super.getEditInfo(n);
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 1) {
			flags &= ~FLAG_CENTER_OFF;
			if (ei.getCheckbox().getState())
				flags |= FLAG_CENTER_OFF;
			if (hasCenterOff())
				momentary = false;
			setPoints();
		} else
			super.setEditValue(n, ei);
	}

	private boolean hasCenterOff() {
		return (flags & FLAG_CENTER_OFF) != 0;
	}

	public int getShortcut() {
		return 'S';
	}
}
