package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;
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

    public ImageService(Mandelbrot mandelbrot) {
        this.mandelbrot = mandelbrot;
        this.hue = ThreadLocalRandom.current().nextFloat();
        this.saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
    }

    @GetMapping("/api/v1/generate-mandelbrot-tile")
    public ResponseEntity<int[]> generateMandelbrotTile(@RequestParam int size,
                                        @RequestParam double xc,
                                        @RequestParam double yc,
                                        @RequestParam double scale,
                                        @RequestParam int maxIterations
    ) {

        if (inProgressCounter.getAndIncrement() < maxExecutableTasks) {
            return ResponseEntity.status(429).build();
        }

        double probability = ThreadLocalRandom.current().nextDouble();

//        if (Math.random() < 0.1) {
//            return ResponseEntity.internalServerError().build();
//        } else if (Math.random() < -1) {
//            try {
//                Thread.sleep(Duration.ofMinutes(5));
//            } catch (InterruptedException e) {
//                log.error("Cant emulate long response with thread.sleep", e);
//            }
//            return ResponseEntity.unprocessableEntity().build();
//        } else if (Math.random() < -1) {
//            System.exit(-1);
//            return ResponseEntity.status(418).build();
//        } else {
//            // do logic
//            return ResponseEntity.ok(new int[]{0, 0, 0, 0}); // ...
//        }

        if (probability < systemExitProbability) {
            System.exit(-1);
        }

        int[] mandelbrotImage = mandelbrot.getMandelbrotImage(size, xc, yc, scale, maxIterations, hue, saturation);

        inProgressCounter.decrementAndGet();
        return ResponseEntity.ok(mandelbrotImage);
    }
}
