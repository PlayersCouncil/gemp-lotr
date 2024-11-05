
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_023_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("gone", "101_23");
					put("aragorn", "1_365");
					put("arwen", "1_30");
					put("gimli", "1_12");
					put("gandalf", "2_122");
					put("sam", "1_311");

					put("dwarf-event", "1_3");
					put("elf-event", "1_37");
					put("gandalf-event", "1_78");
					put("gondor-event", "1_116");
					put("shire-event", "1_116");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");
					put("runner6", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void IWouldHaveGoneWithYoutotheEndStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *I Would Have Gone With You to the End
		* Side: Free Peoples
		* Culture: gondor
		* Twilight Cost: 2
		* Type: condition
		* Subtype: Support Area
		* Game Text: Each time you play a skirmish event during a skirmish involving a companion with the Aragorn signet, you may spot Aragorn to stack that event here.
		* 	Skirmish: Spot Aragorn and discard X cards here to make a companion with the Aragorn signet strength +X (limit +3).
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gone = scn.GetFreepsCard("gone");

		assertTrue(gone.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gone.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, gone.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, gone.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, gone.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(gone, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(2, gone.getBlueprint().getTwilightCost());
		//assertEquals(, gone.getBlueprint().getStrength());
		//assertEquals(, gone.getBlueprint().getVitality());
		//assertEquals(, gone.getBlueprint().getResistance());
		//assertEquals(Signet., gone.getBlueprint().getSignet());
		//assertEquals(, gone.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void GornSignetSkirmishEventsAreInterceptedAndUsedToPumpGornSignet() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gone = scn.GetFreepsCard("gone");

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl aragorn = scn.GetFreepsCard("aragorn");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl sam = scn.GetFreepsCard("sam");

		PhysicalCardImpl dwarfE = scn.GetFreepsCard("dwarf-event");
		PhysicalCardImpl elfE = scn.GetFreepsCard("elf-event");
		PhysicalCardImpl gondorE = scn.GetFreepsCard("gondor-event");
		PhysicalCardImpl gandalfE = scn.GetFreepsCard("gandalf-event");
		PhysicalCardImpl shireE = scn.GetFreepsCard("shire-event");

		scn.FreepsMoveCardToSupportArea(gone);
		scn.FreepsMoveCharToTable(aragorn, gimli, arwen, gandalf, sam);
		scn.FreepsMoveCardToHand(dwarfE, elfE, gondorE, gandalfE, shireE);

		PhysicalCardImpl runner1 = scn.GetShadowCard("runner1");
		PhysicalCardImpl runner2 = scn.GetShadowCard("runner2");
		PhysicalCardImpl runner3 = scn.GetShadowCard("runner3");
		PhysicalCardImpl runner4 = scn.GetShadowCard("runner4");
		PhysicalCardImpl runner5 = scn.GetShadowCard("runner5");
		PhysicalCardImpl runner6 = scn.GetShadowCard("runner6");

		scn.ShadowMoveCharToTable(runner1, runner2, runner3, runner4, runner5, runner6);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{gimli, runner2},
				new PhysicalCardImpl[]{arwen, runner3},
				new PhysicalCardImpl[]{gandalf, runner4},
				new PhysicalCardImpl[]{frodo, runner5},
				new PhysicalCardImpl[]{sam, runner6});

		assertEquals(0, scn.GetStackedCards(gone).size());

		// Frodo is the only non-aragorn-signet in the fellowship, so let's test the lack of trigger
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPlayCard(shireE);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.FreepsDecisionAvailable("I Would Have Gone With You to the End"));
		// response from the one ring
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetStackedCards(gone).size());

		// 1
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.FreepsActionAvailable(gone)); // no stacks yet
		scn.FreepsPlayCard(gondorE);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetStackedCards(gone).size());
		// The convoluted checking here is because the old event has been deleted and recreated
		// after going into the discard pile and getting redirected as a stacked item.
		// But not for the other events??? no idea why tbh
		assertTrue(scn.GetStackedCards(gone).stream().anyMatch(x -> x.getBlueprintId() == gondorE.getBlueprintId()));
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// 2
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.FreepsActionAvailable(gone));
		scn.FreepsPlayCard(dwarfE);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetStackedCards(gone).size());
		assertEquals(Zone.STACKED, dwarfE.getZone());
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// 3
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.FreepsActionAvailable(gone));
		scn.FreepsPlayCard(elfE);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetStackedCards(gone).size());
		assertEquals(Zone.STACKED, elfE.getZone());
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		// 4
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.FreepsActionAvailable(gone));
		scn.FreepsPlayCard(gandalfE);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(4, scn.GetStackedCards(gone).size());
		assertEquals(Zone.STACKED, gandalfE.getZone());
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();


		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.FreepsActionAvailable(gone));
		scn.FreepsUseCardAction(gone);
		assertTrue(scn.FreepsDecisionAvailable("Choose stacked cards to discard"));
		assertEquals(3, scn.GetStrength(sam));
		scn.FreepsChooseCards(gandalfE, elfE, dwarfE);
		//lots of candidates, but only one skirmishing, so Sam gets auto-picked
		assertEquals(6, scn.GetStrength(sam));
		assertEquals(1, scn.GetStackedCards(gone).size());
		assertTrue(scn.GetStackedCards(gone).stream().anyMatch(x -> x.getBlueprintId() == gondorE.getBlueprintId()));
		assertEquals(Zone.DISCARD, gandalfE.getZone());
		assertEquals(Zone.DISCARD, elfE.getZone());
		assertEquals(Zone.DISCARD, dwarfE.getZone());
	}
}
