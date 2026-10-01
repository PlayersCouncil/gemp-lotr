package com.gempukku.lotro.hall;

public record GameTimer(boolean longGame, String name, int maxSecondsPerPlayer, int maxSecondsPerDecision) {

    public static final GameTimer DEFAULT_TIMER = new GameTimer(false, "Default", 60 * 45, 60 * 6);
    public static final GameTimer BLITZ_TIMER = new GameTimer(false, "Blitz!", 60 * 25, 60 * 3);
    public static final GameTimer SLOW_TIMER = new GameTimer(false, "Slow", 60 * 80, 60 * 10);
    public static final GameTimer GLACIAL_TIMER = new GameTimer(true, "Glacial", 60 * 60 * 24, 60 * 60 * 24);
    // 5 minutes timeout, 40 minutes per game per player
    public static final GameTimer COMPETITIVE_TIMER = new GameTimer(false, "Competitive", 60 * 40, 60 * 5);
    public static final GameTimer CHAMPIONSHIP_TIMER = new GameTimer(false, "WC", 60 * 20, 60 * 10);
    public static final GameTimer EXPANDED_CHAMPIONSHIP_TIMER = new GameTimer(false, "WC_Expanded", 60 * 25, 60 * 10);
    public static final GameTimer TOURNAMENT_TIMER = new GameTimer(false, "Tournament", 60 * 40, 60 * 5);

    /**
     * The timers a player can pick for a Casual table, keyed by the code the Play form sends (resolved by
     * {@link #ResolveTimer}), in menu order.
     */
    public static final java.util.Map<String, GameTimer> HALL_TIMERS;
    static {
        var timers = new java.util.LinkedHashMap<String, GameTimer>();
        timers.put("default", DEFAULT_TIMER);
        timers.put("blitz", BLITZ_TIMER);
        timers.put("WC", CHAMPIONSHIP_TIMER);
        timers.put("slow", SLOW_TIMER);
        timers.put("glacial", GLACIAL_TIMER);
        HALL_TIMERS = java.util.Collections.unmodifiableMap(timers);
    }

    @Override
    public String toString() {
        return "This game table uses the '" + name + "' timer.  " + describeLimits();
    }

    /**
     * The timer's limits in a sentence, e.g. "Each player has a total time bank of 45 minutes, and will time out with
     * a loss if they run out of their time bank or take longer than 6 minutes between actions."  The Play popup's Game
     * Timer (i) shows it.
     */
    public String describeLimits() {
        return "Each player has a total time bank of " + duration(maxSecondsPerPlayer)
                + ", and will time out with a loss if they run out of their time bank or take longer than "
                + duration(maxSecondsPerDecision) + " between actions.";
    }

    // "45 minutes", "1 minute", "1 day" (the Glacial timer's 1440 minutes), "2 hours"
    private static String duration(int seconds) {
        int minutes = seconds / 60;
        if (minutes >= 1440 && minutes % 1440 == 0)
            return plural(minutes / 1440, "day");
        if (minutes > 60 && minutes % 60 == 0)
            return plural(minutes / 60, "hour");
        return plural(minutes, "minute");
    }

    private static String plural(int count, String unit) {
        return count + " " + unit + (count == 1 ? "" : "s");
    }
    public static GameTimer ResolveTimer(String timer) {
        if (timer != null) {
            switch (timer.toLowerCase()) {
                case "blitz":
                    return GameTimer.BLITZ_TIMER;
                case "slow":
                    return GameTimer.SLOW_TIMER;
                case "glacial":
                    return GameTimer.GLACIAL_TIMER;
                case "competitive":
                    return GameTimer.COMPETITIVE_TIMER;
                case "wc":
                    return GameTimer.CHAMPIONSHIP_TIMER;
                case "wc_expanded":
                    return GameTimer.EXPANDED_CHAMPIONSHIP_TIMER;
                case "tournament":
                    return GameTimer.TOURNAMENT_TIMER;
            }
        }
        return GameTimer.DEFAULT_TIMER;
    }
}
