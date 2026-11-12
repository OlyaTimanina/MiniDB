package minidb;

import java.io.Serializable;

public class Column implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private DataType type;
    private boolean primaryKey;
    private boolean nullable;
    private String referencedTable;
    private String referencedColumn;

    public Column(String name, DataType type,
                  boolean primaryKey, boolean nullable) {
        this(name, type, primaryKey, nullable, null, null);
    }

    public Column(String name, DataType type,
                  boolean primaryKey, boolean nullable,
                  String referencedTable, String referencedColumn) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
        this.nullable = nullable;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isNullable() {
        return nullable;
    }
    public String getReferencedTable() {
        return referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }
}