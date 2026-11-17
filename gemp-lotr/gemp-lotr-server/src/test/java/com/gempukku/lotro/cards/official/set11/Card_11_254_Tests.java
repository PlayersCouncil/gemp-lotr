package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_254_Tests
{

	// The Fellowship-block path with Pelennor Flat in place of site 9, which no test reaches: the Shadow player's
	// copy is put on the path as site 2 and the fellowship moves to it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// A hand of both sides and several card types: a player's hand holds his Shadow cards too.
					put("guard", "1_7");        // Dwarf Guard: a Free Peoples companion
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession
					put("sleep", "1_84");       // Sleep, Caradhras: a Free Peoples event
					put("runner", "1_178");     // Goblin Runner: a Shadow minion

					put("savage", "1_151");     // Uruk Savage: a minion on the table, so that there is a maneuver phase; "Damage +1." is its whole text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("flat", "11_254");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PelennorFlatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pelennor Flat
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Plains</b>. At the start of the maneuver phase, the Free Peoples player must add a burden or discard 3 cards from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("flat");

		assertEquals("Pelennor Flat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTheFreePeoplesPlayerMayAddABurdenAndKeepHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var runner = scn.GetFreepsCard("runner");
		var shadowRunner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var flat = scn.GetShadowSite("flat");

		scn.StartGame();
		scn.MoveCardToAdventurePath(flat);
		scn.SetFreepsHand(guard, armor, sleep, runner);
		scn.SetShadowHand(shadowRunner);
		scn.FreepsPass();

		// The Shadow player's copy lies there.
		assertSame(flat, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage);

		// Nothing happens in the Shadow phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInHand(guard, armor, sleep, runner);
		assertInHand(shadowRunner);

		scn.ShadowPass();

		// It is the Free Peoples player who is asked, whoever's site this is.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));

		scn.FreepsChooseOption("burden");

		// One burden, on his own Ring-bearer, and every card still in hand.
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertInHand(guard, armor, sleep, runner);
		assertInHand(shadowRunner);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase asks nothing more.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.FreepsGetBurdens());
		assertInHand(guard, armor, sleep, runner);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTheFreePeoplesPlayerMayDiscardThreeCardsOfHisChoiceFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var sleep = scn.GetFreepsCard("sleep");
		var runner = scn.GetFreepsCard("runner");
		var shadowRunner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("flat"));
		scn.SetFreepsHand(guard, armor, sleep, runner);
		scn.SetShadowHand(shadowRunner);
		scn.FreepsPass();

		assertEquals("Pelennor Flat", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage);
		assertInHand(guard, armor, sleep, runner);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPass();
		scn.FreepsChooseOption("Discard");

		// Any card of his hand, whatever its side or type, and exactly three of them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, armor, sleep, runner));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, sleep, runner);

		assertInDiscard(guard, sleep, runner);
		assertInHand(armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		// The Shadow player's hand is not the one the site speaks of.
		assertInHand(shadowRunner);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFewerThanThreeCardsInHandTheBurdenIsAddedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("flat"));
		scn.SetFreepsHand(guard, armor);
		scn.FreepsPass();

		assertEquals("Pelennor Flat", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage);
		assertInHand(guard, armor);
		assertEquals(0, scn.FreepsGetBurdens());

		scn.ShadowPass();

		// Three cards cannot be discarded from a hand of two, and only what can be done in full may be chosen
		// (CR 4.2 "effect").
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertEquals(1, scn.FreepsGetBurdens());
		assertInHand(guard, armor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
