package me.petrolingus.mandelbrotsetvisualization.uiservice;

import me.petrolingus.mandelbrotsetvisualization.dao.Task;
import me.petrolingus.mandelbrotsetvisualization.dao.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class PoolController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolController.class);

    private final Queue<Task> schedule = new ConcurrentLinkedQueue<>();
    private final Queue<Task> progress = new ConcurrentLinkedQueue<>();

    AtomicInteger numberOfTasks = new AtomicInteger();

    private BufferedImage bufferedImage;

    @GetMapping("tasks")
    public @ResponseBody Task getTask() {
        Task task = schedule.poll();
        return task;
    }

    @PostMapping("tasks")
    public ResponseEntity<String> completeTask(@RequestBody Task task) {
        Tile tile = task.tile();
        bufferedImage.setRGB(tile.x(), tile.y(), tile.size(), tile.size(), tile.data(), 0, tile.size());
        numberOfTasks.decrementAndGet();
        return ResponseEntity.ok().build();
    }

    public void add(List<Task> tasks) {
        numberOfTasks.set(tasks.size());
        schedule.addAll(tasks);
    }

    public void schedule(int size) {
        long start = System.currentTimeMillis();
        bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        long stop = System.currentTimeMillis();
        LOGGER.info("Scheduling completed in {}ms", (stop - start));
    }

    public BufferedImage getResult() {

        long start = System.currentTimeMillis();
        while (numberOfTasks.get() > 0) {
            Thread.onSpinWait();
        }
        long stop = System.currentTimeMillis();

        LOGGER.info("Image generation took {}ms", (stop - start));

        return bufferedImage;
    }
}
