package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
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

@@RestController
public class UiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiController.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final RestTemplate restTemplate;

    @Value("${processServiceUrl}")
    private String processServiceUrl;

    public UiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("size", 1024);
        model.addAttribute("xc", -1.0);
        model.addAttribute("yc", 0.0);
        model.addAttribute("scale", 2.0);
        model.addAttribute("maxIterations", 512);
        model.addAttribute("subdivision", 5);
        model.addAttribute("image", "/api/v1/get-plug-image");
        return "index";
    }

    @PostMapping("/")
    public String index(@ModelAttribute("mandelbrotSetParam") MandelbrotSetParam mandelbrotSetParam, Model model) {
        model.addAttribute("size", mandelbrotSetParam.getSize());
        model.addAttribute("xc", mandelbrotSetParam.getXc());
        model.addAttribute("yc", mandelbrotSetParam.getYc());
        model.addAttribute("scale", mandelbrotSetParam.getScale());
        model.addAttribute("maxIterations", mandelbrotSetParam.getMaxIterations());
        model.addAttribute("subdivision", mandelbrotSetParam.getSubdivision());
        model.addAttribute("image", "/api/v1/get-mandelbrot-image" + mandelbrotSetParam);
        return "index";
    }

    @GetMapping(value = "/api/v1/get-plug-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getPlugImage() throws IOException {
        BufferedImage plugImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        plugImage.setRGB(0, 0, Color.WHITE.getRGB());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(plugImage, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());
        return in.readAllBytes();
    }

    @GetMapping(value = "/api/v1/get-mandelbrot-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getMandelbrotImage(@RequestParam int size,
                                                   @RequestParam double xc,
                                                   @RequestParam double yc,
                                                   @RequestParam double scale,
                                                   @RequestParam int maxIterations,
                                                   @RequestParam int subdivision
    ) throws IOException, InterruptedException {

        final int tilesInRow = (int) Math.pow(2, subdivision);
        final int tilesCount = tilesInRow * tilesInRow;
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

                String url = urlGenerator(tileSize, xcTile, ycTile, tileScale, maxIterations);

                tasks.add(() -> {
                    while (true) {
                        try {
                            int[] pixels = restTemplate.getForObject(url, int[].class);
                            image.setRGB(x, y, tileSize, tileSize, pixels, 0, tileSize);
                            return null;
                        } catch (RestClientException e) {
                            LOGGER.info(e.toString());
                        }
                    }
                });
            }
        }

        EXECUTOR_SERVICE.invokeAll(tasks);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());

        return in.readAllBytes();
    }

    @Value("${processServiceUrl}")
    private String urlGenerator(int size, double xc, double yc, double scale, int iterations) {
        return processServiceUrl +
                "?size=" + size +
                "&xc=" + xc +
                "&yc=" + yc +
                "&scale=" + scale +
                "&maxIterations=" + iterations;
    }
}
