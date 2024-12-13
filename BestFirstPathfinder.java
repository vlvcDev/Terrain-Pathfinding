import java.util.*;

public class BestFirstPathfinder implements PathfindingAlgorithm {
    private final Heuristic heuristic;

    public BestFirstPathfinder(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    @Override
    public PathResult findPath(GridGraph graph, Node start, Node end) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(n -> {
                double baseHeuristic = heuristic.estimate(n, end);
                double heightPenalty = Math.abs(n.getHeight() - end.getHeight()) * 3.0; // Triple the height penalty
                return baseHeuristic + heightPenalty;
            })
        );
        
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Node> cameFrom = new HashMap<>();
        Map<Node, Double> costSoFar = new HashMap<>();

        openSet.add(start);
        costSoFar.put(start, 0.0);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.equals(end)) {
                break;
            }

            if (closedSet.contains(current)) {
                continue;  // Skip if we've already processed this node
            }

            closedSet.add(current);

            for (Node next : graph.getNeighbors(current)) {
                if (closedSet.contains(next)) {
                    continue;
                }

                double newCost = costSoFar.get(current) + graph.getCost(current, next);
                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    openSet.add(next);
                }
            }
        }

        List<Node> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        return new PathResult(path, costSoFar.getOrDefault(end, Double.POSITIVE_INFINITY));
    }
}