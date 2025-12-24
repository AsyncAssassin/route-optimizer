package parser;

import graph.Graph;
import model.City;
import model.Criterion;
import model.Road;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Парсер входного файла с данными о дорожной сети и запросах.
 * Обрабатывает три секции: [CITIES], [ROADS], [REQUESTS].
 */
public class InputParser {

    /** Регулярное выражение для строки города: "ID: Название" */
    private static final Pattern CITY_PATTERN = Pattern.compile("(\\d+):\\s*(.+)");

    /** Регулярное выражение для строки дороги: "ID1 - ID2: длина, время, стоимость" */
    private static final Pattern ROAD_PATTERN = Pattern.compile("(\\d+)\\s*-\\s*(\\d+):\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)");

    /** Регулярное выражение для запроса: "Город1 -> Город2 | (П1,П2,П3)" */
    private static final Pattern REQUEST_PATTERN = Pattern.compile("(.+?)\\s*->\\s*(.+?)\\s*\\|\\s*\\(([ДВС]),([ДВС]),([ДВС])\\)");

    private Graph graph;
    private List<Request> requests;

    /**
     * Результат парсинга запроса на построение маршрута.
     */
    public static class Request {
        private final String fromCity;
        private final String toCity;
        private final List<Criterion> priorities;

        public Request(String fromCity, String toCity, List<Criterion> priorities) {
            this.fromCity = fromCity;
            this.toCity = toCity;
            this.priorities = priorities;
        }

        public String getFromCity() {
            return fromCity;
        }

        public String getToCity() {
            return toCity;
        }

        public List<Criterion> getPriorities() {
            return priorities;
        }

        @Override
        public String toString() {
            return fromCity + " -> " + toCity + " | " + priorities;
        }
    }

    /**
     * Парсит входной файл и строит граф дорожной сети.
     * 
     * @param filename путь к входному файлу
     * @throws IOException при ошибке чтения файла
     * @throws IllegalArgumentException при ошибке формата данных
     */
    public void parse(String filename) throws IOException {
        graph = new Graph();
        requests = new ArrayList<>();

        String currentSection = null;

        // Явно указываем кодировку UTF-8 для корректного чтения кириллицы
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Пропускаем пустые строки
                if (line.isEmpty()) {
                    continue;
                }

                // Определяем секцию
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1);
                    continue;
                }

                // Обрабатываем строку в зависимости от текущей секции
                try {
                    switch (currentSection) {
                        case "CITIES":
                            parseCity(line);
                            break;
                        case "ROADS":
                            parseRoad(line);
                            break;
                        case "REQUESTS":
                            parseRequest(line);
                            break;
                        default:
                            // Игнорируем неизвестные секции
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                            "Ошибка парсинга в строке " + lineNumber + ": " + line + "\n" + e.getMessage());
                }
            }
        }
    }

    /**
     * Парсит строку с информацией о городе.
     * Формат: "ID: Название_города"
     */
    private void parseCity(String line) {
        Matcher matcher = CITY_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат города: " + line);
        }

        int id = Integer.parseInt(matcher.group(1));
        String name = matcher.group(2).trim();

        City city = new City(id, name);
        graph.addCity(city);
    }

    /**
     * Парсит строку с информацией о дороге.
     * Формат: "ID1 - ID2: длина, время, стоимость"
     */
    private void parseRoad(String line) {
        Matcher matcher = ROAD_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат дороги: " + line);
        }

        int fromId = Integer.parseInt(matcher.group(1));
        int toId = Integer.parseInt(matcher.group(2));
        int distance = Integer.parseInt(matcher.group(3));
        int time = Integer.parseInt(matcher.group(4));
        int cost = Integer.parseInt(matcher.group(5));

        City from = graph.getCityById(fromId);
        City to = graph.getCityById(toId);

        if (from == null) {
            throw new IllegalArgumentException("Город с ID " + fromId + " не найден");
        }
        if (to == null) {
            throw new IllegalArgumentException("Город с ID " + toId + " не найден");
        }

        Road road = new Road(from, to, distance, time, cost);
        graph.addRoad(road);
    }

    /**
     * Парсит строку с запросом на построение маршрута.
     * Формат: "Город1 -> Город2 | (Д,В,С)"
     */
    private void parseRequest(String line) {
        Matcher matcher = REQUEST_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат запроса: " + line);
        }

        String fromCity = matcher.group(1).trim();
        String toCity = matcher.group(2).trim();

        // Парсим приоритеты
        List<Criterion> priorities = new ArrayList<>();
        priorities.add(Criterion.fromShortName(matcher.group(3)));
        priorities.add(Criterion.fromShortName(matcher.group(4)));
        priorities.add(Criterion.fromShortName(matcher.group(5)));

        // Проверяем существование городов
        if (!graph.hasCity(fromCity)) {
            throw new IllegalArgumentException("Город отправления не найден: " + fromCity);
        }
        if (!graph.hasCity(toCity)) {
            throw new IllegalArgumentException("Город назначения не найден: " + toCity);
        }

        requests.add(new Request(fromCity, toCity, priorities));
    }

    /**
     * Возвращает построенный граф дорожной сети.
     * 
     * @return граф
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Возвращает список запросов на построение маршрутов.
     * 
     * @return список запросов
     */
    public List<Request> getRequests() {
        return requests;
    }
}
