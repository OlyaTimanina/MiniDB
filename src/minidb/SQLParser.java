package minidb;

import java.util.ArrayList;
import java.util.List;

public class SQLParser {

    public String getCommandType(String query) {
        String normalizedQuery = query.trim().toUpperCase();

        if (normalizedQuery.startsWith("SELECT")) {
            return "SELECT";
        }

        if (normalizedQuery.startsWith("INSERT")) {
            return "INSERT";
        }

        if (normalizedQuery.startsWith("UPDATE")) {
            return "UPDATE";
        }

        if (normalizedQuery.startsWith("DELETE")) {
            return "DELETE";
        }

        if (normalizedQuery.startsWith("CREATE")) {
            return "CREATE";
        }

        if (normalizedQuery.startsWith("DROP")) {
            return "DROP";
        }

        return "UNKNOWN";
    }
    public String getTableName(String query) {
        String[] parts = query.trim().split("\\s+");

        String commandType = getCommandType(query);

        if (commandType.equals("SELECT")) {
            if (parts.length < 4) {
                return null;
            }
            return parts[3].replace(";", "").toLowerCase();
        }

        if (commandType.equals("DELETE")) {
            if (parts.length < 3) {
                return null;
            }
            return parts[2].replace(";", "").toLowerCase();
        }

        if (commandType.equals("INSERT")) {
            if (parts.length < 3) {
                return null;
            }
            return parts[2].replace(";", "").toLowerCase();
        }

        if (commandType.equals("UPDATE")) {
            if (parts.length < 2) {
                return null;
            }
            return parts[1].replace(";", "").toLowerCase();
        }

        if (commandType.equals("CREATE") ||
                commandType.equals("DROP")) {
            if (parts.length < 3) {
                return null;
            }
            return parts[2].replace(";", "").toLowerCase();
        }

        return null;
    }
    public boolean tableExists(Database database, String query) {
        String tableName = getTableName(query);

        if (tableName == null) {
            return false;
        }

        return database.hasTable(tableName);
    }
    public boolean isValidCommand(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        return !getCommandType(query).equals("UNKNOWN");
    }
    public String[] getInsertColumns(String query) {
        int start = query.indexOf('(');
        int end = query.indexOf(')');

        if (start == -1 || end == -1 || end < start) {
            throw new IllegalArgumentException("Некорректный INSERT");
        }

        String columnsPart = query.substring(start + 1, end);

        return columnsPart.split(",");
    }
    public String[] getInsertValues(String query) {
        int valuesIndex = query.toUpperCase().indexOf("VALUES");

        if (valuesIndex == -1) {
            throw new IllegalArgumentException("Отсутствует VALUES");
        }

        int start = query.indexOf('(', valuesIndex);
        int end = query.indexOf(')', start);

        if (start == -1 || end == -1) {
            throw new IllegalArgumentException("Некорректные значения INSERT");
        }

        String valuesPart = query.substring(start + 1, end);

        return valuesPart.split(",");
    }
    public String getUpdateColumn(String query) {
        String upperQuery = query.toUpperCase();
        int setIndex = upperQuery.indexOf("SET");
        int equalsIndex = query.indexOf("=", setIndex);

        if (setIndex == -1 || equalsIndex == -1) {
            throw new IllegalArgumentException("Некорректная команда UPDATE");
        }

        return query.substring(setIndex + 3, equalsIndex).trim();
    }
    public String getUpdateValue(String query) {
        String upperQuery = query.toUpperCase();
        int equalsIndex = query.indexOf('=');
        int whereIndex = upperQuery.indexOf("WHERE");

        if (equalsIndex == -1 || whereIndex == -1 || whereIndex < equalsIndex) {
            throw new IllegalArgumentException("Некорректная команда UPDATE");
        }

        return query.substring(equalsIndex + 1, whereIndex).trim();
    }
    public String getConditionColumn(String query) {
        String upperQuery = query.toUpperCase();
        int whereIndex = upperQuery.indexOf("WHERE");
        int equalsIndex = query.indexOf('=', whereIndex);

        if (whereIndex == -1 || equalsIndex == -1) {
            throw new IllegalArgumentException("Некорректное условие WHERE");
        }

        return query.substring(whereIndex + 5, equalsIndex).trim();
    }
    public String getConditionValue(String query) {
        String upperQuery = query.toUpperCase();
        int whereIndex = upperQuery.indexOf("WHERE");
        int equalsIndex = query.indexOf('=', whereIndex);

        if (whereIndex == -1 || equalsIndex == -1) {
            throw new IllegalArgumentException("Некорректное условие WHERE");
        }

        return query.substring(equalsIndex + 1)
                .replace(";", "")
                .trim();
    }
    public String getDeleteConditionColumn(String query) {
        String upperQuery = query.toUpperCase();
        int whereIndex = upperQuery.indexOf("WHERE");
        int equalsIndex = query.indexOf('=', whereIndex);

        if (whereIndex == -1 || equalsIndex == -1) {
            throw new IllegalArgumentException("Некорректная команда DELETE");
        }

        return query.substring(whereIndex + 5, equalsIndex).trim();
    }
    public String getDeleteConditionValue(String query) {
        String upperQuery = query.toUpperCase();
        int whereIndex = upperQuery.indexOf("WHERE");
        int equalsIndex = query.indexOf('=', whereIndex);

        if (whereIndex == -1 || equalsIndex == -1) {
            throw new IllegalArgumentException("Некорректная команда DELETE");
        }

        return query.substring(equalsIndex + 1)
                .replace(";", "")
                .trim();
    }
    public boolean isCreateTableCommand(String query) {
        String upperQuery = query.trim().toUpperCase();

        return upperQuery.startsWith("CREATE TABLE ");
    }
    public boolean isCreateIndexCommand(String query) {
        String upperQuery = query.trim().toUpperCase();

        return upperQuery.startsWith("CREATE INDEX ");
    }
    public String getCreateIndexTable(String query) {
        String upperQuery = query.toUpperCase();
        int onIndex = upperQuery.indexOf(" ON ");

        if (onIndex == -1) {
            throw new IllegalArgumentException(
                    "Не указана таблица для индекса"
            );
        }

        String remaining = query.substring(onIndex + 4).trim();
        String[] parts = remaining.split("\\s+");

        return parts[0].toLowerCase();
    }
    public String getCreateIndexColumn(String query) {
        int openingBracket = query.indexOf('(');
        int closingBracket = query.indexOf(')');

        if (openingBracket == -1 || closingBracket == -1 ||
                closingBracket < openingBracket) {
            throw new IllegalArgumentException(
                    "Не указан столбец для индекса"
            );
        }

        return query.substring(
                openingBracket + 1,
                closingBracket
        ).trim();
    }
    public String getCreateTableColumns(String query) {
        int start = query.indexOf('(');
        int end = query.lastIndexOf(')');

        if (start == -1 || end == -1 || end < start) {
            throw new IllegalArgumentException("Некорректная команда CREATE TABLE");
        }

        return query.substring(start + 1, end).trim();
    }
    public DataType parseDataType(String typeName) {
        switch (typeName.toUpperCase()) {
            case "INTEGER":
                return DataType.INTEGER;
            case "DOUBLE":
                return DataType.DOUBLE;
            case "STRING":
                return DataType.STRING;
            case "DATE":
                return DataType.DATE;
            case "BOOLEAN":
                return DataType.BOOLEAN;
            default:
                throw new IllegalArgumentException(
                        "Неизвестный тип данных: " + typeName
                );
        }
    }
    public List<Column> parseColumns(String columnsText) {
        List<Column> columns = new ArrayList<>();

        String[] definitions = columnsText.split(",");

        for (String definition : definitions) {
            String[] parts = definition.trim().split("\\s+");

            if (parts.length < 2) {
                throw new IllegalArgumentException(
                        "Некорректное описание столбца: " + definition
                );
            }

            String columnName = parts[0];
            DataType type = parseDataType(parts[1]);

            boolean primaryKey = false;
            boolean nullable = true;
            String referencedTable = null;
            String referencedColumn = null;

            for (int i = 2; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("PRIMARY")) {
                    if (i + 1 < parts.length &&
                            parts[i + 1].equalsIgnoreCase("KEY")) {
                        primaryKey = true;
                        nullable = false;
                        i++;
                    }
                } else if (parts[i].equalsIgnoreCase("NOT")) {
                    if (i + 1 < parts.length &&
                            parts[i + 1].equalsIgnoreCase("NULL")) {
                        nullable = false;
                        i++;
                    }
                } else if (parts[i].equalsIgnoreCase("REFERENCES")) {
                    if (i + 1 >= parts.length) {
                        throw new IllegalArgumentException(
                                "Не указана таблица для внешнего ключа"
                        );
                    }

                    String reference = parts[i + 1];
                    int bracketIndex = reference.indexOf('(');
                    int closingBracketIndex = reference.indexOf(')');

                    if (bracketIndex == -1 || closingBracketIndex == -1) {
                        throw new IllegalArgumentException(
                                "Некорректное описание внешнего ключа"
                        );
                    }

                    referencedTable = reference.substring(0, bracketIndex);
                    referencedColumn = reference.substring(
                            bracketIndex + 1,
                            closingBracketIndex
                    );
                    i++;
                }
            }

            columns.add(new Column(
                    columnName,
                    type,
                    primaryKey,
                    nullable,
                    referencedTable,
                    referencedColumn
            ));
        }

        return columns;
    }
}