import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Routing {

    /**
     * TODO
     * <p>
     * The findPaths function takes a board and a list of goals that contain
     * endpoints that need to be connected. The function returns a list of
     * Paths that connect the points.
     */
    public static ArrayList<Wire> findPaths(Board board, ArrayList<Endpoints> goals) {
        long BOLT = System.currentTimeMillis();

        ArrayList<Wire> guide = new ArrayList<>();

        // Sort the goals from the shortest distance (between each point) to longest
        List<Endpoints> sortedGoals = goals.stream()
                .sorted(Comparator.comparingDouble(goal -> {
                    Coord start = goal.start;
                    Coord end = goal.end;
                    return Math.sqrt(Math.pow((end.row - start.row), 2) + Math.pow((end.column - start.column), 2)); // To calculate the distance for each goal takes O(n) time.
                }))
                .collect(Collectors.toList()); // To sort the list using Collections.sort(), takes O(nlog(n)) time.
        // Total Time Complexity : O(nlog(n))

        for (Endpoints wire : sortedGoals){
            int ID = wire.id;
            Coord copperStart = wire.start;
            Coord copperEnd = wire.end;

            ArrayList<Endpoints> updatedWires = new ArrayList<>();
            ArrayList<Coord> borderCoords = new ArrayList<>();
            ArrayList<Coord> trace = bfs(board, copperStart, copperEnd, borderCoords); // O(n)
            // Returns an ArrayList of "Coord"s if the start of the wire is able to visit its end successfully. Otherwise, it will return null.
            // An empty ArrayList is passed to the function as reference to accumulate "Coord"s that neighbors another wire. In case of when bfs returns null,
            // borderCoords will be used to find the neighboring Wires.
            ArrayList<Integer> touchingNeighbors = neighbors(guide, borderCoords); // O(n^5)
            // The "neighbors" function takes 3 parameters and returns an ArrayList of Integers containing the Wire.IDs of the neighboring wires.

            if (trace != null){
                Wire path = new Wire(ID, trace); // A wire is created, if bfs doesn't return null.
                board.placeWire(path); // Place the wire
                guide.add(path); // Add the wire to the "map" containing all connected Wires.
            } else {
                if (!borderCoords.isEmpty()){ // If the current Endpoint is neighboring any wire
                    fixConnection(board, guide, goals, touchingNeighbors, wire, updatedWires); // Call fixConnection() to rearrange the wires on the board to make the current Endpoint successfully wired.
                }
            }
        }
        double finish = System.currentTimeMillis();
        double timeElapsed = finish - BOLT;
        System.out.println("TIME (in Milliseconds) : " + timeElapsed);

        return guide;
    }

    public static ArrayList<Coord> bfs(Board B, Coord start, Coord end, ArrayList<Coord> borderCoords) { // Breadth-First Search to traverse the board. Time Complexity : O(n)
        // !occupied: 0
        // occupied: -1 or any number except 0
        Queue<Coord> Q = new LinkedList<Coord>();
        Q.add(start); // LinkedList add = O(1)

        Map<Coord, Coord> parent = new HashMap<Coord, Coord>();
        HashSet<Coord> visited = new HashSet<Coord>();
        visited.add(start); //HashSet add = O(1)

        while (! Q.isEmpty()) { // Total Time Complexity: O(n)
            Coord curr = Q.remove(); // LinkedList remove = O(1)
            if (curr.equals(end)){ // O(1)
                return reconstructPath(start, end, parent); // Constructs the shortest path for the wire
            }

            for (Coord c : B.adj(curr)) {  // O(4)
                if (! visited.contains(c)){ // HashSet contains O(1)
                    if ((! B.isOccupied(c) && ! B.isObstacle(c)) || c.equals(end)) {
                        parent.put(c, curr); //HashMap put = O(1)
                        Q.add(c);
                        visited.add(c);
                        for (Coord a : B.adj(c)){ // Find which visited "Coord"s neighbors another wire. O(4)
                            if (B.getValue(a) != 0 && (B.getValue(a) != B.getValue(start)) && !B.isObstacle(a)){ // Finds the Coords that neighbors another Coord with different values
                                borderCoords.add(c); //ArrayList add = O(1)
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    // Works with unweighted edges
    public static ArrayList<Coord> reconstructPath(Coord start, Coord end, Map<Coord, Coord> parent){ // Returns the shortest path between start and end. Time Complexity: O(n)
        ArrayList<Coord> path = new ArrayList<>();
        for (Coord point = end; point != null; point = parent.get(point)){ // O(n)
            path.add(point); // O(1)
            if (point.equals(start)){ // O(1)
                break;
            }
        }
        Collections.reverse(path); // O(n)
        return path;

        //Total Time Complexity : O(n)
    }

    /*
    The fixConnection() performs a few operations on the board:
    - Removes neighboring wires (because they potentially block the current Endpoint)
    - Adds wires after pre-existing wires were removed
    It will recurse if the current Endpoint still fails to connect

    // Time Complexity : O(2^n)
    */

    public static void fixConnection(Board board, ArrayList<Wire> guide, ArrayList<Endpoints> goals, ArrayList<Integer> neighbors, Endpoints curr, ArrayList<Endpoints> updatedWires){
        ArrayList<Integer> toAddLater = new ArrayList<>();

        for (Endpoints e : updatedWires){ // O(n)
            if (neighbors.contains(e.id)){ // O(n)
                neighbors.remove(Integer.valueOf(e.id)); // O(1)
            }
        }
        // Total Time Complexity : O(n^2)

        for (int x : neighbors){ // O(n)
            for (Wire w : guide){ // O(n)
                if (w.id == x){ // O(n)
                    board.removeWire(w); // O(1)
                }
            } // Total Time Complexity : O(n^2)
            Iterator<Wire> itr = guide.iterator();
            while (itr.hasNext()) { // O(n)
                Wire w = itr.next();
                if (w.id == x) {
                    itr.remove(); // O(1)
                }
            } // Total Time Complexity : O(n)
            toAddLater.add(x); // O(1)
        }

        ArrayList<Coord> borderCoords = new ArrayList<>();
        ArrayList<Coord> traceNew = bfs(board, curr.start, curr.end, borderCoords); // O(n)

        // The neighbors() returns the IDs of the neighboring wires (i.e. wires that touch bordering Coords of current Endpoint's scope
        ArrayList<Integer> touchingNeighbors = neighbors(guide, borderCoords);

        if (traceNew != null) {
            Wire path = new Wire(curr.id, traceNew);
            board.placeWire(path); // O(n); Depends on how long the wire is.
            guide.add(path); // O(1)
            updatedWires.add(curr); // O(1)
            for (int x : toAddLater) { // O(n)
                for (Endpoints e : goals) { //O(n)
                    if (x == e.id) {
                        ArrayList<Coord> borderCoordsUpdate = new ArrayList<>();
                        ArrayList<Coord> traceUpdate = bfs(board, e.start, e.end, borderCoordsUpdate); // O(n)
                        ArrayList<Integer> touchingNeighborsUpdate = neighbors(guide, borderCoordsUpdate);
                        if (traceUpdate != null){
                            Wire pathUpdate = new Wire(e.id, traceUpdate);
                            board.placeWire(pathUpdate); // O(n)
                            guide.add(pathUpdate); // O(1)
                        } else {
//                            System.out.println(board.toString());
//                            System.out.println("ID: " + e.id);
//                            System.out.println("WIRES NOT BE TOUCHED: " + updatedWires);
//                            System.out.println("NEW CORRDS: " + touchingNeighborsUpdate);
                            fixConnection(board, guide, goals, touchingNeighborsUpdate, e, updatedWires); // O(2^n)
                        }

                    }
                }
            } // Total Time Complexity: O(n^3)
        } else {
            updatedWires.add(curr); // O(1)
            fixConnection(board, guide, goals, touchingNeighbors, curr, updatedWires); // O(n^2)
        }
    }

    /*
        The neighbors() returns the IDs of the neighboring wires (i.e. wires that touch bordering Coords of current Endpoint's scope
        Time Complexity : O(n^5)
     */
    public static ArrayList<Integer> neighbors(ArrayList<Wire> guide, ArrayList<Coord> borderCoords){
        ArrayList<Integer> touchingNeighbors = new ArrayList<>();
        for (Wire w : guide){ // O(n)
            for (Coord c : borderCoords){ // O(n)
                ArrayList<Coord> ListOfCoords = w.getPoints();
                for (Coord b : ListOfCoords){ // O(n)
                    if (c.isAdjacent(b)){ // O(1)
                        if (!touchingNeighbors.contains(w.id)){ // ArrayList contains = O(n)
                            touchingNeighbors.add(w.id); // O(1)
                        }
                    } // Total Time Complexity : O(n)
                } // Total Time Complexity : O(n^2)
            } // Total Time Complexity : O(n^3)
        } // Total Time Complexity : O(n^5)
        return touchingNeighbors;
    }
}
