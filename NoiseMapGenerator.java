import java.util.Random;

public class NoiseMapGenerator {
    public static double[][] generateHeightMap(int rows, int cols, double scale) {
        return generateHeightMap(rows, cols, scale, System.nanoTime());
    }

    public static double[][] generateHeightMap(int rows, int cols, double scale, long seed) {
        // Find power of 2 that fits our dimensions
        int size = 1;
        while (size < rows || size < cols) {
            size *= 2;
        }
        
        // Generate initial grid
        double[][] grid = new double[size + 1][size + 1];
        Random rand = new Random(seed);
        
        // Set corner points
        grid[0][0] = rand.nextDouble();
        grid[0][size] = rand.nextDouble();
        grid[size][0] = rand.nextDouble();
        grid[size][size] = rand.nextDouble();
        
        // Diamond-Square algorithm
        diamondSquare(grid, size, scale, rand);
        
        // Crop to desired size and normalize
        double[][] result = new double[rows][cols];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        // Find min and max values
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double value = grid[i][j];
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        
        // Normalize to 0-1 range
        double range = max - min;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = (grid[i][j] - min) / range;
            }
        }
        
        return result;
    }
    
    private static void diamondSquare(double[][] grid, int size, double scale, Random rand) {
        int half = size / 2;
        double roughness = scale;
        
        while (half > 0) {
            // Diamond step
            for (int y = half; y < grid.length - 1; y += size) {
                for (int x = half; x < grid[0].length - 1; x += size) {
                    diamondStep(grid, x, y, half, roughness, rand);
                }
            }
            
            // Square step
            for (int y = 0; y < grid.length - 1; y += half) {
                for (int x = (y + half) % size; x < grid[0].length - 1; x += size) {
                    squareStep(grid, x, y, half, roughness, rand);
                }
            }
            
            size = half;
            half /= 2;
            roughness *= 0.5; // Changed from 0.8 to 0.5 for clearer peaks and valleys
        }
    }
    
    private static void diamondStep(double[][] grid, int x, int y, int size, double roughness, Random rand) {
        double avg = (grid[y - size][x - size] + // top left
                     grid[y - size][x + size] + // top right
                     grid[y + size][x - size] + // bottom left
                     grid[y + size][x + size]) // bottom right
                    / 4.0;
        double randomValue = (rand.nextDouble() - 0.5) * roughness; // Adjusted randomness
        grid[y][x] = avg + randomValue;
    }
    
    private static void squareStep(double[][] grid, int x, int y, int size, double roughness, Random rand) {
        int count = 0;
        double avg = 0;
        
        // Check all four sides
        if (y >= size) { avg += grid[y - size][x]; count++; }
        if (y + size < grid.length) { avg += grid[y + size][x]; count++; }
        if (x >= size) { avg += grid[y][x - size]; count++; }
        if (x + size < grid[0].length) { avg += grid[y][x + size]; count++; }
        
        avg /= count;
        double randomValue = (rand.nextDouble() - 0.5) * roughness; // Adjusted randomness
        grid[y][x] = avg + randomValue;
    }
}
