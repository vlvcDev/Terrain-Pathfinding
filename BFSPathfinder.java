import java.util.*;

public class BFSPathfinder implements PathfindingAlgorithm {
    @Override
    public PathResult findPath(GridGraph graph, Node start, Node end) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Double> costSoFar = new HashMap<>();

        // Only consider 4 cardinal directions for true BFS behavior
        int[] dr = {-1, 0, 1, 0};  // North, East, South, West
        int[] dc = {0, 1, 0, -1};

        queue.offer(start);
        visited.add(start);
        costSoFar.put(start, 0.0);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(end)) {
                break;
            }

            // Collect unvisited neighbors
            List<Node> neighbors = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int newRow = current.getRow() + dr[i];
                int newCol = current.getCol() + dc[i];

                if (newRow >= 0 && newRow < graph.getRows() &&
                    newCol >= 0 && newCol < graph.getCols()) {
                    Node next = graph.getNode(newRow, newCol);
                    if (!visited.contains(next)) {
                        neighbors.add(next);
                    }
                }
            }

            // Shuffle neighbors to eliminate directional bias
            Collections.shuffle(neighbors);

            // Add neighbors to the queue
            for (Node next : neighbors) {
                visited.add(next);
                queue.offer(next);
                cameFrom.put(next, current);
                costSoFar.put(next, costSoFar.get(current) + graph.getCost(current, next));
            }
        }

        // Reconstruct path
        List<Node> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        return new PathResult(path, costSoFar.getOrDefault(end, Double.POSITIVE_INFINITY));
    }
}