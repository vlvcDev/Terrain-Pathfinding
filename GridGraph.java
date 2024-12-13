import java.util.*;

public class GridGraph {
    private final int rows;
    private final int cols;
    private final Node[][] nodes;
    private final Map<Node, List<Node>> adjacencyList; // Add this field

    public GridGraph(double[][] heightMap) {
        this.rows = heightMap.length;
        this.cols = heightMap[0].length;
        this.nodes = new Node[rows][cols];
        this.adjacencyList = new HashMap<>();

        // Create nodes
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                nodes[r][c] = new Node(r, c, heightMap[r][c]);
            }
        }

        // Initialize adjacency lists
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Node current = nodes[r][c];
                adjacencyList.put(current, new ArrayList<>());
                if (r > 0) adjacencyList.get(current).add(nodes[r-1][c]);
                if (r < rows-1) adjacencyList.get(current).add(nodes[r+1][c]);
                if (c > 0) adjacencyList.get(current).add(nodes[r][c-1]);
                if (c < cols-1) adjacencyList.get(current).add(nodes[r][c+1]);
            }
        }
    }

    public Node getNode(int r, int c) {
        return nodes[r][c];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public Iterable<Node> getNeighbors(Node n) {
        return adjacencyList.get(n);
    }

    // Add this method to get all edges in the graph
    public List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>();
        for (Node from : adjacencyList.keySet()) {
            for (Node to : adjacencyList.get(from)) {
                edges.add(new Edge(from, to, getCost(from, to)));
            }
        }
        return edges;
    }

    public double getCost(Node from, Node to) {
        double h1 = from.getHeight();
        double h2 = to.getHeight();
        double heightDiff = Math.abs(h2 - h1);
        
        if (heightDiff < 0.0001) {
            return 5.0; // flat ground - base cost
        } else if (h2 < h1) {
            // Downhill
            return Math.max(2.0, 5.0 - (heightDiff * 0.5));
        } else {
            // Uphill: steeper penalty for climbing
            return 5.0 + (heightDiff * 20.0);
        }
    }
}

// Add this class at the end of the file or in a separate file
class Edge {
    private final Node from;
    private final Node to;
    private final double cost;

    public Edge(Node from, Node to, double cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
    }

    public Node getFrom() { return from; }
    public Node getTo() { return to; }
    public double getCost() { return cost; }
}
