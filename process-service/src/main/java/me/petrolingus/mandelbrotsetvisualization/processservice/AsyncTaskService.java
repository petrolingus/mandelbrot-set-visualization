package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncTaskService {

    final Mandelbrot mandelbrot;

    public AsyncTaskService(Mandelbrot mandelbrot) {
        this.mandelbrot = mandelbrot;
    }

    @Async
    public CompletableFuture<int[]> handleExampleRequestAsync(int size, double xc, double yc, double scale, int maxIterations, float hue, float saturation) {
        // Perform some logic here
        int[] mandelbrotImage = mandelbrot.getMandelbrotImage(size, xc, yc, scale, maxIterations, hue, saturation);
        return CompletableFuture.completedFuture(mandelbrotImage);
    }
}
