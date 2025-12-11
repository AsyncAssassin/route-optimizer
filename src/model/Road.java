package model;

/**
 * Представляет дорогу между двумя городами.
 * Дорога характеризуется тремя параметрами: длина, время, стоимость.
 * Все дороги двусторонние.
 */
public class Road {
    private final City from;
    private final City to;
    private final int distance;  // длина в километрах
    private final int time;      // время в минутах
    private final int cost;      // стоимость в рублях

    /**
     * Создаёт новую дорогу между городами.
     * 
     * @param from     начальный город
     * @param to       конечный город
     * @param distance длина дороги в километрах
     * @param time     время в пути в минутах
     * @param cost     стоимость проезда в рублях
     */
    public Road(City from, City to, int distance, int time, int cost) {
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.time = time;
        this.cost = cost;
    }

    public City getFrom() {
        return from;
    }

    public City getTo() {
        return to;
    }

    public int getDistance() {
        return distance;
    }

    public int getTime() {
        return time;
    }

    public int getCost() {
        return cost;
    }

    /**
     * Возвращает значение параметра дороги по заданному критерию.
     * 
     * @param criterion критерий оптимизации
     * @return значение соответствующего параметра
     */
    public int getValueByCriterion(Criterion criterion) {
        switch (criterion) {
            case DISTANCE: return distance;
            case TIME: return time;
            case COST: return cost;
            default: throw new IllegalArgumentException("Неизвестный критерий: " + criterion);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s: %d км, %d мин, %d руб", 
                from.getName(), to.getName(), distance, time, cost);
    }
}
