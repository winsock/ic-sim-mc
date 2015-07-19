package external.simulator.Simulator.parts;


import external.simulator.Simulator.CircuitElm;
import me.querol.andrew.ic.Gui.CircuitGUI;
import org.lwjgl.util.Point;

import java.awt.*;
import java.util.StringTokenizer;

public class JfetElm extends MosfetElm {
    private Polygon gatePoly;
    private Polygon arrowPoly;
    private Point gatePt;

	public JfetElm(int xx, int yy, boolean pnpflag) {
        super(xx, yy, pnpflag);
        noDiagonal = true;
    }

	public JfetElm(int xa, int ya, int xb, int yb, int f,
            StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        noDiagonal = true;
    }

    public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
        setBbox(point1, point2, hs);
        Color colorV1 = getVoltageColor(volts[1]);
        CircuitElm.drawThickLine(g, src[0], src[1], colorV1);
        CircuitElm.drawThickLine(g, src[1], src[2], colorV1);
        Color colorV2 = getVoltageColor(volts[2]);
        CircuitElm.drawThickLine(g, drn[0], drn[1], colorV2);
        CircuitElm.drawThickLine(g, drn[1], drn[2], colorV2);
        Color color = getVoltageColor(volts[0]);
        CircuitElm.drawThickLine(g, point1, gatePt, color);
        CircuitElm.drawPolygon(g, arrowPoly, color);
        CircuitElm.drawPolygon(g, gatePoly, color);
        curcount = updateDotCount(-ids, curcount);
        if (curcount != 0) {
            drawDots(g, src[0], src[1], curcount);
            drawDots(g, src[1], src[2], curcount + 8);
            drawDots(g, drn[0], drn[1], -curcount);
            drawDots(g, drn[1], drn[2], -(curcount + 8));
        }
        drawPosts(g, CircuitElm.lightGrayColor);
    }

    protected void setPoints() {
        super.setPoints();

        // find the coordinates of the various points we need to draw
        // the JFET.
        int hs2 = hs * dsign;
        src = newPointArray(3);
        drn = newPointArray(3);
        interpPoint2(point1, point2, src[0], drn[0], 1, hs2);
        interpPoint2(point1, point2, src[1], drn[1], 1, hs2 / 2);
        interpPoint2(point1, point2, src[2], drn[2], 1 - 10 / dn, hs2 / 2);

        gatePt = interpPoint(point1, point2, 1 - 14 / dn);

        Point ra[] = newPointArray(4);
        interpPoint2(point1, point2, ra[0], ra[1], 1 - 13 / dn, hs);
        interpPoint2(point1, point2, ra[2], ra[3], 1 - 10 / dn, hs);
        gatePoly = createPolygon(ra[0], ra[1], ra[3], ra[2]);
        if (pnp == -1) {
            Point x = interpPoint(gatePt, point1, 18 / dn);
            arrowPoly = calcArrow(gatePt, x, 8, 3);
        } else
            arrowPoly = calcArrow(point1, gatePt, 8, 3);
    }

    public int getDumpType() {
        return 'j';
    }

    // these values are taken from Hayes+Horowitz p155
    protected double getDefaultThreshold() {
        return -4;
    }

    protected double getBeta() {
        return .00125;
    }

    public void getInfo(String arr[]) {
        getFetInfo(arr, "JFET");
    }
}
