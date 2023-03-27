package me.petrolingus.mandelbrotsetvisualization.processservice;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController("/")
public class ImageService {

    private final Mandelbrot mandelbrot;

    public ImageService(Mandelbrot mandelbrot) {
        this.mandelbrot = mandelbrot;
    }

    private float hue;

    private float saturation;

    @PostConstruct
    public void init() {
        hue = ThreadLocalRandom.current().nextFloat();
        saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
    }

    @GetMapping("mandelbrot")
    public int[] getMandelbrot(@RequestParam int size,
                               @RequestParam double xc,
                               @RequestParam double yc,
                               @RequestParam double scale,
                               @RequestParam int maxIterations
    ) {
        return mandelbrot.getMandelbrotImage(size, xc, yc, scale, maxIterations, hue, saturation);
    }
}
