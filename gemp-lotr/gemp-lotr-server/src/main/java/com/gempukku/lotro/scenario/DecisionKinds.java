package com.gempukku.lotro.scenario;

import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.List;
import java.util.Locale;

/**
 * The kinds of thing the game asks that a scenario's steps tell apart by what is being asked, not by its form:
 * the choice of the next skirmish is a choice of cards, as is an archery wound, a reconciling and a sanctuary heal.
 * A 'when' says one of these by its name (nextSkirmish, archeryWound, ...), or that it is not one (not(nextSkirmish)).
 *
 * The game does not mark its decisions with what they are for, so each is recognised here, and only here, by the
 * kind of decision it is and the words the game asks it with, which are fixed in the engine (gemp-lotr-logic):
 *
 *     nextSkirmish       cards      "Choose next skirmish to resolve"          PlayoutSkirmishesGameProcess, in the
 *                                                                               Assignment phase between skirmishes
 *     archeryWound       cards      "Choose character to assign archery       FellowshipPlayerAssignsArcheryDamage-
 *                                   wound to - remaining wounds: n";          GameProcess (and the Shadow player's
 *                                   "Choose minion to assign archery ..."      own, ShadowPlayer...), Archery phase
 *     reconcile          cards      "Reconcile - choose card to discard or    PlayerReconcilesAction: the Regroup
 *                                   press DONE"                                phase, or whatever reconciles
 *     sanctuary          cards      "Sanctuary healing - Choose companion to  SanctuaryRule, ChooseHealOrRemove-
 *                        choice     heal - remaining heals: n"; "Sanctuary    BurdenEffect: the start of a turn at a
 *                                   healing - remaining heals: n - heal the   sanctuary
 *                                   Ring-bearer or remove a burden?"
 *     anotherMove        choice     "Do you want to make another move?"       FellowshipPlayerChoosesToMoveOrStay-
 *                                                                               GameProcess, Regroup phase
 *     optionalResponses  action     "Optional responses"                      TurnProcedure: optional triggers
 *     requiredResponses  actionChoice "Required responses"                    TurnProcedure: required triggers that
 *                                                                               come together, to be ordered
 *     chooseSite         arbitrary  "Choose site to play"                     PlaySiteEffect: a site of an
 *                                                                               unnumbered (Shadows) path
 *     phaseActions       action     "Play <phase> action or Pass";            PlayersPlayPhaseActionsInOrder-, Player-
 *                                   "Choose action to play or Pass"            PlaysPhaseActionsUntilPasses-
 *                                                                               GameProcess; SkirmishActionProcedure-
 *                                                                               Action: a phase's own play-or-pass
 *
 * ScenarioDecisionKindsTest plays a real game to each of them, so that a change of wording in the engine fails a
 * test rather than quietly unmatching scenarios.
 */
public final class DecisionKinds {
    private DecisionKinds() { }

    public enum Kind {
        NEXT_SKIRMISH("nextSkirmish", List.of(AwaitingDecisionType.CARD_SELECTION), List.of("Choose next skirmish to resolve")),
        ARCHERY_WOUND("archeryWound", List.of(AwaitingDecisionType.CARD_SELECTION),
                List.of("Choose character to assign archery wound to", "Choose minion to assign archery wound to")),
        RECONCILE("reconcile", List.of(AwaitingDecisionType.CARD_SELECTION), List.of("Reconcile - choose card to discard")),
        SANCTUARY("sanctuary", List.of(AwaitingDecisionType.CARD_SELECTION, AwaitingDecisionType.MULTIPLE_CHOICE), List.of("Sanctuary healing")),
        ANOTHER_MOVE("anotherMove", List.of(AwaitingDecisionType.MULTIPLE_CHOICE), List.of("Do you want to make another move?")),
        OPTIONAL_RESPONSES("optionalResponses", List.of(AwaitingDecisionType.CARD_ACTION_CHOICE), List.of("Optional responses")),
        REQUIRED_RESPONSES("requiredResponses", List.of(AwaitingDecisionType.ACTION_CHOICE), List.of("Required responses")),
        CHOOSE_SITE("chooseSite", List.of(AwaitingDecisionType.ARBITRARY_CARDS), List.of("Choose site to play")),
        PHASE_ACTIONS("phaseActions", List.of(AwaitingDecisionType.CARD_ACTION_CHOICE), List.of("Choose action to play or Pass")) {
            @Override
            boolean says(String text) {
                return super.says(text) || text.matches("Play .+ action or Pass");
            }
        };

        /** What it is called in a 'when'. */
        public final String term;
        private final List<AwaitingDecisionType> _types;
        private final List<String> _openings;

        Kind(String term, List<AwaitingDecisionType> types, List<String> openings) {
            this.term = term;
            _types = types;
            _openings = openings;
        }

        boolean says(String text) {
            for (String opening : _openings)
                if (text.startsWith(opening))
                    return true;
            return false;
        }

        /** Whether a decision is one of these. */
        public boolean is(AwaitingDecision decision) {
            return decision != null && _types.contains(decision.getDecisionType()) && says(plain(decision.getText()));
        }
    }

    /** What a decision is for, if it is one of the kinds a 'when' can name; null if it is none of them. */
    public static Kind of(AwaitingDecision decision) {
        for (Kind kind : Kind.values())
            if (kind.is(decision))
                return kind;
        return null;
    }

    /** A kind by its name in a 'when' (not case-sensitive); null if it is none. */
    public static Kind named(String term) {
        for (Kind kind : Kind.values())
            if (kind.term.toLowerCase(Locale.ROOT).equals(term.trim().toLowerCase(Locale.ROOT)))
                return kind;
        return null;
    }

    /** Every kind's name, for a message that says what can be written. */
    public static List<String> terms() {
        return java.util.Arrays.stream(Kind.values()).map(kind -> kind.term).toList();
    }

    private static String plain(String text) {
        return text == null ? "" : text.replaceAll("<[^>]*>", "").trim();
    }
}
