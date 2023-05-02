package me.petrolingus.mandelbrotsetvisualization.uiservice;

import me.petrolingus.mandelbrotsetvisualization.dao.Task;
import me.petrolingus.mandelbrotsetvisualization.dao.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

        // 1. Get SCHEDULED task
        // 2. Get IN_PROGRESS task

        if (numberOfTasks.get() == 0) {
            return null;
        }

        Task task = schedule.poll();

        if (task == null) {
            schedule.addAll(progress);
            task = schedule.poll();
            progress.add(task);
        } else {
            progress.add(task);
        }

        if (schedule.size() != 0 || progress.size() != 0) {
            System.out.println(schedule.size() + ":" + progress.size());
        }


        return task;
    }

    @PostMapping("tasks")
    public ResponseEntity<String> completeTask(@RequestBody Task task) {

        if (!progress.remove(task)) {
            return ResponseEntity.ok().build();
        }

        Tile tile = task.tile();

        bufferedImage.setRGB(tile.x(), tile.y(), tile.size(), tile.size(), tile.data(), 0, tile.size());
        numberOfTasks.decrementAndGet();
        return ResponseEntity.ok().build();
    }

    public void add(Task task) {
        numberOfTasks.incrementAndGet();
        schedule.add(task);
    }

    public void schedule(int size) {
        bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        schedule.clear();
        progress.clear();
    }

    public BufferedImage getResult() {

        while (numberOfTasks.get() > 0) {
            Thread.onSpinWait();
        }

        return bufferedImage;
    }
}
