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
            return new Time(resolvePhase(phaseName, value), true, false, false);
        }
        else if (value.toLowerCase().startsWith("end(") && value.endsWith(")")) {
            final String phaseName = value.substring(value.indexOf("(") + 1, value.lastIndexOf(")"));
            if (phaseName.equalsIgnoreCase("current"))
                return new Time(null, false, false, false);
            return new Time(resolvePhase(phaseName, value), false, false, false);
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

    /**
     * The phase named inside "start(...)" or "end(...)".  A name that is no phase is refused: a time with no
     * phase means the end of the current phase, so "end(turn)" would otherwise be read, without a word, as
     * "end(current)".
     */
    private static Phase resolvePhase(String phaseName, String value) throws InvalidCardDefinitionException {
        final Phase phase = Phase.findPhase(phaseName);
        if (phase == null)
            throw new InvalidCardDefinitionException("Unable to resolve time: " + value + " ('" + phaseName
                    + "' is not a phase; the end of the turn is 'endofturn', the end of the current phase 'end(current)')");
        return phase;
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

        /**
         * @return Whether something lasting until this time, begun in a phase of the given kind, is gone when
         * that phase ends: the time is the end of the current phase, or the end of a phase of that kind.
         */
        public boolean endsWithCurrentPhase(Phase current) {
            if (endOfTurn || permanent || start)
                return false;
            return phase == null || phase == current;
        }

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
