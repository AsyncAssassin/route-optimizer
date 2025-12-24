package solver;

import graph.DijkstraPathFinder;
import graph.OptimizedDijkstraPathFinder;
import graph.Graph;
import model.City;
import model.Criterion;
import model.Route;
import parser.InputParser.Request;

import java.util.*;

/**
 * Решатель задачи оптимизации маршрутов.
 * Находит оптимальные маршруты по каждому критерию и выбирает компромиссный вариант.
 * 
 * ОПТИМИЗАЦИЯ: Использует OptimizedDijkstraPathFinder, который выполняет
 * поиск по всем критериям за один проход вместо трёх отдельных запусков.
 */
public class RouteSolver {

    private final Graph graph;
    private final OptimizedDijkstraPathFinder pathFinder;

    public RouteSolver(Graph graph) {
        this.graph = graph;
        this.pathFinder = new OptimizedDijkstraPathFinder(graph);
    }

    /**
     * Результат решения одного запроса.
     * Содержит три оптимальных маршрута и один компромиссный.
     */
    public static class SolutionResult {
        private final Request request;
        private final Map<Criterion, Route> optimalRoutes;
        private final Route compromiseRoute;

        public SolutionResult(Request request, Map<Criterion, Route> optimalRoutes, Route compromiseRoute) {
            this.request = request;
            this.optimalRoutes = optimalRoutes;
            this.compromiseRoute = compromiseRoute;
        }

        public Request getRequest() {
            return request;
        }

        public Map<Criterion, Route> getOptimalRoutes() {
            return optimalRoutes;
        }

        public Route getCompromiseRoute() {
            return compromiseRoute;
        }
    }

    /**
     * Решает запрос на построение маршрута.
     * 
     * @param request запрос с городами отправления/назначения и приоритетами
     * @return результат с оптимальными и компромиссным маршрутами
     */
    public SolutionResult solve(Request request) {
        City from = graph.getCityByName(request.getFromCity());
        City to = graph.getCityByName(request.getToCity());

        // Находим оптимальные маршруты по всем критериям
        Map<Criterion, Route> optimalRoutes = pathFinder.findAllOptimalPaths(from, to);

        // Выбираем компромиссный маршрут на основе приоритетов
        Route compromiseRoute = selectCompromise(optimalRoutes, request.getPriorities());

        return new SolutionResult(request, optimalRoutes, compromiseRoute);
    }

    /**
     * Выбирает компромиссный маршрут на основе заданных приоритетов.
     * 
     * Логика выбора:
     * 1. Собираем все уникальные маршруты из трёх оптимальных.
     * 2. Сортируем их по приоритетам: сначала по первому критерию,
     *    при равенстве — по второму, затем по третьему.
     * 3. Возвращаем лучший маршрут.
     * 
     * @param optimalRoutes маршруты, оптимальные по каждому критерию
     * @param priorities    список критериев в порядке убывания важности
     * @return компромиссный маршрут
     */
    private Route selectCompromise(Map<Criterion, Route> optimalRoutes, List<Criterion> priorities) {
        // Собираем уникальные маршруты
        Set<Route> uniqueRoutes = new HashSet<>(optimalRoutes.values());

        // Если все маршруты одинаковые — возвращаем любой
        if (uniqueRoutes.size() == 1) {
            return uniqueRoutes.iterator().next();
        }

        // Сортируем по приоритетам
        List<Route> sortedRoutes = new ArrayList<>(uniqueRoutes);
        sortedRoutes.sort((r1, r2) -> compareByPriorities(r1, r2, priorities));

        return sortedRoutes.get(0);
    }

    /**
     * Сравнивает два маршрута по списку приоритетов.
     * 
     * @param r1         первый маршрут
     * @param r2         второй маршрут
     * @param priorities критерии в порядке убывания важности
     * @return отрицательное число если r1 лучше, положительное если r2 лучше, 0 если равны
     */
    private int compareByPriorities(Route r1, Route r2, List<Criterion> priorities) {
        for (Criterion criterion : priorities) {
            int value1 = r1.getValueByCriterion(criterion);
            int value2 = r2.getValueByCriterion(criterion);

            if (value1 != value2) {
                return Integer.compare(value1, value2); // Меньше = лучше
            }
        }
        return 0; // Полностью равные маршруты
    }

    /**
     * Решает список запросов.
     * 
     * @param requests список запросов
     * @return список результатов
     */
    public List<SolutionResult> solveAll(List<Request> requests) {
        List<SolutionResult> results = new ArrayList<>();
        for (Request request : requests) {
            results.add(solve(request));
        }
        return results;
    }
}
