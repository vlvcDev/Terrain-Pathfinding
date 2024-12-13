import java.util.*;

public class DijkstraPathfinder implements PathfindingAlgorithm {
    @Override
    public PathResult findPath(GridGraph graph, Node start, Node end) {
        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> prev = new HashMap<>();
        PriorityQueue<NodeDist> pq = new PriorityQueue<>(Comparator.comparingDouble(nd -> nd.dist));

        dist.put(start, 0.0);
        pq.add(new NodeDist(start, 0.0));

        while (!pq.isEmpty()) {
            NodeDist current = pq.poll();
            Node u = current.node;

            if (u.equals(end)) {
                // Found shortest path
                break;
            }

            for (Node v : graph.getNeighbors(u)) {
                double alt = dist.get(u) + graph.getCost(u, v);
                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(new NodeDist(v, alt));
                }
            }
        }

        List<Node> path = reconstructPath(prev, start, end);
        double simulatedTime = dist.getOrDefault(end, Double.POSITIVE_INFINITY);
        return new PathResult(path, simulatedTime);
    }

    private List<Node> reconstructPath(Map<Node, Node> prev, Node start, Node end) {
        List<Node> path = new LinkedList<>();
        Node cur = end;
        while (cur != null && !cur.equals(start)) {
            path.add(0, cur);
            cur = prev.get(cur);
        }
        if (cur != null) {
            path.add(0, start);
        }
        return path;
    }

    private static class NodeDist {
        Node node;
        double dist;
        NodeDist(Node n, double d) { node = n; dist = d; }
    }
}
