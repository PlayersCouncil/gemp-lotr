package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_023_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("narya", "32_23");
					// Gandalf, Friend of Thorin: the Hobbit sets' Gandalf, vitality 4; a Fellowship ability only, and a
					// discard bar that wants two [dwarven] followers (none is laid)
					put("gandalf", "30_26");
					// Gandalf, Friend of the Shirefolk: a Gandalf of the Decipher sets; a strength bonus only
					put("gandalf1", "1_72");
					// Radagast, The Brown: a [gandalf] Wizard ally who is not Gandalf; a Fellowship ability only
					put("radagast", "31_17");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; a strength bonus beside 3 Ents
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability only

					// [gandalf] events: A Wizard Is Never Late (Fellowship, 1) and He gives me courage (Maneuver, 2:
					// spot Gandalf to remove a doubt)
					put("wizard", "30_23");
					put("courage", "30_30");
					put("glamdring", "30_29");  // Glamdring: a [gandalf] card that is no event (an artifact, cost 2)
					put("lore", "30_13");       // Lore of Imladris: an event that is not [gandalf] ([dwarven], cost 1)

					// Fierce in Despair: [raider] Regroup event, cost 7, "Spot 2 [raider] Men to discard a companion
					// (except the Ring-bearer)", the Shadow player choosing; two Easterling Axemen are the Men
					put("despair", "7_148");
					put("axeman1", "4_224");
					put("axeman2", "4_224");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NaryaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Narya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: 1
		 * Game Text: Bearer must be Gandalf.
		 * 		The twilight cost of each [gandalf] event is -1.
		 * 		Response: If Gandalf is about to be discarded, exert him to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("narya");

		assertEquals("Narya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedOnGandalfItMakesEachGandalfEventCostOneLessAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wizard = scn.GetFreepsCard("wizard");
		var courage = scn.GetFreepsCard("courage");
		var glamdring = scn.GetFreepsCard("glamdring");
		var lore = scn.GetFreepsCard("lore");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(narya, wizard, courage, glamdring, lore);

		scn.StartGame();

		assertEquals(1, scn.GetTwilightCost(wizard));
		assertEquals(2, scn.GetTwilightCost(courage));
		assertEquals(2, scn.GetTwilightCost(glamdring));
		assertEquals(1, scn.GetTwilightCost(lore));

		// Gandalf is the one bearer there is, so Narya goes on him unasked.
		assertTrue(scn.FreepsPlayAvailable(narya));
		scn.FreepsPlayCard(narya);
		assertAttachedTo(narya, gandalf);

		assertEquals(0, scn.GetTwilightCost(wizard));
		assertEquals(1, scn.GetTwilightCost(courage));
		assertEquals(2, scn.GetTwilightCost(glamdring));
		assertEquals(1, scn.GetTwilightCost(lore));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfEventPlayedWhileNaryaIsBorneAddsOneTwilightLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var courage = scn.GetFreepsCard("courage");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, narya);
		scn.MoveCardsToHand(courage);

		scn.StartGame();
		scn.MoveMinionsToTable("axeman1");
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(courage);

		// 2 printed - 1 for Narya
		assertEquals(pool + 1, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(courage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithoutGandalfAndPlayableOnTheDecipherGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf1 = scn.GetFreepsCard("gandalf1");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(narya);

		scn.StartGame();

		// A [gandalf] Wizard ally, a [gandalf] companion and Bilbo, and no Gandalf among them.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(narya));

		scn.MoveCompanionsToTable(gandalf1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(narya));
		scn.FreepsPlayCard(narya);
		assertAttachedTo(narya, gandalf1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenGandalfIsAboutToBeDiscardedHeMayExertToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.AttachCardsTo(gandalf, narya);

		var despair = scn.GetShadowCard("despair");

		scn.StartGame();
		assertAttachedTo(narya, gandalf);
		scn.MoveMinionsToTable("axeman1", "axeman2");
		scn.MoveCardsToHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		// Fierce in Despair costs 7.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, balin));
		scn.ShadowChooseCard(gandalf);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narya));
		scn.FreepsChooseOptionalTrigger(narya);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertAttachedTo(narya, gandalf);
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherCompanionAboutToBeDiscardedIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.AttachCardsTo(gandalf, narya);

		var despair = scn.GetShadowCard("despair");

		scn.StartGame();
		assertAttachedTo(narya, gandalf);
		scn.MoveMinionsToTable("axeman1", "axeman2");
		scn.MoveCardsToHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(despair);
		scn.ShadowChooseCard(balin);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(narya));
		assertInZone(Zone.DISCARD, balin);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertAttachedTo(narya, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedGandalfCannotExertToPreventHisDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narya = scn.GetFreepsCard("narya");
		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.AttachCardsTo(gandalf, narya);

		var despair = scn.GetShadowCard("despair");

		scn.StartGame();
		assertAttachedTo(narya, gandalf);
		// Vitality 4, +1 from Narya: four wounds leave him exhausted.
		scn.AddWoundsToChar(gandalf, 4);
		assertEquals(4, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable("axeman1", "axeman2");
		scn.MoveCardsToHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(despair);
		scn.ShadowChooseCard(gandalf);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(narya));
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.DISCARD, narya);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
