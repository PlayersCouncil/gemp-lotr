package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

/**
 * This interface holds the handful of accessors that every other virtual table interface is built on.  It is not a
 * true interface, but since Java does not support partial classes this will have to do.
 *
 * The virtual table is a headless client: it owns a game, answers that game's decisions for both players, and can
 * rearrange the board directly.  It is shared by the unit test rig (VirtualTableScenario, in the test sources) and by
 * anything in the server proper that needs to drive a game without a human on each side.
 */
public interface TableBase {
	/*
	 * These three functions are used in the base interfaces but are unnecessary in the actual implementation, where the
	 * underlying fields can be used instead.
	 *
	 */

	/**
	 * @return Gets the virtual game table.
	 */
	DefaultLotroGame game();

	/**
	 * @return Gets the game state of the virtual table.
	 */
	GameState gameState();

	/**
	 * @return Gets the user decision manager.  This contains information relating to the decision that Gemp is
	 * currently waiting on.
	 */
	DefaultUserFeedback userFeedback();

	/**
	 * @return The name of the first player, which the vast majority of the time is the Free Peoples player by
	 * convention.  All of the "Freeps" functions throughout the virtual table act on behalf of this player.
	 */
	String P1();

	/**
	 * @return The name of the second player, which the vast majority of the time is the Shadow player by convention.
	 * All of the "Shadow" functions throughout the virtual table act on behalf of this player.
	 */
	String P2();

	/**
	 * What the table has kept of all that the given player's seat has been sent by the game.  For the table's own
	 * readings (HandIsRevealed, PlayerSeesCardInHand), not for callers.
	 * @param player The name of the player in the seat asked about.
	 * @return That seat's channel, or null on a table that keeps no channels for its seats (see
	 * VirtualTable.KeepSeatChannels).
	 */
	SeatChannel seatChannel(String player);

	PhysicalCardImpl GetFreepsCard(String cardName);
	PhysicalCardImpl GetShadowCard(String cardName);
	PhysicalCardImpl GetCard(String player, String cardName);

	/**
	 * A sanity check on the table's own bookkeeping, used where a procedure has to end up in a known state for
	 * whatever follows it to make sense.  By default a failed check throws; the unit test rig overrides this so that a
	 * failed check is reported as an ordinary test failure.
	 * @param condition What is expected to be true.
	 * @param message What to report if it is not.
	 */
	default void Verify(boolean condition, String message) {
		if(!condition)
			throw new IllegalStateException(message);
	}
}
