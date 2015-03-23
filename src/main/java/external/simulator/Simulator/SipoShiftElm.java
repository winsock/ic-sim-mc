package external.simulator.Simulator;

import java.util.StringTokenizer;

// contributed by Edward Calver

class SipoShiftElm extends ChipElm {
    private short data = 0;//This has to be a short because there's no unsigned byte and it's screwing with my code
    private boolean clockstate = false;

    public SipoShiftElm(int xx, int yy) {
        super(xx, yy);
    }

    public SipoShiftElm(int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    boolean hasReset() {
        return false;
    }

    String getChipName() {
        return "SIPO shift register";
    }

    void setupPins() {
        sizeX = 9;
        sizeY = 3;
        pins = new Pin[getPostCount()];

        pins[0] = new Pin(1, SIDE_W, "D");
        pins[1] = new Pin(2, SIDE_W, "");
        pins[1].clock = true;

        pins[2] = new Pin(1, SIDE_N, "I7");
        pins[2].output = true;
        pins[3] = new Pin(2, SIDE_N, "I6");
        pins[3].output = true;
        pins[4] = new Pin(3, SIDE_N, "I5");
        pins[4].output = true;
        pins[5] = new Pin(4, SIDE_N, "I4");
        pins[5].output = true;
        pins[6] = new Pin(5, SIDE_N, "I3");
        pins[6].output = true;
        pins[7] = new Pin(6, SIDE_N, "I2");
        pins[7].output = true;
        pins[8] = new Pin(7, SIDE_N, "I1");
        pins[8].output = true;
        pins[9] = new Pin(8, SIDE_N, "I0");
        pins[9].output = true;

    }

    int getPostCount() {
        return 10;
    }

    int getVoltageSourceCount() {
        return 8;
    }

    void execute() {

        if (pins[1].value && !clockstate) {
            clockstate = true;
            data = (short) (data >>> 1);
            if (pins[0].value) data += 128;

            pins[2].value = (data & 128) > 0;
            pins[3].value = (data & 64) > 0;
            pins[4].value = (data & 32) > 0;
            pins[5].value = (data & 16) > 0;
            pins[6].value = (data & 8) > 0;
            pins[7].value = (data & 4) > 0;
            pins[8].value = (data & 2) > 0;
            pins[9].value = (data & 1) > 0;
        }
        if (!pins[1].value) clockstate = false;
    }

    int getDumpType() {
        return 189;
    }

}
