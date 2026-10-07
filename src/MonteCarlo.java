import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Headless batch mode: loads tournament data once, simulates it N times with output and
 * pacing disabled, and reports how often each team reaches each stage.
 *
 * Usage:  java Main --montecarlo [runs] [seed]
 */
public class MonteCarlo implements SimulationListener {
    private static final String[] STAGES = {"Win Grp", "R32", "R16", "QF", "SF", "Final", "Champ"};
    private static final Map<String, Integer> ROUND_WINNER_STAGE = Map.of(
        "ROUND OF 32", 2, "ROUND OF 16", 3, "QUARTER-FINALS", 4, "SEMI-FINALS", 5, "THE FINAL", 6);

    private final Map<Team, int[]> counts = new LinkedHashMap<>();

    @Override public void onGroupComplete(Group group) {
        tally(group.standings().get(0), 0);
    }

    @Override public void onKnockoutBracketReady(List<Team> bracket) {
        for (Team t : bracket) tally(t, 1);
    }

    @Override public void onKnockoutMatch(String round, Match match, Team winner, boolean penalties) {
        Integer stage = ROUND_WINNER_STAGE.get(round);
        if (stage != null) tally(winner, stage);
    }

    private void tally(Team t, int stage) {
        counts.computeIfAbsent(t, k -> new int[STAGES.length])[stage]++;
    }

    public static void run(int runs, Long seed) {
        if (seed != null) Match.setSeed(seed);
        Pacing.enabled = false;

        System.out.println("Loading tournament data...");
        Tournament tournament = new Tournament();
        MonteCarlo mc = new MonteCarlo();
        tournament.setListener(mc);

        PrintStream original = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        long start = System.nanoTime();
        try {
            for (int i = 0; i < runs; i++) tournament.simulate();
        } finally {
            System.setOut(original);
        }
        double secs = (System.nanoTime() - start) / 1e9;

        mc.report(runs, secs, seed);
    }

    private void report(int runs, double secs, Long seed) {
        List<Map.Entry<Team, int[]>> rows = new ArrayList<>(counts.entrySet());
        rows.sort((a, b) -> {
            for (int s = STAGES.length - 1; s >= 0; s--)
                if (b.getValue()[s] != a.getValue()[s]) return b.getValue()[s] - a.getValue()[s];
            return 0;
        });

        System.out.printf("%nFIFA World Cup 2026 — Monte Carlo (%,d tournaments, %.1fs%s)%n",
            runs, secs, seed != null ? ", seed " + seed : "");
        System.out.printf("%-3s %-20s %3s %5s", "#", "Team", "Grp", "FIFA");
        for (String s : STAGES) System.out.printf(" %7s", s);
        System.out.println();
        System.out.println("-".repeat(34 + 8 * STAGES.length));

        int rank = 1;
        for (Map.Entry<Team, int[]> e : rows) {
            Team t = e.getKey();
            System.out.printf("%-3d %-20s %3s %5s", rank++, t.name, t.group, "#" + t.fifaRanking);
            for (int c : e.getValue()) System.out.printf(" %6.1f%%", 100.0 * c / runs);
            System.out.println();
        }
    }
}
