import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Main {
    private static int gridRows = 200;
    private static int gridCols = 300;
    private static double maxHeight = 100.0;
    private static final double NOISE_SCALE = 4.25;
    
    private static GridGraph graph;
    private static JPanel visualizationPanel;
    private static JFrame mainFrame;
    private static JPanel infoPanel; // Add this field
    private static List<PathResult> results; // Add this field at class level
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        mainFrame = new JFrame("Pathfinding Visualization");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Add info panel on the right
        mainFrame.add(createInfoPanel(), BorderLayout.EAST);

        // Create control panel
        JPanel controlPanel = new JPanel();
        JTextField rowsField = new JTextField(String.valueOf(gridRows), 5);
        JTextField colsField = new JTextField(String.valueOf(gridCols), 5);
        JTextField heightField = new JTextField(String.valueOf(maxHeight), 5);
        JButton generateButton = new JButton("Generate Map");
        JButton solveButton = new JButton("Find Paths");
        solveButton.setEnabled(false);

        controlPanel.add(new JLabel("Rows:"));
        controlPanel.add(rowsField);
        controlPanel.add(new JLabel("Columns:"));
        controlPanel.add(colsField);
        controlPanel.add(new JLabel("Max Height:"));
        controlPanel.add(heightField);
        controlPanel.add(generateButton);
        controlPanel.add(solveButton);

        mainFrame.add(controlPanel, BorderLayout.NORTH);

        generateButton.addActionListener(e -> {
            try {
                gridRows = Integer.parseInt(rowsField.getText());
                gridCols = Integer.parseInt(colsField.getText());
                maxHeight = Double.parseDouble(heightField.getText());
                
                // Use the new method without a fixed seed
                double[][] heightMap = NoiseMapGenerator.generateHeightMap(gridRows, gridCols, NOISE_SCALE);
                // Scale the height map to max height
                for (int i = 0; i < heightMap.length; i++) {
                    for (int j = 0; j < heightMap[0].length; j++) {
                        heightMap[i][j] = heightMap[i][j] * maxHeight;
                    }
                }
                
                graph = new GridGraph(heightMap);
                if (visualizationPanel != null) {
                    mainFrame.remove(visualizationPanel);
                }
                // Show initial terrain visualization with zero timings
                visualizationPanel = GraphVisualizer.createVisualization(
                    graph, 
                    new PathResult(new ArrayList<>(), 0), 
                    new PathResult(new ArrayList<>(), 0), 
                    graph.getNode(0, 0), 
                    graph.getNode(gridRows - 1, gridCols - 1),
                    0L, 0L, 0.0, 0.0  // Add placeholder timing values
                );
                    
                mainFrame.add(visualizationPanel, BorderLayout.CENTER);
                mainFrame.pack();
                solveButton.setEnabled(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter valid numbers");
            }
        });

        solveButton.addActionListener(e -> {
            Node start = graph.getNode(0, 0);
            Node end = graph.getNode(gridRows - 1, gridCols - 1);

            // Create all pathfinders based on checkbox state
            List<PathfindingAlgorithm> algorithms = new ArrayList<>();
            List<String> algorithmNames = new ArrayList<>();
            
            // Always add these algorithms
            algorithms.add(new DijkstraPathfinder());
            algorithms.add(new AStarPathfinder(new ManhattanHeuristic()));
            algorithmNames.add("Dijkstra");
            algorithmNames.add("A*");
            
            // Add remaining algorithms
            algorithms.add(new BestFirstPathfinder(new ManhattanHeuristic()));
            algorithms.add(new BFSPathfinder());
            algorithmNames.add("Best-First");
            algorithmNames.add("BFS");

            results = new ArrayList<>(); // Store results in class field
            List<Long> executionTimes = new ArrayList<>();

            // Run each algorithm
            for (PathfindingAlgorithm algorithm : algorithms) {
                long startTime = System.nanoTime();
                PathResult result = algorithm.findPath(graph, start, end);
                long endTime = System.nanoTime();
                results.add(result);
                executionTimes.add(endTime - startTime);
            }

            if (visualizationPanel != null) {
                mainFrame.remove(visualizationPanel);
            }
            visualizationPanel = GraphVisualizer.createVisualization(
                graph, results, algorithmNames, start, end, executionTimes
            );
            
            updateInfoPanel(algorithmNames, executionTimes); // Add this line
                
            mainFrame.add(visualizationPanel, BorderLayout.CENTER);
            mainFrame.pack();
        });

        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }
    
    private static JPanel createInfoPanel() {
        infoPanel = new JPanel(); // Store reference to info panel
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Algorithm Information"));
        infoPanel.setPreferredSize(new Dimension(250, 0));
        
        // Create result labels that we can update later
        JLabel[] resultLabels = new JLabel[5]; // Increased size by 1 for summary
        
        String[] info = {
            "<html><b>Dijkstra:</b> Optimal path using edge weights<br/>Time: ---</html>",
            "<html><b>A*:</b> Heuristic-guided optimal path<br/>Time: ---</html>",
            "<html><b>Best-First:</b> Pure heuristic search<br/>Time: ---</html>",
            "<html><b>BFS:</b> Shortest by edge count<br/>Time: ---</html>",
            "<html><b>Analysis:</b><br/>Best performance: ---<br/>Best path cost: ---</html>" // Add summary label
        };
        
        Color[] colors = GraphVisualizer.getPathColors();
        for (int i = 0; i < info.length; i++) {
            resultLabels[i] = new JLabel(info[i]);
            if (i < colors.length) {
                resultLabels[i].setForeground(colors[i]);
            } else {
                resultLabels[i].setForeground(Color.BLACK); // Summary in black
            }
            resultLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            infoPanel.add(resultLabels[i]);
        }
        
        // Add separator before summary
        infoPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        
        infoPanel.putClientProperty("resultLabels", resultLabels);
        return infoPanel;
    }

    // Add this method to update the info panel with results
    private static void updateInfoPanel(List<String> algorithmNames, List<Long> executionTimes) {
        JLabel[] labels = (JLabel[])infoPanel.getClientProperty("resultLabels"); // Use stored reference
        
        for (int i = 0; i < algorithmNames.size(); i++) {
            String name = algorithmNames.get(i);
            double timeMs = executionTimes.get(i) / 1_000_000.0;
            double simTime = results.get(i).getSimulatedTime(); // Add this line
            labels[i].setText(String.format("<html><b>%s:</b> %s<br/>Real Time: %.2f ms<br/>Simulated Time: %.2f</html>", 
                name, getAlgorithmDescription(name), timeMs, simTime));
        }
        
        // Add performance analysis
        AlgorithmPerformance bestPerf = analyzePerfomance(algorithmNames, executionTimes);
        labels[4].setText(String.format(
            "<html><b>Performance Analysis:</b><br/>" +
            "Fastest execution: %s (%.2f ms)<br/>" +
            "Best path cost: %s (%.2f)<br/>" +
            "Best overall: %s</html>",
            formatWinners(algorithmNames, bestPerf.fastestIndices, executionTimes.get(bestPerf.fastestIndices.get(0)) / 1_000_000.0),
            executionTimes.get(bestPerf.fastestIndices.get(0)) / 1_000_000.0,
            formatWinners(algorithmNames, bestPerf.optimalIndices, results.get(bestPerf.optimalIndices.get(0)).getSimulatedTime()),
            results.get(bestPerf.optimalIndices.get(0)).getSimulatedTime(),
            formatWinners(algorithmNames, bestPerf.overallBestIndices, null)
        ));
    }

    private static class AlgorithmPerformance {
        List<Integer> fastestIndices = new ArrayList<>();
        List<Integer> optimalIndices = new ArrayList<>();
        List<Integer> overallBestIndices = new ArrayList<>();
    }

    private static AlgorithmPerformance analyzePerfomance(List<String> names, List<Long> times) {
        AlgorithmPerformance perf = new AlgorithmPerformance();
        
        // Find fastest execution(s)
        long minTime = Collections.min(times);
        for (int i = 0; i < times.size(); i++) {
            if (Math.abs(times.get(i) - minTime) < 0.0001) {
                perf.fastestIndices.add(i);
            }
        }
        
        // Find best path cost(s)
        double minCost = Double.POSITIVE_INFINITY;
        for (PathResult result : results) {
            minCost = Math.min(minCost, result.getSimulatedTime());
        }
        for (int i = 0; i < results.size(); i++) {
            if (Math.abs(results.get(i).getSimulatedTime() - minCost) < 0.0001) {
                perf.optimalIndices.add(i);
            }
        }
        
        // Calculate overall best (weighted score combining both metrics)
        double bestScore = Double.POSITIVE_INFINITY;
        double[] scores = new double[times.size()];
        
        // Calculate all scores first
        for (int i = 0; i < times.size(); i++) {
            double timeNorm = (double)times.get(i) / Collections.max(times);
            double costNorm = results.get(i).getSimulatedTime() / 
                            Collections.max(results, (a, b) -> 
                                Double.compare(a.getSimulatedTime(), b.getSimulatedTime()))
                            .getSimulatedTime();
            
            scores[i] = (timeNorm * 0.4) + (costNorm * 0.6);
            bestScore = Math.min(bestScore, scores[i]);
        }
        
        // Find all algorithms that achieved the best score
        for (int i = 0; i < scores.length; i++) {
            if (Math.abs(scores[i] - bestScore) < 0.0001) {
                perf.overallBestIndices.add(i);
            }
        }
        
        return perf;
    }

    private static String formatWinners(List<String> names, List<Integer> indices, Double value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) sb.append(", ");
            int index = indices.get(i);
            sb.append(String.format("<font color='%s'>%s</font>", 
                getColorHex(index), names.get(index)));
        }
        return sb.toString();
    }

    private static String getColorHex(int algorithmIndex) {
        Color color = GraphVisualizer.getPathColors()[algorithmIndex];
        return String.format("#%02x%02x%02x", 
            color.getRed(), color.getGreen(), color.getBlue());
    }

    private static String getAlgorithmDescription(String name) {
        switch (name) {
            case "Dijkstra": return "Optimal path using edge weights";
            case "A*": return "Heuristic-guided optimal path";
            case "Best-First": return "Pure heuristic search";
            case "BFS": return "Shortest by edge count";
            default: return "";
        }
    }
}
