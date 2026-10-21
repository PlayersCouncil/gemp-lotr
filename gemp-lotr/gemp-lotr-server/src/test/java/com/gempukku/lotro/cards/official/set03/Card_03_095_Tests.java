package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard1", "3_95");
					put("guard2", "3_95");
					put("defenses", "1_274");   // Sauron's Defenses: [sauron] condition, support area; acts only with 3 companions dead
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition borne by a companion; acts only at a sanctuary
					put("pinned", "1_192");     // Pinned Down: [moria] condition, support area; acts only with a [moria] archer
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession on a [sauron] Orc; a skirmish ability only
					put("mallorn", "1_54");     // Mallorn-trees: [elven] Free Peoples condition, support area; a fellowship action only
					put("saga", "1_114");       // The Saga of Elendil: [gondor] Free Peoples condition on a [gondor] companion; a skirmish ability only
					put("sword", "1_112");      // Ranger's Sword: Free Peoples possession on Aragorn; damage +1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: bears the cards above; a maneuver ability, never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Maneuver:</b> Exert this minion and discard your [sauron] condition to discard a Free Peoples condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard1");

		assertEquals("Orc Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsHimAndDiscardsYourSauronConditionToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga, sword);
		scn.MoveCardsToSupportArea(mallorn);

		var guard1 = scn.GetShadowCard("guard1");
		var guard2 = scn.GetShadowCard("guard2");
		var defenses = scn.GetShadowCard("defenses");
		var evil = scn.GetShadowCard("evil");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard2, scimitar);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveCardsToSupportArea(defenses, pinned);

		assertInZone(Zone.SUPPORT, defenses, pinned, mallorn);
		assertAttachedTo(evil, aragorn);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(scimitar, guard2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowActionAvailable(guard1));

		scn.ShadowUseCardAction(guard1);
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(guard2));

		// The cost: one of his own [sauron] conditions, wherever it stands; not a [moria] condition, nor a
		// [sauron] card that is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(defenses, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, scimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(defenses);
		assertInDiscard(defenses);

		// The target: a Free Peoples condition, wherever it stands; not a Shadow condition, even one a
		// companion bears, nor a Free Peoples card that is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, saga));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(evil, pinned, sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saga);

		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, pinned, mallorn);
		assertAttachedTo(evil, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(scimitar, guard2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExhaustedHeCannotUseTheAbilityWhileAnotherCopyCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(mallorn);

		var guard1 = scn.GetShadowCard("guard1");
		var guard2 = scn.GetShadowCard("guard2");
		var defenses = scn.GetShadowCard("defenses");

		scn.StartGame();
		scn.MoveMinionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(defenses);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(guard1, 1);

		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertInZone(Zone.SUPPORT, defenses, mallorn);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(guard1));
		assertTrue(scn.ShadowActionAvailable(guard2));
	}

	@Test
	public void WithNoSauronConditionOfHisOwnTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(mallorn);

		var guard = scn.GetShadowCard("guard1");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(guard, scimitar);
		scn.MoveCardsToSupportArea(pinned);

		assertInZone(Zone.SUPPORT, pinned, mallorn);
		assertAttachedTo(scimitar, guard);

		// A [moria] condition and a [sauron] possession: nothing to discard for the cost.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowActionAvailable(guard));
	}

	@Test
	public void WithNoFreePeoplesConditionTheAbilityIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var guard = scn.GetShadowCard("guard1");
		var defenses = scn.GetShadowCard("defenses");
		var evil = scn.GetShadowCard("evil");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveCardsToSupportArea(defenses);

		assertInZone(Zone.SUPPORT, defenses);
		assertAttachedTo(evil, aragorn);
		assertAttachedTo(sword, aragorn);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowActionAvailable(guard));

		scn.ShadowUseCardAction(guard);
		scn.ShadowChooseCard(defenses);

		// Both costs paid, and nothing more: the Shadow condition and the possession Aragorn bears stay.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInDiscard(defenses);
		assertAttachedTo(evil, aragorn);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
