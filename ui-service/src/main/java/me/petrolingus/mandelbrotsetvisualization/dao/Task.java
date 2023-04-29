package me.petrolingus.mandelbrotsetvisualization.dao;

import java.util.UUID;

public record Task(UUID uuid, int size, double xc, double yc, double scale, int iterations, int x, int y) {
}
