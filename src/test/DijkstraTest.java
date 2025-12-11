package test;

import graph.DijkstraPathFinder;
import graph.Graph;
import model.City;
import model.Criterion;
import model.Road;
import model.Route;

import java.util.Map;

/**
 * Тесты для алгоритма Дейкстры.
 * Проверяет корректность поиска оптимальных путей по разным критериям.
 */
public class DijkstraTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== Тесты алгоритма Дейкстры ===\n");

        testDirectPathOptimal();
        testDifferentOptimalPaths();
        testNoPathExists();
        testSingleCity();
        testComplexGraph();
        testCyclicGraph();

        System.out.println("\n=== Результаты ===");
        System.out.println("Пройдено: " + testsPassed);
        System.out.println("Провалено: " + testsFailed);
    }

    /**
     * Тест 1: Прямой путь оптимален по всем критериям
     */
    private static void testDirectPathOptimal() {
        System.out.println("Тест 1: Прямой путь оптимален по всем критериям");

        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        // Прямой путь лучше по всем параметрам
        graph.addRoad(new Road(a, b, 100, 60, 200));
        graph.addRoad(new Road(a, c, 200, 100, 300));
        graph.addRoad(new Road(c, b, 200, 100, 300));

        DijkstraPathFinder finder = new DijkstraPathFinder(graph);
        Map<Criterion, Route> routes = finder.findAllOptimalPaths(a, b);

        // Все маршруты должны быть прямыми А → Б
        assertRoute(routes.get(Criterion.DISTANCE), 100, 60, 200, "А -> Б");
        assertRoute(routes.get(Criterion.TIME), 100, 60, 200, "А -> Б");
        assertRoute(routes.get(Criterion.COST), 100, 60, 200, "А -> Б");
    }

    /**
     * Тест 2: Разные оптимальные пути по разным критериям
     */
    private static void testDifferentOptimalPaths() {
        System.out.println("\nТест 2: Разные оптимальные пути по разным критериям");

        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        // Прямой путь: короткий, но дорогой и медленный
        graph.addRoad(new Road(a, b, 100, 120, 500));
        // Обходной путь: длинный, но быстрый и дешёвый
        graph.addRoad(new Road(a, c, 150, 30, 100));
        graph.addRoad(new Road(c, b, 150, 30, 100));

        DijkstraPathFinder finder = new DijkstraPathFinder(graph);
        Map<Criterion, Route> routes = finder.findAllOptimalPaths(a, b);

        // По длине: прямой путь (100 < 300)
        assertRoute(routes.get(Criterion.DISTANCE), 100, 120, 500, "А -> Б");

        // По времени: обходной путь (60 < 120)
        assertRoute(routes.get(Criterion.TIME), 300, 60, 200, "А -> В -> Б");

        // По стоимости: обходной путь (200 < 500)
        assertRoute(routes.get(Criterion.COST), 300, 60, 200, "А -> В -> Б");
    }

    /**
     * Тест 3: Путь не существует
     */
    private static void testNoPathExists() {
        System.out.println("\nТест 3: Путь не существует");

        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        // Только дорога А - В, нет связи с Б
        graph.addRoad(new Road(a, c, 100, 60, 200));

        DijkstraPathFinder finder = new DijkstraPathFinder(graph);
        Route route = finder.findPath(a, b, Criterion.DISTANCE);

        if (!route.exists()) {
            System.out.println("  ✓ Корректно: маршрут не найден");
            testsPassed++;
        } else {
            System.out.println("  ✗ Ошибка: должен был вернуть пустой маршрут");
            testsFailed++;
        }
    }

    /**
     * Тест 4: Начальный и конечный город совпадают
     */
    private static void testSingleCity() {
        System.out.println("\nТест 4: Начальный и конечный город совпадают");

        Graph graph = new Graph();
        City a = new City(1, "А");
        graph.addCity(a);

        DijkstraPathFinder finder = new DijkstraPathFinder(graph);
        Route route = finder.findPath(a, a, Criterion.DISTANCE);

        if (route.exists() && route.getTotalDistance() == 0) {
            System.out.println("  ✓ Корректно: путь из города в себя с нулевыми параметрами");
            testsPassed++;
        } else {
            System.out.println("  ✗ Ошибка: путь должен существовать с нулевыми параметрами");
            testsFailed++;
        }
    }

    /**
     * Тест 5: Сложный граф с множеством путей
     */
    private static void testComplexGraph() {
        System.out.println("\nТест 5: Сложный граф с множеством путей");

        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");
        City d = new City(4, "Г");
        City e = new City(5, "Д");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);
        graph.addCity(d);
        graph.addCity(e);

        // Несколько путей от А до Д:
        // Путь 1: А -> Б -> Д (длина: 200, время: 100, стоимость: 600)
        graph.addRoad(new Road(a, b, 100, 50, 300));
        graph.addRoad(new Road(b, d, 100, 50, 300));

        // Путь 2: А -> В -> Д (длина: 300, время: 60, стоимость: 200)
        graph.addRoad(new Road(a, c, 150, 30, 100));
        graph.addRoad(new Road(c, d, 150, 30, 100));

        // Путь 3: А -> Г -> Д (длина: 250, время: 80, стоимость: 150)
        graph.addRoad(new Road(a, e, 125, 40, 75));
        graph.addRoad(new Road(e, d, 125, 40, 75));

        DijkstraPathFinder finder = new DijkstraPathFinder(graph);
        Map<Criterion, Route> routes = finder.findAllOptimalPaths(a, d);

        // По длине: А -> Б -> Д (200)
        assertRoute(routes.get(Criterion.DISTANCE), 200, 100, 600, "А -> Б -> Г");

        // По времени: А -> В -> Д (60)
        assertRoute(routes.get(Criterion.TIME), 300, 60, 200, "А -> В -> Г");

        // По стоимости: А -> Г -> Д (150)
        assertRoute(routes.get(Criterion.COST), 250, 80, 150, "А -> Д -> Г");
    }

    /**
     * Тест 6: Граф с циклами
     */
    private static void testCyclicGraph() {
        System.out.println("\nТест 6: Граф с циклами");

        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        // Треугольник
        graph.addRoad(new Road(a, b, 100, 60, 200));
        graph.addRoad(new Road(b, c, 100, 60, 200));
        graph.addRoad(new Road(c, a, 100, 60, 200));

        DijkstraPathFinder finder = new DijkstraPathFinder(graph);
        Route route = finder.findPath(a, c, Criterion.DISTANCE);

        // Должен найти прямой путь А -> В (через ребро c-a), а не А -> Б -> В
        if (route.exists() && route.getTotalDistance() == 100) {
            System.out.println("  ✓ Корректно: найден прямой путь в цикле");
            testsPassed++;
        } else {
            System.out.println("  ✗ Ошибка: ожидалась длина 100, получено " + route.getTotalDistance());
            testsFailed++;
        }
    }

    /**
     * Вспомогательный метод для проверки параметров маршрута
     */
    private static void assertRoute(Route route, int expectedDist, int expectedTime, 
                                    int expectedCost, String description) {
        boolean passed = route.exists() 
                && route.getTotalDistance() == expectedDist
                && route.getTotalTime() == expectedTime
                && route.getTotalCost() == expectedCost;

        if (passed) {
            System.out.println("  ✓ " + description + ": Д=" + expectedDist + 
                    ", В=" + expectedTime + ", С=" + expectedCost);
            testsPassed++;
        } else {
            System.out.println("  ✗ " + description + ": ожидалось Д=" + expectedDist + 
                    ", В=" + expectedTime + ", С=" + expectedCost);
            System.out.println("    получено: " + route.getParamsString());
            testsFailed++;
        }
    }
}
