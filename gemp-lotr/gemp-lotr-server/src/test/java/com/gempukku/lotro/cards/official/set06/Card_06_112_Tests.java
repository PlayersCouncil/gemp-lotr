package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wrath", "6_112");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unbound [shire] Hobbit, strength 3
					// Pippin, Trollslayer: an unbound Hobbit of [gondor]; enduring, so he is never the one exerted
					// where a strength is read
					put("pippin", "103_31");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit who is Ring-bound
					put("bounder", "1_286");    // Bounder: a Hobbit ally, and only a companion is unbound
					// Gandalf, Friend of the Shirefolk: a [gandalf] companion, and an unbound one who is no Hobbit
					put("gandalf", "1_72");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally, no companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of another culture
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; "When you play" text only
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongSlowWrathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Long Slow Wrath
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert an unbound Hobbit to make him strength +1 and <b>damage +1</b> for each [gandalf] companion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("Long Slow Wrath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUnboundHobbitToMakeHimStrengthPlus2AndDamagePlus2ForTwoGandalfCompanionsUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, pippin, sam, gandalf, avenger);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{sam, runner2});
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsPlayAvailable(wrath));
		scn.FreepsPlayCard(wrath);

		// Merry in the skirmish and Pippin, who is in none: either may exert. Not the Ring-bound Frodo or Sam, the
		// Bounder (an ally) or Gandalf (no Hobbit).
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, bounder, gandalf, avenger));
		scn.FreepsChooseCard(merry);

		// Gandalf and the Ent Avenger: two [gandalf] companions, so +2 and damage +2, on Merry alone.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(2, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertInDiscard(wrath);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(2, scn.GetKeywordCount(merry, Keyword.DAMAGE));

		// Merry and the Runner both at 5: the tie goes to the Shadow side, and Merry takes a wound and lives.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(merry));

		// Sam's skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
	}

	@Test
	public void WithOneGandalfCompanionBesideAGandalfAllyAndAGondorCompanionTheBonusIsPlus1AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, gandalf, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		scn.FreepsPlayCard(wrath);

		// Merry is the only unbound Hobbit, and exerts unasked. Only Gandalf is counted.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(1, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionItIsStillPlayedAndTheExertionBuysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(wrath);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsPlayAvailable(wrath));
		scn.FreepsPlayCard(wrath);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertInDiscard(wrath);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyUnboundHobbitIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, sam, gandalf);
		scn.MoveCardsToHand(wrath);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Merry cannot exert. Sam and Gandalf could, and do not count.
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wrath));
	}
}
