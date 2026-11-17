package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_016_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver phase
	// of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oin", "30_16");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a fellowship ability only
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] Dwarf companion, vitality 3; a maneuver ability used only on purpose
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
	public void OinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Óin, Knowledgeable Healer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Subtype: Dwarf
		 * Strength: +1
		 * Aid: Discard a [dwarven] card from hand.
		 * Game Text: <b>Aid</b> – Discard a [dwarven] card from hand. <helper>(At the start of the maneuver phase, you may discard a [dwarven] card from hand to transfer this to a companion.)</helper>
		 * 	When you transfer Óin, heal bearer.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("oin");

		assertEquals("Óin", card.getBlueprint().getTitle());
		assertEquals("Knowledgeable Healer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void AidDiscardsADwarvenCardFromHandAndTheTransferHealsTheBearerAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oin = scn.GetFreepsCard("oin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fury = scn.GetFreepsCard("fury");
		var axe = scn.GetFreepsCard("axe");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(oin);

		scn.StartGame();
		scn.MoveCardsToHand(fury, axe, takeup);
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(fury, axe, takeup);
		assertInZone(Zone.SUPPORT, oin);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		scn.FreepsAcceptOptionalTrigger();

		// A [dwarven] event and a [dwarven] possession may pay; a [gandalf] card may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(takeup));
		scn.FreepsChooseCard(fury);
		assertInDiscard(fury);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsChooseCard(balin);
		assertAttachedTo(oin, balin);
		// Nothing asked: one wound of Balin's healed, and Dwalin, who does not bear Óin, keeps both of his.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWithNoDwarvenCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oin = scn.GetFreepsCard("oin");
		var balin = scn.GetFreepsCard("balin");
		var takeup = scn.GetFreepsCard("takeup");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(oin);

		scn.StartGame();
		scn.MoveCardsToHand(takeup);
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, oin);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		assertInZone(Zone.SUPPORT, oin);
		assertInHand(takeup);
		assertEquals(2, scn.GetWoundsOn(balin));
	}

	@Test
	public void AnotherFollowerTransferredToAWoundedCompanionHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oin = scn.GetFreepsCard("oin");
		var bifur = scn.GetFreepsCard("bifur");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveCardsToSupportArea(oin, bifur);

		scn.StartGame();
		scn.MoveCardsToHand(fury);
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(balin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dwalin));
		assertInHand(fury);
		assertInZone(Zone.SUPPORT, oin, bifur);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		scn.FreepsChooseOptionalTrigger(bifur, "Aid");
		// Bifur's Aid: exert a companion.
		scn.FreepsChooseCard(dwalin);
		assertEquals(1, scn.GetWoundsOn(dwalin));
		scn.FreepsChooseCard(balin);
		assertAttachedTo(bifur, balin);

		// Bifur is no Óin: Balin keeps both wounds. Óin's own Aid is still to answer, and is declined.
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, oin);
		assertInHand(fury);
		assertEquals(2, scn.GetWoundsOn(balin));
	}

	@Test
	public void AttachedByAnotherCardWithoutItsAidItStillHealsTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var oin = scn.GetFreepsCard("oin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var warhammer = scn.GetFreepsCard("warhammer");
		var takeup = scn.GetFreepsCard("takeup");
		var takeup2 = scn.GetFreepsCard("takeup2");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, warhammer);
		scn.MoveCardsToSupportArea(oin);

		scn.StartGame();
		// No [dwarven] card in hand, so Óin's own Aid is not offered.
		scn.MoveCardsToHand(takeup, takeup2);
		scn.MoveMinionsToTable("runner");
		scn.AddWoundsToChar(balin, 2);
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertAttachedTo(warhammer, balin);
		assertInZone(Zone.SUPPORT, oin);
		assertInHand(takeup, takeup2);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(warhammer));
		scn.FreepsUseCardAction(warhammer);

		// Two cards in hand, both discarded; Óin, the one [dwarven] follower in the support area, goes to Balin.
		assertInDiscard(takeup, takeup2);
		assertAttachedTo(oin, balin);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
