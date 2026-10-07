import java.util.List;

/**
 * Supplies squad and player data for all tournament teams.
 * Implementations may fetch from live APIs or use embedded static data.
 */
public interface SquadProvider {
    void attachSquads(List<Team> teams) throws Exception;
}