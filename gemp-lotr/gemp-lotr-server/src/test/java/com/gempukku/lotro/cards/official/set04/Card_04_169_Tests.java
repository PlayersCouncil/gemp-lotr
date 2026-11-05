package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "4_169");
					put("crossbow", "4_183");   // Uruk Crossbowman: [isengard] Uruk-hai archer, cost 3; an archery ability used only on purpose
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] Uruk-hai archer, cost 5; archer and damage +1 only
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no archer, cost 2; damage +1 only
					put("marksman", "1_176");   // Goblin Marksman: [moria] archer, cost 3; Archer is its whole text
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
	public void RangedCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ranged Commander
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Archer</b>.<br>The twilight cost of each [isengard] archer is -1.<br><b>Archery:</b> Exert Ranged Commander to make the Free Peoples player wound an unbound companion; this minion does not add to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Ranged Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileHeIsInPlayEachIsengardArcherCostsOneLessAndNothingElseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var crossbow = scn.GetShadowCard("crossbow");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		var fpCrossbow = scn.GetFreepsCard("crossbow");
		scn.MoveCardsToHand(commander, crossbow, assassin, savage, marksman, fpCrossbow);
		assertInHand(commander);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// In hand, before he is in play: each card at its printed cost.
		assertInHand(commander);
		assertEquals(3, scn.GetTwilightCost(crossbow));
		assertEquals(5, scn.GetTwilightCost(assassin));
		assertEquals(2, scn.GetTwilightCost(savage));
		assertEquals(3, scn.GetTwilightCost(marksman));
		assertEquals(3, scn.GetTwilightCost(fpCrossbow));

		scn.ShadowPlayCard(commander);
		assertInPlay(commander);

		// [isengard] archers of two titles, and the Free Peoples player's own copy (the text names no player): -1.
		assertEquals(2, scn.GetTwilightCost(crossbow));
		assertEquals(4, scn.GetTwilightCost(assassin));
		assertEquals(2, scn.GetTwilightCost(fpCrossbow));
		// An [isengard] minion who is no archer, and an archer of another culture: unchanged.
		assertEquals(2, scn.GetTwilightCost(savage));
		assertEquals(3, scn.GetTwilightCost(marksman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsHimToWoundAnUnboundCompanionAndTakesHimOutOfTheArcheryTotalForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
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
		scn.MoveMinionsToTable(commander, crossbow, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Three archers, each adding 1: before he uses his ability he adds like any archer.
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(crossbow));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));

		scn.ShadowUseCardAction(commander);
		// He exerts, and the other archer does not.
		assertEquals(1, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(crossbow));

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
		// He no longer adds; the Crossbowman and the Marksman still do.
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
	public void WithNoUnboundCompanionToWoundTheAbilityIsStillOfferedAndStillTakesHimOutOfTheArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var crossbow = scn.GetShadowCard("crossbow");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(maggot);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// The Ring-bearer and Sam are Ring-bound and Maggot is no companion: nobody is unbound.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertTrue(scn.ShadowActionAvailable(commander));

		scn.ShadowUseCardAction(commander);

		// The cost is paid and nobody is wounded, but he still drops out of the total.
		assertEquals(1, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var crossbow = scn.GetShadowCard("crossbow");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, crossbow);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(commander, 1);
		assertEquals(1, scn.GetWoundsOn(commander));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(1, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(crossbow));
		assertFalse(scn.ShadowActionAvailable(commander));
		assertTrue(scn.ShadowActionAvailable(crossbow));
	}
}
