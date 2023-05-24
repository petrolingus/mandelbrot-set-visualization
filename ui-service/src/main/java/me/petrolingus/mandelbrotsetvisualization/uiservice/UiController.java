package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
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

        if (size > 4096) {
            throw new IllegalArgumentException("Size must be less or equal than 4096");
        }

        if (size < 128) {
            throw new IllegalArgumentException("Size must be more or equal than 128");
        }

        if ((size & (size - 1)) != 0) {
            throw new IllegalArgumentException("The size of the image must be a degree of two");
        }

        LOGGER.info("Task with UUID-{}", UUID.randomUUID());

        final int tilesInRow = (int) Math.pow(2, subdivision);
        final int tilesCount = tilesInRow * tilesInRow;
        final int tileSize = size / tilesInRow;
        final double tileScale = scale / tilesInRow;

        if (tileSize <= 1) {
            throw new IllegalArgumentException("Tile size must be more 1 pixel");
        }

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        AtomicInteger atomicInteger = new AtomicInteger();
        AtomicInteger retriedInteger = new AtomicInteger();

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < tilesInRow; i++) {
            int y = i * tileSize;
            for (int j = 0; j < tilesInRow; j++) {
                int x = j * tileSize;

                double xcTile = (xc + j * tileScale) - (tilesInRow / 2.0 - 0.5) * tileScale;
                double ycTile = (yc - i * tileScale) + (tilesInRow / 2.0 - 0.5) * tileScale;

                String url = urlGenerator(tileSize, xcTile, ycTile, tileScale, maxIterations);

                int index = i * tilesInRow + j;

                tasks.add(() -> {
                    int retried = 0;
                    while (true) {
                        long start = System.currentTimeMillis();
                        try {
                            atomicInteger.incrementAndGet();
                            int[] pixels = restTemplate.getForObject(url, int[].class);
                            long stop = System.currentTimeMillis();
                            LOGGER.info("Chunk {} with size {} generated within {}ms", index, tileScale, (stop - start));

                            image.setRGB(x, y, tileSize, tileSize, pixels, 0, tileSize);

//                            Graphics2D graphics2D = image.createGraphics();
//                            Color decode = Color.decode(Integer.toString(pixels[pixels.length / 2]));
//                            Color color = new Color(255 - decode.getRed(), 255 - decode.getGreen(),
//                                    255 - decode.getBlue());
//                            graphics2D.setColor(color);
//                            graphics2D.setFont(new Font("Arial Black", Font.BOLD, tileSize / 4));
//                            graphics2D.drawString(Integer.toString(index), x + tileSize / 2, y + tileSize / 2);

                            break;
                        } catch (Throwable e) {
                            long stop = System.currentTimeMillis();
                            LOGGER.info("Chunk {} NOT GENERATED! Response time {}ms", index, (stop - start));
                            LOGGER.info(e.toString());
                            retried++;
                        }
                    }
                    retriedInteger.addAndGet(retried);
                    return null;
                });
            }
        }

        EXECUTOR_SERVICE.invokeAll(tasks);

        LOGGER.info("Chunks count: {}", tilesCount);
        LOGGER.info("Requests sends: {}", atomicInteger.get());

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
                "&maxIterations=" + iterations;
    }
}
