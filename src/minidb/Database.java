package minidb;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Database implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Table> tables;

    public Database() {
        tables = new LinkedHashMap<>();
    }

    public void createTable(Table table) {
        String name = table.getName().toLowerCase();

        if (tables.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Таблица уже существует: " + name
            );
        }

        tables.put(name, table);
    }

    public void dropTable(String tableName) {
        if (!tables.containsKey(tableName.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        tables.remove(tableName.toLowerCase());
    }

    public Table getTable(String tableName) {
        return tables.get(tableName.toLowerCase());
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public boolean hasTable(String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }
}