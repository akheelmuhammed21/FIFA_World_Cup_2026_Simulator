import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
 * Fetches live player form ratings from FotMob's unofficial REST API and
 * overlays them on top of the static squad base (player names, positions,
 * fitness, and market values still come from SquadData).
 *
 * Flow:
 *   1. SquadData.attachAll()  — base layer (always succeeds)
 *   2. fetchTeamIds()         — resolve WC2026 team IDs from the league group tables
 *                               (GET /api/data/leagues?id=77)
 *   3. overlayForm() per team — replace each matched player's form with the player rating
 *                               from the team's current squad (GET /api/data/teams?id=N)
 *
 * Coverage is partial: only players in the team's latest FotMob squad who have a rating
 * are matched, and FotMob leaves ratings null for some nations (e.g. USA, Mexico, Japan).
 * Per-team failures are silent; those teams keep their static form.
 * If the league ID fetch fails entirely, static form is kept for all teams.
 *
 * WARNING: FotMob has no official API. Endpoints may change without notice.
 *          Verify WC_LEAGUE_ID at fotmob.com before each tournament.
 */
public class FotMobSquadProvider implements SquadProvider {

    private static final String BASE       = "https://www.fotmob.com/api/data";
    private static final int    WC_LEAGUE_ID = 77;   // FIFA World Cup on FotMob (2026 season confirmed)
    private static final int    MIN_MATCHED  = 3;    // min players matched before accepting overlay

    private int playersUpdated;

    @Override
    public void attachSquads(List<Team> teams) {
        SquadData.attachAll(teams);  // base layer — always applied first

        Map<String, Integer> teamIds;
        try {
            teamIds = fetchTeamIds();
        } catch (Exception e) {
            System.out.println("  [FotMob: team ID fetch failed (" + e.getMessage() + ") — static form used]");
            return;
        }

        int updated = 0;
        for (Team team : teams) {
            Integer fotmobId = teamIds.get(normalize(team.name));
            if (fotmobId == null) continue;
            try {
                overlayForm(team, fotmobId);
                updated++;
            } catch (Exception ignored) {
                // Keep static form for this team
            }
        }
        System.out.println("  [FotMob: live form loaded for " + updated + "/" + teams.size()
            + " teams (" + playersUpdated + " players); others use static form]");
    }

    // ── Team ID resolution ─────────────────────────────────────────────────────

    private Map<String, Integer> fetchTeamIds() throws Exception {
        String json = get(BASE + "/leagues?id=" + WC_LEAGUE_ID);
        Map<String, Integer> ids = parseTeamIds(json);
        if (ids.isEmpty()) throw new IOException("No team IDs parsed from league response");
        return ids;
    }

