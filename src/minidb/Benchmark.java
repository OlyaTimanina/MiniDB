package minidb;

import java.util.ArrayList;
import java.util.List;

public class Benchmark {

    public static void main(String[] args) {
        List<Column> columns = new ArrayList<>();

        columns.add(new Column("id", DataType.INTEGER, true, false));
        columns.add(new Column("group_id", DataType.INTEGER, false, false));

        Table table = new Table("benchmark", columns);

        for (int i = 0; i < 50000; i++) {
            Row row = new Row();

            row.setValue("id", i);
            row.setValue("group_id", i % 100);

            table.addRow(row);
        }

        int searchValue = 42;
        int repetitions = 500;

        long scanStart = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            for (Row row : table.getRows()) {
                if (Integer.valueOf(searchValue).equals(
                        row.getValue("group_id"))) {
                    row.getValue("id");
                }
            }
        }

        long scanTime = System.nanoTime() - scanStart;

        table.createIndex("group_id");

        long indexStart = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            table.findRowsByIndex("group_id", searchValue);
        }

        long indexTime = System.nanoTime() - indexStart;

        System.out.println("Количество записей: " + table.getRows().size());
        System.out.println("Количество поисков: " + repetitions);
        System.out.println("Поиск без индекса: " + scanTime / 1_000_000 + " мс");
        System.out.println("Поиск с индексом: " + indexTime / 1_000_000 + " мс");
    }
}