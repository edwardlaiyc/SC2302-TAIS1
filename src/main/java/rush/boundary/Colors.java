package rush.boundary;

public class Colors {

    private final boolean on;

    public Colors(boolean on) {
        this.on = on;
    }

    // old Windows consoles print the escape codes as junk, so only colour real terminals
    public static boolean supported() {
        if (System.getenv("NO_COLOR") != null || System.console() == null || !System.console().isTerminal()) {
            return false;
        }
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        return !windows || System.getenv("WT_SESSION") != null;
    }

    public String bold(String text) {
        return paint("1", text);
    }

    public String dim(String text) {
        return paint("2", text);
    }

    public String green(String text) {
        return paint("32", text);
    }

    public String red(String text) {
        return paint("31", text);
    }

    public String yellow(String text) {
        return paint("33", text);
    }

    public String cyan(String text) {
        return paint("36", text);
    }

    private String paint(String code, String text) {
        return on ? "\u001B[" + code + "m" + text + "\u001B[0m" : text;
    }
}
