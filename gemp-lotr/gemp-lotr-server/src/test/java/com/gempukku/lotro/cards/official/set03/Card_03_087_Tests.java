package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("advances", "3_87");

					// The minions on the table.
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; his text answers only a weapon played on him
					put("troll", "6_106");      // Troll of Udûn: [sauron] Troll, vitality 5; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					// The Free Peoples player's hand: four different cards that each cost 2 ...
					put("gimli", "1_13");       // Gimli, Son of Glóin
					put("arwen", "1_30");       // Arwen, Daughter of Elrond
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood
					// ... or four that each cost 1 ...
					put("blow", "1_5");         // Cleaving Blow
					put("guard", "1_7");        // Dwarf Guard
					put("cloak", "1_42");       // Elven Cloak
					put("lorien", "1_53");      // Lórien Elf
					// ... or four that each cost 0.
					put("strike", "1_3");       // Axe Strike
					put("fury", "1_4");         // Battle Fury
					put("balin", "1_19");       // Here Lies Balin, Son of Fundin
					put("shaft", "1_22");       // Mithril Shaft

					put("bow", "1_41");         // Elven Bow, cost 1: a card of the Shadow player's own hand
					put("underhill", "1_129");  // The Misadventure of Mr. Underhill: each companion played away from a sanctuary costs +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheDarkLordAdvancesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Dark Lord Advances
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a [sauron] minion to reveal a card at random from the Free Peoples player's hand. That player must discard X cards from hand, where X is the twilight cost of the card revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("advances");

		assertEquals("The Dark Lord Advances", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionToRevealACardAndTheFreePeoplesPlayerDiscardsTwoForACardCostingTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var bow = scn.GetShadowCard("bow");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");

		scn.MoveCardsToHand(advances, bow);
		assertInHand(advances, bow);
		// Whichever of the four is revealed, X is 2.
		scn.MoveCardsToHand(gimli, arwen, legolas, haldir);
		assertInHand(gimli, arwen, legolas, haldir);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troll, troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(advances));

		scn.ShadowPlayCard(advances);

		// Either [sauron] minion, an Orc or a Troll; not the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troop));

		// One card of the four is shown to the Shadow player, and nothing to the owner of the hand.
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		int shown = (scn.ShadowHasCardChoiceAvailable(gimli, false) ? 1 : 0)
				+ (scn.ShadowHasCardChoiceAvailable(arwen, false) ? 1 : 0)
				+ (scn.ShadowHasCardChoiceAvailable(legolas, false) ? 1 : 0)
				+ (scn.ShadowHasCardChoiceAvailable(haldir, false) ? 1 : 0);
		assertEquals(1, shown);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// The Free Peoples player chooses exactly 2 of his 4.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, arwen, legolas, haldir));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, legolas);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(gimli, legolas);
		assertInHand(arwen, haldir);
		// The Shadow player's own hand is untouched.
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(bow);
		assertInDiscard(advances);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARevealedCardCostingOneMakesHimDiscardOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var chieftain = scn.GetShadowCard("chieftain");
		var blow = scn.GetFreepsCard("blow");
		var guard = scn.GetFreepsCard("guard");
		var cloak = scn.GetFreepsCard("cloak");
		var lorien = scn.GetFreepsCard("lorien");

		scn.MoveCardsToHand(advances);
		assertInHand(advances);
		// Whichever of the four is revealed, X is 1.
		scn.MoveCardsToHand(blow, guard, cloak, lorien);
		assertInHand(blow, guard, cloak, lorien);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(advances);
		// The one [sauron] minion exerts unasked.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(blow, guard, cloak, lorien));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(cloak);

		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(cloak);
		assertInHand(blow, guard, lorien);
		assertInDiscard(advances);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARevealedCardCostingNothingMakesHimDiscardNothingAndAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var chieftain = scn.GetShadowCard("chieftain");
		var strike = scn.GetFreepsCard("strike");
		var fury = scn.GetFreepsCard("fury");
		var balin = scn.GetFreepsCard("balin");
		var shaft = scn.GetFreepsCard("shaft");

		scn.MoveCardsToHand(advances);
		assertInHand(advances);
		// Whichever of the four is revealed, X is 0.
		scn.MoveCardsToHand(strike, fury, balin, shaft);
		assertInHand(strike, fury, balin, shaft);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(advances);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Nothing to discard, so nothing is asked: the event is over and the regroup phase goes on.
		assertFalse(scn.FreepsDecisionAvailable("discard"));
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInHand(strike, fury, balin, shaft);
		assertInDiscard(advances);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var chieftain = scn.GetShadowCard("chieftain");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(advances);
		assertInHand(advances);
		scn.MoveCardsToHand(scn.GetFreepsCard("gimli"));

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troop);
		// Vitality 2 with 1 wound: the Orc Chieftain cannot exert.  The Goblin Patrol Troop could, and is not [sauron].
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(advances));
	}

	@Test
	public void WithTheFreePeoplesHandEmptyItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(advances);
		assertInHand(advances);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowPlayAvailable(advances));

		scn.ShadowPlayCard(advances);

		// The cost is paid; there is nothing to reveal and nothing to discard.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInDiscard(advances);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void XIsThePrintedCostOfTheRevealedCardWhateverACardInPlayMakesItCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advances = scn.GetShadowCard("advances");
		var chieftain = scn.GetShadowCard("chieftain");
		var underhill = scn.GetShadowCard("underhill");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");

		scn.MoveCardsToHand(advances);
		assertInHand(advances);
		scn.MoveCardsToHand(gimli, arwen, legolas, haldir);
		assertInHand(gimli, arwen, legolas, haldir);
		scn.MoveCardsToSupportArea(underhill);
		assertInZone(Zone.SUPPORT, underhill);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// Site 2 is no sanctuary: 2 printed + 2 for Mr. Underhill, were any of the four played now.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilightCost(gimli));
		assertEquals(4, scn.GetTwilightCost(arwen));
		assertEquals(4, scn.GetTwilightCost(legolas));
		assertEquals(4, scn.GetTwilightCost(haldir));

		scn.ShadowPlayCard(advances);
		scn.ShadowDismissRevealedCards();

		// X is 2, the printed cost: 2 of the 4, where a cost of 4 would take the whole hand.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(arwen, haldir);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInDiscard(arwen, haldir);
		assertInHand(gimli, legolas);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
