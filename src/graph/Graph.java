package graph;

import model.City;
import model.Road;

import java.util.*;

/**
 * Граф дорожной сети.
 * Реализован на основе списка смежности (adjacency list).
 * 
 * Сложность по памяти: O(V + E), где V — количество городов, E — количество дорог.
 */
public class Graph {
    /** Список смежности: для каждого города хранится список исходящих дорог */
    private final Map<City, List<Road>> adjacencyList;
    
    /** Быстрый доступ к городу по названию */
    private final Map<String, City> citiesByName;
    
    /** Быстрый доступ к городу по ID */
    private final Map<Integer, City> citiesById;

    public Graph() {
        this.adjacencyList = new HashMap<>();
        this.citiesByName = new HashMap<>();
        this.citiesById = new HashMap<>();
    }

    /**
     * Добавляет город в граф.
     * Сложность: O(1) в среднем.
     * 
     * @param city город для добавления
     */
    public void addCity(City city) {
        adjacencyList.putIfAbsent(city, new ArrayList<>());
        citiesByName.put(city.getName(), city);
        citiesById.put(city.getId(), city);
    }

    /**
     * Добавляет двустороннюю дорогу между городами.
     * Сложность: O(1).
     * 
     * @param road дорога для добавления
     */
    public void addRoad(Road road) {
        // Добавляем дорогу в обоих направлениях (граф неориентированный)
        adjacencyList.get(road.getFrom()).add(road);
        
        // Создаём обратную дорогу с теми же параметрами
        Road reverseRoad = new Road(road.getTo(), road.getFrom(), 
                road.getDistance(), road.getTime(), road.getCost());
        adjacencyList.get(road.getTo()).add(reverseRoad);
    }

    /**
     * Возвращает список дорог, исходящих из указанного города.
     * Сложность: O(1).
     * 
     * @param city город-источник
     * @return список дорог из этого города
     */
    public List<Road> getRoadsFrom(City city) {
        return adjacencyList.getOrDefault(city, Collections.emptyList());
    }

    /**
     * Находит город по названию.
     * Сложность: O(1) в среднем.
     * 
     * @param name название города
     * @return город или null, если не найден
     */
    public City getCityByName(String name) {
        return citiesByName.get(name);
    }

    /**
     * Находит город по ID.
     * Сложность: O(1) в среднем.
     * 
     * @param id идентификатор города
     * @return город или null, если не найден
     */
    public City getCityById(int id) {
        return citiesById.get(id);
    }

    /**
     * Возвращает все города графа.
     * 
     * @return коллекция всех городов
     */
    public Collection<City> getAllCities() {
        return Collections.unmodifiableCollection(citiesByName.values());
    }

    /**
     * Возвращает количество городов в графе.
     * 
     * @return число вершин
     */
    public int getCityCount() {
        return citiesByName.size();
    }

    /**
     * Проверяет, существует ли город с указанным названием.
     * 
     * @param name название города
     * @return true если город существует
     */
    public boolean hasCity(String name) {
        return citiesByName.containsKey(name);
    }
}
