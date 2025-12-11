package model;

/**
 * Критерии оптимизации маршрута.
 * Каждый критерий соответствует одному из параметров дороги.
 */
public enum Criterion {
    /** Длина маршрута в километрах */
    DISTANCE("Д", "ДЛИНА"),
    
    /** Время в пути в минутах */
    TIME("В", "ВРЕМЯ"),
    
    /** Стоимость проезда в рублях */
    COST("С", "СТОИМОСТЬ");

    private final String shortName;
    private final String fullName;

    Criterion(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Получает критерий по его короткому обозначению (Д, В, С).
     * 
     * @param shortName короткое обозначение критерия
     * @return соответствующий критерий
     * @throws IllegalArgumentException если обозначение не найдено
     */
    public static Criterion fromShortName(String shortName) {
        for (Criterion c : values()) {
            if (c.shortName.equals(shortName)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Неизвестный критерий: " + shortName);
    }
}
