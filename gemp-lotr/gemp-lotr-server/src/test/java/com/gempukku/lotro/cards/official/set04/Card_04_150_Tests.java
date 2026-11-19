package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_150_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crossbowmen", "4_150");
					put("crossbow", "4_183");   // Uruk Crossbowman: [isengard] Uruk-hai archer; an archery ability used only on purpose
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer, Archer is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: an ally; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EliteCrossbowmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Elite Crossbowmen
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Archer</b>. <b>Damage +1</b>.<br><b>Archery:</b> Exert Elite Crossbowmen to make the Free Peoples player wound an unbound companion; this minion does not add to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crossbowmen");

		assertEquals("Elite Crossbowmen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ArcheryAbilityExertsHimToWoundAnUnboundCompanionAndTakesHimOutOfTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crossbowmen = scn.GetShadowCard("crossbowmen");
		var crossbow = scn.GetShadowCard("crossbow");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(crossbowmen, crossbow, marksman);
		// Vitality 3 with 2 wounds: Gimli is exhausted, so a wound kills him (an exertion could not be his).
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Three archers, each adding 1: before he uses his ability he adds like any archer.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(crossbowmen));
		assertEquals(0, scn.GetWoundsOn(crossbow));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(0, scn.GetFreepsDeadCount());

		scn.ShadowUseCardAction(crossbowmen);
		// He exerts, and the other archer does not.
		assertEquals(1, scn.GetWoundsOn(crossbowmen));
		assertEquals(0, scn.GetWoundsOn(crossbow));

		// The Free Peoples player chooses: unbound companions of two cultures, the exhausted one among them; not the
		// Ring-bearer, not a Ring-bound companion who is not the Ring-bearer, not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, maggot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// The wound kills the exhausted Gimli.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		// He no longer adds; the Crossbowman and the Marksman still do.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Still out of the total as the archery wounds are placed: two of them.
		scn.PassArcheryActions();
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// Gone with the archery phase: each of the three archers adds again.
		assertEquals(3, scn.GetShadowArcheryTotal());
	}

	@Test
	public void WithNoUnboundCompanionToWoundTheAbilityIsStillOfferedAndStillTakesHimOutOfTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crossbowmen = scn.GetShadowCard("crossbowmen");
		var crossbow = scn.GetShadowCard("crossbow");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(crossbowmen, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// The Ring-bearer and Sam are Ring-bound and Maggot is no companion: nobody is unbound.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(crossbowmen));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertTrue(scn.ShadowActionAvailable(crossbowmen));

		scn.ShadowUseCardAction(crossbowmen);

		// The cost is paid and nobody is wounded, but he still drops out of the total.
		assertEquals(1, scn.GetWoundsOn(crossbowmen));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var crossbowmen = scn.GetShadowCard("crossbowmen");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(crossbowmen, crossbow);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(crossbowmen, 2);
		assertEquals(2, scn.GetWoundsOn(crossbowmen));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(2, scn.GetWoundsOn(crossbowmen));
		assertEquals(0, scn.GetWoundsOn(crossbow));
		assertFalse(scn.ShadowActionAvailable(crossbowmen));
		assertTrue(scn.ShadowActionAvailable(crossbow));
	}
}
