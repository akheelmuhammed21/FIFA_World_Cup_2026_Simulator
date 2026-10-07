import java.util.List;

/** Fallback squad provider — hardcoded data from SquadData. */
public class StaticSquadProvider implements SquadProvider {
    @Override
    public void attachSquads(List<Team> teams) {
        SquadData.attachAll(teams);
    }
}