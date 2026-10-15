package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_315_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stout", "1_315");
					// Pippin, Trollslayer: a Hobbit companion who is [gondor]; enduring (his strength is not read here),
					// a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Bounder: [shire] Hobbit ally, vitality 2, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; his assignment ability is used only on purpose
					put("smeagol", "103_116");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, vitality 4; his text needs a weapon
					put("merry", "1_303");
					// Ted Sandyman, Chief's Men's Ally: the Shadow player's Hobbit, a [men] minion, vitality 4; his
					// maneuver ability needs a site he controls, his regroup ability is used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire] Hobbit, vitality 4
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoutandSturdyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Stout and Sturdy
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver or Skirmish
		 * Game Text: <b>Maneuver</b> <i>or</i> <b>Skirmish:</b> Heal a Hobbit.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stout");

		assertEquals("Stout and Sturdy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheManeuverPhaseHealsOneWoundFromAHobbitOfAnyKindOrSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stout = scn.GetFreepsCard("stout");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(stout);

		scn.StartGame();

		scn.MoveMinionsToTable(ted);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(ted, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(ted));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(stout));

		scn.FreepsPlayCard(stout);
		// Hobbit companions of two cultures, a Hobbit ally and the Shadow player's Hobbit minion; not a [shire]
		// companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);

		// One wound of his two, and nobody else's.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertInZone(Zone.DISCARD, stout);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InASkirmishItMayHealAHobbitWhoIsInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stout = scn.GetFreepsCard("stout");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(stout);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(merry, 1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop));
		assertTrue(scn.FreepsPlayAvailable(stout));
		scn.FreepsPlayCard(stout);
		// The Hobbit in the skirmish and the Ring-bearer, who is in none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.DISCARD, stout);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoHobbitWoundedItIsStillPlayedAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stout = scn.GetFreepsCard("stout");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(stout);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(smeagol, 1);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.MANEUVER);
		// The Hobbit is the effect's target, no requirement.
		assertTrue(scn.FreepsPlayAvailable(stout));
		scn.FreepsPlayCard(stout);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, stout);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryOrAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stout = scn.GetFreepsCard("stout");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(stout);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInHand(stout);
		assertFalse(scn.FreepsPlayAvailable(stout));

		// The phase next to the skirmish phase, too.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(stout);
		assertFalse(scn.FreepsPlayAvailable(stout));
	}
}
