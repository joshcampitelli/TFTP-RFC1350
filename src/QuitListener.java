import java.util.Scanner;

public class QuitListener extends Thread {

    private Scanner scanner;
    private Server server;

    public QuitListener(Server server) {
        super("QuitListener");
        this.server = server;
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        String input;
        while (true) {
            input = scanner.nextLine();

            if (input.equals("quit")) {
                server.shutdown();
                break;
            }
        }
    }
}
