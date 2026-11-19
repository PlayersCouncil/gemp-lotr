package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mountains", "2_72");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					// Cave Troll of Moria, Scourge of the Black Pit: a [moria] Troll, vitality 4; damage +1 and fierce,
					// and a requirement and a discount on being played, none of which reaches a reading here
					put("troll", "1_165");
					put("soldier", "1_271");    // Orc Soldier: an Orc who is not [moria] ([sauron]), vitality 2; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TroubledMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Troubled Mountains
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Exert a [moria] minion and spot X burdens to add (X) (limit (5)).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mountains");

		assertEquals("Troubled Mountains", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAMoriaMinionAndSpotsTwoBurdensToAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mountains = scn.GetShadowCard("mountains");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(troop, troll, soldier);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(mountains));
		scn.ShadowPlayCard(mountains);

		// [moria] minions of two races; the Orc of another culture is not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));

		// X is the Shadow player's count, from none to the 2 burdens there are (CR 4.2 "spot").
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(twilight + 2, scn.GetTwilight());
		// The burdens are spotted, not removed.
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(mountains);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingOneOfThreeBurdensAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mountains = scn.GetShadowCard("mountains");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(mountains);
		assertEquals(1, scn.GetWoundsOn(troop));

		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingSixBurdensAddsOnlyFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mountains = scn.GetShadowCard("mountains");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.AddBurdens(6);
		assertEquals(6, scn.GetBurdens());
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(mountains);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The limit caps what X adds, not X.
		assertEquals(6, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("6");

		assertEquals(twilight + 5, scn.GetTwilight());
		assertEquals(6, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyMoriaMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mountains = scn.GetShadowCard("mountains");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(mountains);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(troop, soldier);
		// Vitality 3 with 2 wounds; the [sauron] Orc beside him could exert.
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(mountains));
	}
}
