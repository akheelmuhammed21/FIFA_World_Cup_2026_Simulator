import java.util.List;
import java.util.Map;

/**
 * Supplies the official group stage draw: which 4 teams belong to each group.
 * Implementations may fetch from a live API or use embedded static data.
 */
public interface GroupStageProvider {
    /** Returns a map from group label ("A"–"L") to an ordered list of 4 team names. */
    Map<String, List<String>> fetchGroups() throws Exception;
}