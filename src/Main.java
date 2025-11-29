import javafx.application.Application;

public class Main {

    /**
     * Класс-обертка для запуска JavaFX приложения Domino
     * Решает проблему модульности в современных версиях Java
     */
    public static void main(String[] args) {
        try {
            System.out.println("Запуск игры Домино...");
            System.out.println("Количество игроков: 4");
            System.out.println("Управление:");
            System.out.println("1. Выберите кость из своей руки (зеленые карточки)");
            System.out.println("2. Кликните на игровом поле для размещения кости");
            System.out.println("3. Используйте кнопки для пропуска хода или взятия кости");

            // Запуск JavaFX приложения
            Application.launch(DominoGUI.class, args);

        } catch (Exception e) {
            System.err.println("Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
