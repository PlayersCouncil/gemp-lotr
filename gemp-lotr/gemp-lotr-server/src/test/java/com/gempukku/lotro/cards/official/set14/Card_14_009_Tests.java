package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halbarad", "14_9");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion of cost 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden"
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability only
					// Gondorian Merchant: the one [gondor] ally, vitality 1; his maneuver ability needs initiative and 2 cards
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can carry a wound
					put("narsil", "9_34");

					put("pursuit", "1_111");    // Pursuit Just Behind: a [gondor] event
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession
					put("noble", "1_304");      // Noble Intentions: a [shire] event

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, so that there is a maneuver phase; its trigger is on its play only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSmeagolScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halbarad", "14_9");
					// Sam, Steadfast Friend: Ring-bound, resistance 10, "Each other companion is resistance +1"
					put("sam", "11_172");
				}},
				VirtualTableScenario.FellowshipSites,
				"9_30",                         // Smeagol, Bearer of Great Secrets: Ring-bearer, resistance 7; his texts need a move or the regroup phase
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HalbaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Halbarad, Ranger of the North
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.<br>While the Ring-bearer has resistance 9 or more, Halbarad's twilight cost is -1.<br>At the start of the maneuver phase, you may discard a [gondor] card from hand to heal a [gondor] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halbarad");

		assertEquals("Halbarad", card.getBlueprint().getTitle());
		assertEquals("Ranger of the North", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void HeCostsOneLessWhileTheRingBearerHasResistance9OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var halbarad = scn.GetFreepsCard("halbarad");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(halbarad, aragorn);

		scn.StartGame();

		// Frodo, Son of Drogo: resistance 10 with no burden
		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(halbarad));
		// Another companion in hand keeps his printed cost.
		assertEquals(4, scn.GetTwilightCost(aragorn));

		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(3, scn.GetTwilightCost(halbarad));
		assertEquals(4, scn.GetTwilightCost(aragorn));
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetBurdens());
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(halbarad));
		assertEquals(4, scn.GetTwilightCost(aragorn));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(halbarad);
		// 3 - 1
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, halbarad);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerDoesNotLowerHisCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmeagolScenario();

		var smeagol = scn.GetRingBearer();
		var halbarad = scn.GetFreepsCard("halbarad");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(halbarad);

		scn.StartGame();

		// Smeagol 7, +1 from Sam; Sam 10, Ring-bound and not the Ring-bearer
		assertEquals(0, scn.GetBurdens());
		assertEquals(8, scn.GetResistance(smeagol));
		assertEquals(10, scn.GetResistance(sam));
		assertEquals(3, scn.GetTwilightCost(halbarad));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(halbarad);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, halbarad);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseADiscardedGondorCardHealsAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var halbarad = scn.GetFreepsCard("halbarad");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pursuit = scn.GetFreepsCard("pursuit");
		var sword = scn.GetFreepsCard("sword");
		var noble = scn.GetFreepsCard("noble");
		scn.MoveCompanionsToTable(halbarad, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(pursuit, sword, noble);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(halbarad, 1);
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(merchant, 1);
		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertInHand(pursuit, sword, noble);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halbarad));
		scn.FreepsAcceptOptionalTrigger();

		// Any [gondor] card from hand: an event and a possession, not the [shire] event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuit, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(noble));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);
		assertInDiscard(sword);
		assertInHand(pursuit, noble);

		// A [gondor] companion of either race; not the [gondor] ally, nor the companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(halbarad, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGondorCompanionWoundedTheDiscardHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var halbarad = scn.GetFreepsCard("halbarad");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(halbarad);
		scn.MoveCardsToHand(pursuit);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(halbarad));
		assertInHand(pursuit);
		scn.FreepsPass();
		scn.ShadowPass();

		// The discard is a cost: the offer is made with nothing to heal.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halbarad));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(pursuit);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(halbarad));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGondorCardInHandNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halbarad = scn.GetFreepsCard("halbarad");
		var noble = scn.GetFreepsCard("noble");
		scn.MoveCompanionsToTable(halbarad);
		scn.MoveCardsToHand(noble);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(halbarad, 1);
		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertInHand(noble);
		scn.FreepsPass();
		scn.ShadowPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(halbarad));
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertInHand(noble);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedAtTheStartOfTheManeuverPhaseItIsNotOfferedAgainAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halbarad = scn.GetFreepsCard("halbarad");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(halbarad);
		scn.MoveCardsToHand(pursuit);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(halbarad, 1);
		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertInHand(pursuit);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halbarad));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertInHand(pursuit);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		// The next phase starts with nothing offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(halbarad));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetWoundsOn(halbarad));
		assertInHand(pursuit);
	}
}
