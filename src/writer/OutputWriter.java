package writer;

import model.Criterion;
import model.Route;
import solver.RouteSolver.SolutionResult;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Записывает результаты решения в выходной файл.
 * Формат вывода соответствует требованиям задания.
 */
public class OutputWriter {

    /**
     * Записывает все результаты в файл.
     * 
     * @param results  список результатов решения
     * @param filename путь к выходному файлу
     * @throws IOException при ошибке записи
     */
    public void write(List<SolutionResult> results, String filename) throws IOException {
        // Явно указываем кодировку UTF-8 для корректного отображения кириллицы
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            for (int i = 0; i < results.size(); i++) {
                writeResult(writer, results.get(i));

                // Добавляем пустую строку между запросами (кроме последнего)
                if (i < results.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Записывает результат одного запроса.
     */
    private void writeResult(BufferedWriter writer, SolutionResult result) throws IOException {
        // Порядок вывода критериев: ДЛИНА, ВРЕМЯ, СТОИМОСТЬ
        Criterion[] outputOrder = {Criterion.DISTANCE, Criterion.TIME, Criterion.COST};

        // Выводим оптимальные маршруты по каждому критерию
        for (Criterion criterion : outputOrder) {
            Route route = result.getOptimalRoutes().get(criterion);
            writeLine(writer, criterion.getFullName(), route);
        }

        // Выводим компромиссный маршрут
        writeLine(writer, "КОМПРОМИСС", result.getCompromiseRoute());
    }

    /**
     * Записывает одну строку результата.
     * Формат: КРИТЕРИЙ: Город1 -> Город2 -> ... | Д=..., В=..., С=...
     */
    private void writeLine(BufferedWriter writer, String label, Route route) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(": ");

        if (route.exists()) {
            sb.append(route.getPathString());
            sb.append(" | ");
            sb.append(route.getParamsString());
        } else {
            sb.append("Маршрут не найден");
        }

        writer.write(sb.toString());
        writer.newLine();
    }
}
