package minidb;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {

    private List<String> columns;
    private List<List<Object>> rows;

    public QueryResult(List<String> columns) {
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
    }

    public void addRow(List<Object> row) {
        rows.add(new ArrayList<>(row));
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public void print() {
        if (columns.isEmpty()) {
            System.out.println("Нет данных для отображения.");
            return;
        }

        int[] widths = new int[columns.size()];

        for (int i = 0; i < columns.size(); i++) {
            widths[i] = columns.get(i).length();
        }

        for (List<Object> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                String value = String.valueOf(row.get(i));
                widths[i] = Math.max(widths[i], value.length());
            }
        }

        printSeparator(widths);
        printRow(columns, widths);
        printSeparator(widths);

        for (List<Object> row : rows) {
            List<String> values = new ArrayList<>();

            for (Object value : row) {
                values.add(String.valueOf(value));
            }

            printRow(values, widths);
        }

        printSeparator(widths);
        System.out.println("Количество записей: " + rows.size());
    }

    private void printRow(List<String> values, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            String value = i < values.size() ? values.get(i) : "";

            System.out.printf(
                    "| %-" + widths[i] + "s ",
                    value
            );
        }

        System.out.println("|");
    }

    private void printSeparator(int[] widths) {
        for (int width : widths) {
            System.out.print("+-");
            System.out.print("-".repeat(width));
            System.out.print("-");
        }

        System.out.println("+");
    }
}