package graph;

import model.City;

/**
 * Узел для использования в приоритетной очереди алгоритма Дейкстры.
 * Хранит город и расстояние до него.
 * 
 * Вынесен в отдельный класс для устранения дублирования между
 * DijkstraPathFinder и OptimizedDijkstraPathFinder.
 */
public class DijkstraNode implements Comparable<DijkstraNode> {
    private final City city;
    private final int distance;

    public DijkstraNode(City city, int distance) {
        this.city = city;
        this.distance = distance;
    }

    public City getCity() {
        return city;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public int compareTo(DijkstraNode other) {
        return Integer.compare(this.distance, other.distance);
    }
}
