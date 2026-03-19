package com.gempukku.lotro.framework;

import com.gempukku.lotro.game.DefaultUserFeedback;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.GameState;
import com.gempukku.lotro.logic.timing.DefaultLotroGame;

/**
 * This interface holds all the static definitions used throughout the test rig.  It is not a true interface, but
 * since Java does not support partial classes this will have to do.
 */
public interface TestBase {
	/*
	 * These three functions are used in the base interfaces but are unnecessary in the actual implementation, where the
	 * underlying fields can be used instead.
	 *
	 */

	/**
	 * @return Gets the virtual game table used by the test scenario.
	 */
	DefaultLotroGame game();

	/**
	 * @return Gets the game state of the virtual table used by the test scenario.
	 */
	GameState gameState();

	/**
	 * @return Gets the user decision manager.  This contains information relating to the decision that Gemp is
	 * currently waiting on.
	 */
	DefaultUserFeedback userFeedback();


	PhysicalCardImpl GetFreepsCard(String cardName);
	PhysicalCardImpl GetShadowCard(String cardName);
	PhysicalCardImpl GetCard(String player, String cardName);
}
