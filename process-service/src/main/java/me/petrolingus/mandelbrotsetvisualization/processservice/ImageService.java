package me.petrolingus.mandelbrotsetvisualization.processservice;

import jakarta.annotation.PostConstruct;
import me.petrolingus.mandelbrotsetvisualization.dao.CompletedTask;
import me.petrolingus.mandelbrotsetvisualization.dao.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@EnableScheduling
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final Mandelbrot mandelbrot;

    private float hue;

    private float saturation;

    final RestTemplate restTemplate;

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

        String url = "http://localhost:8080/tasks";

        // Check available tasks and get it if exists
        Task task = restTemplate.getForObject(url, Task.class);

        if (task == null) {
            return;
        }

        LOGGER.info(task.toString());

        // Make task
        long start = System.currentTimeMillis();
        int[] mandelbrotImage = mandelbrot.getMandelbrotImage(task.size(), task.xc(), task.yc(), task.scale(), task.iterations(), hue, saturation);
        long stop = System.currentTimeMillis();

        LOGGER.info("Task {} completed in {} ms", task.uuid().toString(), (stop - start));

        // Create the request body by wrapping the object in HttpEntity
        CompletedTask completedTask = new CompletedTask(task.uuid(), mandelbrotImage, task.x(), task.y(), task.size());
        HttpEntity<CompletedTask> request = new HttpEntity<>(completedTask);

        // Send the request body in HttpEntity for HTTP POST request
        String taskCreateResponse = restTemplate.postForObject(url, request, String.class);

//        LOGGER.info(taskCreateResponse);
    }
}
