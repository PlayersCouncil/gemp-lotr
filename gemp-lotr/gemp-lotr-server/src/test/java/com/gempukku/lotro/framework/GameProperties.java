package com.gempukku.lotro.framework;

import com.gempukku.lotro.common.Names;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.RuleUtils;

import java.util.List;

public interface GameProperties extends TestBase {
	default int GetBurdens() { return gameState().getBurdens(); }
	default void AddBurdens(int count) {
		gameState().addBurdens(count);
	}

	default void RemoveBurdens(int count) {
		gameState().removeBurdens(count);
	}

	default int GetFreepsCultureCount() { return GameUtils.getSpottableFPCulturesCount(game(), P1); }
	default int GetShadowCultureCount() { return GameUtils.getSpottableShadowCulturesCount(game(), P1); }

	default int GetThreats() {
		return gameState().getThreats();
	}
	default void AddThreats(int count) {
		gameState().addThreats(gameState().getCurrentPlayerId(), count);
	}

	default void RemoveThreats(int count) {
		gameState().removeThreats(gameState().getCurrentPlayerId(), count);
	}

	default boolean FreepsHasInitiative() { return PlayConditions.hasInitiative(game(), Side.FREE_PEOPLE); }

	default boolean ShadowHasInitiative() { return PlayConditions.hasInitiative(game(), Side.SHADOW); }

	default int GetFreepsArcheryTotal() { return RuleUtils.calculateFellowshipArcheryTotal(game()); }
	default int GetShadowArcheryTotal() { return RuleUtils.calculateShadowArcheryTotal(game()); }

	default int GetTwilight() { return gameState().getTwilightPool(); }
	default void SetTwilight(int amount) { gameState().setTwilight(amount); }

	default int GetMoveCount() { return gameState().getMoveCount(); }

	default int GetMoveLimit() { return game().getModifiersQuerying().getMoveLimit(game(), 2); }

	/**
	 * @return Whether the game has finished one way or another.
	 */
	default boolean GameIsFinished() { return game().isFinished(); }

	/**
	 * @return Gets the current game phase
	 */
	default Phase GetCurrentPhase() { return gameState().getCurrentPhase(); }

	/**
	 * @return Gets the player who is currently playing their turn.
	 */
	default String GetCurrentPlayer() { return gameState().getCurrentPlayerId(); }

	/**
	 * @return Gets the player whose turn it isn't.
	 */
	default String GetOffPlayer() {
		var order = gameState()
				.getPlayerOrder()
				.getCounterClockwisePlayOrder(GetCurrentPlayer(), true);

		//Skip the first player in the order, as that is the decider.
		order.getNextPlayer();
		return order.getNextPlayer();
	}

	/**
	 * @return Gets the player who is currently making a decision.
	 */
	default String GetDecidingPlayer() { return userFeedback().getUsersPendingDecision().stream().findFirst().get(); }

	/**
	 * @return Gets the player who is not currently making a decision.
	 */
	default String GetNextDecider() {
		var order = gameState()
			.getPlayerOrder()
			.getCounterClockwisePlayOrder(GetDecidingPlayer(), true);

		//Skip the first player in the order, as that is the decider.
		order.getNextPlayer();
		return order.getNextPlayer();
	}



	default boolean RBWearingOneRing() { return game().getGameState().isWearingRing(); }
	default PhysicalCardImpl GetRing() {
		return (PhysicalCardImpl)gameState().getRing(P1);
	}
	default PhysicalCardImpl GetShadowRing() { return (PhysicalCardImpl)gameState().getRing(P2); }
	default PhysicalCardImpl GetRingBearer() { return (PhysicalCardImpl)gameState().getRingBearer(P1); }
	default PhysicalCardImpl GetShadowRingBearer() { return (PhysicalCardImpl)gameState().getRingBearer(P2); }

	default PhysicalCardImpl GetCurrentSite() { return (PhysicalCardImpl)gameState().getCurrentSite(); }
	default int GetCurrentSiteNumber() { return GetCurrentSite().getSiteNumber(); }

	default PhysicalCardImpl GetFreepsSite(int siteNum) { return GetSite(P1, siteNum); }
	default PhysicalCardImpl GetShadowSite(int siteNum) { return GetSite(P2, siteNum); }
	default PhysicalCardImpl GetSite(String playerID, int siteNum)
	{
		PhysicalCardImpl site = (PhysicalCardImpl)gameState().getSite(siteNum);
		if(site != null && site.getOwner().equals(playerID))
			return site;

		List<PhysicalCardImpl> advDeck = (List<PhysicalCardImpl>)gameState().getAdventureDeck(playerID);
		return advDeck.stream().filter(x -> x.getBlueprint().getSiteNumber() == siteNum).findFirst().get();
	}
	default PhysicalCardImpl GetSite(int siteNum) { return (PhysicalCardImpl) gameState().getSite(siteNum);	}
	default PhysicalCardImpl GetFreepsSite(String name) { return GetSiteByName(P1, name); }
	default PhysicalCardImpl GetShadowSite(String name) { return GetSiteByName(P2, name); }
	default PhysicalCardImpl GetSiteByName(String player, String name)
	{
		var attempt = GetCard(player, name);
		if(attempt != null)
			return attempt;

		final String lowername = Names.SanitizeName(name);
		List<PhysicalCardImpl> advDeck = (List<PhysicalCardImpl>)gameState().getAdventureDeck(player);
		return advDeck.stream().filter(x -> x.getBlueprint().getSanitizedTitle().contains(lowername)).findFirst().get();
	}
}
