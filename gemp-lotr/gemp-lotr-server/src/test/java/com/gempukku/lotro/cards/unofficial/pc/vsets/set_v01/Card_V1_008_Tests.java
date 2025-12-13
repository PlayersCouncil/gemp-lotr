package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("lament", "101_8");
					put("gandalf", "1_364");
					put("elrond", "3_13");
					put("gimli", "1_13");
					put("guard", "1_7");
					put("lastalliance", "1_49");

					put("marksman", "1_176");
					put("bladetip", "1_209");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LamentForGandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Title: Lament for the Fallen
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: condition
		 * Subtype: Support Area
		 * Game Text: To play, exert an Elf and spot a unique companion in the dead pile.  Bearer must be an unbound companion.
		 * Bearer cannot be exerted, wounded, or assigned to a skirmish.
		 * At the start of the regroup phase, discard a condition attached to bearer (and heal bearer if Gandalf is in the dead pile).
		 */

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("lament");

		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertFalse(card.getBlueprint().isUnique());
		//assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, card.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet());
		//assertEquals(, card.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies
	}

	@Test
	public void LamentExertsAnElfAndSpotsADeadCompToPlayOnAnUnboundComp() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl lament = scn.GetFreepsCard("lament");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.MoveCardsToHand(lament, guard, elrond);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(lament));
		scn.FreepsPlayCard(elrond);
		assertFalse(scn.FreepsPlayAvailable(lament));
		scn.AddWoundsToChar(gandalf, 4);
		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsPlayAvailable(lament));

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(Zone.HAND, lament.getZone());
		scn.FreepsPlayCard(lament);
		assertTrue(scn.FreepsDecisionAvailable("Choose"));
		assertEquals(2, scn.FreepsGetCardChoiceCount()); // gimli, guard, but not frodo
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(Zone.ATTACHED, lament.getZone());
		assertEquals(gimli, lament.getAttachedTo());
	}

//	@Test
//	public void BearerOfLamentCannotBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
//		//Pre-game setup
//		VirtualTableScenario scn = GetScenario();
//
//		PhysicalCardImpl lament = scn.GetFreepsCard("lament");
//		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
//		PhysicalCardImpl guard = scn.GetFreepsCard("guard");
//
//		scn.MoveCompanionToTable(gimli);
//		scn.AttachCardsTo(gimli, lament);
//		scn.MoveCardsToHand(guard);
//
//		scn.StartGame();
//
//		assertFalse(scn.FreepsCardPlayAvailable(guard)); // gimli can't be spotted, so guard's text can't be fulfilled
//	}

	@Test
	public void BearerOfLamentCannotBeExertedOrWoundedOrAssignedToSkimish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl lament = scn.GetFreepsCard("lament");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, lament);
		scn.MoveCardsToHand(guard);

		PhysicalCardImpl marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo)); //automatically applied as Gimli can't be wounded

		//assignment
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.FreepsGetADParam("freeCharacters").length); // gimli is not allowed to skirmish
		scn.FreepsAssignToMinions(frodo, marksman);
		scn.FreepsResolveSkirmish(frodo);
		assertFalse(scn.FreepsActionAvailable(gimli)); // Can't use gimli's ability as he can't exert
	}

	@Test
	public void AtStartOfRegroupBearerDiscardsAnAttachedCondition() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl lament = scn.GetFreepsCard("lament");
		PhysicalCardImpl lastalliance = scn.GetFreepsCard("lastalliance");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(gimli);
		scn.AddWoundsToChar(gimli, 1);
		scn.AttachCardsTo(gimli, lament);
		scn.AttachCardsTo(gimli, lastalliance); //can't usually go there but who cares
		scn.MoveCardsToHand(guard);

		PhysicalCardImpl bladetip = scn.GetShadowCard("bladetip");
		scn.AttachCardsTo(gimli, bladetip);

		scn.StartGame();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(Zone.ATTACHED, lament.getZone());
		assertEquals(gimli, lament.getAttachedTo());
		assertEquals(Zone.ATTACHED, bladetip.getZone());
		assertEquals(gimli, bladetip.getAttachedTo());
		assertEquals(Zone.ATTACHED, lastalliance.getZone());
		assertEquals(gimli, lastalliance.getAttachedTo());

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to discard"));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(bladetip);
		assertEquals(Zone.ATTACHED, lament.getZone());
		assertEquals(Zone.DISCARD, bladetip.getZone());
		assertEquals(Zone.ATTACHED, lastalliance.getZone());
		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void AtStartOfRegroupBearerDiscardsAndHealsIfGandalfIsDead() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl lament = scn.GetFreepsCard("lament");
		PhysicalCardImpl lastalliance = scn.GetFreepsCard("lastalliance");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl guard = scn.GetFreepsCard("guard");

		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(gandalf, 4);
		scn.AttachCardsTo(gimli, lament);
		scn.AttachCardsTo(gimli, lastalliance); //can't usually go there but who cares
		scn.MoveCardsToHand(guard);

		PhysicalCardImpl bladetip = scn.GetShadowCard("bladetip");
		scn.AttachCardsTo(gimli, bladetip);

		scn.StartGame();

		assertEquals(Zone.DEAD, gandalf.getZone());
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsDecisionAvailable("Choose cards to discard"));
		scn.FreepsChooseCard(bladetip);
		assertEquals(0, scn.GetWoundsOn(gimli));
	}
}
