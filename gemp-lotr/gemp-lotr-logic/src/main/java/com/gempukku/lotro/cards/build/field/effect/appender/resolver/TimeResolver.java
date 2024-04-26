package com.gempukku.lotro.cards.build.field.effect.appender.resolver;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.Phase;

public class TimeResolver {
    public static Time resolveTime(Object value, String defaultValue) throws InvalidCardDefinitionException {
        if (value == null)
            return parseTime(defaultValue.toLowerCase());
        if (value instanceof String)
            return parseTime(((String) value).toLowerCase());

        throw new InvalidCardDefinitionException("Unable to resolve time: " + value);
    }

    private static Time parseTime(String value) throws InvalidCardDefinitionException {

        if(value.toLowerCase().contains("endofgame") || value.toLowerCase().contains("perm")) {
            return new Time(null, false, false, true);
        }
        else if (value.toLowerCase().startsWith("start(") && value.endsWith(")")) {
            final String phaseName = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
            return new Time(Phase.findPhase(phaseName), true, false, false);
        }
        else if (value.toLowerCase().startsWith("end(") && value.endsWith(")")) {
            final String phaseName = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
            if (phaseName.equalsIgnoreCase("current"))
                return new Time(null, false, false, false);
            return new Time(Phase.findPhase(phaseName), false, false, false);
        }
        else if (value.equalsIgnoreCase("endofturn"))
            return new Time(null, false, true, false);
        else {
            Phase phase = Phase.findPhase(value);
            if(phase != null)
                return new Time(phase, true, false, false);
        }

        throw new InvalidCardDefinitionException("Unable to resolve time: " + value);
    }

    public static class Time {
        private final Phase phase;
        private final boolean start;
        private final boolean endOfTurn;
        private final boolean permanent;

        private Time(Phase phase, boolean start, boolean endOfTurn, boolean always) {
            this.phase = phase;
            this.start = start;
            this.endOfTurn = endOfTurn;
            this.permanent = always;
        }

        public Phase getPhase() {
            return phase;
        }

        public boolean isStart() {
            return start;
        }

        public boolean isEndOfTurn() {
            return endOfTurn;
        }

        public boolean isPermanent() { return permanent; }

        public String getHumanReadable() {
            if (endOfTurn) {
                return "the end of the turn";
            }
            else if (permanent) {
                return "the end of the game";
            }
            else if (phase == null) {
                return "the end of the current phase";
            }
            else if (start) {
                return "the start of the " + phase.getHumanReadable() + " phase";
            }
            else {
                return "the end of the " + phase.getHumanReadable() + " phase";
            }
        }
    }
}
