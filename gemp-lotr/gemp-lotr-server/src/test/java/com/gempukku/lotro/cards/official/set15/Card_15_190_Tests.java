package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_190_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Hunter companions of three cultures and races, to exert
					// Elven Guardian: [elven] Elf, Hunter 1; +2 against a wounded minion, a maneuver heal used only on purpose
					put("guardian", "17_7");
					// Silent Traveler: [gondor] Man, Ranger, Hunter 1; a trigger at the start of a skirmish with a roaming minion
					put("traveler", "15_69");
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven] Dwarf, unique; Damage +1, Hunter 3, no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion who is no hunter

					// For the draw deck
					// Legolas, Fleet-footed Hunter: [elven] Elf, unique, Hunter 1; a regroup ability used only on purpose
					put("legolas", "15_18");
					// Unyielding Ranger: [gondor] Man, Ranger, Hunter 1; strength +1 for each roaming minion
					put("ranger", "15_71");
					put("knight", "5_35");      // Gondorian Knight: a companion who is no hunter; Knight is its whole text
					put("tracking", "15_171");  // Tracking Uruk: a hunter who is no companion (a minion); Hunter 1 its whole text
					put("gimli2", "15_5");      // a second Gimli, who cannot be played while the first is in play
				}},
				new HashMap<>()
				{{
					// East Wall of Rohan is a Shadows-block site; the tests lay the Shadow player's copy as site 2.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("eastwall", "15_190");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EastWallofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: East Wall of Rohan
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: At the start of the fellowship phase, you may exert two hunter companions to play a hunter companion
		 * 	from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("eastwall");

		assertEquals("East Wall of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseTwoHunterCompanionsExertToPlayAHunterCompanionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eastwall = scn.GetShadowSite("eastwall");
		var frodo = scn.GetRingBearer();
		var guardian = scn.GetFreepsCard("guardian");
		var traveler = scn.GetFreepsCard("traveler");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var tracking = scn.GetFreepsCard("tracking");
		var gimli2 = scn.GetFreepsCard("gimli2");
		scn.MoveCompanionsToTable(guardian, traveler, gimli, aragorn);

		scn.StartGame();
		scn.MoveCardToAdventurePath(eastwall);
		// Turn 1: the fellowship moves to the East Wall.  Turn 2: the second player's.  Turn 3 begins there.
		scn.FreepsPass();
		assertSame(eastwall, scn.GetCurrentSite());
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(eastwall, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eastwall));
		scn.SetFreepsDrawDeck(legolas, ranger, knight, tracking, gimli2);
		assertEquals(5, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(traveler));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsAcceptOptionalTrigger();
		// Two of the three hunter companions; Frodo and Aragorn are companions and no hunters.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, traveler, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guardian, gimli);

		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(traveler));

		// The deck is shown, then the hunter companions in it are offered.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, ranger));
		// A companion who is no hunter, a hunter who is no companion, and a Gimli whose title is in play are not.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, tracking, gimli2));
		scn.FreepsChooseCardBPFromSelection(ranger);

		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertEquals(4, scn.GetFreepsDeckCount());
		// The deck looked through is shuffled as the Ranger leaves it, and again once the look is over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHunterCompanionInTheDrawDeckTheExertionsArePaidAndNothingIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eastwall = scn.GetShadowSite("eastwall");
		var guardian = scn.GetFreepsCard("guardian");
		var traveler = scn.GetFreepsCard("traveler");
		var knight = scn.GetFreepsCard("knight");
		var tracking = scn.GetFreepsCard("tracking");
		scn.MoveCompanionsToTable(guardian, traveler);

		scn.StartGame();
		scn.MoveCardToAdventurePath(eastwall);
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(eastwall, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eastwall));
		scn.SetFreepsDrawDeck(knight, tracking);
		assertEquals(2, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(traveler));

		// The two hunter companions there are exert unasked.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(1, scn.GetWoundsOn(traveler));

		// The deck is shown with nothing in it that can be chosen, and nothing is played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(tracking, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyOneHunterCompanionAbleToExertNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eastwall = scn.GetShadowSite("eastwall");
		var guardian = scn.GetFreepsCard("guardian");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(guardian, gimli, aragorn);

		scn.StartGame();
		// Gimli (vitality 3) exhausted.  The Guardian, unwounded, could exert twice; Frodo and Aragorn could exert.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guardian));
		scn.MoveCardToAdventurePath(eastwall);
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		scn.SetFreepsDrawDeck(ranger);
		scn.SkipToOtherPlayersTurn();

		assertSame(eastwall, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eastwall));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guardian));
	}

	@Test
	public void DeclinedItLeavesAllAsItWasAndTheSecondPlayerIsOfferedItOnHisOwnTurnThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eastwall = scn.GetShadowSite("eastwall");
		var guardian = scn.GetFreepsCard("guardian");
		var traveler = scn.GetFreepsCard("traveler");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(guardian, traveler);
		// The second player's own two hunter companions, for his turn.
		var secondPlayersGuardian = scn.GetShadowCard("guardian");
		var secondPlayersTraveler = scn.GetShadowCard("traveler");
		scn.MoveCompanionsToTable(secondPlayersGuardian, secondPlayersTraveler);

		scn.StartGame();
		scn.MoveCardToAdventurePath(eastwall);
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertSame(eastwall, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eastwall));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(eastwall));
		scn.SetFreepsDrawDeck(ranger);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(traveler));

		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(traveler));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		// Turn 3 moves the first player on to site 3; turn 4, the second player's, begins at the East Wall.
		scn.SkipToOtherPlayersTurn();

		assertSame(eastwall, scn.GetCurrentSite());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(eastwall));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eastwall));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheStartOfTheShadowAndRegroupPhasesThereOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eastwall = scn.GetShadowSite("eastwall");
		var guardian = scn.GetFreepsCard("guardian");
		var traveler = scn.GetFreepsCard("traveler");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(guardian, traveler);

		scn.StartGame();
		scn.MoveCardToAdventurePath(eastwall);
		scn.SetFreepsDrawDeck(ranger);
		scn.FreepsPass();

		// The fellowship has arrived: the Shadow phase opens on the Shadow player's actions, with no offer ahead of them.
		assertSame(eastwall, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eastwall));

		// No minion: the regroup phase follows, and opens on the Free Peoples player's actions.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eastwall));
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(traveler));
	}
}
