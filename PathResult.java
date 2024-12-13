import java.util.List;

public class PathResult {
    private final List<Node> path;
    private final double simulatedTime;

    public PathResult(List<Node> path, double simulatedTime) {
        this.path = path;
        this.simulatedTime = simulatedTime;
    }

    public List<Node> getPath() {
        return path;
    }

    public double getSimulatedTime() {
        return simulatedTime;
    }

    public double calculateSimulatedTime(GridGraph graph) {
        double totalTime = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalTime += graph.getCost(path.get(i), path.get(i + 1));
        }
        return totalTime;
    }
}
