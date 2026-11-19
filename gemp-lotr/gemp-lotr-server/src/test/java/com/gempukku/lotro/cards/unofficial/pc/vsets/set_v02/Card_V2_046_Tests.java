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

public class Card_V2_046_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("end", "102_46");

					// [rohan] conditions that hold [rohan] tokens.  Each has a skirmish ability that spends
					// its tokens, and none is ever used.
					put("none", "6_97");         // We Left None Alive: unique
					put("doom1", "8_86");        // Doom Drove Them: not unique
					put("doom2", "8_86");
					put("cast", "18_94");        // Cast from the Hall: its ability takes 3 tokens

					// A Path Appointed: Doom Drove Them's [gondor] twin, a condition that holds [gondor] tokens.
					put("path", "8_42");
					// Heavy Chain: a [rohan] possession, an armour that changes no number.  No card puts
					// tokens on it; one test puts two there to have a [rohan] card with tokens that is not
					// a condition.
					put("chain", "4_278");

					// Valiant companions: two [rohan] Men and two Elves.
					put("guard1", "5_83");       // Household Guard: strength 6; "Valiant" is his whole text
					put("guard2", "5_83");
					put("defender", "68_9");     // Elven Defender: strength 5; stronger beside an [elven] token
					put("guardian", "54_76");    // Lorien Guardian: strength 5; a regroup ability
					// Eomer, Third Marshal of Riddermark: a [rohan] companion who is not valiant, strength 7.
					// He is stronger for each wound on a minion he is skirmishing; no minion here is wounded.
					put("eomer", "4_267");
					// Bounder: a [shire] Hobbit ally whose home is site 2, where every test here is fought.
					// Strength 2, vitality 2; his own skirmish ability is never used.
					put("bounder", "1_286");
					// Royal Guard: a [rohan] ally, strength 3, vitality 3.  "Skirmish: Exert this ally to add his
					// strength to a skirmishing Hobbit and make that Hobbit gain valiant until the regroup phase
					// (limit once per phase)."  No ally is valiant in print; this is how one comes to be.
					put("royalguard", "102_54");

					// Goblin Runner: strength 5, vitality 1.
					put("runner1", "1_178");
					put("runner2", "1_178");
					// The Mouth of Sauron, Bearer of Dread News: a lurker, strength 9, vitality 3.  "Response: If
					// a Free Peoples event is played, exert this minion to cancel that event."  Other minions take
					// no more than 1 wound a phase beside him; none here takes more.
					put("mouth", "103_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnEndWorthyofRemembranceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: An End Worthy of Remembrance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot X [rohan] conditions and remove 2 tokens from each to make 3 valiant companions
		 * strength +X until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("end");

		assertEquals("An End Worthy of Remembrance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesTwoTokensFromEachOfTwoRohanConditionsToMakeThreeValiantCompanionsStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var end = scn.GetFreepsCard("end");
		var none = scn.GetFreepsCard("none");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var cast = scn.GetFreepsCard("cast");
		var path = scn.GetFreepsCard("path");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		var guardian = scn.GetFreepsCard("guardian");
		var eomer = scn.GetFreepsCard("eomer");
		var bounder = scn.GetFreepsCard("bounder");
		var royalguard = scn.GetFreepsCard("royalguard");
		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(none, doom1, doom2, cast, path);
		scn.MoveCompanionsToTable(guard1, guard2, defender, guardian, eomer);
		scn.MoveAlliesToTable(bounder, royalguard);
		// [rohan] conditions with exactly two tokens, with three, and with twice two; one with a single
		// token; and a [gondor] condition with two.
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(cast, 3);
		scn.AddTokensToCard(none, 4);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(path, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		// Site 2 is the Bounder's home, so he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { bounder, runner2 },
				new PhysicalCardImpl[] { guard1, runner });

		// No ally is valiant in print.  In the Bounder's own skirmish the Royal Guard makes him valiant until
		// the regroup phase: valiant as the four are, and no companion.
		scn.FreepsResolveSkirmish(bounder);
		assertFalse(scn.HasKeyword(bounder, Keyword.VALIANT));
		scn.FreepsUseCardAction(royalguard);
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		// The Bounder's 2 + 3 does not beat the Runner's 5: one wound of his two.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.FreepsResolveSkirmish(guard1);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 6 companions + 2 for the site.
		assertEquals(8, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(5, scn.GetStrength(guardian));
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.HasKeyword(bounder, Keyword.VALIANT));
		assertEquals(2, scn.GetStrength(bounder));
		var frodoStrength = scn.GetStrength(frodo);
		assertTrue(scn.FreepsPlayAvailable(end));

		scn.FreepsPlayCard(end);
		// X is the number of conditions chosen: from one to the three that have two tokens to give.  The
		// condition with four is one condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, cast, none));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(doom2, path));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(doom1, none);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(none));
		assertEquals(3, scn.GetCultureTokensOn(cast));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(path));

		// Three of the four valiant companions, of either culture and in the skirmish or out of it.  Not a
		// [rohan] companion who is not valiant, nor the Ring-bearer, nor a valiant ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, defender, guardian));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eomer, frodo, bounder));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCards(guard1, defender, guardian);
		assertEquals(6 + 2, scn.GetStrength(guard1));
		assertEquals(5 + 2, scn.GetStrength(defender));
		assertEquals(5 + 2, scn.GetStrength(guardian));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, end);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void RemovingTokensFromOneConditionMakesThemStrengthPlusOneThroughALaterSkirmishUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var doom1 = scn.GetFreepsCard("doom1");
		var cast = scn.GetFreepsCard("cast");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		var guardian = scn.GetFreepsCard("guardian");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(doom1, cast);
		scn.MoveCompanionsToTable(guard1, guard2, defender, guardian);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(cast, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, runner1}, new PhysicalCardImpl[]{defender, runner2});
		scn.FreepsResolveSkirmish(guard1);

		assertTrue(scn.FreepsPlayAvailable(end));
		scn.FreepsPlayCard(end);
		// Two conditions could pay, and the player need not spot both.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, cast));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(cast);
		assertEquals(3 - 2, scn.GetCultureTokensOn(cast));
		assertEquals(2, scn.GetCultureTokensOn(doom1));

		// One in this skirmish, one in the skirmish to come, and one in neither.
		scn.FreepsChooseCards(guard1, defender, guard2);
		assertEquals(6 + 1, scn.GetStrength(guard1));
		assertEquals(5 + 1, scn.GetStrength(defender));
		assertEquals(6 + 1, scn.GetStrength(guard2));
		assertEquals(5, scn.GetStrength(guardian));

		// The Guard at 7 beats his Runner at 5.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner1);

		// In the second skirmish all three are as they were made.
		scn.FreepsResolveSkirmish(defender);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6 + 1, scn.GetStrength(guard1));
		assertEquals(5 + 1, scn.GetStrength(defender));
		assertEquals(6 + 1, scn.GetStrength(guard2));
		assertEquals(5, scn.GetStrength(guardian));

		// The Defender at 6 beats the other Runner at 5, which at 5 would have been a loss.
		scn.FreepsPass();
		scn.ShadowPass();
		assertInZone(Zone.DISCARD, runner2);
		assertEquals(0, scn.GetWoundsOn(defender));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(5, scn.GetStrength(guardian));
	}

	@Test
	public void IsNotOfferedWhenNoRohanConditionHasTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var doom2 = scn.GetFreepsCard("doom2");
		var cast = scn.GetFreepsCard("cast");
		var path = scn.GetFreepsCard("path");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(doom2, cast, path);
		scn.MoveCompanionsToTable(guard1, guard2, defender);
		// One [rohan] condition with a single token and one with none; a [gondor] condition with two.
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(path, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard1, runner);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(0, scn.GetCultureTokensOn(cast));
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertInZone(Zone.HAND, end);
		assertFalse(scn.FreepsPlayAvailable(end));
	}

	@Test
	public void WithOnlyTwoValiantCompanionsBothAreMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var doom1 = scn.GetFreepsCard("doom1");
		var guard1 = scn.GetFreepsCard("guard1");
		var defender = scn.GetFreepsCard("defender");
		var eomer = scn.GetFreepsCard("eomer");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCompanionsToTable(guard1, defender, eomer);
		scn.AddTokensToCard(doom1, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard1, runner);

		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(5, scn.GetStrength(defender));
		assertTrue(scn.FreepsPlayAvailable(end));

		// One condition can pay and two companions are valiant: there is nothing to choose.
		scn.FreepsPlayCard(end);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(6 + 1, scn.GetStrength(guard1));
		assertEquals(5 + 1, scn.GetStrength(defender));
		assertEquals(7, scn.GetStrength(eomer));
		assertInZone(Zone.DISCARD, end);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedAndPaidForWithNoValiantCompanionToMakeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var end = scn.GetFreepsCard("end");
		var doom1 = scn.GetFreepsCard("doom1");
		var eomer = scn.GetFreepsCard("eomer");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCompanionsToTable(eomer);
		scn.AddTokensToCard(doom1, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eomer, runner);

		// 2 companions + 2 for the site.
		assertEquals(4, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(eomer));
		var frodoStrength = scn.GetStrength(frodo);
		assertTrue(scn.FreepsPlayAvailable(end));

		scn.FreepsPlayCard(end);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, end);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanPossessionBearingTokensIsNotOneOfTheConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var doom1 = scn.GetFreepsCard("doom1");
		var chain = scn.GetFreepsCard("chain");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCompanionsToTable(guard1, guard2, defender);
		scn.AttachCardsTo(guard2, chain);
		scn.AddTokensToCard(doom1, 2);
		// No card there is puts tokens on a possession like this one: they are put here to have a [rohan]
		// card with two tokens that is not a condition.
		scn.AddTokensToCard(chain, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard1, runner);

		assertEquals(2, scn.GetCultureTokensOn(chain));
		assertTrue(scn.FreepsPlayAvailable(end));

		// One condition can pay and three companions are valiant: there is nothing to choose.
		scn.FreepsPlayCard(end);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(chain));
		assertEquals(6 + 1, scn.GetStrength(guard1));
		assertEquals(6 + 1, scn.GetStrength(guard2));
		assertEquals(5 + 1, scn.GetStrength(defender));
		assertInZone(Zone.DISCARD, end);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheTokensAreRemovedBeforeTheShadowPlayerMayCancelTheEventAndStayRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var end = scn.GetFreepsCard("end");
		var doom1 = scn.GetFreepsCard("doom1");
		var cast = scn.GetFreepsCard("cast");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var defender = scn.GetFreepsCard("defender");
		var runner = scn.GetShadowCard("runner1");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(end);
		scn.MoveCardsToSupportArea(doom1, cast);
		scn.MoveCompanionsToTable(guard1, guard2, defender);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(cast, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard1);

		var pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertTrue(scn.FreepsPlayAvailable(end));
		scn.FreepsPlayCard(end);
		scn.FreepsChooseCards(doom1, cast);

		// The tokens stand before the "to": they are gone by the time the Shadow player may answer the event.
		assertEquals(2 - 2, scn.GetCultureTokensOn(doom1));
		assertEquals(3 - 2, scn.GetCultureTokensOn(cast));
		assertTrue(scn.ShadowActionAvailable(mouth));
		scn.ShadowUseCardAction(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));

		// Cancelled: nobody is made stronger, and what was paid stays paid.
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(cast));
		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, end);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
