import java.util.List;

public class Squad {
    public final String      teamName;
    public final List<Player> players;

    public Squad(String teamName, List<Player> players) {
        this.teamName = teamName;
        this.players  = players;
    }

    // Stars in form contribute positively to expected goals
    public double starPowerBonus() {
        double bonus = 0;
        for (Player p : players)
            if (p.isStar && p.fitness == Player.Fitness.FIT && p.form > 0.3)
                bonus += Math.min(0.04, p.form * 0.04);
        return Math.min(0.16, bonus);
    }

    // Injured/doubtful stars reduce expected goals
    public double injuryPenalty() {
        double pen = 0;
        for (Player p : players) {
            if (!p.isStar) continue;
            if      (p.fitness == Player.Fitness.OUT)   pen -= 0.09;
            else if (p.fitness == Player.Fitness.DOUBT) pen -= 0.04;
        }
        return Math.max(-0.22, pen);
    }

    // Average squad form weighted lightly
    public double squadFormModifier() {
        if (players.isEmpty()) return 0;
        double avg = players.stream().mapToDouble(p -> p.form).average().orElse(0);
        return avg * 0.08;
    }

    // GK form adds/subtracts from penalty save probability
    public double gkPenaltySaveBonus() {
        return players.stream()
            .filter(p -> p.position == Player.Position.GK && p.isStar)
            .mapToDouble(p -> p.form * 0.06)
            .findFirst().orElse(0.0);
    }

    // Combined offensive lambda modifier, capped at ±0.25
    public double attackLambdaModifier() {
        double total = starPowerBonus() + injuryPenalty() + squadFormModifier();
        return Math.max(-0.25, Math.min(0.25, total));
    }

    public Player getGK() {
        return players.stream()
            .filter(p -> p.position == Player.Position.GK)
            .findFirst().orElse(null);
    }

    // Top 3 stars for display
    public String topStarsDisplay() {
        StringBuilder sb = new StringBuilder();
        players.stream().filter(p -> p.isStar).limit(3).forEach(p -> {
            if (sb.length() > 0) sb.append(", ");
            String fit = p.fitness != Player.Fitness.FIT ? " [" + p.fitnessLabel() + "]" : "";
            sb.append(p.name).append(fit);
        });
        return sb.toString();
    }
}