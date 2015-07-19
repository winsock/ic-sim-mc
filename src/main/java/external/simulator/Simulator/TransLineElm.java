package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class TransLineElm extends CircuitElm {
	double curCount1;
	double curCount2;
	private double delay;
	private double imped;
	private double[] voltageL;
	private double[] voltageR;
	private int lenSteps;
	private int ptr;
	private int width;
	private Point[] posts;
	private Point[] inner;
	private int voltSource1;
	private int voltSource2;
	private double current1;
	private double current2;

	public TransLineElm(int xx, int yy) {
		super(xx, yy);
		delay = 1000 * sim.timeStep;
		imped = 75;
		noDiagonal = true;
		reset();
	}

	public TransLineElm(int xa, int ya, int xb, int yb, int f,
	                    StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		delay = new Double(st.nextToken());
		imped = new Double(st.nextToken());
		width = new Integer(st.nextToken());
		// next slot is for resistance (losses), which is not implemented
		st.nextToken();
		noDiagonal = true;
		reset();
	}

	int getDumpType() {
		return 171;
	}

	int getPostCount() {
		return 4;
	}

	int getInternalNodeCount() {
		return 2;
	}

	String dump() {
		return super.dump() + " " + delay + " " + imped + " " + width + " " + 0.;
	}

	void drag(int xx, int yy) {
		xx = sim.snapGrid(xx);
		yy = sim.snapGrid(yy);
		int w1 = max(sim.gridSize, abs(yy - y));
		int w2 = max(sim.gridSize, abs(xx - x));
		if (w1 > w2) {
			xx = x;
			width = w2;
		} else {
			yy = y;
			width = w1;
		}
		x2 = xx;
		y2 = yy;
		setPoints();
	}

	void reset() {
		if (sim.timeStep == 0)
			return;
		lenSteps = (int) (delay / sim.timeStep);
		System.out.println(lenSteps + " steps");
		if (lenSteps > 100000)
			voltageL = voltageR = null;
		else {
			voltageL = new double[lenSteps];
			voltageR = new double[lenSteps];
		}
		ptr = 0;
		super.reset();
	}

	void setPoints() {
		super.setPoints();
		int ds = (dy == 0) ? sign(dx) : -sign(dy);
		Point p3 = interpPoint(point1, point2, 0, -width * ds);
		Point p4 = interpPoint(point1, point2, 1, -width * ds);
		int sep = sim.gridSize / 2;
		Point p5 = interpPoint(point1, point2, 0, -(width / 2 - sep) * ds);
		Point p6 = interpPoint(point1, point2, 1, -(width / 2 - sep) * ds);
		Point p7 = interpPoint(point1, point2, 0, -(width / 2 + sep) * ds);
		Point p8 = interpPoint(point1, point2, 1, -(width / 2 + sep) * ds);

		// we number the posts like this because we want the lower-numbered
		// points to be on the bottom, so that if some of them are unconnected
		// (which is often true) then the bottom ones will get automatically
		// attached to ground.
		posts = new Point[] { p3, p4, point1, point2 };
		inner = new Point[] { p7, p8, p5, p6 };
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		setBbox(posts[0], posts[3], 0);
		int segments = (int) (dn / 2);
		int ix0 = ptr - 1 + lenSteps;
		double segf = 1. / segments;
		int i;
		Gui.drawRect(inner[2].getX(), inner[2].getY(),
			inner[1].getX() - inner[2].getX() + 2, inner[1].getY() - inner[2].getY() + 2, Color.darkGray.getRGB());
		for (i = 0; i != 4; i++) {
			drawThickLine(g, posts[i], inner[i], getVoltageColor(volts[i]));
		}

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		if (voltageL != null) {
			for (i = 0; i != segments; i++) {
				int ix1 = (ix0 - lenSteps * i / segments) % lenSteps;
				int ix2 = (ix0 - lenSteps * (segments - 1 - i) / segments) % lenSteps;
				double v = (voltageL[ix1] + voltageR[ix2]) / 2;
				Color color = getVoltageColor(v);
				interpPoint(inner[0], inner[1], ps1, i * segf);
				interpPoint(inner[2], inner[3], ps2, i * segf);
				GlStateManager.enableBlend();
				GlStateManager.disableTexture2D();
				GlStateManager.disableCull();
				GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
				GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
				renderer.startDrawing(GL11.GL_LINE);
				renderer.addVertex(g.getGuiLeft() + ps1.getX(), g.getGuiTop() + ps1.getY(), g.getZLevel());
				renderer.addVertex(g.getGuiLeft() + ps2.getX(), g.getGuiTop() + ps2.getY(), g.getZLevel());
				tessellator.draw();
				GlStateManager.enableTexture2D();
				GlStateManager.enableCull();
				GlStateManager.disableBlend();
				interpPoint(inner[2], inner[3], ps1, (i + 1) * segf);
				drawThickLine(g, ps1, ps2, color);
			}
		}
		drawThickLine(g, inner[0], inner[1], getVoltageColor(volts[0]));
		drawPosts(g, lightGrayColor);

		curCount1 = updateDotCount(-current1, curCount1);
		curCount2 = updateDotCount(current2, curCount2);
		if (sim.dragElm != this) {
			drawDots(g, posts[0], inner[0], curCount1);
			drawDots(g, posts[2], inner[2], -curCount1);
			drawDots(g, posts[1], inner[1], -curCount2);
			drawDots(g, posts[3], inner[3], curCount2);
		}
	}

	void setVoltageSource(int n, int v) {
		if (n == 0)
			voltSource1 = v;
		else
			voltSource2 = v;
	}

	void setCurrent(int v, double c) {
		if (v == voltSource1)
			current1 = c;
		else
			current2 = c;
	}

	void stamp() {
		sim.stampVoltageSource(nodes[4], nodes[0], voltSource1);
		sim.stampVoltageSource(nodes[5], nodes[1], voltSource2);
		sim.stampResistor(nodes[2], nodes[4], imped);
		sim.stampResistor(nodes[3], nodes[5], imped);
	}

	void startIteration() {
		// calculate voltages, currents sent over wire
		if (voltageL == null) {
			sim.stop("Transmission line delay too large!", this);
			return;
		}
		voltageL[ptr] = volts[2] - volts[0] + volts[2] - volts[4];
		voltageR[ptr] = volts[3] - volts[1] + volts[3] - volts[5];
		//System.out.println(volts[2] + " " + volts[0] + " " + (volts[2]-volts[0]) + " " + (imped*current1) + " " + voltageL[ptr]);
	/*System.out.println("sending fwd  " + currentL[ptr] + " " + current1);
	  System.out.println("sending back " + currentR[ptr] + " " + current2);*/
		//System.out.println("sending back " + voltageR[ptr]);
		ptr = (ptr + 1) % lenSteps;
	}

	void doStep() {
		if (voltageL == null) {
			sim.stop("Transmission line delay too large!", this);
			return;
		}
		sim.updateVoltageSource(nodes[4], nodes[0], voltSource1, -voltageR[ptr]);
		sim.updateVoltageSource(nodes[5], nodes[1], voltSource2, -voltageL[ptr]);
		if (Math.abs(volts[0]) > 1e-5 || Math.abs(volts[1]) > 1e-5) {
			sim.stop("Need to ground transmission line!", this);
		}
	}

	Point getPost(int n) {
		return posts[n];
	}

	//double getVoltageDiff() { return volts[0]; }
	int getVoltageSourceCount() {
		return 2;
	}

	boolean hasGroundConnection(int n1) {
		return false;
	}

	boolean getConnection(int n1, int n2) {
		return false;
    /*if (comparePair(n1, n2, 0, 1))
      return true;
	  if (comparePair(n1, n2, 2, 3))
	  return true;
	  return false;*/
	}

	public void getInfo(String arr[]) {
		arr[0] = "transmission line";
		arr[1] = getUnitText(imped, CirSim.ohmString);
		arr[2] = "length = " + getUnitText(2.9979e8 * delay, "m");
		arr[3] = "delay = " + getUnitText(delay, "s");
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Delay (s)", delay, 0, 0);
		if (n == 1)
			return new EditInfo("Impedance (ohms)", imped, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0) {
			delay = ei.value;
			reset();
		}
		if (n == 1) {
			imped = ei.value;
			reset();
		}
	}

	@Override
	boolean isWire() {
		return false;
	}
}

