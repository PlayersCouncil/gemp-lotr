package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_118_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sooner", "103_118");
					put("sam", "1_311");          // Sam, Son of Hamfast: Hobbit companion, strength 3, cost 2
					put("merry", "1_302");        // Merry, Friend to Sam: Hobbit companion, strength 3; a skirmish ability never used here
					put("pippin", "1_306");       // Pippin, Friend to Frodo: Hobbit companion; his text is about tales
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability never used here
					put("smeagol", "103_116");    // Smeagol, Once Like Us: a [shire] companion with no race; an assignment ability never used here
					put("bounder", "1_286");      // Bounder: a Hobbit ally; a skirmish ability never used here
					put("sword1", "1_299");       // Hobbit Sword: hand weapon, strength +2, no text
					put("sword2", "1_299");
					put("pipe", "103_113");       // Merry's Pipe: a possession that is no weapon; answers a pipeweed being discarded
					put("cloak", "1_42");         // Elven Cloak: a possession that is no weapon; the minion archery total is -1
					put("rangersword", "1_112");  // Ranger's Sword: hand weapon, strength +2; Aragorn is damage +1
					put("bow", "1_90");           // Aragorn's Bow: ranged weapon; he is an archer, and an archery ability
					put("armor", "1_92");         // Armor: a possession that is no weapon; no more than 1 wound in each skirmish
					put("leaf", "1_300");         // Longbottom Leaf: a Free Peoples possession in the support area; a when-played trigger only

					put("breath", "1_207");       // Black Breath: a Shadow condition a character bears; its bearer cannot be healed
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSoonerWereRidofItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Sooner We're Rid of It
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Hobbit companions.
		 * 	Skirmish: Exert a companion and hinder each card borne by them to make that companion strength +1
		 * 	for each card hindered (+3 if that card is a weapon).  Hinder this condition (or discard this if
		 * 	you can spot more than 4 companions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sooner");

		assertEquals("The Sooner We're Rid of It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForThreeToTheSupportAreaOnceTwoHobbitCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo is the one Hobbit companion. Bounder is a Hobbit and an ally; Smeagol is a [shire] companion
		// and no Hobbit; Aragorn is a companion.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sooner, sam);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(sooner));

		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sooner));

		scn.FreepsPlayCard(sooner);
		// 2 for Sam + 3
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sooner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsACompanionAndHindersEachCardHeBearsMakingHimStrongerThenHindersItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var pipe = scn.GetFreepsCard("pipe");
		var cloak = scn.GetFreepsCard("cloak");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(sooner, leaf);
		scn.AttachCardsTo(merry, sword1, pipe, cloak);
		scn.AttachCardsTo(sam, sword2);
		// Merry's cloak is hindered already.
		scn.HinderCard(cloak);
		// Pippin is exhausted, and Aragorn is hindered: 4 companions can be spotted, with an ally beside them.
		scn.AddWoundsToChar(pippin, 3);
		scn.HinderCard(aragorn);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, runner);

		// 3 + 2 for the Hobbit Sword
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.IsHindered(sword1));
		assertFalse(scn.IsHindered(pipe));
		assertTrue(scn.FreepsActionAvailable(sooner));

		scn.FreepsUseCardAction(sooner);
		// A companion who can exert, in the skirmish or not: no exhausted or hindered companion, ally or minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, aragorn, bounder, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.IsHindered(sword1));
		assertTrue(scn.IsHindered(pipe));
		assertTrue(scn.IsHindered(cloak));
		// What Sam bears, and what lies in the support area, is left alone.
		assertFalse(scn.IsHindered(sword2));
		assertFalse(scn.IsHindered(leaf));
		// 3, + 3 for the sword and + 1 for the pipe; nothing for the sword's own +2, which is hindered with
		// it, and nothing for the cloak, which was hindered before
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		// 3 + 2 for his own sword
		assertEquals(5, scn.GetStrength(sam));

		// With 4 companions to spot the condition is hindered, where it lies.
		assertTrue(scn.IsHindered(sooner));
		assertInZone(Zone.SUPPORT, sooner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void HandWeaponAndRangedWeaponCountThreeEachAndAnyOtherCardBorneCountsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(sooner);
		scn.AttachCardsTo(aragorn, rangersword, armor);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		var breath = scn.GetShadowCard("breath");
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(aragorn, breath);

		scn.SkipToAssignments();
		// The bow makes Aragorn an archer, so it is put on him once the archery phase is past.
		scn.AttachCardsTo(aragorn, bow);
		scn.FreepsAssignAndResolve(sam, runner);

		// 8 + 2 for the Ranger's Sword
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsUseCardAction(sooner);
		// Aragorn is not in the skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn));

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsHindered(rangersword));
		assertTrue(scn.IsHindered(bow));
		assertTrue(scn.IsHindered(armor));
		assertTrue(scn.IsHindered(breath));
		// 8, + 3 for the sword, + 3 for the bow, + 1 for the armor, + 1 for the Shadow player's Black Breath
		assertEquals(16, scn.GetStrength(aragorn));
		assertTrue(scn.IsHindered(sooner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOneRingIsACardTheRingBearerBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		var sword1 = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(sooner);
		scn.AttachCardsTo(frodo, sword1);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		// 3, + 1 for The One Ring, + 2 for the Hobbit Sword
		assertEquals(6, scn.GetStrength(frodo));
		assertFalse(scn.IsHindered(ring));

		scn.FreepsUseCardAction(sooner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsHindered(ring));
		assertTrue(scn.IsHindered(sword1));
		assertEquals(frodo, scn.GetRingBearer());
		// 3, + 1 for the Ring hindered and + 3 for the sword hindered; what each gave him of its own is gone
		assertEquals(7, scn.GetStrength(frodo));
		assertTrue(scn.IsHindered(sooner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsNothingExertsForNoStrengthAndTheConditionIsStillHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(sooner);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertEquals(3, scn.GetStrength(sam));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sooner));

		scn.FreepsUseCardAction(sooner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.IsHindered(sooner));
		assertInZone(Zone.SUPPORT, sooner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		var sword1 = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(sooner);
		scn.AttachCardsTo(sam, sword1);
		// Vitality 4 with 3 wounds, each of them
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertFalse(scn.FreepsActionAvailable(sooner));

		scn.RemoveWoundsFromChar(sam, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sooner));
		assertFalse(scn.IsHindered(sword1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsTheConditionIsDiscardedAfterGivingItsBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sooner = scn.GetFreepsCard("sooner");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword1 = scn.GetFreepsCard("sword1");
		var leaf = scn.GetFreepsCard("leaf");
		// Frodo, Sam, Merry, Pippin and Aragorn
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveCardsToSupportArea(sooner, leaf);
		scn.AttachCardsTo(merry, sword1);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, runner);

		assertEquals(5, scn.GetStrength(merry));
		assertInZone(Zone.SUPPORT, sooner);

		scn.FreepsUseCardAction(sooner);
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.IsHindered(sword1));
		// 3 + 3 for the sword
		assertEquals(6, scn.GetStrength(merry));
		assertInZone(Zone.DISCARD, sooner);
		assertInZone(Zone.SUPPORT, leaf);
		assertFalse(scn.IsHindered(leaf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BonusLastsForTheSkirmishItIsGivenInWhileTheCardsStayHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sooner = scn.GetFreepsCard("sooner");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword1 = scn.GetFreepsCard("sword1");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToSupportArea(sooner);
		scn.AttachCardsTo(merry, sword1);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{merry, runner});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish: the condition is used for Merry, who is waiting for his own.
		assertEquals(5, scn.GetStrength(merry));
		scn.FreepsUseCardAction(sooner);
		scn.FreepsChooseCard(merry);
		// 3 + 3 for the sword
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn 8 against 5: the Savage takes a wound and lives.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(merry);

		// Merry's own skirmish: the bonus has gone, and his sword is still hindered.
		assertTrue(scn.IsCharSkirmishing(merry));
		assertTrue(scn.IsHindered(sword1));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.IsHindered(sooner));
		assertFalse(scn.FreepsActionAvailable(sooner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
