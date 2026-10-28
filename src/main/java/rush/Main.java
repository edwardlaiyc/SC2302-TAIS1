package rush;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import rush.boundary.Colors;
import rush.boundary.CommandInput;
import rush.boundary.ConsoleView;
import rush.boundary.SatisfactionLog;
import rush.control.GameConfig;
import rush.control.GameController;
import rush.control.InputEndedException;

public class Main {

    private static final String USAGE = """
            Usage: run [options]
              --replay FILE   play the commands in FILE instead of reading the keyboard
              --csv FILE      write each customer's satisfaction per turn to FILE
              --no-color      plain text output
            """;

    public static void main(String[] args) {
        String replayFile = null;
        String csvFile = null;
        boolean color = Colors.supported();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--replay") && i + 1 < args.length) {
                replayFile = args[++i];
            } else if (args[i].equals("--csv") && i + 1 < args.length) {
                csvFile = args[++i];
            } else if (args[i].equals("--no-color")) {
                color = false;
            } else {
                System.out.print(USAGE);
                return;
            }
        }

        Colors colors = new Colors(color);
        GameConfig config = GameConfig.standard();
        ConsoleView view = new ConsoleView(colors);

        try (BufferedReader in = replayFile == null
                ? new BufferedReader(new InputStreamReader(System.in))
                : new BufferedReader(new FileReader(replayFile));
             PrintWriter csv = csvFile == null ? null : new PrintWriter(new FileWriter(csvFile))) {

            GameController game = new GameController(config, new CommandInput(in, colors, replayFile != null));
            game.addListener(view);
            if (csv != null) {
                game.addListener(new SatisfactionLog(csv));
            }

            view.showTitle(config.getObjective(), config.getTurns());
            try {
                game.play();
            } catch (InputEndedException e) {
                view.showInputEnded(e.getMessage(), game.getRestaurant(), game.getTurn());
                System.exit(3);
            }
        } catch (IOException e) {
            System.out.println("Could not open file: " + e.getMessage());
            System.exit(1);
        }
    }
}
