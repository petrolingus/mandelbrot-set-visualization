package me.petrolingus.mandelbrotsetvisualization.uiservice;

import me.petrolingus.mandelbrotsetvisualization.dao.CompletedTask;
import me.petrolingus.mandelbrotsetvisualization.dao.Task;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    private final Queue<Task> schedule = new ConcurrentLinkedQueue<>();
    private final Queue<Task> progress = new ConcurrentLinkedQueue<>();
    private final Queue<TileInfo> done = new ConcurrentLinkedQueue<>();

    AtomicInteger processed = new AtomicInteger();

    private BufferedImage bufferedImage;

    @GetMapping("tasks")
    public @ResponseBody Task getTask() {

        // 1. Get SCHEDULED task
        // 2. Get IN_PROGRESS task

        return schedule.poll();
    }

    @PostMapping("tasks")
    public ResponseEntity<String> completeTask(@RequestBody CompletedTask task) {
        bufferedImage.setRGB(task.x(), task.y(), task.tileSize(), task.tileSize(), task.data(), 0, task.tileSize());
        processed.decrementAndGet();
        return ResponseEntity.ok().build();
    }

    public void add(Task task) {
        processed.incrementAndGet();
        schedule.add(task);
    }

    public void schedule(int size) {
        bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    }

    public BufferedImage getResult() {

        while (processed.get() > 0) {
            Thread.onSpinWait();
        }

        return bufferedImage;
    }
}
