package me.petrolingus.mandelbrotsetvisualization.dao;

import java.util.UUID;

public record CompletedTask(UUID uuid, int[] data, int x, int y, int tileSize) {
}