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

        if (numberOfTasks.get() <= 0) {
            return null;
        }

        LOGGER.info("Scheduled: {}, In progress: {}, Number of tasks: {}", schedule.size(), progress.size(), numberOfTasks.get());

        Task task = schedule.poll();

        if (task == null) {
            LOGGER.info("No scheduled tasks. In progress tasks: {}", progress.size());
            schedule.addAll(progress);
            LOGGER.info("Put all in progress tasks to schedule. Scheduled tasks: {}", schedule.size());
            task = schedule.poll();
        }

        if (task == null) {
            LOGGER.info("Take null task!!");
        }

        if (task != null) {
            LOGGER.info("Send task: {}", task.uuid());
            progress.add(task);
        }

        return task;
    }

    @PostMapping("tasks")
    public ResponseEntity<String> completeTask(@RequestBody Task task) {

        LOGGER.info("Completed task: {}", task.uuid());

        if (!progress.removeIf(task1 -> task1.uuid().equals(task.uuid()))) {
            LOGGER.info("progress size: {}, uuid: {}", progress.size(), task.uuid());
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

        long start = System.currentTimeMillis();
        while (numberOfTasks.get() > 0) {
            Thread.onSpinWait();
        }
        long stop = System.currentTimeMillis();

        LOGGER.info("Image generation took {}ms", (stop - start));

        return bufferedImage;
    }
}
