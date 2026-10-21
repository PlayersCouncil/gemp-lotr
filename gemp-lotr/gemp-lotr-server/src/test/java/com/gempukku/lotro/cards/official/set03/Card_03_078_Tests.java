package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hide", "3_78");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text, no damage bonus
					put("troll", "1_165");        // Cave Troll of Moria: [moria] Troll, printed damage +1 and fierce; its other texts are about playing it
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc; a skirmish ability used only on purpose
					put("relics", "1_195");       // Relics of Moria: [moria] support-area condition; a Shadow ability used only on purpose

					put("merry", "1_303");        // Merry, From O'er the Brandywine: [shire] Hobbit companion; +2 only while he bears a weapon
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability only
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");      // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HideandSeekStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Hide and Seek
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Search</b>.<br><b>Skirmish:</b> Spot 2 [shire] companions to make a [moria] minion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var hide = scn.GetFreepsCard("hide");

		assertEquals("Hide and Seek", hide.getBlueprint().getTitle());
		assertNull(hide.getBlueprint().getSubtitle());
		assertFalse(hide.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hide.getBlueprint().getSide());
		assertEquals(Culture.MORIA, hide.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hide.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(hide, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(hide, Timeword.SKIRMISH));
		assertEquals(1, hide.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithOnlyOneShireCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hide = scn.GetShadowCard("hide");
		var troop = scn.GetShadowCard("troop");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Frodo is the one [shire] companion; beside him a Hobbit companion of another culture and a [shire] Hobbit
		// who is an ally.
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(hide);
		assertInHand(hide);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(hide));
	}

	@Test
	public void MakesAMoriaMinionDamagePlusOneUntilItsSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hide = scn.GetShadowCard("hide");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var relics = scn.GetShadowCard("relics");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		// Frodo and Merry: exactly two [shire] companions.
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(hide);
		assertInHand(hide);

		scn.StartGame();
		scn.MoveCardsToSupportArea(relics);
		scn.MoveMinionsToTable(troop, troll, soldier);
		assertInZone(Zone.SUPPORT, relics);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{merry, soldier});
		// The Troll is left over, and the Shadow player leaves him unassigned.
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(troll));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(hide));

		scn.ShadowPlayCard(hide);
		// A [moria] minion in the skirmish and one in none, of two races; the [sauron] Orc in the other skirmish, the
		// [moria] condition and the companions are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, relics, aragorn, merry, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		// The [moria] minion who was not chosen keeps his printed damage +1 alone.
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertInDiscard(hide);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish ends; in the next one the Troop has no damage bonus again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SpottingFrodoAndSmeagolItIsPlayedAndWithNoMoriaMinionDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hide = scn.GetShadowCard("hide");
		var soldier = scn.GetShadowCard("soldier");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Frodo and a [shire] companion who is no Hobbit.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(hide);
		assertInHand(hide);

		scn.StartGame();
		// The only minion is of another culture.
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		// An event may be played though its target is missing.
		assertTrue(scn.ShadowPlayAvailable(hide));

		scn.ShadowPlayCard(hide);

		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertInDiscard(hide);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
