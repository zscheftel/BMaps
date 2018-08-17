import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */

    LinkedHashMap<Long, Node> nodeList = new LinkedHashMap<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        if (nodeList.isEmpty()) {
            return;
        }
        LinkedHashMap<Long, Node> tempnodeList = (LinkedHashMap<Long, Node>) nodeList.clone();
        for (Node node : tempnodeList.values()) {
            if (node.edgesToNeighbors.isEmpty()) {
                nodeList.remove(node.id);
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nodeList.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        ArrayList<Long> rList = new ArrayList<>();
        Node vNode = nodeList.get(v);
        for (Edge edge : vNode.edgesToNeighbors) {
            if (edge.startID == v) {
                rList.add(edge.endID);
            } else if (edge.endID == v) {
                rList.add(edge.startID);
            }
        }
        return rList;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */

    /*
        long upperLatID = lats.get(lats.ceilingKey(lat));
        long lowerLatID = lats.get(lats.floorKey(lat));
        long upperLonID = lons.get(lons.ceilingKey(lon));
        long lowerLonID = lons.get(lons.floorKey(lon));

        double upperLonDiffLat = Math.abs(lon - lon(upperLatID));
        double upperLonDiffLon = Math.abs(lon - lon(upperLonID));

        double lowerLonDiffLat = Math.abs(lon - lon(lowerLatID));
        double lowerLonDiffLon = Math.abs(lon - lon(lowerLonID));

        double upperLatDiffLon = Math.abs(lat - lat(upperLonID));
        double upperLatDiffLat = Math.abs(lat - lat(upperLatID));

        double lowerLatDiffLon = Math.abs(lat - lat(lowerLonID));
        double lowerLatDiffLat = Math.abs(lat - lat(lowerLatID));

        double upperLatBound = lat + Math.max(upperLatDiffLat, upperLatDiffLon);
        double upperLonBound = lon - Math.max(upperLonDiffLat, upperLonDiffLon);
        double lowerLatBound = lat - Math.max(lowerLatDiffLat, lowerLatDiffLon);
        double lowerLonBound = lon + Math.max(lowerLonDiffLat, lowerLonDiffLon);

        double lowLon = Math.min(upperLonBound, lowerLonBound);
        double highLon = Math.max(upperLonBound, lowerLonBound);
        double lowLat = Math.min(upperLatBound, lowerLatBound);
        double highLat = Math.max(upperLatBound, lowerLatBound);

        SortedMap<Double, Long> subLats = lats.subMap(lowLat, true, highLat, true);
        SortedMap<Double, Long> subLons = lons.subMap(lowLon, true, highLon, true);

        ArrayList<Long> ids = new ArrayList<>();
        ids.addAll(subLats.values());
        ids.addAll(subLons.values());

        for (Long id : ids) {
            if (!id.equals(targetNode.id)) {
                Double tempDist = distance(id, targetNode.id);
                if (tempDist < dist) {
                    dist = tempDist;
                    finalID = id;
                }
            }
        }
        */

    long closest(double lon, double lat) {
        Double dist = Double.POSITIVE_INFINITY;
        Long finalID = (long) -1;

        Node targetNode = new Node(Double.toString(lat), Double.toString(lon), "0", "0");
        nodeList.put(targetNode.id, targetNode);

        for (Node node : nodeList.values()) {
            if (!node.id.equals(targetNode.id)) {
                Double tempDist = distance(node.id, targetNode.id);
                if (tempDist < dist) {
                    dist = tempDist;
                    finalID = node.id;
                }
            }
        }
        nodeList.remove(targetNode.id);
        return finalID;

        /*
        for (Long id : ids) {
            if (!id.equals(targetNode.id)) {
                double tempDist = distance(targetNode.id, id);
                if (tempDist < dist) {
                    dist = tempDist;
                    finalID = id;
                }
            }
        }
        nodeList.remove(targetNode.id);
        return finalID;
        */
    }

    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodeList.get(v).lon;
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodeList.get(v).lat;
    }

    class Node {
        double lat;
        double lon;
        String name;
        Long id;
        ArrayList<Edge> edgesToNeighbors;

        Node(String lat, String lon, String id, String name) {
            this.lat = Double.parseDouble(lat);
            this.lon = Double.parseDouble(lon);
            this.name = name;
            this.id = Long.parseLong(id);
            this.edgesToNeighbors = new ArrayList<>();
        }
    }

    class Edge {
        String id;
        String name;
        String maxSpeed;
        Long startID;
        Long endID;
        double distance;

        Edge(String id, Long start, Long end) {
            this.id = id;
            this.startID = start;
            this.endID = end;
            this.distance = distance(start, end);
        }
    }

}
