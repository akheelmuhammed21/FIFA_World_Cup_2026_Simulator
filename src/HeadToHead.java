import java.util.HashMap;
import java.util.Map;

/**
 * Historical head-to-head records encoded as expected-goals advantage
 * for team A over team B. Range: -0.20 (B dominates) to +0.20 (A dominates).
 *
 * Sources: FIFA/Wikipedia match archives, WC history, recent major tournament results.
 */
public class HeadToHead {
    private static final Map<String, Double> H2H = new HashMap<>();

    static {
        // ── CONMEBOL ─────────────────────────────────────────────────────────
        add("Argentina", "Brazil",       +0.02); // Slight ARG edge (2021 Copa)
        add("Argentina", "Colombia",     +0.12);
        add("Argentina", "Ecuador",      +0.18);
        add("Argentina", "England",      +0.10); // Maradona+Messi WC legacy
        add("Argentina", "France",       +0.05); // Won 2022 final; overall H2H even
        add("Argentina", "Germany",      +0.03);
        add("Argentina", "Mexico",       +0.15);
        add("Argentina", "Netherlands",  +0.05); // 2022 WC QF win on pens
        add("Argentina", "Paraguay",     +0.18);
        add("Argentina", "Saudi Arabia", +0.12); // 2022 shock loss but overall dominant
        add("Argentina", "Uruguay",      +0.08);
        add("Argentina", "Venezuela",    +0.18);
        add("Brazil", "Colombia",        +0.12);
        add("Brazil", "Ecuador",         +0.15);
        add("Brazil", "England",         +0.08);
        add("Brazil", "France",          +0.05);
        add("Brazil", "Germany",         -0.08); // 7-1 — psychological edge Germany
        add("Brazil", "Mexico",          +0.15);
        add("Brazil", "Netherlands",     +0.05);
        add("Brazil", "Paraguay",        +0.15);
        add("Brazil", "South Korea",     +0.15);
        add("Brazil", "Uruguay",         +0.10);
        add("Brazil", "USA",             +0.15);
        add("Brazil", "Venezuela",       +0.18);
        add("Colombia", "Ecuador",       +0.10);
        add("Colombia", "Paraguay",      +0.12);
        add("Colombia", "Uruguay",       +0.05);
        add("Uruguay", "England",        +0.05); // 2014 WC win
        add("Uruguay", "Italy",          +0.03);

        // ── UEFA ─────────────────────────────────────────────────────────────
        add("England", "Croatia",        +0.05);
        add("England", "France",         -0.05);  // France recent H2H edge
        add("England", "Germany",        -0.05);  // Classic German dominance historically
        add("England", "Italy",          -0.03);  // EURO 2020 pens loss
        add("England", "Netherlands",    +0.03);
        add("England", "Scotland",       +0.10);
        add("England", "Ukraine",        +0.15);
        add("France", "Germany",         +0.08);  // Recent Nations League wins
        add("France", "Italy",           +0.05);
        add("France", "Morocco",         +0.08);  // Beat Morocco in 2022 SF
        add("France", "Netherlands",     +0.08);
        add("France", "Portugal",        +0.05);
        add("France", "Senegal",         +0.08);
        add("France", "Spain",           -0.03);  // Spain slight H2H edge
        add("Germany", "Denmark",        +0.10);
        add("Germany", "Italy",          +0.08);
        add("Germany", "Netherlands",    +0.10);  // Historical dominance
        add("Germany", "Portugal",       +0.05);
        add("Germany", "Spain",          -0.10);  // Spain 6-0 Nations League 2023
        add("Germany", "Switzerland",    +0.08);
        add("Germany", "Turkey",         +0.10);
        add("Italy", "Croatia",          +0.03);
        add("Italy", "Spain",            -0.05);
        add("Netherlands", "Argentina",  -0.05);
        add("Netherlands", "Uruguay",    +0.05);
        add("Netherlands", "USA",        +0.10);
        add("Portugal", "Croatia",       +0.05);
        add("Portugal", "Morocco",       -0.05); // Morocco 2022 QF upset!
        add("Spain", "Croatia",          +0.10);
        add("Spain", "England",          +0.05);
        add("Spain", "Italy",            +0.05);
        add("Spain", "Morocco",          -0.03); // Morocco beat Spain pens 2022
        add("Spain", "Netherlands",      +0.05);
        add("Spain", "Portugal",         +0.05);
        add("Spain", "Switzerland",      +0.08);

        // Notable 2022 upsets encoded:
        add("Japan", "Germany",          +0.03); // Japan won 2-1 in 2022 group stage
        add("Japan", "Spain",            +0.03); // Japan won 2-1 in 2022 group stage
        add("Japan", "Croatia",          -0.05); // Croatia beat Japan pens 2022

        // ── CONCACAF ─────────────────────────────────────────────────────────
        add("Canada", "Costa Rica",      +0.08);
        add("Canada", "Honduras",        +0.10);
        add("Canada", "Panama",          +0.08);
        add("Mexico", "Canada",          +0.10); // Mexico H2H but Canada closing gap
        add("Mexico", "Costa Rica",      +0.12);
        add("Mexico", "Honduras",        +0.20);
        add("Mexico", "Panama",          +0.15);
        add("Mexico", "USA",             +0.05); // Mexico historically dominant; USA converging
        add("USA", "Canada",             +0.05);
        add("USA", "Costa Rica",         +0.05);
        add("USA", "Honduras",           +0.12);
        add("USA", "Panama",             +0.10);

        // ── CAF ──────────────────────────────────────────────────────────────
        add("Egypt", "Cameroon",         +0.03);
        add("Ivory Coast", "Ghana",      +0.05); // West Africa derby
        add("Morocco", "Cameroon",       +0.08);
        add("Morocco", "Egypt",          +0.08);
        add("Morocco", "Ghana",          +0.10);
        add("Morocco", "Nigeria",        +0.05);
        add("Morocco", "Senegal",        +0.05);
        add("Nigeria", "Cameroon",       +0.05);
        add("Nigeria", "Ghana",          +0.05);
        add("Senegal", "Egypt",          +0.05);
        add("Senegal", "Nigeria",        +0.05);

        // ── AFC ──────────────────────────────────────────────────────────────
        add("Australia", "Japan",        -0.05);
        add("Iran", "Saudi Arabia",      +0.05);
        add("Japan", "Australia",        +0.05);
        add("Japan", "Iran",             +0.05);
        add("Japan", "Saudi Arabia",     +0.08);
        add("Japan", "South Korea",      +0.05);
        add("South Korea", "Australia",  +0.05);
        add("South Korea", "Iran",       +0.05);

        // ── New 2026 qualifiers — UEFA ────────────────────────────────────────
        add("Germany",  "Czechia",           +0.08);
        add("Germany",  "Bosnia Herzegovina",+0.05);
        add("Germany",  "Sweden",            +0.05);
        add("Germany",  "Norway",            +0.05);
        add("France",   "Norway",            +0.08); // France recent H2H edge
        add("England",  "Norway",            +0.03);
        add("England",  "Sweden",            +0.05);
        add("Spain",    "Sweden",            +0.08);
        add("Spain",    "Norway",            +0.05);
        add("Netherlands","Sweden",          +0.05);
        add("Netherlands","Norway",          +0.03);
        add("Portugal", "Czechia",           +0.08);
        add("Portugal", "Bosnia Herzegovina",+0.10);
        add("Croatia",  "Bosnia Herzegovina",+0.03); // regional derby, slight Croatia edge
        add("Norway",   "Sweden",            +0.02); // Scandinavian derby, very even

        // ── New 2026 qualifiers — CAF ─────────────────────────────────────────
        add("France",    "Algeria",          +0.12); // France historical dominance
        add("Argentina", "Algeria",          +0.10);
        add("Germany",   "Algeria",          +0.10); // 2014 WC R16
        add("Germany",   "Ghana",            +0.08);
        add("Spain",     "Algeria",          +0.10);
        add("Portugal",  "Cape Verde",       +0.15);
        add("Spain",     "Cape Verde",       +0.15);
        add("Uruguay",   "Cape Verde",       +0.10);
        add("Portugal",  "DR Congo",         +0.12);
        add("Colombia",  "DR Congo",         +0.08);
        add("Morocco",   "Algeria",          +0.03); // Maghreb derby, slight Morocco edge
        add("Senegal",   "Algeria",          +0.03);
        add("Algeria",   "Egypt",            +0.03);

        // ── New 2026 qualifiers — AFC ─────────────────────────────────────────
        add("Japan",      "Jordan",          +0.12);
        add("South Korea","Jordan",          +0.10);
        add("Saudi Arabia","Jordan",         +0.05);
        add("Iran",        "Jordan",         +0.05);
        add("Argentina",   "Jordan",         +0.15);
        add("Austria",     "Jordan",         +0.08);

        // ── New 2026 qualifiers — CONCACAF ────────────────────────────────────
        add("Brazil",   "Haiti",             +0.18);
        add("Morocco",  "Haiti",             +0.12);
        add("Scotland", "Haiti",             +0.08);
        add("Germany",  "Curacao",           +0.18);
        add("Ecuador",  "Curacao",           +0.12);
        add("Ivory Coast","Curacao",         +0.10);

        // ── Cross-confederation notable ───────────────────────────────────────
        add("Australia", "France",           -0.08);
        add("Colombia",  "Senegal",          +0.03);
        add("Egypt",     "Saudi Arabia",     +0.05);
        add("Morocco",   "Belgium",          +0.05);
        add("Morocco",   "Portugal",         +0.05); // Won 2022 QF
        add("USA",       "Portugal",         -0.05);
    }

    private static void add(String a, String b, double aAdv) {
        H2H.put(a + "|" + b,  aAdv);
        H2H.put(b + "|" + a, -aAdv);
    }

    /** Returns expected-goals advantage for teamA over teamB (0 if no data). */
    public static double getAdvantage(String a, String b) {
        return H2H.getOrDefault(a + "|" + b, 0.0);
    }

    /** Human-readable summary of the H2H picture. */
    public static String describe(String a, String b) {
        double adv = getAdvantage(a, b);
        if (adv >  0.12) return a + " historically dominant";
        if (adv >  0.04) return a + " have H2H edge";
        if (adv < -0.12) return b + " historically dominant";
        if (adv < -0.04) return b + " have H2H edge";
        return "Historically even";
    }
}