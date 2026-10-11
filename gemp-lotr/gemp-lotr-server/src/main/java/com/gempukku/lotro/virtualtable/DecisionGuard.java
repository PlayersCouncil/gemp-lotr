package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;

import java.util.Arrays;

/**
 * What the virtual table says when it is asked to answer or to read something that is not there.  An answer for a
 * player who is not being asked anything, an action of a card that is not on offer, and a reading of a parameter
 * the pending decision does not have each used to die of a NullPointerException somewhere below the call.  Each is
 * now a failed {@link TableBase#Verify}, whose message names the player, what was attempted, and what each player
 * is in fact being asked.  So is a choice of cards given as the answer to a decision that is no choice of cards,
 * which the game used to take for whatever such an answer means there: a pass, a number, an assignment of nobody;
 * and so is declining to choose where the game is not asking for cards, or is asking for at least one.
 *
 * Package-private and not part of the table: nothing here becomes a method of VirtualTable or ScenarioTable.
 */
final class DecisionGuard {
	private DecisionGuard() { }

	/**
	 * Reads a parameter of the given player's pending decision that the caller cannot go on without, and fails,
	 * saying why, if that player is not being asked anything or the decision has no such parameter.
	 * @param attempted What was being done, as a clause that names the player: "P1 was to choose 'Yes'".
	 * @return The parameter's values.
	 */
	static String[] requiredParam(TableBase table, String player, String paramName, String attempted) {
		AwaitingDecision decision = table.userFeedback().getAwaitingDecision(player);
		String[] values = decision == null ? null : decision.getDecisionParameters().get(paramName);
		if(values == null) {
			table.Verify(false, attempted + ", but "
					+ (decision == null ? "that player is not being asked anything."
							: "the decision in front of that player has no '" + paramName + "'.  " + askedOf(table, player))
					+ othersAsked(table, player));
		}
		return values;
	}

	/**
	 * Reads a parameter of the given player's pending decision for a getter, and fails as
	 * {@link #requiredParam} does if there is none to read.
	 */
	static String[] readParam(TableBase table, String player, String paramName) {
		return requiredParam(table, player, paramName,
				"The '" + paramName + "' of " + player + "'s decision was to be read");
	}

	/**
	 * Fails unless the given player is being asked to choose cards in a way that the answer about to be given
	 * fits, saying what was to be chosen and what each player is in fact being asked.  The game asks for cards in
	 * two ways: among cards where they lie, in play or in a hand, which are answered by their ids (CARD_SELECTION),
	 * and among cards it shows, from a pile or a hand that is looked at, which are answered by their places in the
	 * showing (ARBITRARY_CARDS).  Neither answer means at any other decision what it was meant to mean.
	 * @param attempted What was being done, as a clause that names the player: "P1 was to choose Aragorn".
	 * @param fits The ways of asking for cards that the answer fits.
	 * @return The pending decision, which asks for cards in one of those ways.
	 */
	static AwaitingDecision requiredCardChoice(TableBase table, String player, String attempted, AwaitingDecisionType... fits) {
		AwaitingDecision decision = table.userFeedback().getAwaitingDecision(player);
		if(decision == null) {
			table.Verify(false, attempted + ", but that player is not being asked anything." + othersAsked(table, player));
			return null;
		}
		AwaitingDecisionType type = decision.getDecisionType();
		if(!Arrays.asList(fits).contains(type)) {
			String why = type == AwaitingDecisionType.ARBITRARY_CARDS
					? "that player is choosing among cards that are shown, which are not answered by card id"
							+ " (FreepsChooseCards and ShadowChooseCards answer either kind)."
					: type == AwaitingDecisionType.CARD_SELECTION
					? "that player is choosing among cards where they lie, which are not answered by blueprint"
							+ " (FreepsChooseCards and ShadowChooseCards answer either kind)."
					: "the decision in front of that player is not a choice of cards.";
			table.Verify(false, attempted + ", but " + why + "  " + askedOf(table, player) + othersAsked(table, player));
		}
		return decision;
	}

