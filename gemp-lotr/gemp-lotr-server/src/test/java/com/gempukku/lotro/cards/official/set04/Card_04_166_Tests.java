package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_166_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("palantir", "4_166");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, cost 2, site 5; damage +1 and nothing else
					// Isengard Shaman: an [isengard] Orc, site 4; "Response: If an [isengard] Orc is about to take a wound,
					// remove (2) to prevent that wound" (no wound here)
					put("shaman", "3_59");
					// Lurtz, Halfling Hunter: an [uruk-hai] Uruk-hai (the race without the culture); a maneuver and an
					// assignment ability never reached
					put("lurtz", "18_118");
					// Weapons of Isengard: an [isengard] condition in the support area (the culture on no minion); its
					// Shadow ability needs an [isengard] archer in hand, and none is
					put("weapons", "4_211");

					// Cards of distinct titles, laid in the draw decks
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("troop", "1_177");      // Goblin Patrol Troop
					put("runner", "1_178");     // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePalantirOfOrthancStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: The Palantír of Orthanc, Seventh Seeing-stone
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.<br><b>Shadow:</b> Spot an [isengard] minion and remove (2) to reveal the top card of any draw deck. You may place that card beneath that draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("palantir");

		assertEquals("The Palantír of Orthanc", card.getBlueprint().getTitle());
		assertEquals("Seventh Seeing-stone", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAnIsengardMinionButCanOnceOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		var lurtz = scn.GetShadowCard("lurtz");
		var weapons = scn.GetShadowCard("weapons");
		scn.MoveCardsToHand(palantir, savage);
		scn.MoveCardsToSupportArea(weapons);

		scn.StartGame();
		assertInHand(palantir);
		assertInHand(savage);
		assertInZone(Zone.SUPPORT, weapons);
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();
		// The Savage costs 2, and 2 more while roaming at site 2.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		// An [uruk-hai] Uruk-hai and an [isengard] condition: neither is an [isengard] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(palantir));

		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.ShadowPlayAvailable(palantir));
		scn.ShadowPlayCard(palantir);

		assertInZone(Zone.SUPPORT, palantir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesTwoToRevealTheFreePeoplesTopCardAndMayPutItBeneathThatDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1, gimli, aragorn);
		scn.SetDrawDeck(VirtualTableScenario.P2, troop, runner);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertSame(gimli, scn.GetFreepsTopOfDeck());
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		assertSame(troop, scn.GetShadowTopOfDeck());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(palantir));
		scn.ShadowUseCardAction(palantir);
		assertEquals(0, scn.GetTwilight());

		// Either player's draw deck.
		assertTrue(scn.ShadowChoiceAvailable("your deck"));
		assertTrue(scn.ShadowChoiceAvailable("opponent's deck"));
		scn.ShadowChoose("opponent's deck");

		// Shown to both players, and only the top card.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		scn.DismissRevealedCards();

		assertTrue(scn.ShadowDecisionAvailable("beneath"));
		scn.ShadowChooseYes();

		// Gimli is now the bottom card of the deck he came from, with no shuffle; the Shadow deck is untouched.
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertSame(gimli, scn.GetFreepsBottomOfDeck());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertSame(troop, scn.GetShadowTopOfDeck());
		assertSame(runner, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheRevealedCardOnTopOfItsDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1, gimli, aragorn);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertSame(gimli, scn.GetFreepsTopOfDeck());
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		scn.ShadowUseCardAction(palantir);
		scn.ShadowChoose("opponent's deck");
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowDecisionAvailable("beneath"));
		scn.ShadowChooseNo();

		assertSame(gimli, scn.GetFreepsTopOfDeck());
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnIsengardOrcItIsPlayedAndRevealsTheTopOfItsOwnersDeckWhichGoesBeneathIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palantir = scn.GetShadowCard("palantir");
		var shaman = scn.GetShadowCard("shaman");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(palantir);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1, gimli, aragorn);
		scn.SetDrawDeck(VirtualTableScenario.P2, troop, runner);
		assertInHand(palantir);
		scn.MoveMinionsToTable(shaman);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		// An [isengard] minion who is no Uruk-hai meets both spots.
		assertTrue(scn.ShadowPlayAvailable(palantir));
		scn.ShadowPlayCard(palantir);
		assertInZone(Zone.SUPPORT, palantir);

		assertSame(troop, scn.GetShadowTopOfDeck());
		assertSame(runner, scn.GetShadowBottomOfDeck());
		assertTrue(scn.ShadowActionAvailable(palantir));
		scn.ShadowUseCardAction(palantir);
		assertEquals(0, scn.GetTwilight());
		scn.ShadowChoose("your deck");

		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(runner, false));
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowDecisionAvailable("beneath"));
		scn.ShadowChooseYes();

		// Beneath his own deck; the Free Peoples deck is untouched.
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertSame(troop, scn.GetShadowBottomOfDeck());
		assertSame(gimli, scn.GetFreepsTopOfDeck());
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithoutAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var lurtz = scn.GetShadowCard("lurtz");
		var weapons = scn.GetShadowCard("weapons");
		scn.MoveCardsToSupportArea(palantir, weapons);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, palantir);
		assertInZone(Zone.SUPPORT, weapons);
		scn.MoveMinionsToTable(lurtz);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		// (2) can be paid, but an [uruk-hai] Uruk-hai and an [isengard] condition are no [isengard] minion.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(palantir));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, palantir);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(palantir));
	}

	@Test
	public void WithTheFreePeoplesDeckEmptyHisOwnDeckIsRevealedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1);
		scn.SetDrawDeck(VirtualTableScenario.P2, troop, runner);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertSame(troop, scn.GetShadowTopOfDeck());
		scn.ShadowUseCardAction(palantir);

		// Only his own deck can be revealed from, so it is, with no choice put.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		scn.DismissRevealedCards();
		scn.ShadowChooseYes();
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertSame(troop, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithHisOwnDeckEmptyTheFreePeoplesDeckIsRevealedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1, gimli, aragorn);
		scn.SetDrawDeck(VirtualTableScenario.P2);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetShadowDeckCount());
		assertSame(gimli, scn.GetFreepsTopOfDeck());
		scn.ShadowUseCardAction(palantir);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, false));
		scn.DismissRevealedCards();
		scn.ShadowChooseYes();
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertSame(gimli, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithBothDrawDecksEmptyTheAbilityPaysForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var palantir = scn.GetShadowCard("palantir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(palantir);

		scn.StartGame();
		scn.SetDrawDeck(VirtualTableScenario.P1);
		scn.SetDrawDeck(VirtualTableScenario.P2);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetShadowDeckCount());
		// A reveal bought by a cost is offered with nothing to reveal (Q-0163).
		assertTrue(scn.ShadowActionAvailable(palantir));
		scn.ShadowUseCardAction(palantir);
		assertEquals(0, scn.GetTwilight());

		// Nothing is shown and nothing is asked about putting a card beneath.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.ShadowDecisionAvailable("beneath"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
