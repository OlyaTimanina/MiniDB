package minidb;

import java.util.ArrayList;
import java.util.List;

public class SQLExecutor {

    public QueryResult executeSelect(Database database, String tableName) {
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        List<String> columnNames = new ArrayList<>();

        for (Column column : table.getColumns()) {
            columnNames.add(column.getName());
        }

        QueryResult result = new QueryResult(columnNames);

        for (Row row : table.getRows()) {
            List<Object> values = new ArrayList<>();

            for (Column column : table.getColumns()) {
                values.add(row.getValue(column.getName()));
            }

            result.addRow(values);
        }

        return result;
    }
    public void executeInsert(Database database, String tableName, Row row) {
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        for (Column column : table.getColumns()) {
            String referencedTableName = column.getReferencedTable();
            String referencedColumnName = column.getReferencedColumn();

            if (referencedTableName == null || referencedColumnName == null) {
                continue;
            }

            Object value = row.getValue(column.getName());

            if (value == null) {
                continue;
            }

            Table referencedTable = database.getTable(referencedTableName);

            if (referencedTable == null) {
                throw new IllegalArgumentException(
                        "Таблица для внешнего ключа не найдена: " + referencedTableName
                );
            }

            if (referencedTable.getColumn(referencedColumnName) == null) {
                throw new IllegalArgumentException(
                        "Столбец для внешнего ключа не найден: " + referencedColumnName
                );
            }

            boolean referenceExists = false;

            for (Row existingRow : referencedTable.getRows()) {
                if (value.equals(existingRow.getValue(referencedColumnName))) {
                    referenceExists = true;
                    break;
                }
            }

            if (!referenceExists) {
                throw new IllegalArgumentException(
                        "Нарушение внешнего ключа: значение " + value + " не найдено"
                );
            }
        }

        table.addRow(row);
    }
    public void executeUpdate(Database database, String tableName,
                              String columnName, Object newValue,
                              String conditionColumn, Object conditionValue) {
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        Column column = table.getColumn(columnName);
        Column condition = table.getColumn(conditionColumn);

        if (column == null || condition == null) {
            throw new IllegalArgumentException(
                    "Неизвестный столбец"
            );
        }

        for (Row row : new ArrayList<>(table.getRows())) {
            if (conditionValue.equals(row.getValue(conditionColumn))) {
                Row updatedRow = new Row();

                for (Column currentColumn : table.getColumns()) {
                    Object value = currentColumn.getName().equalsIgnoreCase(columnName)
                            ? newValue
                            : row.getValue(currentColumn.getName());

                    updatedRow.setValue(currentColumn.getName(), value);
                }

                table.removeRow(row);
                table.addRow(updatedRow);
            }
        }
    }
    public void executeDelete(Database database, String tableName,
                              String conditionColumn, Object conditionValue) {
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        Column condition = table.getColumn(conditionColumn);

        if (condition == null) {
            throw new IllegalArgumentException(
                    "Неизвестный столбец: " + conditionColumn
            );
        }

        List<Row> rowsToDelete = new ArrayList<>();

        for (Row row : table.getRows()) {
            if (conditionValue.equals(row.getValue(conditionColumn))) {
                rowsToDelete.add(row);
            }
        }

        for (Row row : rowsToDelete) {
            table.removeRow(row);
        }
    }
    public QueryResult executeSelectQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.isValidCommand(query)) {
            throw new IllegalArgumentException("Некорректная команда");
        }

        if (!parser.getCommandType(query).equals("SELECT")) {
            throw new IllegalArgumentException("Ожидалась команда SELECT");
        }

        String tableName = parser.getTableName(query);

        if (tableName == null || !parser.tableExists(database, query)) {
            throw new IllegalArgumentException("Таблица не найдена");
        }

        if (query.toUpperCase().contains("WHERE")) {
            String conditionColumn = parser.getConditionColumn(query);
            String conditionValue = parser.getConditionValue(query);

            Table table = database.getTable(tableName);
            Column column = table.getColumn(conditionColumn);

            if (column == null) {
                throw new IllegalArgumentException(
                        "Столбец не найден: " + conditionColumn
                );
            }

            Object value = parseValue(conditionValue, column.getType());

            return executeSelectWhere(
                    database,
                    tableName,
                    conditionColumn,
                    value
            );
        }

        return executeSelect(database, tableName);
    }
    public QueryResult executeSelectWhere(
            Database database,
            String tableName,
            String conditionColumn,
            Object conditionValue
    ) {
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        Column column = table.getColumn(conditionColumn);

        if (column == null) {
            throw new IllegalArgumentException(
                    "Столбец не найден: " + conditionColumn
            );
        }

        List<Row> matchingRows;

        try {
            matchingRows = table.findRowsByIndex(
                    conditionColumn,
                    conditionValue
            );
        } catch (IllegalArgumentException e) {
            matchingRows = new ArrayList<>();

            for (Row row : table.getRows()) {
                if (conditionValue.equals(
                        row.getValue(column.getName())
                )) {
                    matchingRows.add(row);
                }
            }
        }

        List<String> columnNames = new ArrayList<>();

        for (Column tableColumn : table.getColumns()) {
            columnNames.add(tableColumn.getName());
        }

        QueryResult result = new QueryResult(columnNames);

        for (Row row : matchingRows) {
            List<Object> values = new ArrayList<>();

            for (Column tableColumn : table.getColumns()) {
                values.add(row.getValue(tableColumn.getName()));
            }

            result.addRow(values);
        }

        return result;
    }
    private Object parseValue(String value, DataType type) {
        value = value.trim();

        switch (type) {
            case INTEGER:
                return Integer.parseInt(value);

            case DOUBLE:
                return Double.parseDouble(value);

            case STRING:
                return value.substring(1, value.length() - 1);

            case DATE:
                return java.time.LocalDate.parse(
                        value.substring(1, value.length() - 1)
                );

            case BOOLEAN:
                return Boolean.parseBoolean(value);

            default:
                throw new IllegalArgumentException("Неизвестный тип данных");
        }
    }
    public void executeInsertQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.isValidCommand(query)) {
            throw new IllegalArgumentException("Некорректная команда");
        }

        if (!parser.getCommandType(query).equals("INSERT")) {
            throw new IllegalArgumentException("Ожидалась команда INSERT");
        }

        String tableName = parser.getTableName(query);
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException("Таблица не найдена");
        }

        String[] columnNames = parser.getInsertColumns(query);
        String[] values = parser.getInsertValues(query);

        if (columnNames.length != values.length) {
            throw new IllegalArgumentException("Количество столбцов и значений не совпадает");
        }

        Row row = new Row();

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i].trim();
            Column column = table.getColumn(columnName);

            if (column == null) {
                throw new IllegalArgumentException("Неизвестный столбец: " + columnName);
            }

            Object value = parseValue(values[i], column.getType());
            row.setValue(columnName, value);
        }

        executeInsert(database, tableName, row);
    }
    public void executeUpdateQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.isValidCommand(query)) {
            throw new IllegalArgumentException("Некорректная команда");
        }

        if (!parser.getCommandType(query).equals("UPDATE")) {
            throw new IllegalArgumentException("Ожидалась команда UPDATE");
        }

        String tableName = parser.getTableName(query);
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException("Таблица не найдена");
        }

        String columnName = parser.getUpdateColumn(query);
        String newValueText = parser.getUpdateValue(query);
        String conditionColumn = parser.getConditionColumn(query);
        String conditionValueText = parser.getConditionValue(query);

        Column column = table.getColumn(columnName);
        Column condition = table.getColumn(conditionColumn);

        if (column == null || condition == null) {
            throw new IllegalArgumentException("Неизвестный столбец");
        }

        Object newValue = parseValue(newValueText, column.getType());
        Object conditionValue = parseValue(conditionValueText, condition.getType());

        executeUpdate(
                database,
                tableName,
                columnName,
                newValue,
                conditionColumn,
                conditionValue
        );
    }
    public void executeDeleteQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.isValidCommand(query)) {
            throw new IllegalArgumentException("Некорректная команда");
        }

        if (!parser.getCommandType(query).equals("DELETE")) {
            throw new IllegalArgumentException("Ожидалась команда DELETE");
        }

        String tableName = parser.getTableName(query);
        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException("Таблица не найдена");
        }

        String conditionColumn = parser.getDeleteConditionColumn(query);
        String conditionValueText = parser.getDeleteConditionValue(query);

        Column condition = table.getColumn(conditionColumn);

        if (condition == null) {
            throw new IllegalArgumentException("Неизвестный столбец");
        }

        Object conditionValue = parseValue(
                conditionValueText,
                condition.getType()
        );

        executeDelete(
                database,
                tableName,
                conditionColumn,
                conditionValue
        );
    }
    public void executeCreateTable(Database database, String tableName, List<Column> columns) {
        Table table = new Table(tableName, columns);
        database.createTable(table);
    }
    public void executeDropTable(Database database, String tableName) {
        database.dropTable(tableName);
    }
    public void executeCreateTableQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.isCreateTableCommand(query)) {
            throw new IllegalArgumentException("Ожидалась команда CREATE TABLE");
        }

        String tableName = parser.getTableName(query);
        String columnsText = parser.getCreateTableColumns(query);

        List<Column> columns = parser.parseColumns(columnsText);

        executeCreateTable(database, tableName, columns);
    }
    public void executeCreateIndexQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.isCreateIndexCommand(query)) {
            throw new IllegalArgumentException(
                    "Некорректная команда CREATE INDEX"
            );
        }

        String tableName = parser.getCreateIndexTable(query);
        String columnName = parser.getCreateIndexColumn(query);

        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        table.createIndex(columnName);
    }
    public void executeDropTableQuery(Database database, String query) {
        SQLParser parser = new SQLParser();

        if (!parser.getCommandType(query).equals("DROP")) {
            throw new IllegalArgumentException("Ожидалась команда DROP");
        }

        String tableName = parser.getTableName(query);

        if (tableName == null || !database.hasTable(tableName)) {
            throw new IllegalArgumentException("Таблица не найдена");
        }

        executeDropTable(database, tableName);
    }
}