	/**
	 * Fails unless the given player is being asked to choose cards, in either way, and may choose none (the least
	 * number the decision takes is 0), saying what each player is in fact being asked.  The empty answer that
	 * chooses no card means something else at every other decision: a pass at a choice of actions, a trigger
	 * declined, nobody assigned.  Cards that are only shown are a choice of none of them, and pass.
	 * @param attempted What was being done, as a clause that names the player: "P1 was to decline choosing".
	 */
	static void requiredChoiceThatMayBeDeclined(TableBase table, String player, String attempted) {
		AwaitingDecision decision = requiredCardChoice(table, player, attempted,
				AwaitingDecisionType.CARD_SELECTION, AwaitingDecisionType.ARBITRARY_CARDS);
		String[] min = decision.getDecisionParameters().get("min");
		int least = min == null ? 0 : Integer.parseInt(min[0]);
		if(least > 0) {
			table.Verify(false, attempted + ", but at least " + least + (least == 1 ? " card" : " cards")
					+ " must be chosen there.  " + askedOf(table, player) + othersAsked(table, player));
		}
	}

	/**
	 * Fails unless the given player is being asked for a number, saying what each player is in fact being asked.
	 * @param attempted What was being done, as a clause that names the player: "The number filled in for P1 was
	 *                  to be read".
	 */
	static void requiredNumberQuestion(TableBase table, String player, String attempted) {
		AwaitingDecision decision = table.userFeedback().getAwaitingDecision(player);
		if(decision == null) {
			table.Verify(false, attempted + ", but that player is not being asked anything." + othersAsked(table, player));
		}
		else if(decision.getDecisionType() != AwaitingDecisionType.INTEGER) {
			table.Verify(false, attempted + ", but that player is not being asked for a number.  "
					+ askedOf(table, player) + othersAsked(table, player));
		}
	}

	/** The full names of the given cards, for a failure message; "no card" if none is given. */
	static String names(PhysicalCardImpl... cards) {
		if(cards.length == 0)
			return "no card";
		String[] names = new String[cards.length];
		for (int i = 0; i < cards.length; i++)
			names[i] = GameUtils.getFullName(cards[i]);
		return String.join(", ", names);
	}

	/**
	 * @return Why the given answer cannot be given for the given player: nothing is being asked of that player, or
	 * there is no answer (the action or choice that was looked for is not among those on offer).
	 */
	static String cannotAnswer(TableBase table, String player, String answer) {
		if(table.userFeedback().getAwaitingDecision(player) == null) {
			String attempted = answer == null ? " was to take an action or make a choice"
					: answer.isEmpty() ? " was to pass or decline" : " was to answer \"" + answer + "\"";
			return player + attempted + ", but is not being asked anything." + othersAsked(table, player);
		}
		return "What " + player + " was to take or choose is not on offer, so there is no answer to give.  "
				+ askedOf(table, player) + othersAsked(table, player);
	}

	/**
	 * @param kind The kind of action that was looked for: "a play", "a use", "a transfer".
	 * @return That the card is not offering the player an action of that kind, and what the player is being offered.
	 */
	static String notOffered(TableBase table, String player, PhysicalCardImpl card, String kind) {
		return GameUtils.getFullName(card) + " is not offering " + player + " " + kind + ".  "
				+ askedOf(table, player) + othersAsked(table, player);
	}

	/** What the given player is being asked at this moment, with the actions on offer if it is a choice of actions. */
	static String askedOf(TableBase table, String player) {
		AwaitingDecision decision = table.userFeedback().getAwaitingDecision(player);
		if(decision == null)
			return player + " is not being asked anything.";
		String[] actions = decision.getDecisionParameters().get("actionText");
		return player + " is being asked \"" + decision.getText() + "\""
				+ (actions == null ? "" : actions.length == 0 ? ", with no action on offer" : ", offering " + Arrays.toString(actions))
				+ ".";
	}

	// What everybody but the given player is being asked, to follow a sentence about that player.
	private static String othersAsked(TableBase table, String player) {
		StringBuilder others = new StringBuilder();
		for (String other : new String[] { table.P1(), table.P2() }) {
			if(!other.equals(player))
				others.append("  ").append(askedOf(table, other));
		}
		return others.toString();
	}
}
