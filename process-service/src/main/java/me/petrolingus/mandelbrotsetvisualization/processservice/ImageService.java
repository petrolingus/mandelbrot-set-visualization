package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class ImageService {

    private final Mandelbrot mandelbrot;

    private final float hue;

    private final float saturation;

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
        return mandelbrot.getMandelbrotImage(size, xc, yc, scale, maxIterations, hue, saturation);
    }

    @GetMapping("/probes/live")
    private @ResponseBody String live() {
        return "ALIVE";
    }

    @GetMapping("/probes/ready")
    private @ResponseBody String ready() {
        return "READY";
    }
}
