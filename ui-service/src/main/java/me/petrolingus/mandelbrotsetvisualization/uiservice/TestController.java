package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @Value("#{environment['PROCESS_SERVICE']?:'http://localhost:8181/api/v1/generate-mandelbrot-tile'}")
    private String processServiceUrl;

    final RestTemplate restTemplate;

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/api/v1/performance-test")
    public void performanceTest(@RequestParam(defaultValue = "100") int n) {

        List<Long> measures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long start = System.currentTimeMillis();
            restTemplate.getForObject(processServiceUrl, int[].class);
            long stop = System.currentTimeMillis();
            measures.add((stop - start));
        }

        double average = measures.stream().mapToDouble(Long::doubleValue).average().orElse(-1);
        double median = measures.stream().sorted().toList().get(n / 2);

        LOGGER.info("Result of test: avg={} ms, mean={} ms", average, median);

    }

}
