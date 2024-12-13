// filepath: /c:/Users/vlvcd/CSAlgorithms/HW3/Stoplight-Flow/src/ManhattanHeuristic.java
public class ManhattanHeuristic implements Heuristic {
    @Override
    public double estimate(Node from, Node to) {
        // Simplify heuristic by removing height difference
        return Math.abs(from.getRow() - to.getRow()) + Math.abs(from.getCol() - to.getCol());
    }
}