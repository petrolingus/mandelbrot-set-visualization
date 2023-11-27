package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    private static final int MAX_NUMBER_OF_MEASURES = 33;

    final RestTemplate restTemplate;

    final UiController uiController;

    @Value("${processServiceUrl}")
    private String processServiceUrl;

    public TestController(RestTemplate restTemplate, UiController uiController) {
        this.restTemplate = restTemplate;
        this.uiController = uiController;
    }

    @PostMapping("/api/v1/performance-test")
    public void performanceTest() {



    }

    @PostMapping("/api/v1/launch-test")
    public void launchTest() throws IOException, InterruptedException {

        LOGGER.info("Start testing...");

        UUID uuid = UUID.randomUUID();

        int size = 1024;
        double xc = -1.0;
        double yc = 0.0;
        double scale = 2.0;
        int iterations = 512;

        List<Double> meanList = new ArrayList<>();

        // Warmup
        for (int i = 0; i < 10; i++) {
            uiController.getMandelbrotImage(size, xc, yc, scale, iterations, 5);
        }

        // Subdivision can not more than log(size) / log(2)
        for (int subdivision = 0; subdivision < 7; subdivision++) {

            List<Long> measures = new ArrayList<>();
            for (int i = 0; i < MAX_NUMBER_OF_MEASURES; i++) {
                long start = System.currentTimeMillis();
                uiController.getMandelbrotImage(size, xc, yc, scale, iterations, subdivision);
                long stop = System.currentTimeMillis();
                measures.add((stop - start));
            }

            double avg = measures.stream().mapToDouble(Long::doubleValue).average().orElse(-1);
            double mean = measures.stream().sorted().toList().get(17);
            meanList.add(mean);

//            LOGGER.info("Test {}# subdivisions: {}, avg: {}ms, mean: {}ms", uuid, subdivision, avg, mean);
        }
        LOGGER.info("Result of {} test: {}", uuid, Arrays.toString(meanList.toArray()));
    }

}
