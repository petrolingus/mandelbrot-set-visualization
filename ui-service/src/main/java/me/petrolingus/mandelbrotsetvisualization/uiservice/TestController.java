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
                                  @RequestParam(defaultValue = "10") int imageCount,
                                  @RequestParam(defaultValue = "512") int warmup,
                                  @RequestParam(defaultValue = "30000") int pauseBetweenImages,
                                  @RequestParam(defaultValue = "30000") int pauseBetweenExperiments,
                                  @RequestParam(defaultValue = "false") boolean breakdownEnable,
                                  @RequestParam(defaultValue = "false") boolean timeoutEnable,
                                  @RequestParam(defaultValue = "false") boolean badGatewayEnable
    ) throws IOException, InterruptedException {

        log.info("Start performance test...");

        log.info("Test with Service");
        uiController.selectedEndpoint = 0;
        lab(size, xc, yc, scale, iterations, subdivision, executors, imageCount, pauseBetweenImages, pauseBetweenExperiments, breakdownEnable, timeoutEnable, badGatewayEnable);

        log.info("Test with Ingress");
        uiController.selectedEndpoint = 1;
        lab(size, xc, yc, scale, iterations, subdivision, executors, imageCount, pauseBetweenImages, pauseBetweenExperiments, breakdownEnable, timeoutEnable, badGatewayEnable);

        log.info("Done performance test!");
        return "Done performance test!";
    }

    private void lab(int size, double xc, double yc, double scale, int iterations, int subdivision, int executors, int imageCount, int pauseBetweenImages, int pauseBetweenExperiments, boolean breakdownEnable, boolean timeoutEnable, boolean badGatewayEnable) throws IOException, InterruptedException {

        if (breakdownEnable) {
            log.info("Start experiment [pod restart]...");
            for (int i = 0; i < 11; i++) {
                uiController.breakdownProbabilityParam = i;
                experiment(size, xc, yc, scale, iterations, subdivision, executors, imageCount, pauseBetweenImages, i);
                Thread.sleep(pauseBetweenExperiments);
            }
            uiController.breakdownProbabilityParam = -1;
        }

        if (timeoutEnable) {
            log.info("Start experiment [timeout]...");
            for (int i = 0; i < 11; i++) {
                uiController.timeoutProbabilityParam = i;
                experiment(size, xc, yc, scale, iterations, subdivision, executors, imageCount, pauseBetweenImages, i);
                Thread.sleep(pauseBetweenExperiments);
            }
            uiController.timeoutProbabilityParam = -1;
        }

        if (badGatewayEnable) {
            log.info("Start experiment [bad gateway]...");
            for (int i = 0; i < 11; i++) {
                uiController.badGatewayParam = i;
                experiment(size, xc, yc, scale, iterations, subdivision, executors, imageCount, pauseBetweenImages, i);
                Thread.sleep(pauseBetweenExperiments);
            }
            uiController.badGatewayParam = -1;
        }
    }

    private void experiment(int size, double xc, double yc, double scale, int iterations, int subdivision, int executors, int imageCount, int pauseBetweenImages, int err) throws IOException, InterruptedException {

        List<Long> measures = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            long start = System.currentTimeMillis();
            byte[] mandelbrotImage = uiController.getMandelbrotImage(size, xc, yc, scale, iterations, subdivision, executors);
            long stop = System.currentTimeMillis();
            long took = stop - start;
            measures.add(took);
            log.debug("blackhole: {}", mandelbrotImage.length);
            Thread.sleep(pauseBetweenImages);
        }

        double min = measures.stream().mapToDouble(Long::doubleValue).min().orElse(-1);
        double max = measures.stream().mapToDouble(Long::doubleValue).max().orElse(-1);
        double average = measures.stream().mapToDouble(Long::doubleValue).average().orElse(-1);
        double median = measures.stream().sorted().toList().get(imageCount / 2);
        double p95 = measures.stream().sorted().toList().get((int) Math.round(imageCount * 0.95) - 1);

        log.info("Result of test: err={}, avg={} ms, mean={} ms, p95={}, min={}, max={}", err, average, median, p95, min, max);
    }

}
