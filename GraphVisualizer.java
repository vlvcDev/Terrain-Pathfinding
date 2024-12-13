import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;

public class GraphVisualizer {
    // Update color scheme constants
    private static final Color LOWEST_HEIGHT = Color.BLACK;    // Valleys
    private static final Color HIGHEST_HEIGHT = Color.WHITE;   // Peaks
    private static final int LEGEND_WIDTH = 60;
    private static final Color[] PATH_COLORS = {
        Color.BLUE,    // Dijkstra
        Color.RED,     // A*
        Color.GREEN,  // Best-First
        Color.MAGENTA  // BFS
    };

    public static JPanel createVisualization(GridGraph graph, 
            List<PathResult> results, List<String> algorithmNames,
            Node start, Node end, List<Long> executionTimes) {
        return new GraphPanel(graph, results, algorithmNames, start, end, executionTimes);
    }

    // Add this overloaded method to maintain backward compatibility
    public static JPanel createVisualization(GridGraph graph, 
            PathResult dijkstraResult, PathResult aStarResult,
            Node start, Node end,
            long dijkstraNanoTime, long aStarNanoTime,
            double dijkstraSimTime, double aStarSimTime) {
        
        List<PathResult> results = Arrays.asList(dijkstraResult, aStarResult);
        List<String> algorithmNames = Arrays.asList("Dijkstra", "A*");
        List<Long> executionTimes = Arrays.asList(dijkstraNanoTime, aStarNanoTime);
        
        return new GraphPanel(graph, results, algorithmNames, start, end, executionTimes);
    }

    // Make PATH_COLORS accessible
    public static Color[] getPathColors() {
        return PATH_COLORS;
    }

    private static class GraphPanel extends JPanel {
        private final GridGraph graph;
        private final List<PathResult> results;
        private final List<String> algorithmNames;
        private final Node start;
        private final Node end;
        private final List<Long> executionTimes;

        public GraphPanel(GridGraph graph, List<PathResult> results, 
                List<String> algorithmNames, Node start, Node end,
                List<Long> executionTimes) {
            this.graph = graph;
            this.results = results;
            this.algorithmNames = algorithmNames;
            this.start = start;
            this.end = end;
            this.executionTimes = executionTimes;
            setPreferredSize(new Dimension(800, 600));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Calculate actual drawing area considering legend space
            int mainWidth = getWidth() - LEGEND_WIDTH;
            double cellWidth = (double) mainWidth / graph.getCols();
            double cellHeight = (double) getHeight() / graph.getRows();

            // Find actual min/max heights in the graph
            double minHeight = Double.MAX_VALUE;
            double maxHeight = Double.MIN_VALUE;
            for (int r = 0; r < graph.getRows(); r++) {
                for (int c = 0; c < graph.getCols(); c++) {
                    double h = graph.getNode(r, c).getHeight();
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);
                }
            }

            // Draw terrain
            for (int r = 0; r < graph.getRows(); r++) {
                for (int c = 0; c < graph.getCols(); c++) {
                    Node node = graph.getNode(r, c);
                    double h = node.getHeight();
                    // Normalize height to 0-1 range
                    float normalizedHeight = (float) ((h - minHeight) / (maxHeight - minHeight));
                    g2d.setColor(getHeightColor(normalizedHeight));
                    g2d.fillRect((int)(c * cellWidth), (int)(r * cellHeight), 
                               (int)cellWidth + 1, (int)cellHeight + 1);
                }
            }

            // Draw paths with adjusted width
            drawPaths(g2d, cellWidth, cellHeight);

            // Highlight start/end with adjusted width
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            highlightNode(g2d, start, Color.YELLOW, cellWidth, cellHeight);
            highlightNode(g2d, end, Color.MAGENTA, cellWidth, cellHeight);

            // Draw legend
            drawHeightLegend(g2d, mainWidth, maxHeight);
        }

        private Color getHeightColor(float normalizedHeight) {
            // Directly interpolate between black and white
            return interpolateColor(LOWEST_HEIGHT, HIGHEST_HEIGHT, normalizedHeight);
        }

        private Color interpolateColor(Color c1, Color c2, float ratio) {
            int red = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
            int green = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
            int blue = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
            return new Color(red, green, blue);
        }

        private void drawHeightLegend(Graphics2D g2d, int xPosition, double maxHeight) {
            int legendHeight = getHeight() - 60; // Leave space for timing info
            int legendX = xPosition + 10;
            
            // Draw gradient
            for (int y = 0; y < legendHeight; y++) {
                float normalizedHeight = 1.0f - ((float)y / legendHeight);
                g2d.setColor(getHeightColor(normalizedHeight));
                g2d.fillRect(legendX, y, LEGEND_WIDTH - 20, 1);
            }

            // Draw legend frame
            g2d.setColor(Color.BLACK);
            g2d.drawRect(legendX, 0, LEGEND_WIDTH - 20, legendHeight);

            // Draw height labels
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(String.format("%.1f", maxHeight), legendX + LEGEND_WIDTH - 15, 10);
            g2d.drawString("0.0", legendX + LEGEND_WIDTH - 15, legendHeight - 5);
            g2d.drawString("Height", legendX, legendHeight + 15);
        }

        private void drawPaths(Graphics2D g2d, double w, double h) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            for (int i = 0; i < results.size(); i++) {
                drawPath(g2d, results.get(i).getPath(), PATH_COLORS[i], w, h);
            }
        }

        private void drawPath(Graphics2D g2d, List<Node> path, Color color, double w, double h) {
            g2d.setColor(color);
            for (Node n : path) {
                g2d.fillRect((int)(n.getCol() * w), (int)(n.getRow() * h), 
                            (int)w + 1, (int)h + 1);
            }
        }

        private void highlightNode(Graphics2D g2d, Node n, Color color, double w, double h) {
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect((int)(n.getCol() * w), (int)(n.getRow() * h), 
                        (int)w, (int)h);
        }
    }
}