    /**
     * Matches group-table rows in the FotMob league JSON, e.g.
     *   {"name":"Mexico","shortName":"Mexico","id":6710,"pageUrl":"/teams/6710/overview/mexico",...}
     * Anchoring on the "/teams/" pageUrl skips the many player id/name pairs elsewhere in the response.
     */
    private Map<String, Integer> parseTeamIds(String json) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Pattern p = Pattern.compile(
            "\"name\"\\s*:\\s*\"([^\"]+)\"[^{}]*?\"id\"\\s*:\\s*(\\d+)\\s*,\\s*\"pageUrl\"\\s*:\\s*\"/teams/");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String name = normalize(m.group(1));
            int    id   = Integer.parseInt(m.group(2));
            result.putIfAbsent(name, id);
        }
        return result;
    }

    // ── Form overlay ───────────────────────────────────────────────────────────

    private void overlayForm(Team team, int fotmobId) throws Exception {
        if (team.squad == null) return;

        String json = get(BASE + "/teams?id=" + fotmobId);
        Map<String, Double> ratings = parsePlayerRatings(json);
        if (ratings.isEmpty()) throw new IOException("No player ratings parsed for " + team.name);

        // Build the overlay first and apply it only if enough players matched,
        // so a rejected team keeps its static form untouched.
        List<Player> players = team.squad.players;
        List<Player> updated = new ArrayList<>(players);
        int matched = 0;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Double rating = ratings.get(normalizeName(p.name));
            if (rating == null) continue;
            updated.set(i, withForm(p, fotmobToForm(rating)));
            matched++;
        }

        if (matched < MIN_MATCHED)
            throw new IOException("Only " + matched + " players matched for " + team.name);
        for (int i = 0; i < players.size(); i++) players.set(i, updated.get(i));
        playersUpdated += matched;
    }

    /**
     * Parses FotMob team JSON for player name + season rating.
     * Handles the "squad" → "members" array structure:
     *   {"id":466439,"name":"Mike Maignan",...,"role":{...},"injury":null,"rating":7.36,...}
     * Players without a rating ("rating":null) are skipped. The nested-object allowance
     * stops the match from running past the end of one member into the next.
     */
    private Map<String, Double> parsePlayerRatings(String json) {
        Map<String, Double> result = new LinkedHashMap<>();

        // Match player blocks containing both a name and a rating num
        Pattern memberPat = Pattern.compile(
            "\"name\"\\s*:\\s*\"([^\"]+)\"(?:[^{}]|\\{[^{}]*\\})*?\"rating\"\\s*:\\s*([\\d.]+)"
        );
        Matcher m = memberPat.matcher(json);
        while (m.find()) {
            String name = normalizeName(m.group(1));
            try {
                double rating = Double.parseDouble(m.group(2));
                if (rating >= 3.0 && rating <= 10.0)
                    result.putIfAbsent(name, rating);
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    /** Builds a new Player identical to {@code p} but with updated form. */
    private static Player withForm(Player p, double newForm) {
        return new Player(p.name, p.position, p.club, p.age,
                          newForm, p.fitness, p.valueM, p.isStar);
    }

    /**
     * FotMob rating → form scale (-1.0 to +1.0), calibrated so live and static form share a scale.
     * Moment-matched (Oct 2026) across all 706 rated WC squad players (mean 6.87, sd 0.69) against
     * SquadData's fit/doubtful players (mean 0.32, sd 0.20): 6.0 → 0.07, 6.87 → 0.32, 7.75 → 0.57, 9.0 → 0.93.
     */
    private static double fotmobToForm(double rating) {
        return Math.max(-1.0, Math.min(1.0, (rating - 5.75) / 3.5));
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private String get(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        conn.setConnectTimeout(8_000);
        conn.setReadTimeout(15_000);

        int status = conn.getResponseCode();
        if (status != 200)
            throw new IOException("FotMob returned HTTP " + status);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    // ── Name normalisation ─────────────────────────────────────────────────────

    /** FotMob team names → simulator names. */
    private static String normalize(String name) {
        switch (name) {
            case "United States":
            case "United States of America":  return "USA";
            case "Korea Republic":            return "South Korea";
            case "Türkiye":
            case "Turkiye":                   return "Turkey";
            case "Bosnia & Herzegovina":
            case "Bosnia and Herzegovina":    return "Bosnia Herzegovina";
            case "Ivory Coast":
            case "Côte d'Ivoire":             return "Ivory Coast";
            case "DR Congo":
            case "Congo DR":
            case "Democratic Republic of Congo": return "DR Congo";
            case "Curaçao":                   return "Curacao";
            case "Czech Republic":            return "Czechia";
            default:                          return name;
        }
    }

    /** Lowercase + strip diacritics for fuzzy player name matching. */
    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT)
            .replace("é","e").replace("è","e").replace("ê","e").replace("ë","e")
            .replace("á","a").replace("à","a").replace("â","a").replace("ä","a").replace("ã","a")
            .replace("ó","o").replace("ò","o").replace("ô","o").replace("ö","o")
            .replace("ú","u").replace("ù","u").replace("û","u").replace("ü","u")
            .replace("í","i").replace("ì","i").replace("î","i").replace("ï","i")
            .replace("ñ","n").replace("ç","c").replace("ý","y").replace("ß","ss")
            .replace("-"," ").trim();
    }
}