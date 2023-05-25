package me.petrolingus.mandelbrotsetvisualization.processservice;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ImageService {

    private final Mandelbrot mandelbrot;

    private final float hue;

    private final float saturation;

    @Value("${systemExitProbability}")
    private double systemExitProbability;

    @Value("${maxExecutableTasks}")
    private double maxExecutableTasks;

    private final AtomicInteger inProgressCounter = new AtomicInteger();

    private ExecutorService executorService;

    @Autowired
    private AsyncTaskService asyncTaskService;

    public ImageService(Mandelbrot mandelbrot) {
        this.mandelbrot = mandelbrot;
        this.hue = ThreadLocalRandom.current().nextFloat();
        this.saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
    }

    @GetMapping("/api/v1/kill")
    public void kill() {
        System.exit(-1);
    }

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(8);
    }

    @GetMapping("/api/v1/generate-mandelbrot-tile")
    public CompletableFuture<ResponseEntity<int[]>> generateMandelbrotTile(@RequestParam int size,
                                        @RequestParam double xc,
                                        @RequestParam double yc,
                                        @RequestParam double scale,
                                        @RequestParam int maxIterations
    ){
        return asyncTaskService.handleExampleRequestAsync(size, xc, yc, scale, maxIterations, hue, saturation)
                .thenApply(result -> new ResponseEntity<>(result, HttpStatus.OK));
    }
}
