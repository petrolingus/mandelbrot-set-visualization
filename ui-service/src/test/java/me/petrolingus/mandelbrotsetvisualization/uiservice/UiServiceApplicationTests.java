package me.petrolingus.mandelbrotsetvisualization.uiservice;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UiServiceApplicationTests {

    @ParameterizedTest
    @ValueSource(ints = {4097, 5000, Integer.MAX_VALUE})
	void getMandelbrotImage_whenSizeMore4096_thenThrowIllegalArgumentException(int size) {
        UiController uiController = new UiController(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            uiController.getMandelbrotImage(size, 0, 0, 0, 0, 0);
        });
        String expectedMessage = "Size must be less or equal than 4096";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
	}

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, 0, 127})
    void getMandelbrotImage_whenSizeLess128_thenThrowIllegalArgumentException(int size) {
        UiController uiController = new UiController(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            uiController.getMandelbrotImage(size, 0, 0, 0, 0, 0);
        });
        String expectedMessage = "Size must be more or equal than 128";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }

    @ParameterizedTest
    @ValueSource(ints = {150, 500, 1000})
    void getImage_whenImageSizeIsNotDegreeOfTwo_thenThrowIllegalArgumentException(int size) {
        UiController uiController = new UiController(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            uiController.getMandelbrotImage(size, 0, 0, 0, 0, 0);
        });
        String expectedMessage = "The size of the image must be a degree of two";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 8, 9})
    void getImage_whenTileSizeMoreOnePixel_thenOk(int subdivision) {
        UiController uiController = new UiController(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            uiController.getMandelbrotImage(1024, 0, 0, 0, 0, subdivision);
        });
        String expectedMessage = "Tile size must be more 1 pixel";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 11, 12})
    void getImage_whenTileSizeLessOrEqualOnePixel_thenThrowIllegalArgumentException(int subdivision) {
        UiController uiController = new UiController(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            uiController.getMandelbrotImage(1024, 0, 0, 0, 0, subdivision);
        });
        String expectedMessage = "Tile size must be more 1 pixel";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage, expectedMessage);
    }

}
