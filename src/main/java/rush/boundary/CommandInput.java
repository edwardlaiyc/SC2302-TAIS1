package rush.boundary;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import rush.control.InputEndedException;
import rush.control.ManagerInput;
import rush.entity.CookTask;
import rush.entity.Customer;
import rush.entity.InvalidTaskException;
import rush.entity.Restaurant;
import rush.entity.SeatTask;
import rush.entity.ServeTask;
import rush.entity.Staff;
import rush.entity.Table;
import rush.entity.TakeOrderTask;
import rush.entity.Task;
import rush.entity.WaitTask;

public class CommandInput implements ManagerInput {

    private static final String HELP = """
              <number>     the task with that number in the list
              seat C2 T1   seat customer C2 at table T1          (Host, Waiter)
              order C2     take C2's order                       (Waiter)
              serve C2     serve C2's dish once it is READY      (Waiter)
              cook C2      do one preparation unit of C2's order (Chef)
              wait         do nothing this turn
              help         show this list
            """;

    private final BufferedReader in;
    private final Colors colors;
    private final boolean echo;

    // echo is on for replay files, so the screen shows each command as if it was typed
    public CommandInput(BufferedReader in, Colors colors, boolean echo) {
        this.in = in;
        this.colors = colors;
        this.echo = echo;
    }

    @Override
    public Task chooseTask(Staff staff, List<Task> allowed, Restaurant restaurant) throws InvalidTaskException {
        StringBuilder options = new StringBuilder();
        for (int i = 0; i < allowed.size(); i++) {
            options.append("  ").append(i + 1).append(") ").append(allowed.get(i).getCommand());
        }
        System.out.println(colors.bold(" " + staff.getTitle()) + colors.dim(options.toString()));

        while (true) {
            String line = readLine(staff.getTitle() + "> ");
            if (line.isEmpty()) {
                continue;
            }
            if (line.equalsIgnoreCase("help")) {
                System.out.print(HELP);
                continue;
            }
            return toTask(line, allowed, restaurant);
        }
    }

    @Override
    public boolean wantsHappyHour() {
        while (true) {
            String answer = readLine("Start Happy Hour? 20% off new orders and +15 for waiting customers, this turn and next (y/n)> ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println(colors.yellow("   Please answer y or n."));
        }
    }

    private String readLine(String prompt) {
        System.out.print(" " + prompt);
        while (true) {
            String line;
            try {
                line = in.readLine();
            } catch (IOException e) {
                line = null;
            }
            if (line == null) {
                System.out.println();
                throw new InputEndedException("The input ended before the game finished.");
            }
            line = line.trim();
            if (line.startsWith("#")) {
                continue; // comment line in a replay file
            }
            if (echo) {
                System.out.println(line);
            }
            return line;
        }
    }

    private Task toTask(String line, List<Task> allowed, Restaurant restaurant) throws InvalidTaskException {
        String[] words = line.toLowerCase().split("\\s+");
        String command = words[0];

        if (command.matches("\\d+")) {
            int choice = Integer.parseInt(command);
            if (choice < 1 || choice > allowed.size()) {
                throw new InvalidTaskException("Pick a number from 1 to " + allowed.size() + ".");
            }
            return allowed.get(choice - 1);
        }

        switch (command) {
            case "wait":
                return new WaitTask();
            case "seat":
                expectWords(words, 3, "seat C2 T1");
                return new SeatTask(findCustomer(words[1], restaurant), findTable(words[2], restaurant));
            case "order":
                expectWords(words, 2, "order C2");
                return new TakeOrderTask(findCustomer(words[1], restaurant));
            case "serve":
                expectWords(words, 2, "serve C2");
                return new ServeTask(findCustomer(words[1], restaurant));
            case "cook":
                expectWords(words, 2, "cook C2");
                return new CookTask(findCustomer(words[1], restaurant));
            default:
                throw new InvalidTaskException("'" + line + "' is not a command. Type a number, or 'help'.");
        }
    }

    private void expectWords(String[] words, int count, String example) throws InvalidTaskException {
        if (words.length != count) {
            throw new InvalidTaskException("Write it like this: " + example + ".");
        }
    }

    private Customer findCustomer(String word, Restaurant restaurant) throws InvalidTaskException {
        String digits = word.startsWith("c") ? word.substring(1) : word;
        Customer customer = digits.matches("\\d+") ? restaurant.findCustomer(Integer.parseInt(digits)) : null;
        if (customer == null) {
            throw new InvalidTaskException("There is no customer " + word.toUpperCase() + ".");
        }
        return customer;
    }

    private Table findTable(String word, Restaurant restaurant) throws InvalidTaskException {
        String digits = word.startsWith("t") ? word.substring(1) : word;
        Table table = digits.matches("\\d+") ? restaurant.findTable(Integer.parseInt(digits)) : null;
        if (table == null) {
            throw new InvalidTaskException("There is no table " + word.toUpperCase() + ".");
        }
        return table;
    }
}
