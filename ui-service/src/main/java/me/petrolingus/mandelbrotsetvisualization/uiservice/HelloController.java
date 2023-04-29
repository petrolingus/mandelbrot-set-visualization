package me.petrolingus.mandelbrotsetvisualization.uiservice;

import jakarta.annotation.PostConstruct;
import me.petrolingus.mandelbrotsetvisualization.dao.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
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
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class HelloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    final RestTemplate restTemplate;
    final PoolController poolController;

    @Value("${processServiceUrl}")
    String processServiceUrl;

    @Value("${logUrl}")
    String logUrl;

    private static final StringBuilder stringBuilder = new StringBuilder();

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private static BufferedImage plugImage;

    public HelloController(RestTemplate restTemplate, PoolController poolController) {
        this.restTemplate = restTemplate;
        this.poolController = poolController;
    }

    @PostConstruct
    public void init() {
        System.out.println(processServiceUrl);
        plugImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        plugImage.setRGB(0, 0, Color.WHITE.getRGB());
    }

    @GetMapping("/")
    public String hello(Model model) {
        model.addAttribute("size", 1024);
        model.addAttribute("xc", -1.0);
        model.addAttribute("yc", 0.0);
        model.addAttribute("scale", 2.0);
        model.addAttribute("maxIterations", 512);
        model.addAttribute("subdivision", 5);
        model.addAttribute("image", "/get-plug-image");
        model.addAttribute("timeUrl", logUrl);
        return "index";
    }

    @PostMapping("/")
    public String hello(@ModelAttribute("mandelbrotSetParam") MandelbrotSetParam mandelbrotSetParam, Model model) {
        model.addAttribute("size", mandelbrotSetParam.getSize());
        model.addAttribute("xc", mandelbrotSetParam.getXc());
        model.addAttribute("yc", mandelbrotSetParam.getYc());
        model.addAttribute("scale", mandelbrotSetParam.getScale());
        model.addAttribute("maxIterations", mandelbrotSetParam.getMaxIterations());
        model.addAttribute("subdivision", mandelbrotSetParam.getSubdivision());
        model.addAttribute("image", "/get-image" + mandelbrotSetParam);
        model.addAttribute("timeUrl", logUrl);
        return "index";
    }

    @GetMapping(value = "get-plug-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getPlugImage() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(plugImage, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());
        return in.readAllBytes();
    }

    @GetMapping(value = "get-image", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImage(@RequestParam int size,
                                         @RequestParam double xc,
                                         @RequestParam double yc,
                                         @RequestParam double scale,
                                         @RequestParam int maxIterations,
                                         @RequestParam int subdivision
    ) throws IOException, InterruptedException, ExecutionException {

//        if (subdivision > 6) {
//            subdivision = 6;
//        }
//
//        logger.info("start process:");
//
//        long start = System.nanoTime();

        final int tilesInRow = (int) Math.pow(2, subdivision);
        final int tilesCount = tilesInRow * tilesInRow;
        final int tileSize = size / tilesInRow;
        final double tileScale = scale / tilesInRow;

        poolController.schedule(size);

        for (int i = 0; i < tilesInRow; i++) {
            int y = i * tileSize;
            for (int j = 0; j < tilesInRow; j++) {
                int x = j * tileSize;

                double xcTile = (xc + j * tileScale) - (tilesInRow / 2.0 - 0.5) * tileScale;
                double ycTile = (yc - i * tileScale) + (tilesInRow / 2.0 - 0.5) * tileScale;

                UUID uuid = UUID.randomUUID();
                Task task = new Task(uuid, tileSize, xcTile, ycTile, tileScale, maxIterations, x, y);
                poolController.add(task);
            }
        }

        BufferedImage image = poolController.getResult();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream in = new ByteArrayInputStream(os.toByteArray());

//        long stop = System.nanoTime();
//        Duration took = Duration.of(stop - start, ChronoUnit.NANOS);
//
//        stringBuilder.append(Instant.now().truncatedTo(ChronoUnit.MILLIS)).append(':').append(" Process image took: ").append(took.toMillis()).append("ms\n");

        return in.readAllBytes();
    }

    @GetMapping(value = "getLogMessages", produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String getLogMessages() {
        return stringBuilder.toString();
    }
}
