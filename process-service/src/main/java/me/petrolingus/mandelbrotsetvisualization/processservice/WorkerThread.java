package me.petrolingus.mandelbrotsetvisualization.processservice;

import me.petrolingus.mandelbrotsetvisualization.dao.Params;
import me.petrolingus.mandelbrotsetvisualization.dao.Task;
import me.petrolingus.mandelbrotsetvisualization.dao.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

public class WorkerThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final RestTemplate restTemplate;

    private final float hue;

    private final float saturation;

    public WorkerThread(RestTemplate restTemplate, float hue, float saturation) {
        this.restTemplate = restTemplate;
        this.hue = hue;
        this.saturation = saturation;
    }

    @Override
    public void run() {

        String url = "http://localhost:8080/tasks";

        // Check available tasks and get it if exists
        Task task = restTemplate.getForObject(url, Task.class);

        if (task == null) {
            Thread.onSpinWait();
            return;
        }

        LOGGER.info("Received task: {}", task.uuid());

        // Make task
        Params params = task.params();
        Mandelbrot mandelbrot = new Mandelbrot();
        int[] mandelbrotImage = mandelbrot.getMandelbrotImage(params.size(), params.xc(), params.yc(), params.scale(), params.iterations(), hue, saturation);

        // Create the request body by wrapping the object in HttpEntity
        Tile tile = task.tile();
        Task result = new Task(task.uuid(), null, new Tile(tile.x(), tile.y(), tile.size(), mandelbrotImage));
        HttpEntity<Task> request = new HttpEntity<>(result);

        // Send the request body in HttpEntity for HTTP POST request
        restTemplate.postForObject(url, request, String.class);
    }
}
