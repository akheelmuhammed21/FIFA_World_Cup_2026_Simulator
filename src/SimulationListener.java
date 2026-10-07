import java.util.List;

public interface SimulationListener {
    default void onGroupComplete(Group group) {}
    default void onKnockoutBracketReady(List<Team> bracket) {}
    default void onKnockoutMatch(String round, Match match, Team winner, boolean penalties) {}
    default void onChampion(Team champion, Team runnerUp, Team third) {}
}