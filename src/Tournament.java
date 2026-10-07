import java.util.*;

/**
 * Simulation factors per team:
 *   strength         — aggregate 1–100 rating (squad depth, quality, tactics)
 *   fifaRanking      — official FIFA ranking position (early 2026)
 *   form             — recent form coefficient [-1.0, +1.0] based on qualifying / Nations League
 *   northAmericaFactor — expected-goals bonus for teams playing in their home continent (0.0–0.12)
 *
 * Groups are sourced via GroupStageProvider:
 *   - FootballApiProvider fetches live data from football-data.org (requires FD_API_KEY env var)
 *   - StaticGroupStageProvider uses the official Dec 2025 draw as fallback
 *
 * Match lambda (expected goals) formula in Match.java:
 *   base + strengthEffect + formEffect + H2H_advantage + NA_venue_factor + squadModifier
 */
public class Tournament {
    private static final String[] GROUP_NAMES = {"A","B","C","D","E","F","G","H","I","J","K","L"};
    private final List<Group> groups = new ArrayList<>();
    private SimulationListener listener;
    private Team thirdPlaceTeam;

    public void setListener(SimulationListener l) { listener = l; }

    public Tournament() {
        List<Team> teams = buildTeams();
        loadSquads(teams);

        Map<String, Team> byName = new LinkedHashMap<>();
        for (Team t : teams) byName.put(t.name, t);

        try {
            setupGroups(loadGroups(), byName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load group data", e);
        }
    }

    // ── Squad loading (static base + FotMob live form overlay) ─────────────────
    // FotMobSquadProvider always attaches the static squads first and handles its own
    // network failures, so no fallback is needed here.

    private void loadSquads(List<Team> teams) {
        new FotMobSquadProvider().attachSquads(teams);
    }

    // ── Group loading (football-data.org → static fallback) ───────────────────

    private Map<String, List<String>> loadGroups() throws Exception {
        FootballApiProvider api = FootballApiProvider.fromEnv();
        if (api != null) {
            try {
                Map<String, List<String>> g = api.fetchGroups();
                System.out.println("  [Live group data from football-data.org]");
                return g;
            } catch (Exception e) {
                System.out.println("  [API unavailable (" + e.getMessage() + ") — using static draw data]");
            }
        }
        return new StaticGroupStageProvider().fetchGroups();
    }

    private void setupGroups(Map<String, List<String>> groupDef, Map<String, Team> byName) {
        for (Map.Entry<String, List<String>> entry : groupDef.entrySet()) {
            List<Team> groupTeams = new ArrayList<>();
            for (String name : entry.getValue()) {
                Team t = byName.get(name);
                if (t == null) System.out.println("  [WARNING] Unknown team in draw: '" + name + "'");
                else { t.group = entry.getKey(); groupTeams.add(t); }
            }
            if (groupTeams.size() == 4)
                groups.add(new Group(entry.getKey(), groupTeams));
        }
        // Sort groups alphabetically so output is always A → L
        groups.sort(Comparator.comparing(g -> g.name));
    }

    // ── Team roster ────────────────────────────────────────────────────────────
    // new Team(name, confederation, strength, fifaRanking, form, northAmericaFactor)

    private List<Team> buildTeams() {
        return new ArrayList<>(Arrays.asList(

            // ── UEFA (16) ────────────────────────────────────────────────────
            new Team("France",            "UEFA",     91,  2,  0.65, 0.00),
            new Team("Spain",             "UEFA",     90,  3,  0.75, 0.00),
            new Team("England",           "UEFA",     88,  4,  0.55, 0.00),
            new Team("Portugal",          "UEFA",     87,  6,  0.50, 0.00),
            new Team("Germany",           "UEFA",     85,  7,  0.70, 0.00),
            new Team("Netherlands",       "UEFA",     83,  8,  0.55, 0.00),
            new Team("Belgium",           "UEFA",     81,  9, -0.10, 0.00), // ageing golden generation
            new Team("Norway",            "UEFA",     81, 11,  0.55, 0.00), // Haaland-led peak generation
            new Team("Switzerland",       "UEFA",     76, 15,  0.35, 0.00),
            new Team("Scotland",          "UEFA",     75, 20,  0.35, 0.00),
            new Team("Sweden",            "UEFA",     75, 19,  0.40, 0.00), // Gyokeres + Isak
            new Team("Austria",           "UEFA",     73, 23,  0.45, 0.00),
            new Team("Turkey",            "UEFA",     72, 22,  0.40, 0.00),
            new Team("Croatia",           "UEFA",     78, 14, -0.15, 0.00), // Modric final WC, declining
            new Team("Czechia",           "UEFA",     70, 37,  0.30, 0.00), // knocked Italy out of playoffs
            new Team("Bosnia Herzegovina","UEFA",     66, 55,  0.40, 0.00), // knocked Italy out; Demirovic era

            // ── CONMEBOL (6) ─────────────────────────────────────────────────
            new Team("Argentina",    "CONMEBOL", 93,  1,  0.80, 0.03), // defending champions
            new Team("Brazil",       "CONMEBOL", 89,  5,  0.30, 0.03), // new generation, mixed form
            new Team("Colombia",     "CONMEBOL", 77, 12,  0.65, 0.03),
            new Team("Uruguay",      "CONMEBOL", 75, 11,  0.50, 0.02),
            new Team("Ecuador",      "CONMEBOL", 67, 25,  0.30, 0.02),
            new Team("Paraguay",     "CONMEBOL", 63, 33,  0.25, 0.02),

            // ── CONCACAF (6 — USA, Canada, Mexico as hosts) ──────────────────
            new Team("USA",     "CONCACAF", 74, 18,  0.60, 0.12),
            new Team("Mexico",  "CONCACAF", 72, 20,  0.50, 0.10),
            new Team("Canada",  "CONCACAF", 70, 27,  0.70, 0.10),
            new Team("Panama",  "CONCACAF", 61, 40,  0.30, 0.05),
            new Team("Haiti",   "CONCACAF", 53, 88,  0.15, 0.03),
            new Team("Curacao", "CONCACAF", 50, 95,  0.20, 0.03), // first-ever WC; smallest nation to qualify

            // ── CAF (10) ─────────────────────────────────────────────────────
            new Team("Morocco",      "CAF",  76, 16,  0.70, 0.00),
            new Team("Senegal",      "CAF",  72, 21,  0.55, 0.00),
            new Team("Algeria",      "CAF",  68, 32,  0.35, 0.00), // Bennacer + Mahrez
            new Team("Ivory Coast",  "CAF",  67, 29,  0.45, 0.00),
            new Team("Egypt",        "CAF",  65, 34,  0.25, 0.00), // Mo Salah
            new Team("DR Congo",     "CAF",  64, 50,  0.30, 0.00), // Lukebakio, Silas
            new Team("Ghana",        "CAF",  62, 38,  0.20, 0.00),
            new Team("Tunisia",      "CAF",  61, 41,  0.25, 0.00),
            new Team("Cape Verde",   "CAF",  60, 62,  0.30, 0.00), // Gilberto, giant-killers
            new Team("South Africa", "CAF",  59, 43,  0.25, 0.00),

            // ── AFC (9) ──────────────────────────────────────────────────────
            new Team("Japan",       "AFC",  76, 17,  0.60, 0.00), // Mitoma / Kubo generation
            new Team("South Korea", "AFC",  74, 19,  0.45, 0.00), // Son's final WC
            new Team("Australia",   "AFC",  67, 26,  0.40, 0.00),
            new Team("Saudi Arabia","AFC",  65, 35,  0.30, 0.00),
            new Team("Iran",        "AFC",  63, 36,  0.30, 0.00),
            new Team("Uzbekistan",  "AFC",  61, 38,  0.40, 0.00),
            new Team("Iraq",        "AFC",  58, 44,  0.35, 0.00),
            new Team("Qatar",       "AFC",  57, 46,  0.15, 0.00),
            new Team("Jordan",      "AFC",  55, 68,  0.25, 0.00), // first-ever WC

            // ── OFC (1) ──────────────────────────────────────────────────────
            new Team("New Zealand", "OFC",  52, 48,  0.20, 0.00)
        ));
    }

    // ── Tournament flow ────────────────────────────────────────────────────────

    public void simulate() {
        printBanner();
        runGroupStage();
        List<Team> r32 = selectQualifiers();
        printQualifiers(r32);
        runKnockoutStage(r32);
    }

    private void printBanner() {
        System.out.println();
        System.out.println("+========================================================+");
        System.out.println("|          FIFA WORLD CUP 2026 SIMULATOR                 |");
        System.out.println("|          United 2026  |  USA - Canada - Mexico         |");
        System.out.println("|  Factors: Strength · Form · Squad · H2H · NA Venue     |");
        System.out.println("+========================================================+");
        Pacing.ms(600);
    }

    private void runGroupStage() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                      GROUP STAGE");
        System.out.println("=".repeat(60));
        for (Group g : groups) {
            g.play();
            g.print();
            Pacing.ms(250);          // 250 ms gap between groups
            if (listener != null) listener.onGroupComplete(g);
        }
    }

