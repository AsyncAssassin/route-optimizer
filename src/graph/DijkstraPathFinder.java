package graph;

import model.City;
import model.Criterion;
import model.Road;
import model.Route;

import java.util.*;

/**
 * Реализация алгоритма Дейкстры для поиска кратчайшего пути.
 * Использует приоритетную очередь (min-heap) для эффективного выбора следующей вершины.
 * 
 * Временная сложность: O((V + E) · log V)
 * - V раз извлекаем минимум из очереди: O(V · log V)
 * - E раз обновляем расстояния и добавляем в очередь: O(E · log V)
 * 
 * Пространственная сложность: O(V) для хранения расстояний и предшественников.
 */
public class DijkstraPathFinder {

    private final Graph graph;

    public DijkstraPathFinder(Graph graph) {
        this.graph = graph;
    }

    /**
     * Находит оптимальный маршрут между двумя городами по заданному критерию.
     * 
     * @param from      начальный город
     * @param to        конечный город
     * @param criterion критерий оптимизации (длина, время или стоимость)
     * @return оптимальный маршрут или пустой маршрут, если путь не существует
     */
    public Route findPath(City from, City to, Criterion criterion) {
        // Расстояния от начальной вершины до всех остальных
        Map<City, Integer> distances = new HashMap<>();
        
        // Предшественники для восстановления пути
        Map<City, City> predecessors = new HashMap<>();
        
        // Дороги, по которым пришли (для подсчёта всех параметров)
        Map<City, Road> usedRoads = new HashMap<>();
        
        // Множество посещённых вершин
        Set<City> visited = new HashSet<>();
        
        // Приоритетная очередь (min-heap)
        PriorityQueue<DijkstraNode> queue = new PriorityQueue<>();

        // Инициализация: расстояние до начальной вершины = 0
        for (City city : graph.getAllCities()) {
            distances.put(city, Integer.MAX_VALUE);
        }
        distances.put(from, 0);
        queue.add(new DijkstraNode(from, 0));

        // Основной цикл алгоритма Дейкстры
        while (!queue.isEmpty()) {
            DijkstraNode current = queue.poll();
            City currentCity = current.getCity();

            // Пропускаем уже обработанные вершины
            if (visited.contains(currentCity)) {
                continue;
            }
            visited.add(currentCity);

            // Достигли целевой вершины — можно завершить
            if (currentCity.equals(to)) {
                break;
            }

            // Релаксация рёбер
            for (Road road : graph.getRoadsFrom(currentCity)) {
                City neighbor = road.getTo();
                
                if (visited.contains(neighbor)) {
                    continue;
                }

                // Вес ребра по выбранному критерию
                int edgeWeight = road.getValueByCriterion(criterion);
                int newDistance = distances.get(currentCity) + edgeWeight;

                // Обновляем расстояние, если нашли более короткий путь
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    predecessors.put(neighbor, currentCity);
                    usedRoads.put(neighbor, road);
                    queue.add(new DijkstraNode(neighbor, newDistance));
                }
            }
        }

        // Путь не найден
        if (!visited.contains(to) || distances.get(to) == Integer.MAX_VALUE) {
            return Route.empty();
        }

        // Восстанавливаем путь от конца к началу
        return reconstructRoute(from, to, predecessors, usedRoads);
    }

    /**
     * Восстанавливает маршрут по карте предшественников.
     * Вычисляет суммарные параметры по всем трём критериям.
     */
    private Route reconstructRoute(City from, City to, 
                                   Map<City, City> predecessors, 
                                   Map<City, Road> usedRoads) {
        List<City> path = new ArrayList<>();
        int totalDistance = 0;
        int totalTime = 0;
        int totalCost = 0;

        // Идём от конца к началу
        City current = to;
        while (current != null) {
            path.add(current);
            
            Road road = usedRoads.get(current);
            if (road != null) {
                totalDistance += road.getDistance();
                totalTime += road.getTime();
                totalCost += road.getCost();
            }
            
            current = predecessors.get(current);
        }

        // Переворачиваем путь (был от конца к началу)
        Collections.reverse(path);

        return new Route(path, totalDistance, totalTime, totalCost);
    }

    /**
     * Находит оптимальные маршруты по всем трём критериям.
     * 
     * @param from начальный город
     * @param to   конечный город
     * @return карта: критерий -> оптимальный маршрут
     */
    public Map<Criterion, Route> findAllOptimalPaths(City from, City to) {
        Map<Criterion, Route> results = new EnumMap<>(Criterion.class);
        
        for (Criterion criterion : Criterion.values()) {
            Route route = findPath(from, to, criterion);
            results.put(criterion, route);
        }
        
        return results;
    }
}
