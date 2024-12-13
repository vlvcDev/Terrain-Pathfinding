public class Node {
    private final int row;
    private final int col;
    private final double height;

    public Node(int row, int col, double height) {
        this.row = row;
        this.col = col;
        this.height = height;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public double getHeight() { return height; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node)o;
        return row == node.row && col == node.col;
    }

    @Override
    public int hashCode() {
        return row * 31 + col;
    }
}
