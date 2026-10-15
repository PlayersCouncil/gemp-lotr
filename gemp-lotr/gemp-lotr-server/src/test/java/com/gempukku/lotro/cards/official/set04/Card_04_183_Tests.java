package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_183_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crossbow1", "4_183");
					put("crossbow2", "4_183");
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer, Archer is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCrossbowmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Crossbowman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Archer</b>.<br><b>Archery:</b> Exert this minion to make the Free Peoples player wound an unbound companion; this minion does not add to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crossbow1");

		assertEquals("Uruk Crossbowman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ArcheryAbilityExertsThisCopyToWoundAnUnboundCompanionAndTakesItOutOfTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crossbow1 = scn.GetShadowCard("crossbow1");
		var crossbow2 = scn.GetShadowCard("crossbow2");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(crossbow1, crossbow2, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Three archers, each adding 1: an unused Crossbowman adds like any archer.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(crossbow1));
		assertEquals(0, scn.GetWoundsOn(crossbow2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));

		scn.ShadowUseCardAction(crossbow1);
		// The copy used exerts, and the other copy does not.
		assertEquals(1, scn.GetWoundsOn(crossbow1));
		assertEquals(0, scn.GetWoundsOn(crossbow2));

		// The Free Peoples player chooses: unbound companions of two cultures; not the Ring-bearer, not a
		// Ring-bound companion who is not the Ring-bearer, not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, maggot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		// The copy used no longer adds; the other copy and the Marksman still do.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Still out of the total as the archery wounds are placed: two of them.
		scn.PassArcheryActions();
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// Gone with the archery phase: each of the three archers adds again.
		assertEquals(3, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedOnceThisCopyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crossbow1 = scn.GetShadowCard("crossbow1");
		var crossbow2 = scn.GetShadowCard("crossbow2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(crossbow1, crossbow2);
		// Vitality 2 with 1 wound: this copy cannot exert.
		scn.AddWoundsToChar(crossbow1, 1);
		assertEquals(1, scn.GetWoundsOn(crossbow1));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(1, scn.GetWoundsOn(crossbow1));
		assertEquals(0, scn.GetWoundsOn(crossbow2));
		assertFalse(scn.ShadowActionAvailable(crossbow1));
		assertTrue(scn.ShadowActionAvailable(crossbow2));
	}
}
