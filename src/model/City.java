package model;

import java.util.Objects;

/**
 * Представляет город в дорожной сети.
 * Город идентифицируется уникальным ID и имеет название.
 */
public class City {
    private final int id;
    private final String name;

    /**
     * Создаёт новый город.
     * 
     * @param id   уникальный идентификатор города
     * @param name название города
     */
    public City(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