    // Top 2 per group + 8 best third-place = 32
    private List<Team> selectQualifiers() {
        List<Team> r32    = new ArrayList<>();
        List<Team> thirds = new ArrayList<>();

        for (Group g : groups) {
            List<Team> st = g.standings();
            r32.add(st.get(0));
            r32.add(st.get(1));
            thirds.add(st.get(2));
        }

        thirds.sort((a, b) -> {
            if (b.points != a.points)                      return b.points - a.points;
            if (b.goalDifference() != a.goalDifference())  return b.goalDifference() - a.goalDifference();
            if (b.goalsFor != a.goalsFor)                  return b.goalsFor - a.goalsFor;
            return a.fifaRanking - b.fifaRanking;
        });
        for (int i = 0; i < 8; i++) r32.add(thirds.get(i));
        return r32;
    }

    private void printQualifiers(List<Team> r32) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                 ROUND OF 32 QUALIFIERS");
        System.out.println("=".repeat(60));
        System.out.println("\n  Group Winners:");
        for (int i = 0; i < 12; i++)
            System.out.printf("    Group %-2s  %-24s [FIFA #%d]%n",
                GROUP_NAMES[i], r32.get(i * 2).name, r32.get(i * 2).fifaRanking);
        System.out.println("\n  Runners-up:");
        for (int i = 0; i < 12; i++)
            System.out.printf("    Group %-2s  %-24s [FIFA #%d]%n",
                GROUP_NAMES[i], r32.get(i * 2 + 1).name, r32.get(i * 2 + 1).fifaRanking);
        System.out.println("\n  Best 3rd-place teams advancing:");
        for (int i = 24; i < 32; i++)
            System.out.printf("    %-24s [FIFA #%d | Form: %s]%n",
                r32.get(i).name, r32.get(i).fifaRanking, r32.get(i).formLabel());
    }

    // ── Knockout stage ─────────────────────────────────────────────────────────

    private void runKnockoutStage(List<Team> r32) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   KNOCKOUT STAGE");
        System.out.println("=".repeat(60));

        List<Team> bracket  = buildBracket(r32);
        if (listener != null) listener.onKnockoutBracketReady(new ArrayList<>(bracket));
        //                                         analysis pause, result pause (ms)
        List<Team> r16      = playRound("ROUND OF 32",    bracket, 350,  550);  // 900 ms/match × 16 = 14.4 s
        List<Team> qf       = playRound("ROUND OF 16",    r16,     400,  700);  // 1100 ms/match × 8  =  8.8 s
        List<Team> sf       = playRound("QUARTER-FINALS", qf,      500,  900);  // 1400 ms/match × 4  =  5.6 s

        List<Team> sfLosers = new ArrayList<>();
        List<Team> finalists = playSemis(sf, sfLosers);

        playThirdPlace(sfLosers);
        playFinal(finalists);
    }

    private List<Team> buildBracket(List<Team> r32) {
        List<Team> winners   = new ArrayList<>();
        List<Team> runnersUp = new ArrayList<>();
        List<Team> thirds    = new ArrayList<>(r32.subList(24, 32));

        for (int i = 0; i < 12; i++) {
            winners.add(r32.get(i * 2));
            runnersUp.add(r32.get(i * 2 + 1));
        }

        // Reshuffle until no Round-of-32 tie is a rematch of a group-stage fixture
        List<Team> bracket = null;
        for (int attempt = 0; attempt < 1000; attempt++) {
            Collections.shuffle(winners,   Match.RNG);
            Collections.shuffle(runnersUp, Match.RNG);
            Collections.shuffle(thirds,    Match.RNG);

            List<Team> seeded = new ArrayList<>();
            seeded.addAll(winners);    // 0–11
            seeded.addAll(runnersUp);  // 12–23
            seeded.addAll(thirds);     // 24–31

            bracket = new ArrayList<>();
            boolean clash = false;
            for (int i = 0; i < 16; i++) {
                Team a = seeded.get(i), b = seeded.get(31 - i);
                if (a.group != null && a.group.equals(b.group)) clash = true;
                bracket.add(a);
                bracket.add(b);
            }
            if (!clash) break;
        }
        return bracket;
    }

    // analysisPause: ms between showing analysis and revealing the score
    // resultPause:   ms after revealing the result before the next match
    private List<Team> playRound(String label, List<Team> teams,
                                  long analysisPause, long resultPause) {
        Pacing.ms(800);
        System.out.printf("%n-- %-20s %s%n", label, "-".repeat(Math.max(0, 38 - label.length())));
        List<Team> winners = new ArrayList<>();
        for (int i = 0; i < teams.size(); i += 2) {
            Team a = teams.get(i), b = teams.get(i + 1);
            Match m = new Match(a, b);
            System.out.println(m.analysis());
            Pacing.ms(analysisPause);
            Team w = m.simulateKnockout();
            String pen = m.wentToPenalties() ? " (pen)" : "";
            System.out.printf("  Result: %s%s  ->  %s advances%n%n", m, pen, w.name);
            Pacing.ms(resultPause);
            winners.add(w);
            if (listener != null) {
                final String fl = label; final Match fm = m; final Team fw = w;
                listener.onKnockoutMatch(fl, fm, fw, fm.wentToPenalties());
            }
        }
        return winners;
    }

    private List<Team> playSemis(List<Team> teams, List<Team> losers) {
        Pacing.ms(800);
        System.out.printf("%n-- %-20s %s%n", "SEMI-FINALS", "-".repeat(38));
        List<Team> winners = new ArrayList<>();
        for (int i = 0; i < teams.size(); i += 2) {
            Team a = teams.get(i), b = teams.get(i + 1);
            Match m = new Match(a, b);
            System.out.println(m.analysis());
            Pacing.ms(700);          // read analysis
            Team w = m.simulateKnockout();
            Team l = (w == a) ? b : a;
            String pen = m.wentToPenalties() ? " (pen)" : "";
            System.out.printf("  Result: %s%s  ->  %s advances%n%n", m, pen, w.name);
            Pacing.ms(1200);         // absorb result before next semi
            winners.add(w);
            losers.add(l);
            if (listener != null) {
                final Match fm = m; final Team fw = w;
                listener.onKnockoutMatch("SEMI-FINALS", fm, fw, fm.wentToPenalties());
            }
        }
        return winners;
    }

    private void playThirdPlace(List<Team> losers) {
        Pacing.ms(800);
        System.out.printf("%n-- %-20s %s%n", "THIRD-PLACE PLAY-OFF", "-".repeat(38));
        Team a = losers.get(0), b = losers.get(1);
        Match m = new Match(a, b);
        System.out.println(m.analysis());
        Pacing.ms(800);
        Team w = m.simulateKnockout();
        String pen = m.wentToPenalties() ? " (pen)" : "";
        System.out.printf("  Result: %s%s  ->  %s finishes 3rd%n", m, pen, w.name);
        thirdPlaceTeam = w;
        Pacing.ms(1200);
        if (listener != null) listener.onKnockoutMatch("THIRD-PLACE PLAY-OFF", m, w, m.wentToPenalties());
    }

    private void playFinal(List<Team> finalists) {
        Pacing.ms(1000);
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println("                        THE FINAL");
        System.out.println("=".repeat(60));
        Team a = finalists.get(0), b = finalists.get(1);
        Match m = new Match(a, b);
        System.out.println(m.analysis());
        Pacing.ms(1200);             // suspense: read the analysis carefully
        Team w = m.simulateKnockout();
        String pen = m.wentToPenalties() ? " (pen)" : "";
        System.out.printf("%n  %s%s%n", m, pen);
        Pacing.ms(1800);             // dramatic pause before champion is announced
        System.out.println();
        System.out.println("+========================================================+");
        System.out.printf("|  FIFA WORLD CUP 2026 CHAMPION: %-24s|%n", w.name);
        System.out.println("+========================================================+");
        if (listener != null) {
            final Team fw = w, fru = (w == a) ? b : a;
            listener.onKnockoutMatch("THE FINAL", m, fw, m.wentToPenalties());
            listener.onChampion(fw, fru, thirdPlaceTeam);
        }
    }
}