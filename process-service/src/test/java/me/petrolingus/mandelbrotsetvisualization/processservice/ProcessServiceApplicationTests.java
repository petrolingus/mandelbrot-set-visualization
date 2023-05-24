package me.petrolingus.mandelbrotsetvisualization.processservice;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

class ProcessServiceApplicationTests {

	@Test
	void getImage_whenImageSizeIsNotDegreeOfTwo_thenThrowIllegalArgumentException() {
		Mandelbrot mandelbrot = new Mandelbrot();

		List<Point2D.Double> points = new ArrayList<>();
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(-0.2, 0.8));
		points.add(new Point2D.Double(-1, 0));
		points.add(new Point2D.Double(10, 10));

		for (Point2D.Double point : points) {
			double v = mandelbrot.calculatePixel(point.getX(), point.getY(), 8);
			System.out.println(v);
		}
	}

}
