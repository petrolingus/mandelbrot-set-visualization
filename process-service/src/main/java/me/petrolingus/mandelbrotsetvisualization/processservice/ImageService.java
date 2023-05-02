package me.petrolingus.mandelbrotsetvisualization.processservice;

import jakarta.annotation.PostConstruct;
import me.petrolingus.mandelbrotsetvisualization.dao.Params;
import me.petrolingus.mandelbrotsetvisualization.dao.Task;
import me.petrolingus.mandelbrotsetvisualization.dao.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@EnableScheduling
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final Mandelbrot mandelbrot;

    private float hue;

    private float saturation;

    final RestTemplate restTemplate;

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    public ImageService(Mandelbrot mandelbrot, RestTemplate restTemplate) {
        this.mandelbrot = mandelbrot;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        hue = ThreadLocalRandom.current().nextFloat();
        saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1)
    public void process() {

        for (int i = 0; i < executor.getMaximumPoolSize() - executor.getActiveCount(); i++) {
            executor.submit(() -> {

                String url = "http://localhost:8080/tasks";

                // Check available tasks and get it if exists
                Task task = restTemplate.getForObject(url, Task.class);

                if (task == null) {
                    return;
                }

                LOGGER.info(task.toString());

                // Make task
                long start = System.currentTimeMillis();
                Params params = task.params();
                int[] mandelbrotImage = mandelbrot.getMandelbrotImage(params.size(), params.xc(), params.yc(), params.scale(), params.iterations(), hue, saturation);
                long stop = System.currentTimeMillis();

                LOGGER.info("Task {} completed in {} ms", task.uuid().toString(), (stop - start));

                // Create the request body by wrapping the object in HttpEntity
                Tile tile = task.tile();
                Task result = new Task(task.uuid(), null, new Tile(tile.x(), tile.y(), tile.size(), mandelbrotImage));
                HttpEntity<Task> request = new HttpEntity<>(result);

                // Send the request body in HttpEntity for HTTP POST request
                restTemplate.postForObject(url, request, String.class);
            });
        }
    }

}
