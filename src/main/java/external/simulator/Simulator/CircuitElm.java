package external.simulator.Simulator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import me.querol.andrew.ic.Gui.CircuitGUI;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.Point;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.Rectangle;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class CircuitElm {
    static final double pi = 3.14159265358979323846;
    public static NumberFormat showFormat;
    static double voltageRange = 5;
    static double currentMult, powerMult;
    static CirSim sim;
    static ReadableColor whiteColor, selectColor, lightGrayColor;
    private static NumberFormat shortFormat;
    private static NumberFormat noCommaFormat;
    private static int colorScaleCount = 32;
    private static ReadableColor[] colorScale;
    private static Point ps1;
    private static Point ps2;
    int x, y, x2, y2, flags, nodes[], voltSource;
    int dx, dy, dsign;
    double dn;
    Point point1, point2, lead1, lead2;
    double volts[];
    double current, curcount;
    Rectangle boundingBox;
    boolean noDiagonal;
    private boolean selected;
    private double dpx1;
    private double dpy1;

    CircuitElm(int xx, int yy) {
        x = x2 = xx;
        y = y2 = yy;
        flags = getDefaultFlags();
        allocNodes();
        initBoundingBox();
    }

    CircuitElm(int xa, int ya, int xb, int yb, int f) {
        x = xa;
        y = ya;
        x2 = xb;
        y2 = yb;
        flags = f;
        allocNodes();
        initBoundingBox();
    }

    static void initClass(CirSim s) {
        sim = s;
        colorScale = new Color[colorScaleCount];
        int i;
        for (i = 0; i != colorScaleCount; i++) {
            double v = i * 2. / colorScaleCount - 1;
            if (v < 0) {
                int n1 = (int) (128 * -v) + 127;
                int n2 = (int) (127 * (1 + v));
                colorScale[i] = new Color(n1, n2, n2);
            } else {
                int n1 = (int) (128 * v) + 127;
                int n2 = (int) (127 * (1 - v));
                colorScale[i] = new Color(n2, n1, n2);
            }
        }

        ps1 = new Point();
        ps2 = new Point();

        showFormat = DecimalFormat.getInstance();
        showFormat.setMaximumFractionDigits(2);
        shortFormat = DecimalFormat.getInstance();
        shortFormat.setMaximumFractionDigits(1);
        noCommaFormat = DecimalFormat.getInstance();
        noCommaFormat.setMaximumFractionDigits(10);
        noCommaFormat.setGroupingUsed(false);
    }

    private static void drawThickLine(CircuitGUI g, int x, int y, int x2, int y2, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.startDrawingQuads();
        worldrenderer.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        worldrenderer.addVertex(x, y, 0);
        worldrenderer.addVertex(x2, y2, 0);
        worldrenderer.addVertex(x2 + 1, y2 + 1, 0);
        worldrenderer.addVertex(x + 1, y + 1, 0);
        worldrenderer.finishDrawing();
    }

    static void drawThickLine(CircuitGUI g, Point pa, Point pb, Color color) {
        drawThickLine(g, pa.getX(), pa.getY(), pb.getX(), pb.getY(), color);
    }

    static void drawThickPolygon(CircuitGUI g, int xs[], int ys[], int c, Color color) {
        int i;
        for (i = 0; i != c - 1; i++)
            drawThickLine(g, xs[i], ys[i], xs[i + 1], ys[i + 1], color);
        drawThickLine(g, xs[i], ys[i], xs[0], ys[0], color);
    }

    static void drawThickPolygon(CircuitGUI g, Polygon p, Color color) {
        drawThickPolygon(g, p.xpoints, p.ypoints, p.npoints, color);
    }

    static void drawThickCircle(CircuitGUI g, int cx, int cy, int ri, Color color) {
        int a;
        double m = pi / 180;
        double r = ri * .98;
        for (a = 0; a != 360; a += 20) {
            double ax = Math.cos(a * m) * r + cx;
            double ay = Math.sin(a * m) * r + cy;
            double bx = Math.cos((a + 20) * m) * r + cx;
            double by = Math.sin((a + 20) * m) * r + cy;
            drawThickLine(g, (int) ax, (int) ay, (int) bx, (int) by, color);
        }
    }

    static String getVoltageDText(double v) {
        return getUnitText(Math.abs(v), "V");
    }

    static String getVoltageText(double v) {
        return getUnitText(v, "V");
    }

    static String getUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-14)
            return "0 " + u;
        if (va < 1e-9)
            return showFormat.format(v * 1e12) + " p" + u;
        if (va < 1e-6)
            return showFormat.format(v * 1e9) + " n" + u;
        if (va < 1e-3)
            return showFormat.format(v * 1e6) + " " + CirSim.muString + u;
        if (va < 1)
            return showFormat.format(v * 1e3) + " m" + u;
        if (va < 1e3)
            return showFormat.format(v) + " " + u;
        if (va < 1e6)
            return showFormat.format(v * 1e-3) + " k" + u;
        if (va < 1e9)
            return showFormat.format(v * 1e-6) + " M" + u;
        return showFormat.format(v * 1e-9) + " G" + u;
    }

    static String getShortUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-13)
            return null;
        if (va < 1e-9)
            return shortFormat.format(v * 1e12) + "p" + u;
        if (va < 1e-6)
            return shortFormat.format(v * 1e9) + "n" + u;
        if (va < 1e-3)
            return shortFormat.format(v * 1e6) + CirSim.muString + u;
        if (va < 1)
            return shortFormat.format(v * 1e3) + "m" + u;
        if (va < 1e3)
            return shortFormat.format(v) + u;
        if (va < 1e6)
            return shortFormat.format(v * 1e-3) + "k" + u;
        if (va < 1e9)
            return shortFormat.format(v * 1e-6) + "M" + u;
        return shortFormat.format(v * 1e-9) + "G" + u;
    }

    static String getCurrentText(double i) {
        return getUnitText(i, "A");
    }

    static String getCurrentDText(double i) {
        return getUnitText(Math.abs(i), "A");
    }

    static int abs(int x) {
        return x < 0 ? -x : x;
    }

    static int sign(int x) {
        return (x < 0) ? -1 : (x == 0) ? 0 : 1;
    }

    static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    static double distance(Point p1, Point p2) {
        double x = p1.getX() - p2.getX();
        double y = p1.getY() - p2.getY();
        return Math.sqrt(x * x + y * y);
    }

    int getDumpType() {
        return 0;
    }

    Class getDumpClass() {
        return getClass();
    }

    int getDefaultFlags() {
        return 0;
    }

    void initBoundingBox() {
        boundingBox = new Rectangle();
        boundingBox.setBounds(min(x, x2), min(y, y2),
                abs(x2 - x) + 1, abs(y2 - y) + 1);
    }

    void allocNodes() {
        nodes = new int[getPostCount() + getInternalNodeCount()];
        volts = new double[getPostCount() + getInternalNodeCount()];
    }

    String dump() {
        int t = getDumpType();
        return (t < 127 ? ((char) t) + " " : t + " ") + x + " " + y + " " +
                x2 + " " + y2 + " " + flags;
    }

    void reset() {
        int i;
        for (i = 0; i != getPostCount() + getInternalNodeCount(); i++)
            volts[i] = 0;
        curcount = 0;
    }

    abstract void draw(CircuitGUI screen, int mouseX, int mouseY, float partialTicks);

    void setCurrent(int x, double c) {
        current = c;
    }

    double getCurrent() {
        return current;
    }

    void doStep() {
    }

    void delete() {
    }

    void startIteration() {
    }

    double getPostVoltage(int x) {
        return volts[x];
    }

    void setNodeVoltage(int n, double c) {
        volts[n] = c;
        calculateCurrent();
    }

    void calculateCurrent() {
    }

    void setPoints() {
        dx = x2 - x;
        dy = y2 - y;
        dn = Math.sqrt(dx * dx + dy * dy);
        dpx1 = dy / dn;
        dpy1 = -dx / dn;
        dsign = (dy == 0) ? sign(dx) : sign(dy);
        point1 = new Point(x, y);
        point2 = new Point(x2, y2);
    }

    void calcLeads(int len) {
        if (dn < len || len == 0) {
            lead1 = point1;
            lead2 = point2;
            return;
        }
        lead1 = interpPoint(point1, point2, (dn - len) / (2 * dn));
        lead2 = interpPoint(point1, point2, (dn + len) / (2 * dn));
    }

    Point interpPoint(Point a, Point b, double f) {
        Point p = new Point();
        interpPoint(a, b, p, f);
        return p;
    }

    void interpPoint(Point a, Point b, Point c, double f) {
        int xpd = b.getX() - a.getX();
        int ypd = b.getY() - a.getY();
    /*double q = (a.x*(1-f)+b.x*f+.48);
      System.out.println(q + " " + (int) q);*/
        c.setX((int) Math.floor(a.getX() * (1 - f) + b.getX() * f + .48));
        c.setY((int) Math.floor(a.getY() * (1 - f) + b.getY() * f + .48));
    }

    void interpPoint(Point a, Point b, Point c, double f, double g) {
        int xpd = b.getX() - a.getX();
        int ypd = b.getY() - a.getY();
        int gx = b.getY() - a.getY();
        int gy = a.getX() - b.getX();
        g /= Math.sqrt(gx * gx + gy * gy);
        c.setX((int) Math.floor(a.getX() * (1 - f) + b.getX() * f + g * gx + .48));
        c.setY((int) Math.floor(a.getY() * (1 - f) + b.getY() * f + g * gy + .48));
    }

    Point interpPoint(Point a, Point b, double f, double g) {
        Point p = new Point();
        interpPoint(a, b, p, f, g);
        return p;
    }

    void interpPoint2(Point a, Point b, Point c, Point d, double f, double g) {
        int xpd = b.getX() - a.getX();
        int ypd = b.getY() - a.getY();
        int gx = b.getY() - a.getY();
        int gy = a.getX() - b.getX();
        g /= Math.sqrt(gx * gx + gy * gy);
        c.setX((int) Math.floor(a.getX() * (1 - f) + b.getX() * f + g * gx + .48));
        c.setY((int) Math.floor(a.getY() * (1 - f) + b.getY() * f + g * gy + .48));
        d.setX((int) Math.floor(a.getX() * (1 - f) + b.getX() * f - g * gx + .48));
        d.setY((int) Math.floor(a.getY() * (1 - f) + b.getY() * f - g * gy + .48));
    }

    void draw2Leads(CircuitGUI g, Color color) {
        // draw first lead
        getVoltageColor(volts[0]);
        drawThickLine(g, point1, lead1, color);

        // draw second lead
        getVoltageColor(volts[1]);
        drawThickLine(g, lead2, point2, color);
    }

    Point[] newPointArray(int n) {
        Point a[] = new Point[n];
        while (n > 0)
            a[--n] = new Point();
        return a;
    }

    void drawDots(CircuitGUI g, Point pa, Point pb, double pos) {
        drawDots(g, pa, pb, pos, (Color) Color.YELLOW);
    }

    void drawDots(CircuitGUI g, Point pa, Point pb, double pos, Color color) {
        if (sim.stopped || pos == 0 || !sim.dots)
            return;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.startDrawing(GL11.GL_POINTS);
        renderer.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        int dx = pb.getX() - pa.getX();
        int dy = pb.getY() - pa.getY();
        double dn = Math.sqrt(dx * dx + dy * dy);
        int ds = 16;
        pos %= ds;
        if (pos < 0)
            pos += ds;
        double di = 0;
        for (di = pos; di < dn; di += ds) {
            int x0 = (int) (pa.getX() + di * dx / dn);
            int y0 = (int) (pa.getY() + di * dy / dn);
            renderer.addVertex(x0, y0, 0);
        }
        renderer.finishDrawing();
    }

    Polygon calcArrow(Point a, Point b, double al, double aw) {
        Polygon poly = new Polygon();
        Point p1 = new Point();
        Point p2 = new Point();
        int adx = b.getX() - a.getX();
        int ady = b.getY() - a.getY();
        double l = Math.sqrt(adx * adx + ady * ady);
        poly.addPoint(b.getX(), b.getY());
        interpPoint2(a, b, p1, p2, 1 - al / l, aw);
        poly.addPoint(p1.getX(), p1.getY());
        poly.addPoint(p2.getX(), p2.getY());
        return poly;
    }

    Polygon createPolygon(Point a, Point b, Point c) {
        Polygon p = new Polygon();
        p.addPoint(a.getX(), a.getY());
        p.addPoint(b.getX(), b.getY());
        p.addPoint(c.getX(), c.getY());
        return p;
    }

    Polygon createPolygon(Point a, Point b, Point c, Point d) {
        Polygon p = new Polygon();
        p.addPoint(a.getX(), a.getY());
        p.addPoint(b.getX(), b.getY());
        p.addPoint(c.getX(), c.getY());
        p.addPoint(d.getX(), d.getY());
        return p;
    }

    Polygon createPolygon(Point a[]) {
        Polygon p = new Polygon();
        int i;
        for (i = 0; i != a.length; i++)
            p.addPoint(a[i].getX(), a[i].getY());
        return p;
    }

    void drag(int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        if (noDiagonal) {
            if (Math.abs(x - xx) < Math.abs(y - yy)) {
                xx = x;
            } else {
                yy = y;
            }
        }
        x2 = xx;
        y2 = yy;
        setPoints();
    }

    void move(int dx, int dy) {
        x += dx;
        y += dy;
        x2 += dx;
        y2 += dy;
        boundingBox.setLocation(dx, dy);
        setPoints();
    }

    // determine if moving this element by (dx,dy) will put it on top of another element
    boolean allowMove(int dx, int dy) {
        int nx = x + dx;
        int ny = y + dy;
        int nx2 = x2 + dx;
        int ny2 = y2 + dy;
        int i;
        for (i = 0; i != sim.elmList.size(); i++) {
            CircuitElm ce = sim.getElm(i);
            if (ce.x == nx && ce.y == ny && ce.x2 == nx2 && ce.y2 == ny2)
                return false;
            if (ce.x == nx2 && ce.y == ny2 && ce.x2 == nx && ce.y2 == ny)
                return false;
        }
        return true;
    }

    void movePoint(int n, int dx, int dy) {
        if (n == 0) {
            x += dx;
            y += dy;
        } else {
            x2 += dx;
            y2 += dy;
        }
        setPoints();
    }

    void drawPosts(CircuitGUI g, Color color) {
        int i;
        for (i = 0; i != getPostCount(); i++) {
            Point p = getPost(i);
            drawPost(g, p.getX(), p.getY(), nodes[i], color);
        }
    }

    void stamp() {
    }

    int getVoltageSourceCount() {
        return 0;
    }

    int getInternalNodeCount() {
        return 0;
    }

    void setNode(int p, int n) {
        nodes[p] = n;
    }

    void setVoltageSource(int n, int v) {
        voltSource = v;
    }

    int getVoltageSource() {
        return voltSource;
    }

    double getVoltageDiff() {
        return volts[0] - volts[1];
    }

    boolean nonLinear() {
        return false;
    }

    int getPostCount() {
        return 2;
    }

    int getNode(int n) {
        return nodes[n];
    }

    Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : null;
    }

    void drawPost(CircuitGUI g, int x0, int y0, int n, Color color) {
        if (sim.dragElm == null && !needsHighlight() &&
                sim.getCircuitNode(n).links.size() == 2)
            return;
        if (sim.mouseMode == CirSim.MODE_DRAG_ROW ||
                sim.mouseMode == CirSim.MODE_DRAG_COLUMN)
            return;
        drawPost(g, x0, y0, color);
    }

    void drawPost(CircuitGUI g, int x0, int y0, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.startDrawing(GL11.GL_LINE_LOOP);
        renderer.setColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        int a;
        double m = pi / 180;
        double r = 2.94;
        for (a = 0; a != 360; a += 20) {
            double ax = Math.cos(a * m) * r + x0;
            double ay = Math.sin(a * m) * r + y0;
            double bx = Math.cos((a + 20) * m) * r + x0;
            double by = Math.sin((a + 20) * m) * r + y0;
            renderer.addVertex(ax, ay, 0);
            renderer.addVertex(bx, by, 0);
        }
        renderer.finishDrawing();
    }

    void setBbox(int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int q = x1;
            x1 = x2;
            x2 = q;
        }
        if (y1 > y2) {
            int q = y1;
            y1 = y2;
            y2 = q;
        }
        boundingBox.setBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    void setBbox(Point p1, Point p2, double w) {
        setBbox(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        int gx = p2.getY() - p1.getY();
        int gy = p1.getX() - p2.getX();
        int dpx = (int) (dpx1 * w);
        int dpy = (int) (dpy1 * w);
        adjustBbox(p1.getX() + dpx, p1.getY() + dpy, p1.getX() - dpx, p1.getY() - dpy);
    }

    void adjustBbox(int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int q = x1;
            x1 = x2;
            x2 = q;
        }
        if (y1 > y2) {
            int q = y1;
            y1 = y2;
            y2 = q;
        }
        x1 = min(boundingBox.getX(), x1);
        y1 = min(boundingBox.getY(), y1);
        x2 = max(boundingBox.getX() + boundingBox.getWidth() - 1, x2);
        y2 = max(boundingBox.getY() + boundingBox.getHeight() - 1, y2);
        boundingBox.setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    void adjustBbox(Point p1, Point p2) {
        adjustBbox(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    boolean isCenteredText() {
        return false;
    }

    void drawCenteredText(CircuitGUI g, String s, int x, int y, boolean cx, Color color) {
        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int w = renderer.getStringWidth(s);
        if (cx)
            x -= w / 2;
        adjustBbox(x, y - renderer.FONT_HEIGHT / 2,
                x + w, y + renderer.FONT_HEIGHT / 2);
        g.drawString(renderer, s, x, y + renderer.FONT_HEIGHT / 2, color.hashCode());
    }

    void drawValues(CircuitGUI g, String s, double hs, Color color) {
        if (s == null)
            return;
        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int w = renderer.getStringWidth(s);
        int ya = renderer.FONT_HEIGHT / 2;
        int xc, yc;
        if (this instanceof RailElm || this instanceof SweepElm) {
            xc = x2;
            yc = y2;
        } else {
            xc = (x2 + x) / 2;
            yc = (y2 + y) / 2;
        }
        int dpx = (int) (dpx1 * hs);
        int dpy = (int) (dpy1 * hs);
        if (dpx == 0) {
            g.drawString(renderer, s, xc - w / 2, yc - abs(dpy) - 2, color.hashCode());
        } else {
            int xx = xc + abs(dpx) + 2;
            if (this instanceof VoltageElm || (x < x2 && y > y2))
                xx = xc - (w + abs(dpx) + 2);
            g.drawString(renderer, s, xx, yc + dpy + ya, color.hashCode());
        }
    }

    void drawCoil(CircuitGUI g, int hs, Point p1, Point p2,
                  double v1, double v2, Color color) {
        double len = distance(p1, p2);
        int segments = 30; // 10*(int) (len/10);
        int i;
        double segf = 1. / segments;

        ps1.setLocation(p1);
        for (i = 0; i != segments; i++) {
            double cx = (((i + 1) * 6. * segf) % 2) - 1;
            double hsx = Math.sqrt(1 - cx * cx);
            if (hsx < 0)
                hsx = -hsx;
            interpPoint(p1, p2, ps2, i * segf, hsx * hs);
            double v = v1 + (v2 - v1) * i / segments;
            getVoltageColor(v);
            drawThickLine(g, ps1, ps2, color);
            ps1.setLocation(ps2);
        }
    }

    void updateDotCount() {
        curcount = updateDotCount(current, curcount);
    }

    double updateDotCount(double cur, double cc) {
        if (sim.stopped)
            return cc;
        double cadd = cur * currentMult;
    /*if (cur != 0 && cadd <= .05 && cadd >= -.05)
      cadd = (cadd < 0) ? -.05 : .05;*/
        cadd %= 8;
    /*if (cadd > 8)
      cadd = 8;
	  if (cadd < -8)
	  cadd = -8;*/
        return cc + cadd;
    }

    void doDots(CircuitGUI g) {
        updateDotCount();
        if (sim.dragElm != this)
            drawDots(g, point1, point2, curcount);
    }

    void doAdjust() {
    }

    void setupAdjust() {
    }

    public void getInfo(String arr[]) {
    }

    int getBasicInfo(String arr[]) {
        arr[1] = "I = " + getCurrentDText(getCurrent());
        arr[2] = "Vd = " + getVoltageDText(getVoltageDiff());
        return 3;
    }

    ReadableColor getVoltageColor(double volts) {
        if (needsHighlight()) {
            return selectColor;
        }
        int c = (int) ((volts + voltageRange) * (colorScaleCount - 1) /
                (voltageRange * 2));
        if (c < 0)
            c = 0;
        if (c >= colorScaleCount)
            c = colorScaleCount - 1;
        return colorScale[c];
    }

    Color getPowerColor(double w0) {
        w0 *= powerMult;
        double w = (w0 < 0) ? -w0 : w0;
        if (w > 1)
            w = 1;
        int rg = 128 + (int) (w * 127);
        int b = (int) (128 * (1 - w));
        if (w0 > 0)
            return new Color(rg, b, b);
        else
            return new Color(b, rg, b);
    }

    Color setConductanceColor(CircuitGUI g, double w0) {
        w0 *= powerMult;
        double w = (w0 < 0) ? -w0 : w0;
        if (w > 1)
            w = 1;
        int rg = (int) (w * 255);
        return new Color(rg, rg, rg);
    }

    double getPower() {
        return getVoltageDiff() * current;
    }

    double getScopeValue(int x) {
        return (x == 1) ? getPower() : getVoltageDiff();
    }

    String getScopeUnits(int x) {
        return (x == 1) ? "W" : "V";
    }

    public EditInfo getEditInfo(int n) {
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
    }

    boolean getConnection(int n1, int n2) {
        return true;
    }

    boolean hasGroundConnection(int n1) {
        return false;
    }

    abstract boolean isWire();

    boolean canViewInScope() {
        return getPostCount() <= 2;
    }

    boolean comparePair(int x1, int x2, int y1, int y2) {
        return ((x1 == y1 && x2 == y2) || (x1 == y2 && x2 == y1));
    }

    boolean needsHighlight() {
        return sim.mouseElm == this || selected;
    }

    boolean isSelected() {
        return selected;
    }

    void setSelected(boolean x) {
        selected = x;
    }

    void selectRect(Rectangle r) {
        selected = r.intersects(boundingBox);
    }

    Rectangle getBoundingBox() {
        return boundingBox;
    }

    boolean needsShortcut() {
        return getShortcut() > 0;
    }

    int getShortcut() {
        return 0;
    }

    boolean isGraphicElmt() {
        return false;
    }
}
