import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        try {
            // Запуск JavaFX приложения
            Application.launch(DominoGUI.class, args);

        } catch (Exception e) {
            System.err.println("Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
