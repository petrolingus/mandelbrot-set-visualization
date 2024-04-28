package me.petrolingus.simpleprocessservice;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.FastMath;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@Slf4j
public class ProcessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessServiceApplication.class, args);
    }

    @SneakyThrows
    @GetMapping("/api/v1/get-mandelbrot-tile")
    public synchronized ResponseEntity<int[]> getMandelbrotTile(
            @RequestParam(defaultValue = "512") int size,
            @RequestParam(defaultValue = "-1") double xc,
            @RequestParam(defaultValue = "0") double yc,
            @RequestParam(defaultValue = "2") double scale,
            @RequestParam(defaultValue = "128") int iterations
    ) {
        log.info(Thread.currentThread().getName());
        int[] image = getMandelbrotImage(size, xc, yc, scale, iterations);
        TimeUnit.MILLISECONDS.sleep(1000);
        return ResponseEntity.ok(image);
    }

    public int[] getMandelbrotImage(int size, double xc, double yc, double scale, int iterations) {

        double halfScale = scale / 2;
        double factor = scale / size;
        double xStart = xc - halfScale;
        double yStart = yc - halfScale;
        float hue = ThreadLocalRandom.current().nextFloat();
        float saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;

        int[] pixels = new int[size * size];
        for (int i = 0; i < size; i++) {
            double x0 = xStart + factor * i;
            for (int j = 0; j < size; j++) {
                double y0 = yStart + factor * j;

                float brightness = (float) calculatePixel(x0, y0, iterations);

                int id = (size - 1 - j) * size + i;
                pixels[id] = Color.HSBtoRGB(hue, saturation, brightness);
            }
        }

        return pixels;
    }

    public double calculatePixel(double x, double y, int maxIterations) {
        double ix = 0;
        double iy = 0;
        int iteration = 0;
        while (ix * ix + iy * iy < 4 && iteration < maxIterations) {
            double xtemp = ix * ix - iy * iy + x;
            iy = 2 * ix * iy + y;
            ix = xtemp;
            iteration++;
        }
        return FastMath.log(iteration) / FastMath.log(maxIterations);
    }
}