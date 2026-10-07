public class Player {
    public enum Position { GK, DEF, MID, FWD }
    public enum Fitness  { FIT, DOUBT, OUT }

    public final String   name;
    public final Position position;
    public final String   club;
    public final int      age;
    public final double   form;    // -1.0 to +1.0
    public final Fitness  fitness;
    public final int      valueM;  // market value in millions EUR
    public final boolean  isStar;

    public Player(String name, Position pos, String club, int age,
                  double form, Fitness fitness, int valueM, boolean isStar) {
        this.name     = name;
        this.position = pos;
        this.club     = club;
        this.age      = age;
        this.form     = form;
        this.fitness  = fitness;
        this.valueM   = valueM;
        this.isStar   = isStar;
    }

    public String fitnessLabel() {
        switch (fitness) {
            case FIT:   return "Fit";
            case DOUBT: return "Doubt";
            case OUT:   return "OUT";
            default:    return "?";
        }
    }
}