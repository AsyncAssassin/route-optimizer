package test;

import graph.Graph;
import parser.InputParser;
import solver.RouteSolver;
import solver.RouteSolver.SolutionResult;
import writer.OutputWriter;
import model.Criterion;
import model.Route;

import java.io.*;
import java.util.List;

/**
 * Интеграционные тесты.
 * Проверяют полный цикл работы программы от входного файла до результата.
 */
public class IntegrationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static final String TEST_INPUT = "test_integration_input.txt";
    private static final String TEST_OUTPUT = "test_integration_output.txt";

    public static void main(String[] args) {
        System.out.println("=== Интеграционные тесты ===\n");

        testExampleFromTask();
        testComplexScenario();
        testNoPathScenario();
        testOutputFormat();

        // Очистка
        new File(TEST_INPUT).delete();
        new File(TEST_OUTPUT).delete();

        System.out.println("\n=== Результаты ===");
        System.out.println("Пройдено: " + testsPassed);
        System.out.println("Провалено: " + testsFailed);
    }

    /**
     * Тест 1: Пример из условия задачи
     */
    private static void testExampleFromTask() {
        System.out.println("Тест 1: Пример из условия задачи");

        String input = "[CITIES]\n" +
                "1: Москва\n" +
                "2: Санкт-Петербург\n" +
                "3: Нижний Новгород\n" +
                "4: Казань\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 700, 480, 800\n" +
                "1 - 3: 400, 250, 300\n" +
                "2 - 3: 1100, 700, 1200\n" +
                "3 - 4: 350, 300, 500\n" +
                "1 - 4: 800, 600, 1000\n" +
                "\n" +
                "[REQUESTS]\n" +
                "Москва -> Санкт-Петербург | (Д,В,С)\n" +
                "Нижний Новгород -> Казань | (С,В,Д)\n";

        try {
            writeFile(TEST_INPUT, input);
            
            InputParser parser = new InputParser();
            parser.parse(TEST_INPUT);
            
            RouteSolver solver = new RouteSolver(parser.getGraph());
            List<SolutionResult> results = solver.solveAll(parser.getRequests());
            
            OutputWriter writer = new OutputWriter();
            writer.write(results, TEST_OUTPUT);

            // Проверяем первый запрос: Москва -> СПб
            SolutionResult result1 = results.get(0);
            Route distRoute = result1.getOptimalRoutes().get(Criterion.DISTANCE);

            // Прямой путь должен быть оптимален (700, 480, 800)
            if (distRoute.getTotalDistance() == 700 && 
                distRoute.getTotalTime() == 480 && 
                distRoute.getTotalCost() == 800) {
                System.out.println("  ✓ Запрос 1: Москва -> СПб корректен");
                testsPassed++;
            } else {
                System.out.println("  ✗ Запрос 1: неверные параметры маршрута");
                System.out.println("    Получено: " + distRoute.getParamsString());
                testsFailed++;
            }

            // Проверяем второй запрос: НН -> Казань
            SolutionResult result2 = results.get(1);
            Route costRoute = result2.getOptimalRoutes().get(Criterion.COST);

            // Прямой путь должен быть оптимален (350, 300, 500)
            if (costRoute.getTotalCost() == 500) {
                System.out.println("  ✓ Запрос 2: НН -> Казань корректен");
                testsPassed++;
            } else {
                System.out.println("  ✗ Запрос 2: неверные параметры маршрута");
                testsFailed++;
            }

        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            e.printStackTrace();
            testsFailed += 2;
        }
    }

    /**
     * Тест 2: Сложный сценарий с разными оптимальными путями
     */
    private static void testComplexScenario() {
        System.out.println("\nТест 2: Сложный сценарий с разными оптимальными путями");

        // Граф где каждый критерий даёт разный оптимальный путь
        String input = "[CITIES]\n" +
                "1: Старт\n" +
                "2: Финиш\n" +
                "3: Короткий\n" +
                "4: Быстрый\n" +
                "5: Дешёвый\n" +
                "\n" +
                "[ROADS]\n" +
                // Путь через "Короткий": Д=100, но В=200, С=500
                "1 - 3: 50, 100, 250\n" +
                "3 - 2: 50, 100, 250\n" +
                // Путь через "Быстрый": В=60, но Д=300, С=400
                "1 - 4: 150, 30, 200\n" +
                "4 - 2: 150, 30, 200\n" +
                // Путь через "Дешёвый": С=100, но Д=400, В=300
                "1 - 5: 200, 150, 50\n" +
                "5 - 2: 200, 150, 50\n" +
                "\n" +
                "[REQUESTS]\n" +
                "Старт -> Финиш | (Д,В,С)\n" +
                "Старт -> Финиш | (В,Д,С)\n" +
                "Старт -> Финиш | (С,Д,В)\n";

        try {
            writeFile(TEST_INPUT, input);
            
            InputParser parser = new InputParser();
            parser.parse(TEST_INPUT);
            
            RouteSolver solver = new RouteSolver(parser.getGraph());
            List<SolutionResult> results = solver.solveAll(parser.getRequests());

            // Запрос с приоритетом (Д,В,С) — должен выбрать короткий путь (Д=100)
            Route comp1 = results.get(0).getCompromiseRoute();
            if (comp1.getTotalDistance() == 100) {
                System.out.println("  ✓ Приоритет (Д,В,С): выбран кратчайший маршрут");
                testsPassed++;
            } else {
                System.out.println("  ✗ Приоритет (Д,В,С): ожидалась длина 100");
                testsFailed++;
            }

            // Запрос с приоритетом (В,Д,С) — должен выбрать быстрый путь (В=60)
            Route comp2 = results.get(1).getCompromiseRoute();
            if (comp2.getTotalTime() == 60) {
                System.out.println("  ✓ Приоритет (В,Д,С): выбран быстрый маршрут");
                testsPassed++;
            } else {
                System.out.println("  ✗ Приоритет (В,Д,С): ожидалось время 60");
                testsFailed++;
            }

            // Запрос с приоритетом (С,Д,В) — должен выбрать дешёвый путь (С=100)
            Route comp3 = results.get(2).getCompromiseRoute();
            if (comp3.getTotalCost() == 100) {
                System.out.println("  ✓ Приоритет (С,Д,В): выбран дешёвый маршрут");
                testsPassed++;
            } else {
                System.out.println("  ✗ Приоритет (С,Д,В): ожидалась стоимость 100");
                testsFailed++;
            }

        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed += 3;
        }
    }

    /**
     * Тест 3: Сценарий когда путь не существует
     */
    private static void testNoPathScenario() {
        System.out.println("\nТест 3: Путь не существует");

        String input = "[CITIES]\n" +
                "1: Остров1\n" +
                "2: Остров2\n" +
                "3: Материк\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 3: 100, 60, 200\n" +
                // Остров2 не связан с другими городами
                "\n" +
                "[REQUESTS]\n" +
                "Остров1 -> Остров2 | (Д,В,С)\n";

        try {
            writeFile(TEST_INPUT, input);
            
            InputParser parser = new InputParser();
            parser.parse(TEST_INPUT);
            
            RouteSolver solver = new RouteSolver(parser.getGraph());
            List<SolutionResult> results = solver.solveAll(parser.getRequests());

            Route route = results.get(0).getOptimalRoutes().get(Criterion.DISTANCE);

            if (!route.exists()) {
                System.out.println("  ✓ Корректно обработан несуществующий путь");
                testsPassed++;
            } else {
                System.out.println("  ✗ Должен был вернуть пустой маршрут");
                testsFailed++;
            }

        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Тест 4: Проверка формата выходного файла
     */
    private static void testOutputFormat() {
        System.out.println("\nТест 4: Проверка формата выходного файла");

        String input = "[CITIES]\n" +
                "1: А\n" +
                "2: Б\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 100, 60, 200\n" +
                "\n" +
                "[REQUESTS]\n" +
                "А -> Б | (Д,В,С)\n";

        try {
            writeFile(TEST_INPUT, input);
            
            InputParser parser = new InputParser();
            parser.parse(TEST_INPUT);
            
            RouteSolver solver = new RouteSolver(parser.getGraph());
            List<SolutionResult> results = solver.solveAll(parser.getRequests());
            
            OutputWriter writer = new OutputWriter();
            writer.write(results, TEST_OUTPUT);

            // Читаем выходной файл
            String output = readFile(TEST_OUTPUT);

            // Проверяем наличие всех обязательных элементов
            boolean hasLength = output.contains("ДЛИНА:");
            boolean hasTime = output.contains("ВРЕМЯ:");
            boolean hasCost = output.contains("СТОИМОСТЬ:");
            boolean hasCompromise = output.contains("КОМПРОМИСС:");
            boolean hasParams = output.contains("Д=100, В=60, С=200");

            if (hasLength && hasTime && hasCost && hasCompromise && hasParams) {
                System.out.println("  ✓ Формат выходного файла корректен");
                testsPassed++;
            } else {
                System.out.println("  ✗ Неверный формат выходного файла");
                System.out.println("  Содержимое:\n" + output);
                testsFailed++;
            }

        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void writeFile(String filename, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    private static String readFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
