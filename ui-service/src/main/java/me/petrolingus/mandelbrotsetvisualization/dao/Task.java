package me.petrolingus.mandelbrotsetvisualization.dao;

import java.util.Objects;
import java.util.UUID;

public record Task(UUID uuid, Params params, Tile tile) implements Comparable<Task> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task that = (Task) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public int compareTo(Task o) {
        return uuid.compareTo(o.uuid);
    }
}
