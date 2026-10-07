public class Main {
    /**
     * No arguments: launch the GUI.
     * --montecarlo [runs] [seed]: headless batch simulation (default 10,000 runs).
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--montecarlo")) {
            int runs  = args.length > 1 ? Integer.parseInt(args[1]) : 10_000;
            Long seed = args.length > 2 ? Long.parseLong(args[2]) : null;
            MonteCarlo.run(runs, seed);
            return;
        }
        SimulatorWindow.launch();
    }
}
