package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    final RestTemplate restTemplate;

    final UiController uiController;

    public TestController(RestTemplate restTemplate, UiController uiController) {
        this.restTemplate = restTemplate;
        this.uiController = uiController;
    }

    @PostMapping("/api/v1/performance-test")
    public String performanceTest(@RequestParam(defaultValue = "128") int size,
                                  @RequestParam(defaultValue = "-1") double xc,
                                  @RequestParam(defaultValue = "0") double yc,
                                  @RequestParam(defaultValue = "2") double scale,
                                  @RequestParam(defaultValue = "64") int iterations,
                                  @RequestParam(defaultValue = "6") int subdivision,
                                  @RequestParam(defaultValue = "1") int executors,
                                  @RequestParam(defaultValue = "10") int n
    ) throws IOException, InterruptedException {

        log.info("Start performance test...");

        List<Long> measures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long start = System.currentTimeMillis();
            byte[] mandelbrotImage = uiController.getMandelbrotImage(size, xc, yc, scale, iterations, subdivision, executors);
            long stop = System.currentTimeMillis();
            long took = stop - start;
            log.info("Experiment #{} took: {}ms", i, took);
            measures.add(took);
            log.debug("blackhole: {}", mandelbrotImage.length);
            Thread.sleep(30000);
            uiController.warmup(512);
        }

        double average = measures.stream().mapToDouble(Long::doubleValue).average().orElse(-1);
        double median = measures.stream().sorted().toList().get(n / 2);

        String result = String.format("Result of test: avg=%f ms, mean=%f ms", average, median);
        log.info(result);
        return result;
    }

}
