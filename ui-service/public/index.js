window.onload = function () {
    const canvas = document.getElementById("canvas");
    const ctx = canvas.getContext("2d");

    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);

    // http://localhost:63342/mandelbrot-service-demo/index.html?size=512&xc=-1&yc=0&scale=2&iterations=128&subdivision=3

    const size = urlParams.get('size');
    const xc = Number(urlParams.get('xc'));
    const yc = Number(urlParams.get('yc'));
    const scale = urlParams.get('scale');
    const iterations = urlParams.get('iterations');
    const subdivision = Number(urlParams.get('subdivision'));

    let tilesInRow = Math.pow(2, subdivision);
    if (tilesInRow > 16) {
        tilesInRow = 16
    }
    const tileSize = size / tilesInRow;
    const tileScale = scale / tilesInRow;

    //draw images
    for (let i = 0; i < tilesInRow; i++) {
        const y = i * tileSize;
        for (let j = 0; j < tilesInRow; j++) {

            const x = j * tileSize;

            const xcTile = (xc + j * tileScale) - (tilesInRow / 2.0 - 0.5) * tileScale;
            const ycTile = (yc - i * tileScale) + (tilesInRow / 2.0 - 0.5) * tileScale;

            const img = new Image();
            img.onload = function () {
                ctx.drawImage(img, x, y, tileSize, tileSize);
            };

            img.src = `http://localhost:80/api/v1/generate-mandelbrot-tile?size=${tileSize}&xc=${xcTile}&yc=${ycTile}&scale=${tileScale}&iterations=${iterations}`
        }
    }
};