package me.petrolingus.mandelbrotsetvisualization.processservice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    final RestTemplate restTemplate;

    private final float hue;

    private final float saturation;

    public ImageService(RestTemplate restTemplate) {
        this.hue = ThreadLocalRandom.current().nextFloat();
        this.saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void run() {
        int poolSize = 5;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
        while (true) {
            Runnable worker = new WorkerThread(restTemplate, hue, saturation);
            executor.execute(worker);
        }
    }
}
