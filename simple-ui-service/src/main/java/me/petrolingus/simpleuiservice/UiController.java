package me.petrolingus.simpleuiservice;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class UiController {

    private final RestTemplate restTemplate;

    public UiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    @GetMapping(value = "/api/v1/get-mandelbrot-image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getMandelbrotImage(
            @RequestParam(defaultValue = "512") int size,
            @RequestParam(defaultValue = "-1") double xc,
            @RequestParam(defaultValue = "0") double yc,
            @RequestParam(defaultValue = "2") double scale,
            @RequestParam(defaultValue = "128") int iterations,
            @RequestParam(defaultValue = "4") int subdivision
    ) {

        log.info("request to process mandelbrot set: {}|{}|{}|{}|{}|{}", size, xc, yc, scale, iterations, subdivision);

        final int tilesInRow = (int) Math.pow(2, subdivision);
        final int tileSize = size / tilesInRow;
        final double tileScale = scale / tilesInRow;

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < tilesInRow; i++) {
            int y = i * tileSize;
            for (int j = 0; j < tilesInRow; j++) {
                int x = j * tileSize;

                double xcTile = (xc + j * tileScale) - (tilesInRow / 2.0 - 0.5) * tileScale;
                double ycTile = (yc - i * tileScale) + (tilesInRow / 2.0 - 0.5) * tileScale;
                String url = getMandelbrotTileUrl(tileSize, xcTile, ycTile, tileScale, iterations);

                tasks.add(() -> {
                    UUID uuid = UUID.randomUUID();
                    log.info("start task: {}", uuid);
                    long start = System.currentTimeMillis();
                    while (true) {
                        try {
                            int[] pixels = restTemplate.getForObject(url, int[].class);
                            image.setRGB(x, y, tileSize, tileSize, pixels, 0, tileSize);
                            log.info("complete task {} in {} ms", uuid, System.currentTimeMillis() - start);
                            return null;
                        } catch (Throwable e) {
                            log.warn("exception during process {}: {}", uuid, e.getMessage());
                            Thread.yield();
                        }
                    }
                });
            }
        }

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            executorService.invokeAll(tasks);
        }

        // Return image
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());
        return in.readAllBytes();
    }

    private String getMandelbrotTileUrl(int size, double xc, double yc, double scale, int iterations) {
        return "http://localhost:8081" +
                "/api/v1/get-mandelbrot-tile" +
                "?size=" + size +
                "&xc=" + xc +
                "&yc=" + yc +
                "&scale=" + scale +
                "&iterations=" + iterations;
    }
}
