package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_108_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elbereth", "52_108");

					put("nelya", "1_233");
					put("goblinarcher", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing,
				VirtualTableScenario.Fellowship //We need the Ring-bearer's skirmish to be cancelable
		);
	}

	@Test
	public void OElberethGilthonielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: *O Elbereth! Gilthoniel!
		* Side: Free Peoples
		* Culture: Shire
		* Twilight Cost: 1
		* Type: condition
		* Subtype: 
		* Strength: 1
		* Game Text: <b>Tale.</b>  Bearer must be the Ring-bearer.
		* 	<b>Skirmish:</b> Discard this condition to make the Ring-bearer strength +4 if skirmishing a Nazgul or to take off The One Ring.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var elbereth = scn.GetFreepsCard("elbereth");

		assertTrue(elbereth.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, elbereth.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, elbereth.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, elbereth.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(elbereth, Keyword.TALE));
		assertEquals(1, elbereth.getBlueprint().getTwilightCost());
		assertEquals(1, elbereth.getBlueprint().getStrength());
	}

	@Test
	public void OElberethGilthonielGoesOnRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var elbereth = scn.GetFreepsCard("elbereth");
		scn.MoveCardsToHand(elbereth);

		scn.StartGame();
		assertEquals(Zone.HAND, elbereth.getZone());
		scn.FreepsPlayCard(elbereth);
		assertEquals(Zone.ATTACHED, elbereth.getZone());
		assertSame(scn.GetRingBearer(), elbereth.getAttachedTo());
	}

	@Test
	public void SkirmishAbilityCanDiscardToTakeOffRing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.MoveMinionsToTable(goblinarcher);

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
	public void SkirmishAbilityCanMakeRBStrengthPlus4IfSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveMinionsToTable(nelya);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, nelya);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertEquals(5, scn.GetStrength(frodo)); // 3 + 1 from ring +1 from O Elbereth itself
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}

	@Test
	public void SkirmishAbilityCantMakeRBStrengthPlus4IfNotSkirmishingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var elbereth = scn.GetFreepsCard("elbereth");
		scn.AttachCardsTo(frodo, elbereth);

		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.MoveMinionsToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();

		//Assignments
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, goblinarcher);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.FreepsActionAvailable(elbereth));
		assertEquals(5, scn.GetStrength(frodo)); // 3 + 1 from ring +1 from O Elbereth itself
		assertEquals(Zone.ATTACHED, elbereth.getZone());

		scn.FreepsUseCardAction(elbereth);
		assertEquals(4, scn.GetStrength(frodo)); // 3 + 1 from ring, nothing from O Elbereth itself
		assertEquals(Zone.DISCARD, elbereth.getZone());
	}
}
