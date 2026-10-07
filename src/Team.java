public class Team {
    public final String name;
    public final String confederation;
    public final int strength;          // 1–100 aggregate rating
    public final int fifaRanking;       // FIFA World Ranking position (1 = best)
    public final double form;           // -1.0 (terrible) to +1.0 (excellent) — recent 10 matches
    public final double northAmericaFactor; // expected-goals boost playing in NA (0.0–0.12)

    // Squad — attached after team list is built; null = no player data loaded
    public Squad squad;

    // Group label ("A"–"L") — set when groups are built; used to avoid same-group R32 ties
    public String group;

    // Group stage accumulators
    public int played, wins, draws, losses, goalsFor, goalsAgainst, points;

    public Team(String name, String confederation, int strength,
                int fifaRanking, double form, double northAmericaFactor) {
        this.name = name;
        this.confederation = confederation;
        this.strength = strength;
        this.fifaRanking = fifaRanking;
        this.form = form;
        this.northAmericaFactor = northAmericaFactor;
    }

    public void recordResult(int gf, int ga) {
        played++;
        goalsFor += gf;
        goalsAgainst += ga;
        if (gf > ga)       { wins++;  points += 3; }
        else if (gf == ga) { draws++; points++;    }
        else               { losses++;             }
    }

    public void resetStats() {
        played = wins = draws = losses = goalsFor = goalsAgainst = points = 0;
    }

    public int goalDifference() { return goalsFor - goalsAgainst; }

    public String formLabel() {
        if (form >= 0.65) return "Excellent";
        if (form >= 0.40) return "Good";
        if (form >= 0.15) return "Average";
        if (form >= -0.10) return "Poor";
        return "Bad";
    }

    @Override
    public String toString() { return name; }
}