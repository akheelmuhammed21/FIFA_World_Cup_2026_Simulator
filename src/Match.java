import java.util.Random;

public class Match {
    // Shared by Match and Tournament (bracket shuffles) so a single seed reproduces a whole run
    static final Random RNG = new Random();

    public static void setSeed(long seed) { RNG.setSeed(seed); }

    public final Team home;
    public final Team away;
    public int homeGoals;
    public int awayGoals;
    private boolean penalties;

    public Match(Team home, Team away) {
        this.home = home;
        this.away = away;
    }

    // ── Group stage ────────────────────────────────────────────────────────────

    public void simulateGroup() {
        homeGoals = poisson(lambda(home, away));
        awayGoals = poisson(lambda(away, home));
        home.recordResult(homeGoals, awayGoals);
        away.recordResult(awayGoals, homeGoals);
    }

    // ── Knockout stage ─────────────────────────────────────────────────────────

    public Team simulateKnockout() {
        homeGoals = poisson(lambda(home, away));
        awayGoals = poisson(lambda(away, home));
        if (homeGoals != awayGoals) return leader();

        // Extra time (~30 min = ⅓ of regular time)
        homeGoals += poisson(lambda(home, away) * 0.33);
        awayGoals += poisson(lambda(away, home) * 0.33);
        if (homeGoals != awayGoals) return leader();

        penalties = true;
        return shootout();
    }

    // ── Analytics ──────────────────────────────────────────────────────────────

    /**
     * Returns a compact pre-match analysis string used in the knockout bracket display.
     * Shows FIFA ranking, form, venue boost, H2H context, and win probability.
     */
    public String analysis() {
        double[] p = outcomeProbabilities(lambda(home, away), lambda(away, home));
        double homeWin = p[0] * 100, draw = p[1] * 100, awayWin = p[2] * 100;
        String h2h = HeadToHead.describe(home.name, away.name);
        String homeStars = home.squad != null ? home.squad.topStarsDisplay() : "-";
        String awayStars = away.squad != null ? away.squad.topStarsDisplay() : "-";

        return String.format(
            "  [#%d FIFA | Form: %-9s | NA: +%.0f%%]  vs  [#%d FIFA | Form: %-9s | NA: +%.0f%%]%n" +
            "  Stars: %-36s | Stars: %s%n" +
            "  H2H: %-40s | 90 min: %s %.0f%%  /  Draw %.0f%%  /  %s %.0f%%",
            home.fifaRanking, home.formLabel(), home.northAmericaFactor * 100,
            away.fifaRanking, away.formLabel(), away.northAmericaFactor * 100,
            homeStars, awayStars,
            h2h,
            home.name, homeWin, draw, away.name, awayWin
        );
    }

    /**
     * Exact 90-minute {win, draw, loss} probabilities for two independent Poisson scorers,
     * summed over scorelines up to 15 goals each (tail mass is negligible at these lambdas).
     */
    static double[] outcomeProbabilities(double lH, double lA) {
        final int MAX = 15;
        double[] pH = poissonPmf(lH, MAX), pA = poissonPmf(lA, MAX);
        double win = 0, draw = 0, loss = 0;
        for (int i = 0; i <= MAX; i++)
            for (int j = 0; j <= MAX; j++) {
                double pij = pH[i] * pA[j];
                if (i > j) win += pij; else if (i == j) draw += pij; else loss += pij;
            }
        double total = win + draw + loss;
        return new double[] { win / total, draw / total, loss / total };
    }

    private static double[] poissonPmf(double lam, int max) {
        double[] pmf = new double[max + 1];
        pmf[0] = Math.exp(-lam);
        for (int k = 1; k <= max; k++) pmf[k] = pmf[k - 1] * lam / k;
        return pmf;
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private Team leader() { return homeGoals >= awayGoals ? home : away; }

    private Team shootout() {
        int h = 0, a = 0;
        for (int i = 0; i < 5; i++) {
            if (kick(home, away)) h++;
            if (kick(away, home)) a++;
        }
        while (h == a) { // sudden death
            if (kick(home, away)) h++;
            if (kick(away, home)) a++;
        }
        return h > a ? home : away;
    }

    private boolean kick(Team shooter, Team keeper) {
        // Shooter conversion base 72%, reduced by the *opposing* goalkeeper's save bonus
        double gkBonus = keeper.squad != null ? keeper.squad.gkPenaltySaveBonus() : 0.0;
        return RNG.nextDouble() < 0.72 + (shooter.strength - 65) * 0.002 - gkBonus;
    }

    /**
     * Expected goals for 'att' vs 'def', factoring in:
     *   - Strength differential (FIFA-calibrated 1–100 scale)
     *   - Team form going into the tournament
     *   - Head-to-head historical record
     *   - North America venue familiarity
     */
    double lambda(Team att, Team def) {
        double base     = 1.25;
        double strength = (att.strength - def.strength) / 100.0 * 2.5;
        double form     = att.form * 0.20 - def.form * 0.10;
        double h2h      = HeadToHead.getAdvantage(att.name, def.name);
        double venue    = att.northAmericaFactor;
        double squad    = att.squad != null ? att.squad.attackLambdaModifier() : 0.0;
        return Math.max(0.3, base + strength + form + h2h + venue + squad);
    }

    // Poisson sample via Knuth algorithm
    private int poisson(double lam) {
        double threshold = Math.exp(-lam);
        double p = 1.0;
        int k = 0;
        do { p *= RNG.nextDouble(); k++; } while (p > threshold);
        return k - 1;
    }

    public boolean wentToPenalties() { return penalties; }

    @Override
    public String toString() {
        return String.format("%-22s %2d - %-2d %-22s", home.name, homeGoals, awayGoals, away.name);
    }
}