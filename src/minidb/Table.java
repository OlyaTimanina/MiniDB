package minidb;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class Table implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private List<Column> columns;
    private List<Row> rows;
    private Map<String, Map<Object, List<Row>>> indexes;
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();

        if (indexes == null) {
            indexes = new HashMap<>();
        }
    }

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
        this.indexes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Column getColumn(String columnName) {
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }

    public void addRow(Row row) {
        Map<String, Object> values = row.getValues();

        for (String columnName : values.keySet()) {
            if (getColumn(columnName) == null) {
                throw new IllegalArgumentException(
                        "Неизвестный столбец: " + columnName
                );
            }
        }

        for (Column column : columns) {
            Object value = values.get(column.getName());

            if (value == null) {
                if (!column.isNullable()) {
                    throw new IllegalArgumentException(
                            "Поле не может быть пустым: " + column.getName()
                    );
                }
                continue;
            }

            if (!isValidType(column.getType(), value)) {
                throw new IllegalArgumentException(
                        "Неверный тип данных для поля: " + column.getName()
                );
            }

            if (column.isPrimaryKey()) {
                for (Row existingRow : rows) {
                    if (value.equals(existingRow.getValue(column.getName()))) {
                        throw new IllegalArgumentException(
                                "Первичный ключ уже существует: " + value
                        );
                    }
                }
            }
        }

        rows.add(row);
        for (String indexName : indexes.keySet()) {
            Object value = row.getValue(indexName);

            indexes.get(indexName)
                    .computeIfAbsent(value, key -> new ArrayList<>())
                    .add(row);
        }
    }
    public void createIndex(String columnName) {
        Column column = getColumn(columnName);

        if (column == null) {
            throw new IllegalArgumentException(
                    "Столбец не найден: " + columnName
            );
        }

        String indexName = column.getName().toLowerCase();

        if (indexes.containsKey(indexName)) {
            throw new IllegalArgumentException(
                    "Индекс уже существует: " + columnName
            );
        }

        Map<Object, List<Row>> index = new HashMap<>();

        for (Row row : rows) {
            Object value = row.getValue(column.getName());

            index.computeIfAbsent(value, key -> new ArrayList<>()).add(row);
        }

        indexes.put(indexName, index);
    }
    public List<Row> findRowsByIndex(String columnName, Object value) {
        String indexName = columnName.toLowerCase();

        if (!indexes.containsKey(indexName)) {
            throw new IllegalArgumentException(
                    "Индекс не найден: " + columnName
            );
        }

        Map<Object, List<Row>> index = indexes.get(indexName);

        List<Row> result = index.get(value);

        if (result == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(result);
    }
    private boolean isValidType(DataType type, Object value) {
        switch (type) {
            case INTEGER:
                return value instanceof Integer;
            case DOUBLE:
                return value instanceof Double;
            case STRING:
                return value instanceof String;
            case DATE:
                return value instanceof LocalDate;
            case BOOLEAN:
                return value instanceof Boolean;
            default:
                return false;
        }
    }

    public void removeRow(Row row) {
        rows.remove(row);

        for (String indexName : indexes.keySet()) {
            Object value = row.getValue(indexName);

            List<Row> indexedRows = indexes.get(indexName).get(value);

            if (indexedRows != null) {
                indexedRows.remove(row);

                if (indexedRows.isEmpty()) {
                    indexes.get(indexName).remove(value);
                }
            }
        }
    }
}