package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ImageService {

    private final Mandelbrot mandelbrot;

    private final float hue;

    private final float saturation;

    private final AtomicInteger inProgress = new AtomicInteger();

    public ImageService(Mandelbrot mandelbrot) {
        this.mandelbrot = mandelbrot;
        this.hue = ThreadLocalRandom.current().nextFloat();
        this.saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
    }

    @GetMapping("/api/v1/generate-mandelbrot-tile")
    public int[] generateMandelbrotTile(@RequestParam int size,
                                        @RequestParam double xc,
                                        @RequestParam double yc,
                                        @RequestParam double scale,
                                        @RequestParam int maxIterations
    ) {
        inProgress.incrementAndGet();
        int[] mandelbrotImage = mandelbrot.getMandelbrotImage(size, xc, yc, scale, maxIterations, hue, saturation);
        inProgress.decrementAndGet();
        return mandelbrotImage;
    }

    @GetMapping("/probes/live")
    private @ResponseBody String live() {
        return "ALIVE";
    }

    @GetMapping("/probes/ready")
    private @ResponseBody String ready() {
//        if (inProgress.get() > 10) {
//            return ResponseEntity.status(500).build();
//        }
        return "READY";
    }
}
