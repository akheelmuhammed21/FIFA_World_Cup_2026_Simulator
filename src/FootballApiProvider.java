import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;

/**
 * Fetches the official group stage draw from the football-data.org API (v4).
 *
 * Setup (one of three options):
 *   1. Environment variable:  export FD_API_KEY=your_key
 *   2. JVM property:          -DFD_API_KEY=your_key
 *   3. File config.properties in the working directory containing: FD_API_KEY=your_key
 *
 * Free-tier API keys: https://www.football-data.org/client/register
 */
public class FootballApiProvider implements GroupStageProvider {

    private static final String STANDINGS_URL =
        "https://api.football-data.org/v4/competitions/WC2026/standings";

    private final String apiKey;

    public FootballApiProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Reads the API key from env / JVM property / config file.
     * Returns null if no key is found anywhere.
     */
    public static FootballApiProvider fromEnv() {
        String key = System.getenv("FD_API_KEY");
        if (key == null) key = System.getProperty("FD_API_KEY");
        if (key == null) {
            try (InputStream is = new FileInputStream("config.properties")) {
                Properties p = new Properties();
                p.load(is);
                key = p.getProperty("FD_API_KEY");
            } catch (Exception ignored) {}
        }
        return (key != null && !key.isEmpty()) ? new FootballApiProvider(key) : null;
    }

    @Override
    public Map<String, List<String>> fetchGroups() throws Exception {
        String json = fetchJson(STANDINGS_URL);
        Map<String, List<String>> groups = parseStandings(json);
        if (groups.size() != 12)
            throw new IOException("Expected 12 groups, got " + groups.size());
        return groups;
    }

    // ── HTTP ──────────────────────────────────────────────────────────────────

    private String fetchJson(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-Auth-Token", apiKey);
        conn.setConnectTimeout(6_000);
        conn.setReadTimeout(12_000);

        int status = conn.getResponseCode();
        if (status != 200)
            throw new IOException("football-data.org returned HTTP " + status);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    // ── JSON parsing (no external library) ───────────────────────────────────

    /**
     * Parses the football-data.org /standings response.
     *
     * Expected shape (simplified):
     * {
     *   "standings": [
     *     { "group": "GROUP_A", "table": [ {"team": {"name": "Mexico"}}, ... ] },
     *     ...
     *   ]
     * }
     */
    private Map<String, List<String>> parseStandings(String json) {
        Map<String, List<String>> result = new LinkedHashMap<>();

        // Find each "group":"GROUP_X" occurrence and its position
        Pattern groupTag  = Pattern.compile("\"group\"\\s*:\\s*\"GROUP_([A-L])\"");
        Pattern teamBlock = Pattern.compile("\"team\"\\s*:\\s*\\{([^}]+)\\}");
        Pattern nameField = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

        Matcher gm = groupTag.matcher(json);
        List<int[]> positions  = new ArrayList<>();
        List<String> letters   = new ArrayList<>();

        while (gm.find()) {
            positions.add(new int[]{gm.start(), gm.end()});
            letters.add(gm.group(1));
        }

        for (int i = 0; i < letters.size(); i++) {
            // Only process the first occurrence of each group label (TOTAL standings)
            if (result.containsKey(letters.get(i))) continue;

            int start = positions.get(i)[1];
            int end   = (i + 1 < positions.size()) ? positions.get(i + 1)[0] : json.length();
            String block = json.substring(start, end);

            List<String> teams = new ArrayList<>();
            Matcher tb = teamBlock.matcher(block);
            while (tb.find() && teams.size() < 4) {
                Matcher nm = nameField.matcher(tb.group(1));
                if (nm.find()) teams.add(normalize(nm.group(1)));
            }
            if (teams.size() == 4) result.put(letters.get(i), teams);
        }
        return result;
    }

    // ── Name normalisation (API names → simulator names) ─────────────────────

    private static String normalize(String name) {
        switch (name) {
            case "Czech Republic":              return "Czechia";
            case "Korea Republic":              return "South Korea";
            case "Bosnia and Herzegovina":
            case "Bosnia & Herzegovina":        return "Bosnia Herzegovina";
            case "Ivory Coast":
            case "Côte d'Ivoire":               return "Ivory Coast";
            case "DR Congo":
            case "Congo DR":
            case "Democratic Republic of Congo":return "DR Congo";
            case "Türkiye":                     return "Turkey";
            case "Curaçao":                     return "Curacao";
            case "United States":               return "USA";
            default:                            return name;
        }
    }
}