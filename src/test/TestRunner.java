package test;

/**
 * Запуск всех тестов.
 * 
 * Компиляция и запуск:
 * javac -d out src/model/*.java src/graph/*.java src/parser/*.java src/solver/*.java src/writer/*.java src/test/*.java
 * java -cp out test.TestRunner
 * 
 * Для нагрузочного тестирования:
 * java -cp out test.LoadTest
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      ТЕСТИРОВАНИЕ СИСТЕМЫ ОПТИМИЗАЦИИ МАРШРУТОВ  ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        long startTime = System.currentTimeMillis();

        // Запуск тестов алгоритма Дейкстры
        System.out.println("────────────────────────────────────────────────────");
        DijkstraTest.main(args);

        System.out.println("\n────────────────────────────────────────────────────");
        // Запуск тестов выбора компромисса
        CompromiseTest.main(args);

        System.out.println("\n────────────────────────────────────────────────────");
        // Запуск тестов парсера
        ParserTest.main(args);

        System.out.println("\n────────────────────────────────────────────────────");
        // Запуск интеграционных тестов
        IntegrationTest.main(args);

        long endTime = System.currentTimeMillis();

        System.out.println("\n════════════════════════════════════════════════════");
        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
        System.out.println("════════════════════════════════════════════════════");
        
        System.out.println("\nДля запуска нагрузочного теста:");
        System.out.println("  java -cp out test.LoadTest");
        System.out.println("\nДля запуска теста производительности:");
        System.out.println("  java -cp out test.PerformanceTest");
    }
}
