package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class Mandelbrot {

    public double calculatePixel(double x, double y, int maxIterations) {
        double ix = 0;
        double iy = 0;
        int iteration = 0;
        while (ix * ix + iy * iy < 4 && iteration < maxIterations) {
            double xtemp = ix * ix - iy * iy + x;
            iy = 2 * ix * iy + y;
            ix = xtemp;
            iteration++;
        }
        return FastMath.log(iteration) / FastMath.log(maxIterations);
    }

    public int[] getMandelbrotImage(int size, double xCenter, double yCenter, double scale, int maxIterations, float hue, float saturation) {

        double halfScale = scale / 2;
        double factor = scale / size;
        double xStart = xCenter - halfScale;
        double yStart = yCenter - halfScale;

        int[] pixels = new int[size * size];

        for (int i = 0; i < size; i++) {
            double x0 = xStart + factor * i;
            for (int j = 0; j < size; j++) {
                double y0 = yStart + factor * j;

                float brightness = (float) calculatePixel(x0, y0, maxIterations);

                int id = (size - 1 - j) * size + i;
                pixels[id] = Color.HSBtoRGB(hue, saturation, brightness);
            }
        }

        return pixels;
    }

}
