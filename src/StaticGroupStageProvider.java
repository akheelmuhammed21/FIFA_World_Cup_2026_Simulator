import java.util.*;

/**
 * Official FIFA World Cup 2026 group draw results (December 5, 2025 – Kennedy Center, Washington D.C.).
 * Used as fallback when the live API is unavailable or no API key is configured.
 */
public class StaticGroupStageProvider implements GroupStageProvider {

    @Override
    public Map<String, List<String>> fetchGroups() {
        Map<String, List<String>> g = new LinkedHashMap<>();
        g.put("A", Arrays.asList("Mexico",      "South Korea",       "South Africa",   "Czechia"));
        g.put("B", Arrays.asList("Canada",       "Switzerland",       "Qatar",          "Bosnia Herzegovina"));
        g.put("C", Arrays.asList("Brazil",       "Morocco",           "Scotland",       "Haiti"));
        g.put("D", Arrays.asList("USA",           "Paraguay",          "Australia",      "Turkey"));
        g.put("E", Arrays.asList("Germany",       "Ecuador",           "Ivory Coast",    "Curacao"));
        g.put("F", Arrays.asList("Netherlands",   "Japan",             "Tunisia",        "Sweden"));
        g.put("G", Arrays.asList("Belgium",       "Iran",              "Egypt",          "New Zealand"));
        g.put("H", Arrays.asList("Spain",         "Uruguay",           "Saudi Arabia",   "Cape Verde"));
        g.put("I", Arrays.asList("France",        "Senegal",           "Norway",         "Iraq"));
        g.put("J", Arrays.asList("Argentina",     "Austria",           "Algeria",        "Jordan"));
        g.put("K", Arrays.asList("Portugal",      "Colombia",          "Uzbekistan",     "DR Congo"));
        g.put("L", Arrays.asList("England",       "Croatia",           "Panama",         "Ghana"));
        return g;
    }
}