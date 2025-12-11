package test;

import graph.Graph;
import parser.InputParser;
import parser.InputParser.Request;
import model.Criterion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Тесты для парсера входных данных.
 * Проверяет корректность обработки файла input.txt.
 */
public class ParserTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static final String TEST_FILE = "test_input.txt";

    public static void main(String[] args) {
        System.out.println("=== Тесты парсера ===\n");

        testValidInput();
        testCitiesWithSpaces();
        testMultipleRequests();
        testDifferentPriorities();
        testInvalidCityFormat();
        testInvalidRoadFormat();
        testNonexistentCity();

        // Удаляем тестовый файл
        new java.io.File(TEST_FILE).delete();

        System.out.println("\n=== Результаты ===");
        System.out.println("Пройдено: " + testsPassed);
        System.out.println("Провалено: " + testsFailed);
    }

    /**
     * Тест 1: Корректный входной файл
     */
    private static void testValidInput() {
        System.out.println("Тест 1: Корректный входной файл");

        String content = "[CITIES]\n" +
                "1: Москва\n" +
                "2: Санкт-Петербург\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 700, 480, 800\n" +
                "\n" +
                "[REQUESTS]\n" +
                "Москва -> Санкт-Петербург | (Д,В,С)\n";

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            Graph graph = parser.getGraph();
            List<Request> requests = parser.getRequests();

            boolean citiesOk = graph.getCityCount() == 2;
            boolean requestsOk = requests.size() == 1;
            boolean cityNamesOk = graph.hasCity("Москва") && graph.hasCity("Санкт-Петербург");

            if (citiesOk && requestsOk && cityNamesOk) {
                System.out.println("  ✓ Файл распарсен корректно");
                testsPassed++;
            } else {
                System.out.println("  ✗ Ошибка парсинга");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Тест 2: Города с пробелами в названиях
     */
    private static void testCitiesWithSpaces() {
        System.out.println("\nТест 2: Города с пробелами в названиях");

        String content = "[CITIES]\n" +
                "1: Нижний Новгород\n" +
                "2: Ростов-на-Дону\n" +
                "3: Санкт-Петербург\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 100, 60, 200\n" +
                "2 - 3: 100, 60, 200\n" +
                "\n" +
                "[REQUESTS]\n" +
                "Нижний Новгород -> Санкт-Петербург | (В,Д,С)\n";

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            Graph graph = parser.getGraph();

            if (graph.hasCity("Нижний Новгород") && graph.hasCity("Ростов-на-Дону")) {
                System.out.println("  ✓ Города с пробелами распознаны");
                testsPassed++;
            } else {
                System.out.println("  ✗ Города с пробелами не найдены");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Тест 3: Несколько запросов
     */
    private static void testMultipleRequests() {
        System.out.println("\nТест 3: Несколько запросов");

        String content = "[CITIES]\n" +
                "1: А\n" +
                "2: Б\n" +
                "3: В\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 100, 60, 200\n" +
                "2 - 3: 100, 60, 200\n" +
                "1 - 3: 150, 90, 300\n" +
                "\n" +
                "[REQUESTS]\n" +
                "А -> Б | (Д,В,С)\n" +
                "Б -> В | (С,Д,В)\n" +
                "А -> В | (В,С,Д)\n";

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            List<Request> requests = parser.getRequests();

            if (requests.size() == 3) {
                System.out.println("  ✓ Три запроса распознаны");
                testsPassed++;
            } else {
                System.out.println("  ✗ Ожидалось 3 запроса, получено: " + requests.size());
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Тест 4: Разные комбинации приоритетов
     */
    private static void testDifferentPriorities() {
        System.out.println("\nТест 4: Разные комбинации приоритетов");

        String content = "[CITIES]\n" +
                "1: А\n" +
                "2: Б\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 100, 60, 200\n" +
                "\n" +
                "[REQUESTS]\n" +
                "А -> Б | (С,В,Д)\n";

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            Request request = parser.getRequests().get(0);
            List<Criterion> priorities = request.getPriorities();

            boolean correctOrder = priorities.get(0) == Criterion.COST
                    && priorities.get(1) == Criterion.TIME
                    && priorities.get(2) == Criterion.DISTANCE;

            if (correctOrder) {
                System.out.println("  ✓ Приоритеты (С,В,Д) распознаны корректно");
                testsPassed++;
            } else {
                System.out.println("  ✗ Неверный порядок приоритетов");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ Исключение: " + e.getMessage());
            testsFailed++;
        }
    }

    /**
     * Тест 5: Некорректный формат города
     */
    private static void testInvalidCityFormat() {
        System.out.println("\nТест 5: Некорректный формат города");

        String content = "[CITIES]\n" +
                "Москва\n" +  // Нет ID
                "\n" +
                "[ROADS]\n" +
                "\n" +
                "[REQUESTS]\n";

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            System.out.println("  ✗ Должно было выброситься исключение");
            testsFailed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Корректно выброшено исключение: " + e.getMessage());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("  ✗ Неверный тип исключения: " + e.getClass().getSimpleName());
            testsFailed++;
        }
    }

    /**
     * Тест 6: Некорректный формат дороги
     */
    private static void testInvalidRoadFormat() {
        System.out.println("\nТест 6: Некорректный формат дороги");

        String content = "[CITIES]\n" +
                "1: А\n" +
                "2: Б\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 100, 60\n" +  // Не хватает стоимости
                "\n" +
                "[REQUESTS]\n";

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            System.out.println("  ✗ Должно было выброситься исключение");
            testsFailed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Корректно выброшено исключение: " + e.getMessage());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("  ✗ Неверный тип исключения: " + e.getClass().getSimpleName());
            testsFailed++;
        }
    }

    /**
     * Тест 7: Несуществующий город в запросе
     */
    private static void testNonexistentCity() {
        System.out.println("\nТест 7: Несуществующий город в запросе");

        String content = "[CITIES]\n" +
                "1: А\n" +
                "2: Б\n" +
                "\n" +
                "[ROADS]\n" +
                "1 - 2: 100, 60, 200\n" +
                "\n" +
                "[REQUESTS]\n" +
                "А -> В | (Д,В,С)\n";  // Город В не существует

        try {
            writeTestFile(content);
            InputParser parser = new InputParser();
            parser.parse(TEST_FILE);

            System.out.println("  ✗ Должно было выброситься исключение");
            testsFailed++;
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Корректно выброшено исключение: " + e.getMessage());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("  ✗ Неверный тип исключения: " + e.getClass().getSimpleName());
            testsFailed++;
        }
    }

    /**
     * Записывает содержимое в тестовый файл
     */
    private static void writeTestFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(TEST_FILE)) {
            writer.write(content);
        }
    }
}
