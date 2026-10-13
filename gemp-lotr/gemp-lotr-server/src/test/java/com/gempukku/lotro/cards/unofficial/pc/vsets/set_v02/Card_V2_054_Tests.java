package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "102_54");

					put("theoden", "4_292");    // Theoden, Son of Thengel: [rohan] Man, cost 2; a regroup ability
					put("theodred", "13_138");  // Theodred, Second Marshal of the Mark: a [rohan] Man who is not Theoden
					put("elite", "4_265");      // Elite Rider: [rohan] Man; stronger against a wounded minion
					// Guma, Plains Farmer: [rohan] Man who is an ally (home 4T); a fellowship ability, never used
					put("guma", "4_277");
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man who is an ally, cost 1; to play, spot 2 [rohan] Men
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8
					// Merry, Master Holbytla: a [rohan] Hobbit companion.  He is a Man as well only while 3 [rohan]
					// companions (or Eowyn) can be spotted; his trigger answers another [rohan] Man exerting, and
					// his skirmish ability is never used.
					put("holbytla", "103_85");
					put("spear", "4_288");      // Rider's Spear: hand weapon for a [rohan] Man, strength +2

					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength 3; stronger only with a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: strength 3; his text is about [shire] tales
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3; a fellowship ability
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2, strength 2; a skirmish ability

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RoyalGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Royal Guard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 3T
		 * Game Text: To play, spot Theoden (or 3 [rohan] Men).
		 * 	Skirmish: Exert this ally to add his strength to a skirmishing Hobbit and make that Hobbit gain <b>valiant</b> until the regroup phase (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Royal Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
	}

	@Test
	public void CannotBePlayedUntilTheodenIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		var aragorn = scn.GetFreepsCard("aragorn");
		// One [rohan] Man with a name much like the king's, and a Man of another culture.
		scn.MoveCompanionsToTable(theodred, aragorn);
		scn.MoveCardsToHand(guard, theoden);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));

		scn.FreepsPlayCard(theoden);

		// Theoden and Theodred are only two [rohan] Men: it is Theoden who is spotted.
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertEquals(2, scn.GetTwilight());

		scn.FreepsPlayCard(guard);

		assertInZone(Zone.SUPPORT, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithThreeRohanMenInsteadOfTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var elite = scn.GetFreepsCard("elite");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var holbytla = scn.GetFreepsCard("holbytla");
		// Merry, Master Holbytla is the one [rohan] character in print who is no Man: he and the Elite Rider are
		// only two [rohan] companions, so he is a Hobbit and nothing else.  The second [rohan] Man is an ally.
		scn.MoveCompanionsToTable(elite, holbytla, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(guard, herdsman);

		scn.StartGame();

		// Two [rohan] Men, a [rohan] Hobbit and a [gondor] Man.
		assertTrue(scn.IsRace(holbytla, Race.HOBBIT));
		assertFalse(scn.IsRace(holbytla, Race.MAN));
		assertFalse(scn.FreepsPlayAvailable(guard));

		// The third [rohan] Man is an ally too, so Merry is still no Man.
		scn.FreepsPlayCard(herdsman);

		assertFalse(scn.IsRace(holbytla, Race.MAN));
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(guard);

		assertInZone(Zone.SUPPORT, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsOnceInEachSkirmishToAddHisStrengthToTheSkirmishingHobbitAndMakeThatHobbitValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(merry, pippin, sam);
		// Another [rohan] ally who could exert (vitality 2) stands beside the Guard: "this ally" is the Guard alone.
		scn.MoveAlliesToTable(guard, herdsman);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{pippin, runner2},
				new PhysicalCardImpl[]{sam, runner3});

		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(3, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(merry));
		assertFalse(scn.HasKeyword(merry, Keyword.VALIANT));
		assertTrue(scn.FreepsActionAvailable(guard));

		scn.FreepsUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		// The Hobbits whose skirmishes have not begun are not skirmishing: nothing was asked and nothing changed.
		assertTrue(scn.IsCharAssigned(pippin));
		assertEquals(3, scn.GetStrength(pippin));
		assertFalse(scn.HasKeyword(pippin, Keyword.VALIANT));
		assertEquals(3, scn.GetStrength(sam));
		assertFalse(scn.HasKeyword(sam, Keyword.VALIANT));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Once per phase: with one wound on a vitality of 3 the Guard could exert again.
		scn.ShadowPass();
		assertFalse(scn.IsExhausted(guard));
		assertFalse(scn.FreepsActionAvailable(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Merry at 6 beats the Goblin Runner's 5.
		scn.FreepsPass();
		assertInDiscard(runner1);
		assertEquals(0, scn.GetWoundsOn(merry));

		// The next skirmish is another phase: the limit is new, and what was added to Merry lasted for his skirmish.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(guard));

		scn.FreepsUseCardAction(guard);

		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(pippin));
		assertTrue(scn.HasKeyword(pippin, Keyword.VALIANT));
		assertEquals(3, scn.GetStrength(sam));
		assertFalse(scn.HasKeyword(sam, Keyword.VALIANT));

		scn.PassSkirmishActions();
		assertInDiscard(runner2);

		// A third skirmish, with a Hobbit in it: the Guard is exhausted and cannot exert, and the other ally,
		// who could, does not pay for him.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertFalse(scn.FreepsActionAvailable(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheHobbitStaysValiantThroughLaterSkirmishesAndUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveAlliesToTable(guard);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{pippin, runner2});

		scn.FreepsResolveSkirmish(merry);
		assertFalse(scn.HasKeyword(merry, Keyword.VALIANT));
		scn.FreepsUseCardAction(guard);
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		scn.PassSkirmishActions();

		// During the skirmish after his own, and when it is over.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishing(pippin));
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(pippin, Keyword.VALIANT));
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(merry, Keyword.VALIANT));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(merry, Keyword.VALIANT));
	}

	@Test
	public void AddsHisStrengthAsItIsWhenUsedAndTheAmountStaysWhenHisStrengthChangesLater() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveAlliesToTable(guard);
		scn.AttachCardsTo(guard, spear);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner1);

		// 3 + 2 for the Rider's Spear
		assertEquals(5, scn.GetStrength(guard));
		// 3 + 1 for The One Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(9, scn.GetStrength(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.VALIANT));

		// The Guard loses his spear: what was added to Frodo is not worked out again.
		scn.MoveCardsToDiscard(spear);
		assertEquals(3, scn.GetStrength(guard));
		assertEquals(9, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AHobbitAllySkirmishingAtHisHomeSiteIsASkirmishingHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(guard, bounder);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		// The fellowship moves to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{bounder, runner1},
				new PhysicalCardImpl[]{merry, runner2});
		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.IsCharSkirmishing(bounder));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetStrength(bounder));
		assertFalse(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertTrue(scn.FreepsActionAvailable(guard));

		scn.FreepsUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(5, scn.GetStrength(bounder));
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertEquals(3, scn.GetStrength(merry));
		assertFalse(scn.HasKeyword(merry, Keyword.VALIANT));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoHobbitSkirmishingTheGuardMayStillExertAndNobodyIsStrongerOrValiant() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(guard);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{merry, runner2});
		// A Man is skirmishing; the Hobbit's skirmish has not begun.
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.IsCharAssigned(merry));
		assertEquals(0, scn.GetWoundsOn(guard));
		// The cost can be paid, so the action is there to be used.
		assertTrue(scn.FreepsActionAvailable(guard));

		scn.FreepsUseCardAction(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertEquals(3, scn.GetStrength(merry));
		assertFalse(scn.HasKeyword(merry, Keyword.VALIANT));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
