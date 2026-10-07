package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_079_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "53_79");
					put("runner", "1_178");       // Goblin Runner: [moria] Orc
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc
					put("scimitar", "1_180");     // Goblin Scimitar: [moria] possession, cost 0, bearer must be a [moria] Orc
					put("armory", "1_173");       // Goblin Armory: [moria] condition, cost 0
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsNotPlayableUnlessASauronOrMoriaItemInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(card);
		//A [moria] card that could be played, but is not an item
		scn.MoveCardsToDiscard(armory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		//Playing from the discard pile is treated as a cost: with nothing to play, the event is withheld
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(card));

		//The Scimitar can go on the Runner
		scn.MoveCardsToDiscard(scimitar);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(card));
	}
}
