package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "19_12");
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion, cost 2; abilities used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: an unbound [dwarven] Dwarf companion; a skirmish ability never used
					put("barliman", "1_70");      // Barliman Butterbur: a [gandalf] ally; a fellowship ability never used

					put("olog", "11_108");        // Beastly Olog-hai: fierce, damage +1, strength 13, vitality 3; an assignment ability never used
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
					put("marksman", "1_176");     // Goblin Marksman: strength 7, vitality 1; archer is its whole text
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Aragorn, Well-traveled Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: Fellowship. Ranger.
		 * 	While the Ring-bearer is the only other companion you can spot, Aragorn is an archer, defender +1, and can take no more than 1 wound each skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Well-traveled Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void BesideTheRingBearerAloneAragornIsAnArcherAndDefenderUntilAnotherCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		// An ally is no companion.
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, barliman);
		assertInZone(Zone.HAND, sam);
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		// The Ring-bearer gains nothing.
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));

		scn.FreepsPlayCard(sam);

		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BesideTheRingBearerAloneAragornTakesNoMoreThanOneWoundInEachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var olog = scn.GetShadowCard("olog");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(olog);

		// A minion in play is no companion either.
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(0, scn.GetWoundsOn(olog));

		scn.SkipToAssignments();
		// Aragorn's archery wound, the only minion's to take.
		assertEquals(1, scn.GetWoundsOn(olog));

		scn.FreepsAssignToMinions(aragorn, olog);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// Beastly Olog-hai (13) beats Aragorn (8): one wound and one for damage +1, held to one.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Olog-hai is fierce, and beats him again: the cap is one in each skirmish.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, olog);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheCapOnWoundsIsAragornsAloneAndTheRingBearerTakesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var olog = scn.GetShadowCard("olog");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(olog, savage);

		scn.SkipToArcheryWounds();
		// Aragorn's own arrow: the Shadow player picks the minion.
		scn.ShadowChooseCard(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, olog }, new PhysicalCardImpl[] { frodo, savage });
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassSkirmishActions();

		// Beastly Olog-hai (13) beats Aragorn (8): one wound and one for damage +1, held to one.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		// The Ruling Ring offers to turn each wound to the Ring-bearer into a burden; declined both times.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDeclineOptionalTrigger();

		// Uruk Savage (5) beats the Ring-bearer (3): one wound and one for damage +1, both taken.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The Olog-hai is fierce.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAnotherCompanionToSpotAragornIsNoArcherAndTakesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var olog = scn.GetShadowCard("olog");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(olog);

		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.SkipToAssignments();
		// No archer in the fellowship.
		assertEquals(0, scn.GetWoundsOn(olog));

		scn.FreepsAssignToMinions(aragorn, olog);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// Beastly Olog-hai (13) beats Aragorn (8): one wound and one for damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OutsideASkirmishAragornTakesMoreThanOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(marksman, marksman2);

		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.SkipToArcheryWounds();
		// Two Goblin Marksmen: a minion archery total of 2, both placed on Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		// Aragorn's own arrow.
		scn.ShadowChooseCard(marksman);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(marksman);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
