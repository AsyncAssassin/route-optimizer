package test;

import graph.Graph;
import graph.OptimizedDijkstraPathFinder;
import model.City;
import model.Criterion;
import model.Road;
import model.Route;

import java.util.*;

/**
 * Нагрузочный тест системы оптимизации маршрутов.
 * 
 * Проверяет:
 * - Масштабируемость на больших графах (до 10000 вершин)
 * - Стабильность при большом количестве запросов
 * - Потребление памяти
 * - Деградацию производительности под нагрузкой
 */
public class LoadTest {

    private static final Random random = new Random(42);

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           НАГРУЗОЧНОЕ ТЕСТИРОВАНИЕ СИСТЕМЫ                ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        // Тест 1: Масштабируемость по размеру графа
        testScalability();

        // Тест 2: Стресс-тест - много запросов
        testHighLoad();

        // Тест 3: Экстремальные графы
        testExtremeGraphs();

        // Тест 4: Профиль памяти
        testMemoryProfile();

        System.out.println("\n════════════════════════════════════════════════════════════");
        System.out.println("Нагрузочное тестирование завершено");
        System.out.println("════════════════════════════════════════════════════════════");
    }

    /**
     * Тест 1: Масштабируемость по размеру графа
     */
    private static void testScalability() {
        System.out.println("═══ ТЕСТ 1: Масштабируемость ═══\n");
        System.out.println("Вершины │ Рёбра    │ Время (мс) │ Запросов/сек │ Память (MB)");
        System.out.println("────────┼──────────┼────────────┼──────────────┼────────────");

        int[] sizes = {100, 500, 1000, 2500, 5000, 10000};

        for (int size : sizes) {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long memBefore = runtime.totalMemory() - runtime.freeMemory();

            Graph graph = generateRandomGraph(size, size * 3);
            OptimizedDijkstraPathFinder finder = new OptimizedDijkstraPathFinder(graph);

            City from = graph.getCityById(1);
            City to = graph.getCityById(size);

            // Прогрев
            for (int i = 0; i < 5; i++) {
                finder.findAllOptimalPaths(from, to);
            }

            // Замер
            int iterations = Math.max(10, 1000 / size);
            long start = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                finder.findAllOptimalPaths(from, to);
            }
            long elapsed = (System.nanoTime() - start) / 1_000_000;

            long memAfter = runtime.totalMemory() - runtime.freeMemory();
            double memUsedMB = (memAfter - memBefore) / (1024.0 * 1024.0);
            double requestsPerSec = iterations * 1000.0 / Math.max(1, elapsed);

            System.out.printf("%7d │ %8d │ %10d │ %12.1f │ %10.2f%n",
                    size, size * 3, elapsed, requestsPerSec, memUsedMB);
        }
        System.out.println();
    }

    /**
     * Тест 2: Высокая нагрузка - много запросов
     */
    private static void testHighLoad() {
        System.out.println("═══ ТЕСТ 2: Стресс-тест (10000 запросов) ═══\n");

        Graph graph = generateRandomGraph(1000, 3000);
        OptimizedDijkstraPathFinder finder = new OptimizedDijkstraPathFinder(graph);

        int totalRequests = 10000;
        int successCount = 0;
        int failCount = 0;
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        List<Long> responseTimes = new ArrayList<>(totalRequests);

        System.out.println("Выполнение " + totalRequests + " запросов...");

        for (int i = 0; i < totalRequests; i++) {
            int fromId = random.nextInt(1000) + 1;
            int toId = random.nextInt(1000) + 1;

            City from = graph.getCityById(fromId);
            City to = graph.getCityById(toId);

            long start = System.nanoTime();
            Map<Criterion, Route> result = finder.findAllOptimalPaths(from, to);
            long elapsed = System.nanoTime() - start;

            responseTimes.add(elapsed / 1000); // микросекунды

            if (result.get(Criterion.DISTANCE).exists() || fromId == toId) {
                successCount++;
            } else {
                failCount++;
            }

            totalTime += elapsed;
            minTime = Math.min(minTime, elapsed);
            maxTime = Math.max(maxTime, elapsed);
        }

        Collections.sort(responseTimes);
        long p50 = responseTimes.get(totalRequests / 2);
        long p95 = responseTimes.get((int) (totalRequests * 0.95));
        long p99 = responseTimes.get((int) (totalRequests * 0.99));

        System.out.println("\nРезультаты:");
        System.out.printf("  Успешных запросов:  %d (%.1f%%)%n", successCount, 100.0 * successCount / totalRequests);
        System.out.printf("  Без пути:           %d (%.1f%%)%n", failCount, 100.0 * failCount / totalRequests);
        System.out.printf("  Среднее время:      %.2f мкс%n", totalTime / 1000.0 / totalRequests);
        System.out.printf("  Мин/Макс:           %.2f / %.2f мкс%n", minTime / 1000.0, maxTime / 1000.0);
        System.out.printf("  Персентили:         p50=%.0f мкс, p95=%.0f мкс, p99=%.0f мкс%n", (double) p50, (double) p95, (double) p99);
        System.out.printf("  Пропускная способность: %.0f req/sec%n", totalRequests * 1_000_000_000.0 / totalTime);
        System.out.println();
    }

    /**
     * Тест 3: Экстремальные графы
     */
    private static void testExtremeGraphs() {
        System.out.println("═══ ТЕСТ 3: Экстремальные графы ═══\n");

        // 3.1 Линейный граф (цепочка) - худший случай для BFS, нормальный для Дейкстры
        System.out.println("3.1 Линейный граф (1000 вершин в цепочку):");
        Graph linearGraph = new Graph();
        for (int i = 1; i <= 1000; i++) {
            linearGraph.addCity(new City(i, "City" + i));
        }
        for (int i = 1; i < 1000; i++) {
            linearGraph.addRoad(new Road(
                    linearGraph.getCityById(i),
                    linearGraph.getCityById(i + 1),
                    random.nextInt(100) + 1, random.nextInt(60) + 1, random.nextInt(200) + 1));
        }

        testGraph(linearGraph, 1, 1000, "    ");

        // 3.2 Полный граф (много рёбер)
        System.out.println("3.2 Плотный граф (200 вершин, ~20000 рёбер):");
        Graph denseGraph = generateDenseGraph(200);
        testGraph(denseGraph, 1, 200, "    ");

        // 3.3 Звезда (одна вершина связана со всеми)
        System.out.println("3.3 Граф-звезда (1001 вершина):");
        Graph starGraph = new Graph();
        starGraph.addCity(new City(1, "Center"));
        for (int i = 2; i <= 1001; i++) {
            starGraph.addCity(new City(i, "Point" + i));
            starGraph.addRoad(new Road(
                    starGraph.getCityById(1),
                    starGraph.getCityById(i),
                    random.nextInt(100) + 1, random.nextInt(60) + 1, random.nextInt(200) + 1));
        }
        testGraph(starGraph, 2, 500, "    ");

        System.out.println();
    }

    /**
     * Тест 4: Профиль памяти
     */
    private static void testMemoryProfile() {
        System.out.println("═══ ТЕСТ 4: Профиль памяти ═══\n");

        Runtime runtime = Runtime.getRuntime();

        System.out.println("Размер   │ Граф (MB) │ Поиск (MB) │ Всего (MB)");
        System.out.println("─────────┼───────────┼────────────┼───────────");

        int[] sizes = {1000, 2000, 5000};

        for (int size : sizes) {
            runtime.gc();
            long memStart = runtime.totalMemory() - runtime.freeMemory();

            Graph graph = generateRandomGraph(size, size * 3);

            runtime.gc();
            long memAfterGraph = runtime.totalMemory() - runtime.freeMemory();

            OptimizedDijkstraPathFinder finder = new OptimizedDijkstraPathFinder(graph);
            // Выполняем несколько поисков чтобы аллоцировать память
            for (int i = 0; i < 10; i++) {
                finder.findAllOptimalPaths(graph.getCityById(1), graph.getCityById(size));
            }

            runtime.gc();
            long memAfterSearch = runtime.totalMemory() - runtime.freeMemory();

            double graphMB = (memAfterGraph - memStart) / (1024.0 * 1024.0);
            double searchMB = (memAfterSearch - memAfterGraph) / (1024.0 * 1024.0);
            double totalMB = (memAfterSearch - memStart) / (1024.0 * 1024.0);

            System.out.printf("%8d │ %9.2f │ %10.2f │ %9.2f%n",
                    size, graphMB, searchMB, totalMB);
        }
        System.out.println();
    }

    // ═══ Вспомогательные методы ═══

    private static void testGraph(Graph graph, int fromId, int toId, String prefix) {
        OptimizedDijkstraPathFinder finder = new OptimizedDijkstraPathFinder(graph);
        City from = graph.getCityById(fromId);
        City to = graph.getCityById(toId);

        // Прогрев
        finder.findAllOptimalPaths(from, to);

        // Замер
        long start = System.nanoTime();
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            finder.findAllOptimalPaths(from, to);
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        Map<Criterion, Route> result = finder.findAllOptimalPaths(from, to);
        Route route = result.get(Criterion.DISTANCE);

        System.out.printf("%sВремя: %d мс (%d итераций), Путь: %s%n",
                prefix, elapsed, iterations, 
                route.exists() ? route.getCities().size() + " городов" : "не найден");
    }

    private static Graph generateRandomGraph(int cityCount, int edgeCount) {
        Graph graph = new Graph();

        for (int i = 1; i <= cityCount; i++) {
            graph.addCity(new City(i, "City" + i));
        }

        // Гарантируем связность через остовное дерево
        for (int i = 2; i <= cityCount; i++) {
            int parent = random.nextInt(i - 1) + 1;
            graph.addRoad(new Road(
                    graph.getCityById(parent),
                    graph.getCityById(i),
                    random.nextInt(100) + 10,
                    random.nextInt(60) + 5,
                    random.nextInt(200) + 20));
        }

        // Добавляем дополнительные рёбра
        int additionalEdges = edgeCount - (cityCount - 1);
        for (int i = 0; i < additionalEdges; i++) {
            int from = random.nextInt(cityCount) + 1;
            int to = random.nextInt(cityCount) + 1;
            if (from != to) {
                graph.addRoad(new Road(
                        graph.getCityById(from),
                        graph.getCityById(to),
                        random.nextInt(100) + 10,
                        random.nextInt(60) + 5,
                        random.nextInt(200) + 20));
            }
        }

        return graph;
    }

    private static Graph generateDenseGraph(int cityCount) {
        Graph graph = new Graph();

        for (int i = 1; i <= cityCount; i++) {
            graph.addCity(new City(i, "City" + i));
        }

        // Добавляем рёбра между всеми парами (почти полный граф)
        for (int i = 1; i <= cityCount; i++) {
            for (int j = i + 1; j <= cityCount; j++) {
                if (random.nextDouble() < 0.5) { // 50% рёбер
                    graph.addRoad(new Road(
                            graph.getCityById(i),
                            graph.getCityById(j),
                            random.nextInt(100) + 10,
                            random.nextInt(60) + 5,
                            random.nextInt(200) + 20));
                }
            }
        }

        return graph;
    }
}
