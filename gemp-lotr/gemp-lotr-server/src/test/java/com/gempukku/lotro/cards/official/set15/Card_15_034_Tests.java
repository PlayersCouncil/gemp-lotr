package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quickbeam", "15_34");

					// Ents
					put("onod", "17_26");       // Woodland Onod: [gandalf] Ent companion, cost 4; a skirmish ability used only on purpose
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: [gandalf] Ent ally, cost 4; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, cost 4; strength +2 at 3 Ents, never read here
					// [gandalf] companions who are no Ents
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("huorn", "9_25");       // Huorn: [gandalf] Tree companion; its strength and response are never read here
					// For the discard pile
					put("ottar", "1_80");       // Ottar, Man of Laketown: a [gandalf] ally, no companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("wizard", "1_78");      // Mysterious Wizard: a [gandalf] Skirmish event
					// Cards that hold culture tokens
					put("laststand", "18_21");  // Last Stand: [gandalf] support-area condition that holds [gandalf] tokens
					put("halfling", "8_18");    // Not the First Halfling: [gandalf] support-area condition that holds [gandalf] tokens
					put("fear", "13_30");       // Fear and Great Wonder: [gandalf] support-area condition, its token spent
					put("path", "8_42");        // A Path Appointed: [gondor] support-area condition that holds [gondor] tokens

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuickbeamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Quickbeam, Hastiest of All Ents
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Quickbeam is twilight cost -1 for each Ent you can spot.<br><b>Maneuver:</b> Discard this companion to place another [gandalf] companion from your discard pile on the bottom of your draw deck and reinforce a [gandalf] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quickbeam");

		assertEquals("Quickbeam", card.getBlueprint().getTitle());
		assertEquals("Hastiest of All Ents", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void IsTwilightCostMinusOneForEachEntSpottedCompanionOrAllyAndNotForOtherGandalfCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		var avenger = scn.GetFreepsCard("avenger");
		// [gandalf] companions who are no Ents, in play throughout.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("huorn"));
		scn.MoveCardsToHand(quickbeam, onod, treebeard, avenger);

		scn.StartGame();

		assertInZone(Zone.HAND, quickbeam, onod, treebeard, avenger);
		// No Ent: Gandalf and Huorn do not count.
		assertEquals(4, scn.GetTwilightCost(quickbeam));
		// Another Ent in hand is discounted by nothing.
		assertEquals(4, scn.GetTwilightCost(avenger));

		scn.FreepsPlayCard(onod);
		assertInZone(Zone.FREE_CHARACTERS, onod);
		// One Ent companion: 4 - 1
		assertEquals(3, scn.GetTwilightCost(quickbeam));
		assertEquals(4, scn.GetTwilightCost(avenger));

		scn.FreepsPlayCard(treebeard);
		assertInZone(Zone.SUPPORT, treebeard);
		// And an Ent ally: 4 - 2
		assertEquals(2, scn.GetTwilightCost(quickbeam));
		assertEquals(4, scn.GetTwilightCost(avenger));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(quickbeam);

		assertInZone(Zone.FREE_CHARACTERS, quickbeam);
		// 4 printed - 2 for Woodland Onod and Treebeard
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityDiscardsQuickbeamToPlaceAnotherGandalfCompanionBeneathTheDeckAndReinforceAGandalfToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var onod = scn.GetFreepsCard("onod");
		var ottar = scn.GetFreepsCard("ottar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wizard = scn.GetFreepsCard("wizard");
		var avenger = scn.GetFreepsCard("avenger");
		// The other player's [gandalf] companion in his own discard pile: a card no card of this pile is.
		var shadowHuorn = scn.GetShadowCard("huorn");
		var laststand = scn.GetFreepsCard("laststand");
		var halfling = scn.GetFreepsCard("halfling");
		var fear = scn.GetFreepsCard("fear");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(quickbeam);
		scn.MoveCardsToSupportArea(laststand, halfling, fear, path);
		scn.AddTokensToCard(laststand, 2);
		scn.AddTokensToCard(halfling, 1);
		scn.AddTokensToCard(path, 1);
		scn.MoveCardsToDiscard(gandalf, onod, ottar, aragorn, wizard);
		scn.MoveCardsToDiscard(shadowHuorn);
		scn.MoveCardsToHand(avenger);

		scn.StartGame();

		assertInZone(Zone.DISCARD, gandalf, onod, ottar, aragorn, wizard, shadowHuorn);
		assertInZone(Zone.HAND, avenger);
		assertEquals(2, scn.GetCultureTokensOn(laststand));
		assertEquals(1, scn.GetCultureTokensOn(halfling));
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(path));
		// Not a fellowship ability.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(quickbeam));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(quickbeam));
		assertEquals(5, scn.GetFreepsDiscardCount());
		var deckCount = scn.GetFreepsDeckCount();
		var shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(quickbeam);

		// The cost: Quickbeam is discarded, and so lies in the discard pile as the companion is chosen.
		assertInZone(Zone.DISCARD, quickbeam);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, onod));
		// "Another": not Quickbeam himself; an ally, a [gondor] companion and a [gandalf] event are no
		// [gandalf] companions; a companion in hand or in the other player's pile is not in this pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(quickbeam, ottar, aragorn, wizard, avenger, shadowHuorn));
		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.DECK, gandalf);
		assertSame(gandalf, scn.GetFreepsBottomOfDeck());
		assertEquals(deckCount + 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DISCARD, quickbeam, onod, ottar, aragorn, wizard, shadowHuorn);

		// The reinforce: only a card that already has a [gandalf] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(laststand, halfling));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fear, path));
		scn.FreepsChooseCard(halfling);

		assertEquals(2, scn.GetCultureTokensOn(halfling));
		assertEquals(2, scn.GetCultureTokensOn(laststand));
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherGandalfCompanionInTheDiscardPileItIsStillOfferedAndOnlyReinforces() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var ottar = scn.GetFreepsCard("ottar");
		var aragorn = scn.GetFreepsCard("aragorn");
		var laststand = scn.GetFreepsCard("laststand");
		scn.MoveCompanionsToTable(quickbeam);
		scn.MoveCardsToSupportArea(laststand);
		scn.AddTokensToCard(laststand, 2);
		scn.MoveCardsToDiscard(ottar, aragorn);

		scn.StartGame();

		assertInZone(Zone.DISCARD, ottar, aragorn);
		assertEquals(2, scn.GetCultureTokensOn(laststand));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(quickbeam));
		var deckCount = scn.GetFreepsDeckCount();
		var bottom = scn.GetFreepsBottomOfDeck();

		scn.FreepsUseCardAction(quickbeam);

		// Paid, nothing placed (Quickbeam, now in the pile, is not "another"), and the token still reinforced.
		assertInZone(Zone.DISCARD, quickbeam, ottar, aragorn);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertEquals(3, scn.GetCultureTokensOn(laststand));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoGandalfTokenInPlayTheCompanionIsStillPlacedAndNothingIsReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quickbeam = scn.GetFreepsCard("quickbeam");
		var onod = scn.GetFreepsCard("onod");
		var fear = scn.GetFreepsCard("fear");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(quickbeam);
		scn.MoveCardsToSupportArea(fear, path);
		scn.AddTokensToCard(path, 1);
		scn.MoveCardsToDiscard(onod);

		scn.StartGame();

		assertInZone(Zone.DISCARD, onod);
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(path));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(quickbeam));

		scn.FreepsUseCardAction(quickbeam);

		// The lone [gandalf] companion goes beneath the deck unasked, and no card is asked for a token.
		assertInZone(Zone.DISCARD, quickbeam);
		assertSame(onod, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetCultureTokensOn(fear));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
