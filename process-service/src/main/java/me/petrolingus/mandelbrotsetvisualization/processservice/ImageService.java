package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final Mandelbrot mandelbrot;

    private float hue;

    private float saturation;

    @Value("#{environment['BREAKDOWN_PROBABILITY']?:-1}")
    private double breakdownProbability;

    @Value("#{environment['BAD_GATEWAY_PROBABILITY']?:-1}")
    private double badGatewayProbability;

    @Value("#{environment['TIMEOUT_PROBABILITY']?:-1}")
    private double timeoutProbability;

    @Value("#{environment['IS_COLORED']?:true}")
    private boolean isColored;

    public ImageService(Mandelbrot mandelbrot) {
        this.mandelbrot = mandelbrot;
        this.hue = ThreadLocalRandom.current().nextFloat();
        this.saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
    }

    @GetMapping("/api/v1/generate-mandelbrot-tile")
    public synchronized ResponseEntity<int[]> generateMandelbrotTile(@RequestParam(defaultValue = "128") int size,
                                                                     @RequestParam(defaultValue = "-1") double xc,
                                                                     @RequestParam(defaultValue = "0") double yc,
                                                                     @RequestParam(defaultValue = "2") double scale,
                                                                     @RequestParam(defaultValue = "128") int iterations
    ) throws InterruptedException {

        double rand = Math.random();

        double breakdown = breakdownProbability / 100.0;
        if (rand < breakdown) {
            System.exit(-1);
        }

        double badGateway = badGatewayProbability / 100.0;
        if (rand < badGateway) {
            return ResponseEntity.status(502).build();
        }

        double timeout = timeoutProbability / 100.0;
        if (rand < timeout) {
            Thread.sleep(10_000);
        }

        // Generate image
        int[] image = foo(size, xc, yc, scale, iterations);

        return ResponseEntity.ok(image);
    }

    @GetMapping(value = "/api/v1/generate-mandelbrot-tile-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] generateMandelbrotTileImage(@RequestParam(defaultValue = "128") int size,
                                                            @RequestParam(defaultValue = "-1") double xc,
                                                            @RequestParam(defaultValue = "0") double yc,
                                                            @RequestParam(defaultValue = "2") double scale,
                                                            @RequestParam(defaultValue = "128") int iterations) throws IOException {

        int[] data = foo(size, xc, yc, scale, iterations);

        // Return result
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, size, size, data, 0, size);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());
        return in.readAllBytes();
    }

    @GetMapping("/api/v1/warmup")
    public void warmup() {
        foo(64, -1, 0, 2, 32);
    }

    private int[] foo(int size, double xc, double yc, double scale, int iterations) {

        if (isColored) {
            this.hue = ThreadLocalRandom.current().nextFloat();
            this.saturation = 0.2f * ThreadLocalRandom.current().nextFloat() + 0.2f;
        }

        // Generate image
        return mandelbrot.getMandelbrotImage(size, xc, yc, scale, iterations, hue, saturation);
    }

    @GetMapping("/probe/liveness")
    private @ResponseBody String live() {
        return "ALIVE";
    }

    @GetMapping("/probe/readiness")
    private @ResponseBody String ready() {
        return "READY";
    }
}
