package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("garrison", "10_33");

					// The [gondor] Men, and the companions who nearly are.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					// Gondorian Merchant: the [gondor] Man ally, cost 1; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");

					// Elendil's Valor: Maneuver event, cost 0: "Make a [gondor] companion defender +1 until the regroup phase."
					// Aragorn is the only [gondor] companion on each board where it is played, and is chosen unasked.
					// Each play takes a card from the hand.
					put("valor", "1_103");
					put("valor2", "1_103");
					put("valor3", "1_103");
					// Dwarven Armor: cards to hold in hand and nothing else (no Dwarf to bear one).
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HardyGarrisonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Hardy Garrison
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [gondor] Men.<br>Each time you lose initiative, you may wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("garrison");

		assertEquals("Hardy Garrison", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoGondorMenAreSpottedAndItsOwnPlayInTheFellowshipPhaseWoundsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// One [gondor] Man, beside a [gondor] Hobbit and a [rohan] Man.
		scn.MoveCompanionsToTable(aragorn, pippin, rider);
		scn.MoveCardsToHand(garrison, merchant, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();

		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(garrison));

		// A [gondor] Man ally beside the companion makes two.
		scn.FreepsPlayCard(merchant);
		assertTrue(scn.FreepsPlayAvailable(garrison));

		// Four cards to three: its own play loses initiative, once it is in play. There is no minion to wound in
		// the fellowship phase, so nothing is offered.
		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(garrison);
		assertInZone(Zone.SUPPORT, garrison);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachLossOfInitiativeOffersToWoundAMinionAndAnExhaustedOneIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var valor3 = scn.GetFreepsCard("valor3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(garrison);
		scn.MoveCardsToHand(valor, valor2, valor3, scn.GetFreepsCard("filler1"));

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		// Vitality 3: exhausted, so a wound kills it.
		scn.AddWoundsToChar(troop, 2);
		assertEquals(2, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(troop));

		// Four cards to three: initiative is lost, and the offer is made. Declined, nothing changes.
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A card more makes four: initiative is back from the next answer on.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler2"));
		scn.ShadowPass();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Lost a second time: offered again. Either minion may be wounded, the exhausted one too; no companion.
		scn.FreepsPlayCard(valor2);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.MoveCardsToHand(scn.GetFreepsCard("filler3"));
		scn.ShadowPass();
		assertTrue(scn.FreepsHasInitiative());

		// A third time: the exhausted Troop wounded is killed.
		scn.FreepsPlayCard(valor3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(troop);
		assertInZone(Zone.DISCARD, troop);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHandGoingFromThreeToTwoOrFromFiveToFourOrBackToFourOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var valor3 = scn.GetFreepsCard("valor3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(garrison);
		scn.MoveCardsToHand(valor, valor2, valor3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Three cards to two: the Shadow has initiative already, so none is lost.
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		scn.FreepsPlayCard(valor);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(garrison));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Three cards more make five: the Shadow loses initiative at the next answer, which is no loss of the
		// Free Peoples player's.
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		scn.ShadowPass();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(garrison));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Five cards to four: initiative is kept.
		scn.FreepsPlayCard(valor2);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(garrison));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(troop));

		// Four cards to three on the same board: the offer is made.
		scn.FreepsPlayCard(valor3);
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(garrison));
	}
}
