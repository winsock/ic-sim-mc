package external.simulator.parts;

import external.simulator.CirSim;
import external.simulator.CircuitElm;
import external.simulator.EditInfo;
import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class LampElm extends CircuitElm {
	private final double roomTemp = 300;
	private final int filament_len = 24;
	private double resistance;
	private double temp;
	private double nom_pow;
	private double nom_v;
	private double warmTime;
	private double coolTime;
	private Point[] bulbLead;
	private Point[] filament;
	private Point bulb;
	private int bulbR;

	public LampElm(int xx, int yy) {
		super(xx, yy);
		temp = roomTemp;
		nom_pow = 100;
		nom_v = 120;
		warmTime = .4;
		coolTime = .4;
	}

	public LampElm(int xa, int ya, int xb, int yb, int f,
	               StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		temp = new Double(st.nextToken());
		nom_pow = new Double(st.nextToken());
		nom_v = new Double(st.nextToken());
		warmTime = new Double(st.nextToken());
		coolTime = new Double(st.nextToken());
	}

	protected String dump() {
		return super.dump() + " " + temp + " " + nom_pow + " " + nom_v +
			" " + warmTime + " " + coolTime;
	}

	public int getDumpType() {
		return 181;
	}

	public void reset() {
		super.reset();
		temp = roomTemp;
	}

	protected void setPoints() {
		super.setPoints();
		int llen = 16;
		calcLeads(llen);
		bulbLead = newPointArray(2);
		filament = newPointArray(2);
		bulbR = 20;
		filament[0] = interpPoint(lead1, lead2, 0, filament_len);
		filament[1] = interpPoint(lead1, lead2, 1, filament_len);
		double br = filament_len - Math.sqrt(bulbR * bulbR - llen * llen);
		bulbLead[0] = interpPoint(lead1, lead2, 0, br);
		bulbLead[1] = interpPoint(lead1, lead2, 1, br);
		bulb = interpPoint(filament[0], filament[1], .5);
	}

	private Color getTempColor() {
		if (temp < 1200) {
			int x = (int) (255 * (temp - 800) / 400);
			if (x < 0)
				x = 0;
			return new Color(x, 0, 0);
		}
		if (temp < 1700) {
			int x = (int) (255 * (temp - 1200) / 500);
			if (x < 0)
				x = 0;
			return new Color(255, x, 0);
		}
		if (temp < 2400) {
			int x = (int) (255 * (temp - 1700) / 700);
			if (x < 0)
				x = 0;
			return new Color(255, 255, x);
		}
		return Color.WHITE;
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		double v1 = volts[0];
		double v2 = volts[1];
		setBbox(point1, point2, 4);
		adjustBbox(bulb.getX() - bulbR, bulb.getY() - bulbR,
			bulb.getX() + bulbR, bulb.getY() + bulbR);
		// adjustbbox
		draw2Leads(g, lightGrayColor);

		int a;
		double m = Math.PI / 180;
		double r = bulbR;

		Color color = getTempColor();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		renderer.startDrawing(GL11.GL_TRIANGLE_FAN);
		renderer.addVertex(g.getGuiLeft() + bulb.getX(), g.getGuiTop() + bulb.getY(), g.getZLevel()); // Add center
		double ax = Math.cos(m) * r + bulb.getX();
		double ay = Math.sin(m) * r + bulb.getY();
		renderer.addVertex(g.getGuiLeft() + ax, g.getGuiTop() + ay, g.getZLevel()); // Add the first outer vertex to start the triangle fan

		for (a = 20; a != 360; a += 20) {
			double bx = Math.cos(a * m) * r + bulb.getX();
			double by = Math.sin(a + 20 * m) * r + bulb.getY();
			renderer.addVertex(g.getGuiLeft() + bx, g.getGuiTop() + by, g.getZLevel());
		}
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();

		drawThickCircle(g, bulb.getX(), bulb.getY(), bulbR, whiteColor);
		drawThickLine(g, lead1, filament[0], getVoltageColor(v1));
		drawThickLine(g, lead2, filament[1], getVoltageColor(v2));
		drawThickLine(g, filament[0], filament[1], getVoltageColor((v1 + v2) * .5));
		updateDotCount();
		if (sim.getDragElm() != this) {
			drawDots(g, point1, lead1, curcount);
			double cc = curcount + (dn - 16) / 2;
			drawDots(g, lead1, filament[0], cc);
			cc += filament_len;
			drawDots(g, filament[0], filament[1], cc);
			cc += 16;
			drawDots(g, filament[1], lead2, cc);
			drawDots(g, lead2, point2, curcount);
		}
		drawPosts(g, lightGrayColor);
	}

	protected void calculateCurrent() {
		current = (volts[0] - volts[1]) / resistance;
		//System.out.print(this + " res current set to " + current + "\n");
	}

	public void stamp() {
		sim.stampNonLinear(nodes[0]);
		sim.stampNonLinear(nodes[1]);
	}

	protected boolean nonLinear() {
		return true;
	}

	protected void startIteration() {
		// based on http://www.intusoft.com/nlpdf/nl11.pdf
		double nom_r = nom_v * nom_v / nom_pow;
		// this formula doesn't work for values over 5390
		double tp = (temp > 5390) ? 5390 : temp;
		resistance = nom_r * (1.26104 -
			4.90662 * Math.sqrt(17.1839 / tp - 0.00318794) -
			7.8569 / (tp - 187.56));
		double cap = 1.57e-4 * nom_pow;
		double capw = cap * warmTime / .4;
		double capc = cap * coolTime / .4;
		//System.out.println(nom_r + " " + (resistance/nom_r));
		temp += getPower() * sim.getTimeStep() / capw;
		double cr = 2600 / nom_pow;
		temp -= sim.getTimeStep() * (temp - roomTemp) / (capc * cr);
		//System.out.println(capw + " " + capc + " " + temp + " " +resistance);
	}

	public void doStep() {
		sim.stampResistor(nodes[0], nodes[1], resistance);
	}

	public void getInfo(String arr[]) {
		arr[0] = "lamp";
		getBasicInfo(arr);
		arr[3] = "R = " + getUnitText(resistance, CirSim.ohmString);
		arr[4] = "P = " + getUnitText(getPower(), "W");
		arr[5] = "T = " + ((int) temp) + " K";
	}

	public EditInfo getEditInfo(int n) {
		// ohmString doesn't work here on linux
		if (n == 0)
			return new EditInfo("Nominal Power", nom_pow, 0, 0);
		if (n == 1)
			return new EditInfo("Nominal Voltage", nom_v, 0, 0);
		if (n == 2)
			return new EditInfo("Warmup Time (s)", warmTime, 0, 0);
		if (n == 3)
			return new EditInfo("Cooldown Time (s)", coolTime, 0, 0);
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0 && ei.getValue() > 0)
			nom_pow = ei.getValue();
		if (n == 1 && ei.getValue() > 0)
			nom_v = ei.getValue();
		if (n == 2 && ei.getValue() > 0)
			warmTime = ei.getValue();
		if (n == 3 && ei.getValue() > 0)
			coolTime = ei.getValue();
	}

	@Override
	protected boolean isWire() {
		return false;
	}
}
