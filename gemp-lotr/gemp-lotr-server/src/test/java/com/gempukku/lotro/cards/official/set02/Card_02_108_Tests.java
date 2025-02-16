package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_108_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("elbereth", "2_108");

					put("nelya", "1_233");
					put("goblinarcher", "1_176");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.IsildursBaneRing,
				GenericCardTestHelper.Fellowship //We need the Ring-bearer's skirmish to be cancelable
		);
	}

	@Test
	public void OElberethGilthonielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: O Elbereth! Gilthoniel!
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Strength: 1
		 * Game Text: <b>Tale</b>. Bearer must be the Ring-bearer.<br><b>Skirmish:</b> Discard this condition to take off The One Ring or to cancel a skirmish involving the Ring-bearer and a Nazgûl.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elbereth");

		assertEquals("O Elbereth! Gilthoniel!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void OElberethGilthonielGoesOnRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var elbereth = scn.GetFreepsCard("elbereth");
		scn.FreepsMoveCardToHand(elbereth);

		scn.StartGame();
		assertEquals(Zone.HAND, elbereth.getZone());
		scn.FreepsPlayCard(elbereth);
		assertEquals(Zone.ATTACHED, elbereth.getZone());
		assertSame(scn.GetRingBearer(), elbereth.getAttachedTo());
	}

	@Test
	public void SkirmishAbilityCanDiscardToTakeOffRing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.ShadowMoveCharToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger(); // IB ring turning archery wound into 2 burdens

		//Assignments
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, goblinarcher);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.RBWearingOneRing());
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}

	@Test
	public void SkirmishAbilityCanCancelRBSkirmishIfSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var nelya = scn.GetShadowCard("nelya");
		scn.ShadowMoveCharToTable(nelya);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, nelya);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}

	@Test
	public void SkirmishAbilityCantCancelRBSkirmishIfNotSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.ShadowMoveCharToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();

		//Assignments
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, goblinarcher);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		scn.FreepsChooseMultipleChoiceOption("cancel");
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}
}
