import graph.Graph;
import parser.InputParser;
import solver.RouteSolver;
import solver.RouteSolver.SolutionResult;
import writer.OutputWriter;

import java.io.IOException;
import java.util.List;

/**
 * Точка входа программы оптимизации маршрутов.
 * 
 * Программа читает входные данные из файла input.txt,
 * находит оптимальные маршруты по трём критериям (длина, время, стоимость)
 * и записывает результаты в output.txt.
 * 
 */
public class Main {

    private static final String INPUT_FILE = "input.txt";
    private static final String OUTPUT_FILE = "output.txt";

    public static void main(String[] args) {
        try {
            // 1. Парсинг входных данных
            System.out.println("Чтение входных данных из " + INPUT_FILE + "...");
            InputParser parser = new InputParser();
            parser.parse(INPUT_FILE);

            Graph graph = parser.getGraph();
            List<InputParser.Request> requests = parser.getRequests();

            System.out.println("Загружено городов: " + graph.getCityCount());
            System.out.println("Загружено запросов: " + requests.size());

            // 2. Решение задачи
            System.out.println("Поиск оптимальных маршрутов...");
            RouteSolver solver = new RouteSolver(graph);
            List<SolutionResult> results = solver.solveAll(requests);

            // 3. Запись результатов
            System.out.println("Запись результатов в " + OUTPUT_FILE + "...");
            OutputWriter writer = new OutputWriter();
            writer.write(results, OUTPUT_FILE);

            System.out.println("Готово! Результаты сохранены в " + OUTPUT_FILE);

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка в данных: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
