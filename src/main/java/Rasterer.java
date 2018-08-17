import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */


public class Rasterer {

    public Rasterer() {
        // YOUR CODE HERE
    }

    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        boolean successfulQuery = true;
        Map<String, Object> returnMap = new LinkedHashMap<>();
        // Assigning values from params
        Double lowerRightLong = params.get("lrlon");
        Double upperLeftLong = params.get("ullon");
        Double width = params.get("w");
        Double upperLeftLat = params.get("ullat");
        Double lowerRightLat = params.get("lrlat");
        // Total distances across the display frame
        double xDistance = Math.abs(MapServer.ROOT_ULLON - MapServer.ROOT_LRLON);
        double yDistance = Math.abs(MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT);
        // If the query is invalid, set successfulQuery to false
        if (lowerRightLat > upperLeftLat
                || lowerRightLong < upperLeftLong) {
            successfulQuery = false;
        }
        // Calculating the depth
        double imageLonDPP = Math.abs(lowerRightLong - upperLeftLong) / width;
        double tempLonDPP = Math.abs(MapServer.ROOT_ULLON - MapServer.ROOT_LRLON)
                / MapServer.TILE_SIZE;
        double depth = 0;
        while (tempLonDPP > imageLonDPP && depth < 7) {
            tempLonDPP = tempLonDPP / 2;
            depth = depth + 1;
        }
        returnMap.put("depth", (int) depth);
        // Calculating the number of tiles per row and column, and the x and y distance between them
        double numberOfTiles = Math.pow(4, depth);
        double numberOfTilesInARow = Math.sqrt(numberOfTiles);
        double xDistancePerTile = xDistance / numberOfTilesInARow;
        double yDistancePerTile = yDistance / numberOfTilesInARow;
        // xTiles
        double xFromOriginToStart = Math.abs(MapServer.ROOT_ULLON - upperLeftLong);
        double xStart = xFromOriginToStart / xDistancePerTile;
        double xFromOriginToEnd = Math.abs(MapServer.ROOT_ULLON - lowerRightLong);
        double xEnd = xFromOriginToEnd / xDistancePerTile;
        xStart = Math.floor(xStart);
        xEnd = Math.floor(xEnd);
        if (xStart >= numberOfTilesInARow) {
            xStart = numberOfTilesInARow - 1;
        }
        if (xEnd >= numberOfTilesInARow) {
            xEnd = numberOfTilesInARow - 1;
        }
        double rasterUpperLeftLon = MapServer.ROOT_ULLON + (xStart * xDistancePerTile);
        double rasterLowerRightLon = MapServer.ROOT_ULLON + ((xEnd + 1) * xDistancePerTile);
        returnMap.put("raster_ul_lon", rasterUpperLeftLon);
        returnMap.put("raster_lr_lon", rasterLowerRightLon);
        double yFromOriginToStart = Math.abs(MapServer.ROOT_ULLAT - upperLeftLat);
        double yStart = yFromOriginToStart / yDistancePerTile;
        double yFromOriginToEnd = Math.abs(MapServer.ROOT_ULLAT - lowerRightLat);
        double yEnd = yFromOriginToEnd / yDistancePerTile;
        yStart = Math.floor(yStart);
        yEnd = Math.floor(yEnd);
        if (yStart >= numberOfTilesInARow) {
            yStart = numberOfTilesInARow - 1;
        }
        if (yEnd >= numberOfTilesInARow) {
            yEnd = numberOfTilesInARow - 1;
        }
        double rasterUpperLeftLat = MapServer.ROOT_ULLAT - (yStart * yDistancePerTile);
        double rasterLowerRightLat = MapServer.ROOT_ULLAT - ((yEnd + 1) * yDistancePerTile);
        returnMap.put("raster_ul_lat", rasterUpperLeftLat);
        returnMap.put("raster_lr_lat", rasterLowerRightLat);
        double numOfXTiles = xEnd - xStart + 1;
        double numOfYTiles = yEnd - yStart + 1;
        String[][] xyGrid = new String[(int) numOfYTiles][(int) numOfXTiles];
        String depthStr = "d" + Integer.toString((int) depth) + "_";
        for (double i = yStart, k = 0; i < yEnd + 1; i++, k++) {
            for (double j = xStart, r = 0; j < xEnd + 1; j++, r++) {
                xyGrid[(int) k][(int) r] = depthStr + "x" + Integer.toString((int) j)
                        + "_y" + Integer.toString((int) i) + ".png";
            }
        }
        returnMap.put("render_grid", xyGrid);
        returnMap.put("query_success", successfulQuery);
        return returnMap;
    }
}
