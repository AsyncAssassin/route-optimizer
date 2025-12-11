package test;

import graph.Graph;
import model.City;
import model.Criterion;
import model.Road;
import model.Route;
import parser.InputParser;
import solver.RouteSolver;
import solver.RouteSolver.SolutionResult;

import java.util.Arrays;
import java.util.List;

/**
 * Тесты для логики выбора компромиссного маршрута.
 * Проверяет корректность работы приоритетов.
 */
public class CompromiseTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== Тесты выбора компромисса ===\n");

        testAllRoutesSame();
        testPriorityDistance();
        testPriorityCost();
        testPriorityTime();
        testTieBreaker();
        testThreeWayTie();

        System.out.println("\n=== Результаты ===");
        System.out.println("Пройдено: " + testsPassed);
        System.out.println("Провалено: " + testsFailed);
    }

    /**
     * Тест 1: Все маршруты одинаковые
     */
    private static void testAllRoutesSame() {
        System.out.println("Тест 1: Все оптимальные маршруты совпадают");

        Graph graph = createSimpleGraph();
        RouteSolver solver = new RouteSolver(graph);

        List<Criterion> priorities = Arrays.asList(Criterion.DISTANCE, Criterion.TIME, Criterion.COST);
        InputParser.Request request = new InputParser.Request("А", "Б", priorities);

        SolutionResult result = solver.solve(request);
        Route compromise = result.getCompromiseRoute();

        // При совпадении всех маршрутов — любой из них корректен
        if (compromise.exists() && compromise.getTotalDistance() == 100) {
            System.out.println("  ✓ Компромисс выбран корректно");
            testsPassed++;
        } else {
            System.out.println("  ✗ Ошибка выбора компромисса");
            testsFailed++;
        }
    }

    /**
     * Тест 2: Приоритет длины (Д,В,С)
     */
    private static void testPriorityDistance() {
        System.out.println("\nТест 2: Приоритет длины (Д,В,С)");

        Graph graph = createConflictGraph();
        RouteSolver solver = new RouteSolver(graph);

        // Приоритет: длина > время > стоимость
        List<Criterion> priorities = Arrays.asList(Criterion.DISTANCE, Criterion.TIME, Criterion.COST);
        InputParser.Request request = new InputParser.Request("А", "Б", priorities);

        SolutionResult result = solver.solve(request);
        Route compromise = result.getCompromiseRoute();

        // Должен выбрать короткий путь А -> Б (Д=100)
        if (compromise.getTotalDistance() == 100) {
            System.out.println("  ✓ Выбран кратчайший маршрут: Д=" + compromise.getTotalDistance());
            testsPassed++;
        } else {
            System.out.println("  ✗ Ожидалась длина 100, получено: " + compromise.getTotalDistance());
            testsFailed++;
        }
    }

    /**
     * Тест 3: Приоритет стоимости (С,В,Д)
     */
    private static void testPriorityCost() {
        System.out.println("\nТест 3: Приоритет стоимости (С,В,Д)");

        Graph graph = createConflictGraph();
        RouteSolver solver = new RouteSolver(graph);

        // Приоритет: стоимость > время > длина
        List<Criterion> priorities = Arrays.asList(Criterion.COST, Criterion.TIME, Criterion.DISTANCE);
        InputParser.Request request = new InputParser.Request("А", "Б", priorities);

        SolutionResult result = solver.solve(request);
        Route compromise = result.getCompromiseRoute();

        // Должен выбрать дешёвый путь А -> В -> Б (С=200)
        if (compromise.getTotalCost() == 200) {
            System.out.println("  ✓ Выбран дешёвый маршрут: С=" + compromise.getTotalCost());
            testsPassed++;
        } else {
            System.out.println("  ✗ Ожидалась стоимость 200, получено: " + compromise.getTotalCost());
            testsFailed++;
        }
    }

    /**
     * Тест 4: Приоритет времени (В,Д,С)
     */
    private static void testPriorityTime() {
        System.out.println("\nТест 4: Приоритет времени (В,Д,С)");

        Graph graph = createTimeConflictGraph();
        RouteSolver solver = new RouteSolver(graph);

        // Приоритет: время > длина > стоимость
        List<Criterion> priorities = Arrays.asList(Criterion.TIME, Criterion.DISTANCE, Criterion.COST);
        InputParser.Request request = new InputParser.Request("А", "Б", priorities);

        SolutionResult result = solver.solve(request);
        Route compromise = result.getCompromiseRoute();

        // Должен выбрать быстрый путь
        if (compromise.getTotalTime() == 40) {
            System.out.println("  ✓ Выбран быстрый маршрут: В=" + compromise.getTotalTime());
            testsPassed++;
        } else {
            System.out.println("  ✗ Ожидалось время 40, получено: " + compromise.getTotalTime());
            testsFailed++;
        }
    }

    /**
     * Тест 5: Разрешение ничьей по второму приоритету
     */
    private static void testTieBreaker() {
        System.out.println("\nТест 5: Разрешение ничьей по второму приоритету");

        Graph graph = createTieGraph();
        RouteSolver solver = new RouteSolver(graph);

        // Два маршрута с одинаковой длиной, но разным временем
        // Приоритет: длина > время > стоимость
        List<Criterion> priorities = Arrays.asList(Criterion.DISTANCE, Criterion.TIME, Criterion.COST);
        InputParser.Request request = new InputParser.Request("А", "Б", priorities);

        SolutionResult result = solver.solve(request);
        Route compromise = result.getCompromiseRoute();

        // При равной длине должен выбрать по времени
        if (compromise.getTotalTime() == 40) {
            System.out.println("  ✓ Ничья по длине разрешена по времени: В=" + compromise.getTotalTime());
            testsPassed++;
        } else {
            System.out.println("  ✗ Ожидалось время 40, получено: " + compromise.getTotalTime());
            testsFailed++;
        }
    }

    /**
     * Тест 6: Три разных оптимальных маршрута
     */
    private static void testThreeWayTie() {
        System.out.println("\nТест 6: Три разных оптимальных маршрута");

        Graph graph = createThreeWayGraph();
        RouteSolver solver = new RouteSolver(graph);

        // Проверяем, что по каждому критерию свой оптимальный маршрут
        List<Criterion> priorities = Arrays.asList(Criterion.COST, Criterion.DISTANCE, Criterion.TIME);
        InputParser.Request request = new InputParser.Request("А", "Г", priorities);

        SolutionResult result = solver.solve(request);
        
        Route distRoute = result.getOptimalRoutes().get(Criterion.DISTANCE);
        Route timeRoute = result.getOptimalRoutes().get(Criterion.TIME);
        Route costRoute = result.getOptimalRoutes().get(Criterion.COST);
        Route compromise = result.getCompromiseRoute();

        System.out.println("  Маршрут по длине: " + distRoute.getPathString() + " | " + distRoute.getParamsString());
        System.out.println("  Маршрут по времени: " + timeRoute.getPathString() + " | " + timeRoute.getParamsString());
        System.out.println("  Маршрут по стоимости: " + costRoute.getPathString() + " | " + costRoute.getParamsString());
        System.out.println("  Компромисс (С,Д,В): " + compromise.getPathString() + " | " + compromise.getParamsString());

        // При приоритете (С,Д,В) должен выбрать самый дешёвый
        if (compromise.getTotalCost() == costRoute.getTotalCost()) {
            System.out.println("  ✓ Компромисс совпадает с оптимальным по стоимости");
            testsPassed++;
        } else {
            System.out.println("  ✗ Компромисс должен совпадать с оптимальным по стоимости");
            testsFailed++;
        }
    }

    // === Вспомогательные методы создания графов ===

    /**
     * Простой граф: прямой путь оптимален по всем критериям
     */
    private static Graph createSimpleGraph() {
        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        graph.addRoad(new Road(a, b, 100, 60, 200));
        graph.addRoad(new Road(a, c, 200, 100, 400));
        graph.addRoad(new Road(c, b, 200, 100, 400));

        return graph;
    }

    /**
     * Граф с конфликтом: прямой путь короткий но дорогой
     */
    private static Graph createConflictGraph() {
        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        // Прямой путь: короткий, но дорогой
        graph.addRoad(new Road(a, b, 100, 60, 500));
        // Обходной: длинный, но дешёвый
        graph.addRoad(new Road(a, c, 150, 30, 100));
        graph.addRoad(new Road(c, b, 150, 30, 100));

        return graph;
    }

    /**
     * Граф с приоритетом времени
     */
    private static Graph createTimeConflictGraph() {
        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);

        // Путь 1: быстрый, но длинный и дорогой
        graph.addRoad(new Road(a, c, 200, 20, 400));
        graph.addRoad(new Road(c, b, 200, 20, 400));
        // Путь 2: медленный, но короткий и дешёвый
        graph.addRoad(new Road(a, b, 100, 100, 100));

        return graph;
    }

    /**
     * Граф для теста ничьей (одинаковая длина, разное время)
     */
    private static Graph createTieGraph() {
        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");
        City d = new City(4, "Г");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);
        graph.addCity(d);

        // Путь 1: А -> В -> Б (Д=200, В=40)
        graph.addRoad(new Road(a, c, 100, 20, 100));
        graph.addRoad(new Road(c, b, 100, 20, 100));
        // Путь 2: А -> Г -> Б (Д=200, В=80)
        graph.addRoad(new Road(a, d, 100, 40, 100));
        graph.addRoad(new Road(d, b, 100, 40, 100));

        return graph;
    }

    /**
     * Граф с тремя разными оптимальными маршрутами
     */
    private static Graph createThreeWayGraph() {
        Graph graph = new Graph();
        City a = new City(1, "А");
        City b = new City(2, "Б");
        City c = new City(3, "В");
        City d = new City(4, "Г");

        graph.addCity(a);
        graph.addCity(b);
        graph.addCity(c);
        graph.addCity(d);

        // Путь 1: А -> Б -> Г — короткий (Д=150), медленный (В=100), дорогой (С=400)
        graph.addRoad(new Road(a, b, 50, 50, 200));
        graph.addRoad(new Road(b, d, 100, 50, 200));

        // Путь 2: А -> В -> Г — средний (Д=200), быстрый (В=40), средний (С=300)
        graph.addRoad(new Road(a, c, 100, 20, 150));
        graph.addRoad(new Road(c, d, 100, 20, 150));

        // Путь 3: А -> Г — длинный (Д=300), медленный (В=120), дешёвый (С=100)
        graph.addRoad(new Road(a, d, 300, 120, 100));

        return graph;
    }
}
