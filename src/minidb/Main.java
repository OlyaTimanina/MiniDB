package minidb;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        SQLParser parser = new SQLParser();

        StorageManager storageManager = new StorageManager("minidb.dat");

        Database database;

        try {
            database = storageManager.load();
        } catch (Exception e) {
            System.out.println("Ошибка загрузки базы данных: " + e.getMessage());
            return;
        }

        SQLExecutor executor = new SQLExecutor();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите SQL-команду (или EXIT для выхода):");

            String query = scanner.nextLine();

            if (query.trim().equalsIgnoreCase("EXIT")) {
                System.out.println("Работа программы завершена.");
                break;
            }

            try {
                if (query.trim().toUpperCase().startsWith("SELECT")) {
                    QueryResult consoleResult = executor.executeSelectQuery(database, query);
                    consoleResult.print();
                } else if (query.trim().toUpperCase().startsWith("INSERT")) {
                    executor.executeInsertQuery(database, query);
                    System.out.println("Запись успешно добавлена.");
                } else if (query.trim().toUpperCase().startsWith("UPDATE")) {
                    executor.executeUpdateQuery(database, query);
                    System.out.println("Запись успешно обновлена.");
                } else if (query.trim().toUpperCase().startsWith("DELETE")) {
                    executor.executeDeleteQuery(database, query);
                    System.out.println("Запись успешно удалена.");
                } else if (query.trim().toUpperCase().startsWith("CREATE TABLE")) {
                    executor.executeCreateTableQuery(database, query);
                    System.out.println("Таблица успешно создана.");
                } else if (query.trim().toUpperCase().startsWith("CREATE INDEX")) {
                    executor.executeCreateIndexQuery(database, query);
                    System.out.println("Индекс успешно создан.");
                } else if (query.trim().toUpperCase().startsWith("DROP TABLE")) {
                    executor.executeDropTableQuery(database, query);
                    System.out.println("Таблица успешно удалена.");
                } else {
                    System.out.println("Неизвестная команда.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }

            try {
                storageManager.save(database);
            } catch (Exception e) {
                System.out.println("Ошибка сохранения базы данных: " + e.getMessage());
            }
        }
    }
}