package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Представляет маршрут — последовательность городов с суммарными параметрами.
 * Хранит общую длину, время и стоимость пути.
 */
public class Route {
    private final List<City> cities;
    private final int totalDistance;
    private final int totalTime;
    private final int totalCost;

    /**
     * Создаёт маршрут из списка городов и суммарных параметров.
     * 
     * @param cities        список городов в порядке следования
     * @param totalDistance общая длина маршрута в километрах
     * @param totalTime     общее время в пути в минутах
     * @param totalCost     общая стоимость в рублях
     */
    public Route(List<City> cities, int totalDistance, int totalTime, int totalCost) {
        this.cities = new ArrayList<>(cities);
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.totalCost = totalCost;
    }

    /**
     * Создаёт пустой маршрут (путь не найден).
     */
    public static Route empty() {
        return new Route(Collections.emptyList(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Проверяет, существует ли маршрут.
     * 
     * @return true если маршрут найден, false если путь не существует
     */
    public boolean exists() {
        return !cities.isEmpty();
    }

    public List<City> getCities() {
        return Collections.unmodifiableList(cities);
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getTotalCost() {
        return totalCost;
    }

    /**
     * Возвращает значение суммарного параметра по заданному критерию.
     * 
     * @param criterion критерий оптимизации
     * @return значение соответствующего суммарного параметра
     */
    public int getValueByCriterion(Criterion criterion) {
        switch (criterion) {
            case DISTANCE: return totalDistance;
            case TIME: return totalTime;
            case COST: return totalCost;
            default: throw new IllegalArgumentException("Неизвестный критерий: " + criterion);
        }
    }

    /**
     * Формирует строку пути: Город1 -> Город2 -> ... -> ГородN
     * 
     * @return строковое представление пути
     */
    public String getPathString() {
        if (!exists()) {
            return "Маршрут не найден";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cities.size(); i++) {
            sb.append(cities.get(i).getName());
            if (i < cities.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

    /**
     * Формирует строку параметров: Д=..., В=..., С=...
     * 
     * @return строка с параметрами маршрута
     */
    public String getParamsString() {
        return String.format("Д=%d, В=%d, С=%d", totalDistance, totalTime, totalCost);
    }

    @Override
    public String toString() {
        return getPathString() + " | " + getParamsString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return cities.equals(route.cities);
    }

    @Override
    public int hashCode() {
        return cities.hashCode();
    }
}
