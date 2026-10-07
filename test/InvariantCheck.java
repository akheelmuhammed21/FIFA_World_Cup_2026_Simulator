import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Simulates 2,000 full tournaments and asserts structural invariants: group bookkeeping,
 * standings order, bracket validity (32 unique teams, no same-group R32 ties), knockout
 * consistency, a single surviving champion, outcome probabilities, and the H2H tiebreaker.
 *
 * Run from the project root:
 *   javac -d out src/*.java test/*.java && java -cp out InvariantCheck
 */
public class InvariantCheck implements SimulationListener {
    static int fails = 0;
    static void check(boolean ok, String msg) { if (!ok && fails++ < 10) System.err.println("FAIL: " + msg); }

    List<Group> groups = new ArrayList<>();
    Set<Team> alive = new HashSet<>();
    int koMatches = 0;
    Team champ;

    public void onGroupComplete(Group g) {
        groups.add(g);
        List<Team> st = g.standings();
        int pts = 0, gf = 0, ga = 0;
        for (Team t : st) { check(t.played == 3, g.name + " played!=3"); pts += t.points; gf += t.goalsFor; ga += t.goalsAgainst; }
        check(gf == ga, "group goals unbalanced");
        check(pts >= 12 && pts <= 18, "group points out of range " + pts);
        for (int i = 0; i + 1 < st.size(); i++) check(st.get(i).points >= st.get(i + 1).points, "standings not sorted by points");
    }
    public void onKnockoutBracketReady(List<Team> b) {
        check(b.size() == 32 && new HashSet<>(b).size() == 32, "bracket not 32 unique teams");
        for (int i = 0; i < 32; i += 2) check(!b.get(i).group.equals(b.get(i + 1).group), "same-group R32 tie " + b.get(i) + " v " + b.get(i + 1));
        alive.addAll(b);
    }
    public void onKnockoutMatch(String r, Match m, Team w, boolean pens) {
        koMatches++;
        check(w == m.home || w == m.away, "winner not in match");
        if (!r.equals("THIRD-PLACE PLAY-OFF")) {
            check(alive.contains(m.home) && alive.contains(m.away), "eliminated team playing in " + r);
            alive.remove(w == m.home ? m.away : m.home);
        }
        if (pens) check(m.homeGoals == m.awayGoals, "pens but score not level");
        else check(m.homeGoals != m.awayGoals && (w == m.home) == (m.homeGoals > m.awayGoals), "winner/score mismatch");
    }
    public void onChampion(Team c, Team ru, Team third) { champ = c; check(c != ru && c != third && ru != third, "podium duplicates"); }

    public static void main(String[] a) throws Exception {
        Pacing.enabled = false;
        PrintStream out = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        Tournament t = new Tournament();
        for (int run = 0; run < 2000; run++) {
            InvariantCheck c = new InvariantCheck();
            t.setListener(c);
            t.simulate();
            check(c.groups.size() == 12, "groups != 12");
            check(c.koMatches == 32, "knockout matches != 32 (" + c.koMatches + ")");
            check(c.alive.size() == 1 && c.alive.contains(c.champ), "champion not last team standing");
        }
        System.setOut(out);

        // Probabilities sum to 1 and favour the stronger side
        Method op = Match.class.getDeclaredMethod("outcomeProbabilities", double.class, double.class);
        op.setAccessible(true);
        double[] p = (double[]) op.invoke(null, 2.2, 0.6);
        check(Math.abs(p[0] + p[1] + p[2] - 1) < 1e-9 && p[0] > p[2], "outcomeProbabilities");
        double[] q = (double[]) op.invoke(null, 1.3, 1.3);
        check(Math.abs(q[0] - q[2]) < 1e-9, "symmetric lambdas not symmetric");

        // Tiebreaker: head-to-head beats overall goal difference
        Team A = new Team("A","X",70,10,0,0), B = new Team("B","X",70,20,0,0), C = new Team("C","X",70,30,0,0), D = new Team("D","X",70,40,0,0);
        Group g = new Group("Z", List.of(A, B, C, D));
        Field f = Group.class.getDeclaredField("matches"); f.setAccessible(true);
        @SuppressWarnings("unchecked") List<Match> ms = (List<Match>) f.get(g);
        int[][] res = {{0,1,1,0},{0,2,0,3},{0,3,0,1},{1,2,5,0},{1,3,0,1},{2,3,0,0}}; // A beats B; B has better GD overall
        Team[] T = {A,B,C,D};
        for (int[] r : res) { Match m = new Match(T[r[0]], T[r[1]]); m.homeGoals = r[2]; m.awayGoals = r[3];
            T[r[0]].recordResult(r[2], r[3]); T[r[1]].recordResult(r[3], r[2]); ms.add(m); }
        List<Team> st = g.standings();
        check(A.points == B.points && B.goalDifference() > A.goalDifference(), "test setup");
        check(st.indexOf(A) < st.indexOf(B), "H2H tiebreaker not applied: " + st);

        System.out.println(fails == 0 ? "ALL CHECKS PASSED (2000 tournaments)" : fails + " FAILURES");
    }
}
