package test;

import graph.DijkstraPathFinder;
import graph.Graph;
import graph.OptimizedDijkstraPathFinder;
import model.City;
import model.Criterion;
import model.Road;
import model.Route;

import java.util.Map;
import java.util.Random;

/**
 * Тест производительности: сравнение обычной и оптимизированной версий Дейкстры.
 * 
 * Демонстрирует выигрыш от оптимизации на графах разного размера.
 */
public class PerformanceTest {

    public static void main(String[] args) {
        System.out.println("=== Тест производительности ===\n");

        // Тестируем на графах разного размера
        int[] sizes = {100, 500, 1000, 2000};
        
        for (int size : sizes) {
            testPerformance(size);
        }

        System.out.println("\n=== Проверка корректности оптимизированной версии ===\n");
        testCorrectness();
    }

    /**
     * Сравнивает производительность двух реализаций.
     */
    private static void testPerformance(int cityCount) {
        System.out.println("Граф: " + cityCount + " городов");
        
        // Генерируем случайный граф
        Graph graph = generateRandomGraph(cityCount);
        City from = graph.getCityById(1);
        City to = graph.getCityById(cityCount);

        DijkstraPathFinder original = new DijkstraPathFinder(graph);
        OptimizedDijkstraPathFinder optimized = new OptimizedDijkstraPathFinder(graph);

        // Прогрев JVM
        for (int i = 0; i < 10; i++) {
            original.findAllOptimalPaths(from, to);
            optimized.findAllOptimalPaths(from, to);
        }

        // Замер обычной версии
        int iterations = 100;
        long startOriginal = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            original.findAllOptimalPaths(from, to);
        }
        long timeOriginal = (System.nanoTime() - startOriginal) / 1_000_000;

        // Замер оптимизированной версии
        long startOptimized = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            optimized.findAllOptimalPaths(from, to);
        }
        long timeOptimized = (System.nanoTime() - startOptimized) / 1_000_000;

        double speedup = (double) timeOriginal / timeOptimized;

        System.out.printf("  Обычная версия:        %d мс (%d итераций)%n", timeOriginal, iterations);
        System.out.printf("  Оптимизированная:      %d мс (%d итераций)%n", timeOptimized, iterations);
        System.out.printf("  Ускорение:             %.2fx%n%n", speedup);
    }

    /**
     * Проверяет, что оптимизированная версия даёт те же результаты.
     */
    private static void testCorrectness() {
        Graph graph = generateRandomGraph(50);
        
        DijkstraPathFinder original = new DijkstraPathFinder(graph);
        OptimizedDijkstraPathFinder optimized = new OptimizedDijkstraPathFinder(graph);

        City from = graph.getCityById(1);
        City to = graph.getCityById(50);

        Map<Criterion, Route> originalResults = original.findAllOptimalPaths(from, to);
        Map<Criterion, Route> optimizedResults = optimized.findAllOptimalPaths(from, to);

        boolean allMatch = true;
        for (Criterion criterion : Criterion.values()) {
            Route origRoute = originalResults.get(criterion);
            Route optRoute = optimizedResults.get(criterion);

            boolean match = origRoute.getTotalDistance() == optRoute.getTotalDistance()
                    && origRoute.getTotalTime() == optRoute.getTotalTime()
                    && origRoute.getTotalCost() == optRoute.getTotalCost();

            if (match) {
                System.out.println("✓ " + criterion.getFullName() + ": результаты совпадают");
            } else {
                System.out.println("✗ " + criterion.getFullName() + ": РАСХОЖДЕНИЕ!");
                System.out.println("  Обычная:        " + origRoute.getParamsString());
                System.out.println("  Оптимизированная: " + optRoute.getParamsString());
                allMatch = false;
            }
        }

        System.out.println();
        if (allMatch) {
            System.out.println("✓ Оптимизированная версия корректна!");
        } else {
            System.out.println("✗ Обнаружены расхождения!");
        }
    }

    /**
     * Генерирует случайный связный граф.
     */
    private static Graph generateRandomGraph(int cityCount) {
        Graph graph = new Graph();
        Random random = new Random(42); // Фиксированный seed для воспроизводимости

        // Создаём города
        for (int i = 1; i <= cityCount; i++) {
            graph.addCity(new City(i, "Город" + i));
        }

        // Создаём связный граф (цепочка)
        for (int i = 1; i < cityCount; i++) {
            City from = graph.getCityById(i);
            City to = graph.getCityById(i + 1);
            graph.addRoad(new Road(from, to,
                    random.nextInt(100) + 10,
                    random.nextInt(60) + 5,
                    random.nextInt(200) + 20));
        }

        // Добавляем случайные рёбра для плотности
        int extraEdges = cityCount * 2;
        for (int i = 0; i < extraEdges; i++) {
            int fromId = random.nextInt(cityCount) + 1;
            int toId = random.nextInt(cityCount) + 1;
            if (fromId != toId) {
                City from = graph.getCityById(fromId);
                City to = graph.getCityById(toId);
                graph.addRoad(new Road(from, to,
                        random.nextInt(100) + 10,
                        random.nextInt(60) + 5,
                        random.nextInt(200) + 20));
            }
        }

        return graph;
    }
}
