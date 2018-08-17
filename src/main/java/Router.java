import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     *
     * @param // g       The graph to use.
     * @param // stlon   The longitude of the start location.
     * @param // stlat   The latitude of the start location.
     * @param // destlon The longitude of the destination location.
     * @param // destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     **/

    /* public static LinkedList<Long> parentChain(Long id, HashMap<Long, Long> p) {
        long tempID = id;
        LinkedList<Long> rLst = new LinkedList<>();
        rLst.addFirst(tempID);
        while (p.get(tempID) != -1) {
            rLst.addFirst(p.get(tempID));
            tempID = p.get(tempID);
        }
        return rLst;
    } */

    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {

        HashSet<Long> marked = new HashSet<>();
        HashMap<Long, Long> parents = new HashMap<>();
        HashMap<Long, Double> bestDist = new HashMap<>();

        Long startID = g.closest(stlon, stlat);
        Long endID = g.closest(destlon, destlat);
        Long currID = startID;
        GraphDB.Node currNode = g.nodeList.get(currID);

        PriorityQueue<Long> fringe = new PriorityQueue<>((o1, o2) -> {
            Double priOne = bestDist.get(o1) + g.distance(o1, endID);
            Double priTwo = bestDist.get(o2) + g.distance(o2, endID);
            if (priOne > priTwo) {
                return 1;
            } else if (priOne < priTwo) {
                return -1;
            }
            return 0;
        });

        marked.add(currID);
        parents.put(currID, (long) -1);
        bestDist.put(currID, (double) 0);

        while (!currID.equals(endID)) {
            for (GraphDB.Edge edge : currNode.edgesToNeighbors) {
                double currDist = bestDist.get(currID) + edge.distance;
                if (!marked.contains(edge.endID)) {
                    /* if (edge.endID.equals(endID)) {
                        parents.put(edge.endID, currID);
                        bestDist.put(edge.endID, currDist);
                        currID = endID;
                        return parentChain(currID, parents);
                        */
                    /*} else */
                    if (!parents.containsKey(edge.endID)
                            || !bestDist.containsKey(edge.endID)) {
                        parents.put(edge.endID, currID);
                        bestDist.put(edge.endID, currDist);
                        fringe.add(edge.endID);
                    } else if (currDist < bestDist.get(edge.endID)) {
                        parents.replace(edge.endID, currID);
                        bestDist.replace(edge.endID, currDist);
                        fringe.add(edge.endID);
                    }
                }
            }
            currID = fringe.poll();
            if (currID == null) {
                return new ArrayList<>();
            }
            marked.add(currID);
            currNode = g.nodeList.get(currID);
        }
        LinkedList<Long> rLst = new LinkedList<>();
        rLst.addFirst(currID);
        while (parents.get(currID) != -1) {
            rLst.addFirst(parents.get(currID));
            currID = parents.get(currID);
        }
        return rLst;
    }

        /*
        HashSet<Long> marked = new HashSet<>();
        HashMap<Long, Double> bestDistToNode = new HashMap<>();
        HashMap<Long, ArrayList<Long>> bestPathToNode = new HashMap<>();

        Long startNodeID = g.closest(stlon, stlat);
        Long endNodeID = g.closest(destlon, destlat);
        Long currentID = startNodeID;
        GraphDB.Node currentNode = g.nodeList.get(currentID);

        PriorityQueue<Long> fringe = new PriorityQueue<>((o1, o2) -> {
            Double myPri = bestDistToNode.get(o1) + g.distance(o1, endNodeID);
            Double otherPri = bestDistToNode.get(o2) + g.distance(o2, endNodeID);
            if (myPri > otherPri) {
                return 1;
            } else if (myPri < otherPri) {
                return -1;
            }
            return 0;
        });

        bestDistToNode.put(startNodeID, (double) 0);
        bestPathToNode.put(startNodeID, new ArrayList<Long>());
        bestPathToNode.get(startNodeID).add(startNodeID);
        fringe.add(startNodeID);
        int i = 0;

        while (!currentID.equals(endNodeID)) {
            for (GraphDB.Edge edge : currentNode.edgesToNeighbors) {
                if (!marked.contains(edge.endID)) {
                    double currDis = bestDistToNode.get(currentID) + edge.distance;
                    ArrayList<Long> currLst =
                            (ArrayList<Long>) bestPathToNode.get(currentID).clone();
                    currLst.add(edge.endID);
                    if (edge.endID.equals(endNodeID)) {
                        bestDistToNode.replace(currentID, currDis);
                        bestPathToNode.replace(currentID, currLst);
                        return bestPathToNode.get(currentID);
                    } else if (!bestDistToNode.containsKey(edge.endID)) {
                        bestDistToNode.put(edge.endID, currDis);
                        bestPathToNode.put(edge.endID, currLst);
                        fringe.add(edge.endID);
                    } else if (currDis < bestDistToNode.get(edge.endID)) {
                        bestDistToNode.replace(edge.endID, currDis);
                        bestPathToNode.replace(edge.endID, currLst);
                        fringe.add(edge.endID);
                    }
                }
            }
            currentID = fringe.poll();
            currentNode = g.nodeList.get(currentID);
            marked.add(currentID);
        }
        return bestPathToNode.get(currentID);

        */


    /**
     * Create the list of directions corresponding to a route on the graph.
     *
     * @param g     The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        return null; // FIXME
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /**
         * Default name for an unknown way.
         */
        public static final String UNKNOWN_ROAD = "unknown road";

        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction a given NavigationDirection represents.
         */
        int direction;
        /**
         * The name of the way I represent.
         */
        String way;
        /**
         * The distance along this way I represent.
         */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
