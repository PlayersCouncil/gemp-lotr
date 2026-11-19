package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_018_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ori", "30_18");
					put("balin", "30_2");       // Balin, Brother of Dwalin: the one Balin; a fellowship ability only
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: another [dwarven] Dwarf companion; a maneuver ability used only on purpose
					// Bifur, Inarticulate: another [dwarven] follower; Aid - exert a companion; a win trigger not reached
					put("bifur", "30_5");
					// Dwarven Warhammer: a [dwarven] hand weapon for a [dwarven] companion; "Maneuver: Discard 2 cards
					// from hand to attach a [dwarven] follower from your support area to bearer"
					put("warhammer", "33_7");

					// Cards for the hand. Battle Fury: a [dwarven] skirmish event. Dwarven Axe: a [dwarven] possession.
					// Take up arms. Fight!: a [gandalf] skirmish event.
					put("fury", "30_3");
					put("axe", "31_1");
					put("takeup", "30_31");
					put("takeup2", "30_31");

					put("runner", "31_30");     // Goblin Runner: a [moria] Orc, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OriStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Ori, Dwarven Chronicler
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +1
		 * Aid: Discard a [dwarven] card from hand.
		 * Game Text: <b>Aid</b> – Discard a [dwarven] card from hand. <helper>(At the start of the maneuver phase, you may discard a [dwarven] card from hand to transfer this to a companion.)</helper>
		 * 	When you transfer Ori to Balin, you may draw 3 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ori");

		assertEquals("Ori", card.getBlueprint().getTitle());
		assertEquals("Dwarven Chronicler", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidDiscardsADwarvenCardFromHandAndTheTransferToBalinMayDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fury = scn.GetFreepsCard("fury");
		var axe = scn.GetFreepsCard("axe");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(ori);

		scn.StartGame();
		scn.MoveCardsToHand(fury, axe, takeup);
		scn.MoveMinionsToTable("runner");
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(fury, axe, takeup);
		assertInZone(Zone.SUPPORT, ori);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori, "Aid"));
		scn.FreepsAcceptOptionalTrigger();

		// A [dwarven] event and a [dwarven] possession may pay; a [gandalf] card may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(takeup));
		scn.FreepsChooseCard(fury);
		assertInDiscard(fury);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsChooseCard(balin);
		assertAttachedTo(ori, balin);
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori, "Optional trigger"));
		scn.FreepsAcceptOptionalTrigger();

		// Three cards, not two or four.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToACompanionWhoIsNotBalinItDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fury = scn.GetFreepsCard("fury");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(ori);

		scn.StartGame();
		scn.MoveCardsToHand(fury, axe);
		scn.MoveMinionsToTable("runner");
		assertInHand(fury, axe);
		assertInZone(Zone.SUPPORT, ori);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(fury);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// Dwalin, Brother of Balin, is no Balin.
		scn.FreepsChooseCard(dwalin);
		assertAttachedTo(ori, dwalin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ori));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherFollowerTransferredToBalinDrawsNothingAndOrisDrawMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ori = scn.GetFreepsCard("ori");
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fury = scn.GetFreepsCard("fury");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(ori, bifur);

		scn.StartGame();
		scn.MoveCardsToHand(fury, axe);
		scn.MoveMinionsToTable("runner");
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertInHand(fury, axe);
		assertInZone(Zone.SUPPORT, ori, bifur);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori, "Aid"));
		scn.FreepsChooseOptionalTrigger(bifur, "Aid");
		// Bifur's Aid: exert a companion.
		scn.FreepsChooseCard(dwalin);
		assertEquals(1, scn.GetWoundsOn(dwalin));
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsChooseCard(balin);
		assertAttachedTo(bifur, balin);

		// Bifur is no Ori: no draw is offered, and Ori's own Aid is still to answer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ori, "Optional trigger"));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(axe);
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsChooseCard(balin);
		assertAttachedTo(ori, balin);

		// Ori to Balin: the draw is offered, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWithNoDwarvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(ori);

		scn.StartGame();
		scn.MoveCardsToHand(takeup);
		scn.MoveMinionsToTable("runner");
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, ori);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ori, "Aid"));
		assertInZone(Zone.SUPPORT, ori);
		assertInHand(takeup);
		assertEquals(1, scn.GetFreepsHandCount());
	}

	@Test
	public void AttachedToBalinByAnotherCardWithoutItsAidItMayStillDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ori = scn.GetFreepsCard("ori");
		var balin = scn.GetFreepsCard("balin");
		var warhammer = scn.GetFreepsCard("warhammer");
		var takeup = scn.GetFreepsCard("takeup");
		var takeup2 = scn.GetFreepsCard("takeup2");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, warhammer);
		scn.MoveCardsToSupportArea(ori);

		scn.StartGame();
		// No [dwarven] card in hand, so Ori's own Aid is not offered.
		scn.MoveCardsToHand(takeup, takeup2);
		scn.MoveMinionsToTable("runner");
		assertAttachedTo(warhammer, balin);
		assertInZone(Zone.SUPPORT, ori);
		assertInHand(takeup, takeup2);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(warhammer));
		scn.FreepsUseCardAction(warhammer);

		// Both cards in hand discarded; Ori, the one [dwarven] follower in the support area, goes to Balin.
		assertInDiscard(takeup, takeup2);
		assertAttachedTo(ori, balin);
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ori));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
