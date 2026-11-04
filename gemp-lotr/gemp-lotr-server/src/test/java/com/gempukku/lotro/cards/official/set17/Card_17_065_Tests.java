package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("primitive", "17_65");
					put("primitive2", "17_65");

					// Man of Bree: a [men] Man, cost 2, vitality 1, site 4; strength +1 for each card in the Free
					// Peoples hand
					put("bree", "11_90");
					put("bree2", "11_90");
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his strength bonus wants a [men]
					// minion bearing a possession, his Skirmish ability is not reached
					put("saruman", "17_48");
					// Dunlending Brigand: a [dunland] Man with no text
					put("brigand", "4_10");
					// Fleet of Corsair Ships: a [men] support-area possession; a play trigger and a Regroup ability only
					put("fleet", "18_66");

					// Aragorn, Ranger of the North: his Maneuver ability is not used
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VengefulPrimitiveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vengeful Primitive
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: You may play this minion any time you could play a maneuver event. When you play this minion in the maneuver phase, you may discard X [men] minions from play to add (X).
		*/

		var scn = GetScenario();

		var primitive = scn.GetFreepsCard("primitive");

		assertEquals("Vengeful Primitive", primitive.getBlueprint().getTitle());
		assertNull(primitive.getBlueprint().getSubtitle());
		assertFalse(primitive.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, primitive.getBlueprint().getSide());
		assertEquals(Culture.MEN, primitive.getBlueprint().getCulture());
		assertEquals(CardType.MINION, primitive.getBlueprint().getCardType());
		assertEquals(Race.MAN, primitive.getBlueprint().getRace());
		assertEquals(6, primitive.getBlueprint().getTwilightCost());
		assertEquals(15, primitive.getBlueprint().getStrength());
		assertEquals(3, primitive.getBlueprint().getVitality());
		assertEquals(4, primitive.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedInTheManeuverPhaseItMayDiscardMenMinionsFromPlayToAddOneForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var primitive = scn.GetShadowCard("primitive");
		var bree = scn.GetShadowCard("bree");
		var bree2 = scn.GetShadowCard("bree2");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToHand(primitive);
		scn.MoveCardsToSupportArea(fleet);

		scn.StartGame();
		scn.MoveMinionsToTable(bree, saruman, brigand);
		scn.StackCardsOn(fleet, bree2);
		assertInZone(Zone.HAND, primitive);
		assertInZone(Zone.STACKED, bree2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(primitive));
		scn.ShadowPlayCard(primitive);
		assertInZone(Zone.SHADOW_CHARACTERS, primitive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(primitive));
		int twilight = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();

		// [men] minions in play, the Primitive among them; not a Man of [dunland], a [men] possession, nor a [men]
		// minion stacked on it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bree, saruman, primitive));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, fleet, bree2));
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(bree, saruman);

		assertInZone(Zone.DISCARD, bree, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, primitive, brigand);
		assertInZone(Zone.STACKED, bree2);
		// X = 2
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ThePrimitiveMayDiscardItselfToAddOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var primitive = scn.GetShadowCard("primitive");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToHand(primitive);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.HAND, primitive);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(primitive);
		int twilight = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(bree, primitive));
		scn.ShadowChooseCards(primitive);

		assertInZone(Zone.DISCARD, primitive);
		assertInZone(Zone.SHADOW_CHARACTERS, bree);
		// X = 1
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheChoiceOfMinionsToDiscardStartsAtOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var primitive = scn.GetShadowCard("primitive");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToHand(primitive);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.HAND, primitive);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(primitive);
		scn.ShadowAcceptOptionalTrigger();
		// Declining the offer is the one way to discard none (Q-0015 as amended by Q-0034; shapes.md, F-0300).
		assertTrue(scn.ShadowHasCardChoiceAvailable(bree, primitive));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(bree);
		assertInZone(Zone.DISCARD, bree);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningTheOfferDiscardsNothingAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var primitive = scn.GetShadowCard("primitive");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToHand(primitive);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.HAND, primitive);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(primitive);
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(primitive));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, primitive, bree);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PlayedInTheShadowPhaseItMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var primitive = scn.GetShadowCard("primitive");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToHand(primitive);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.HAND, primitive);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(primitive));
		scn.ShadowPlayCard(primitive);
		assertInZone(Zone.SHADOW_CHARACTERS, primitive, bree);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(primitive));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACopyAlreadyInPlayMakesNoOfferWhenTheSecondIsPlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var primitive = scn.GetShadowCard("primitive");
		var primitive2 = scn.GetShadowCard("primitive2");
		scn.MoveCardsToHand(primitive2);

		scn.StartGame();
		scn.MoveMinionsToTable(primitive);
		assertInZone(Zone.HAND, primitive2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(primitive2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(primitive2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(primitive));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, primitive, primitive2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var primitive = scn.GetShadowCard("primitive");
		var bree = scn.GetShadowCard("bree");
		scn.MoveCardsToHand(primitive);

		scn.StartGame();
		scn.MoveMinionsToTable(bree);
		assertInZone(Zone.HAND, primitive);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(primitive));
	}
}
