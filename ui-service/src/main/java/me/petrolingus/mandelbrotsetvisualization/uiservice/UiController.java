package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class UiController {

    private final RestTemplate restTemplate;

    @Value("#{environment['PROCESS_SERVICE']?:'http://localhost:8181/api/v1/generate-mandelbrot-tile'}")
    private String processServiceUrl;

    public UiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping(value = "/api/v1/get-mandelbrot-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getMandelbrotImage(@RequestParam(defaultValue = "512") int size,
                                                   @RequestParam(defaultValue = "-1") double xc,
                                                   @RequestParam(defaultValue = "0") double yc,
                                                   @RequestParam(defaultValue = "2") double scale,
                                                   @RequestParam(defaultValue = "128") int iterations,
                                                   @RequestParam(defaultValue = "4") int subdivision,
                                                   @RequestParam(defaultValue = "1") int executors
    ) throws IOException, InterruptedException {

        if (subdivision > 6) {
            subdivision = 6;
        }

        final int tilesInRow = (int) Math.pow(2, subdivision);
        final int tileSize = size / tilesInRow;
        final double tileScale = scale / tilesInRow;

        ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(executors);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        // Create requests pool
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < tilesInRow; i++) {
            int y = i * tileSize;
            for (int j = 0; j < tilesInRow; j++) {
                int x = j * tileSize;

                double xcTile = (xc + j * tileScale) - (tilesInRow / 2.0 - 0.5) * tileScale;
                double ycTile = (yc - i * tileScale) + (tilesInRow / 2.0 - 0.5) * tileScale;

                String url = urlGenerator(tileSize, xcTile, ycTile, tileScale, iterations);

                tasks.add(() -> {
                    int[] pixels = restTemplate.getForObject(url, int[].class);
                    image.setRGB(x, y, tileSize, tileSize, pixels, 0, tileSize);
                    return null;
                });
            }
        }

        // Execute all requests
        WORKER_THREAD_POOL.invokeAll(tasks);

        // Return image
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());
        return in.readAllBytes();
    }

    private String urlGenerator(int size, double xc, double yc, double scale, int iterations) {
        return processServiceUrl +
                "?size=" + size +
                "&xc=" + xc +
                "&yc=" + yc +
                "&scale=" + scale +
                "&iterations=" + iterations;
    }
}
