package me.petrolingus.mandelbrotsetvisualization.uiservice;

import java.util.Objects;

public final class MandelbrotSetParam {
    private int size;
    private double xc;
    private double yc;
    private double scale;
    private int maxIterations;
    private int subdivision;

    public MandelbrotSetParam() {

    }

    public MandelbrotSetParam(int size, double xc, double yc, double scale, int maxIterations, int subdivision) {
        this.size = size;
        this.xc = xc;
        this.yc = yc;
        this.scale = scale;
        this.maxIterations = maxIterations;
        this.subdivision = subdivision;
    }

    public int getSize() {
        return size;
    }

    public double getXc() {
        return xc;
    }

    public double getYc() {
        return yc;
    }

    public double getScale() {
        return scale;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getSubdivision() {
        return subdivision;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setXc(double xc) {
        this.xc = xc;
    }

    public void setYc(double yc) {
        this.yc = yc;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public void setSubdivision(int subdivision) {
        this.subdivision = subdivision;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MandelbrotSetParam) obj;
        return this.size == that.size &&
                Double.doubleToLongBits(this.xc) == Double.doubleToLongBits(that.xc) &&
                Double.doubleToLongBits(this.yc) == Double.doubleToLongBits(that.yc) &&
                Double.doubleToLongBits(this.scale) == Double.doubleToLongBits(that.scale) &&
                this.subdivision == that.subdivision;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, xc, yc, scale, subdivision);
    }

    @Override
    public String toString() {
        return "?size=" + size +
                "&xc=" + xc +
                "&yc=" + yc +
                "&scale=" + scale +
                "&maxIterations=" + maxIterations +
                "&subdivision=" + subdivision;
    }
}
