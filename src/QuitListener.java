import java.util.Scanner;

public class QuitListener extends Thread {

    private Scanner scanner;
    private Server server;

    public QuitListener(Server server, String title) {
        super(title);
        this.server = server;
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        String input;
        while (true) {
            System.out.printf("Do you want to shutdown? (quit): ");
            input = scanner.nextLine();

            if (input.equals("quit")) {
                server.shutdown();
                break;
            }
        }
    }
}
