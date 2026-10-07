import java.util.*;

public class Group {
    public final String name;
    public final List<Team> teams;
    private final List<Match> matches = new ArrayList<>();

    public Group(String name, List<Team> teams) {
        this.name = name;
        this.teams = new ArrayList<>(teams);
    }

    public void play() {
        matches.clear();
        for (Team t : teams) t.resetStats();
        for (int i = 0; i < teams.size(); i++)
            for (int j = i + 1; j < teams.size(); j++) {
                Match m = new Match(teams.get(i), teams.get(j));
                m.simulateGroup();
                matches.add(m);
            }
    }

    /**
     * FIFA World Cup 2026 tiebreakers: points, then among teams level on points —
     * head-to-head points, H2H goal difference, H2H goals scored — then overall goal
     * difference, overall goals scored, and finally FIFA ranking (fair play not modelled).
     */
    public List<Team> standings() {
        Map<Integer, List<Team>> byPoints = new HashMap<>();
        for (Team t : teams) byPoints.computeIfAbsent(t.points, k -> new ArrayList<>()).add(t);

        Map<Team, int[]> h2h = new HashMap<>(); // {points, goal difference, goals for}
        for (List<Team> tied : byPoints.values()) {
            for (Team t : tied) h2h.put(t, new int[3]);
            for (Match m : matches) {
                if (!tied.contains(m.home) || !tied.contains(m.away)) continue;
                addH2H(h2h.get(m.home), m.homeGoals, m.awayGoals);
                addH2H(h2h.get(m.away), m.awayGoals, m.homeGoals);
            }
        }

        List<Team> sorted = new ArrayList<>(teams);
        sorted.sort((a, b) -> {
            if (b.points != a.points) return b.points - a.points;
            int[] ha = h2h.get(a), hb = h2h.get(b);
            for (int k = 0; k < 3; k++) if (hb[k] != ha[k]) return hb[k] - ha[k];
            if (b.goalDifference() != a.goalDifference()) return b.goalDifference() - a.goalDifference();
            if (b.goalsFor != a.goalsFor)                 return b.goalsFor - a.goalsFor;
            return a.fifaRanking - b.fifaRanking;
        });
        return sorted;
    }

    private static void addH2H(int[] rec, int gf, int ga) {
        rec[0] += gf > ga ? 3 : gf == ga ? 1 : 0;
        rec[1] += gf - ga;
        rec[2] += gf;
    }

    public void print() {
        System.out.println("\n GROUP " + name);
        System.out.println(" " + "-".repeat(55));
        for (Match m : matches) {
            System.out.println("  " + m);
            Pacing.ms(150);          // 150 ms per group match → 6 × 150 = 900 ms per group
        }
        System.out.println();
        // Table header includes FIFA ranking for analytical context
        System.out.printf("  %-3s %-22s %4s %2s %2s %2s %2s %3s %3s %4s %3s%n",
            "#", "Team", "Rank", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");
        System.out.println("  " + "-".repeat(58));
        List<Team> st = standings();
        for (int i = 0; i < st.size(); i++) {
            Team t = st.get(i);
            System.out.printf("  %-3d %-22s %4s %2d %2d %2d %2d %3d %3d %+4d %3d%n",
                i + 1, t.name, "#" + t.fifaRanking,
                t.played, t.wins, t.draws, t.losses,
                t.goalsFor, t.goalsAgainst, t.goalDifference(), t.points);
        }
        Pacing.ms(300);              // 300 ms after each group's standings table
    }
}