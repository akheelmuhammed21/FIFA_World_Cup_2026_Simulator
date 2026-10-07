public class Pacing {
    /** Set false for headless batch runs (Monte Carlo) to skip all dramatic pauses. */
    public static volatile boolean enabled = true;

    public static void ms(long millis) {
        if (!enabled) return;
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
