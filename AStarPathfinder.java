import java.util.*;

public class AStarPathfinder implements PathfindingAlgorithm {
    private final Heuristic heuristic;

    public AStarPathfinder(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    @Override
    public PathResult findPath(GridGraph graph, Node start, Node end) {
        int rows = graph.getRows();
        int cols = graph.getCols();
        
        // Use arrays instead of HashMaps for faster access
        double[][] gScore = new double[rows][cols];
        double[][] fScore = new double[rows][cols];
        boolean[][] closed = new boolean[rows][cols];
        Node[][] cameFrom = new Node[rows][cols];
        
        // Initialize arrays
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gScore[i][j] = Double.POSITIVE_INFINITY;
                fScore[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        gScore[start.getRow()][start.getCol()] = 0;
        fScore[start.getRow()][start.getCol()] = heuristic.estimate(start, end);

        PriorityQueue<NodeDist> openSet = new PriorityQueue<>(
            1000, // Initial capacity hint
            (a, b) -> Double.compare(
                fScore[a.node.getRow()][a.node.getCol()],
                fScore[b.node.getRow()][b.node.getCol()]
            )
        );
        
        openSet.add(new NodeDist(start, fScore[start.getRow()][start.getCol()]));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll().node;
            int curRow = current.getRow();
            int curCol = current.getCol();

            if (current.equals(end)) {
                return new PathResult(reconstructPath(cameFrom, start, end), 
                                   gScore[curRow][curCol]);
            }

            if (closed[curRow][curCol]) {
                continue;
            }
            closed[curRow][curCol] = true;

            // Inline neighbor checking for better performance
            int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}};
            for (int[] dir : directions) {
                int newRow = curRow + dir[0];
                int newCol = curCol + dir[1];
                
                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                    if (closed[newRow][newCol]) {
                        continue;
                    }

                    Node neighbor = graph.getNode(newRow, newCol);
                    double tentativeG = gScore[curRow][curCol] + graph.getCost(current, neighbor);
                    
                    if (tentativeG < gScore[newRow][newCol]) {
                        cameFrom[newRow][newCol] = current;
                        gScore[newRow][newCol] = tentativeG;
                        fScore[newRow][newCol] = tentativeG + heuristic.estimate(neighbor, end);
                        openSet.add(new NodeDist(neighbor, fScore[newRow][newCol]));
                    }
                }
            }
        }

        return new PathResult(List.of(), Double.POSITIVE_INFINITY);
    }

    private List<Node> reconstructPath(Node[][] cameFrom, Node start, Node end) {
        List<Node> path = new LinkedList<>();
        Node cur = end;
        while (cur != null && !cur.equals(start)) {
            path.add(0, cur);
            cur = cameFrom[cur.getRow()][cur.getCol()];
        }
        if (cur != null) {
            path.add(0, start);
        }
        return path;
    }

    private static class NodeDist {
        final Node node;
        final double f;
        NodeDist(Node n, double f) { 
            this.node = n; 
            this.f = f;
        }
    }
}
