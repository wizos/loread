package me.wizos.loread.utils;

/**
 * Created by Wizos on 2018/7/10.
 */

public class RGB {
    int r;
    int g;
    int b;

    public RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public String toString() {
        return "RGB{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                '}';
    }
}
