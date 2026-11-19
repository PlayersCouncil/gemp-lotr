package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_135_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The card is not unique: two copies, so that "this minion" can be told from another.
					put("uruk1", "12_135");
					put("uruk2", "12_135");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: a [dwarven] Dwarf companion; a skirmish ability used only on purpose
					put("bounder", "1_286");     // Bounder: a [shire] Hobbit ally whose home is site 2, where he may be assigned; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarbaricUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Barbaric Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Assignment:</b> Make the Free Peoples player assign this minion to an unwounded companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruk1");

		assertEquals("Barbaric Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilityHasTheFreePeoplesPlayerAssignThisMinionToAnUnwoundedCompanionOfHisChoiceAndOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// The maneuver phase, next door to the assignment phase: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(uruk1));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(uruk1));
		assertTrue(scn.ShadowActionAvailable(uruk2));

		scn.ShadowUseCardAction(uruk1);

		// The Free Peoples player chooses: the Ring-bearer and Aragorn, unwounded companions of two cultures; not
		// Gimli, who is wounded, nor the Bounder, an unwounded ally at his home site who is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, uruk1));
		assertFalse(scn.IsCharAssigned(uruk2));
		assertFalse(scn.IsCharAssigned(frodo));

		// Once assigned, this copy offers its ability no more; the other copy, unassigned, still does.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(uruk1));
		assertTrue(scn.ShadowActionAvailable(uruk2));
	}

	@Test
	public void WithNoUnwoundedCompanionLeftToAssignTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(uruk1));
		assertTrue(scn.ShadowActionAvailable(uruk2));

		// The Ring-bearer is the one unwounded companion: he is assigned with nothing asked.
		scn.ShadowUseCardAction(uruk2);
		assertTrue(scn.IsCharAssignedAgainst(frodo, uruk2));

		// Now nobody unwounded is left to assign the first copy to, and it does not offer its ability.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(uruk1));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.ShadowActionAvailable(uruk1));
	}
}
