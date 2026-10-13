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

public class Card_V3_107_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forfrodo1", "103_107");
					put("forfrodo2", "103_107");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: unique [gondor] companion, strength 8, vitality 4
					put("sam", "1_311");            // Sam, Son of Hamfast: unique [shire] companion, Ring-bound, strength 3, vitality 4
					put("merry", "1_302");          // Merry, Friend to Sam: unique [shire] companion
					put("pippin", "1_306");         // Pippin, Friend to Frodo: unique [shire] companion
					put("gimli", "1_13");           // Gimli, Son of Gloin: unique [dwarven] companion
					// Smeagol, Once Like Us: unique [shire] companion with no race; Ring-bound, strength 3, vitality 4. His
					// assignment ability needs a fellowship of [shire] companions only.
					put("smeagol", "103_116");
					// Merry, Master Holbytla: unique [rohan] companion, a Hobbit, and a Man as well while Eowyn can be
					// spotted. His trigger answers another [rohan] Man exerting, and nobody of [rohan] exerts here.
					put("holbytla", "103_85");
					// Pippin, Trollslayer: unique [gondor] companion, a Hobbit, strength 3; a skirmish ability never used
					// here. Never in play beside the other Pippin.
					put("trollslayer", "103_31");
					put("eowyn", "7_229");          // Eowyn, Restless Maiden: unique [rohan] companion who is a Man, as Aragorn is
					put("knight", "5_35");          // Gondorian Knight: [gondor] companion who is not unique; Knight is its whole text
					put("businessman", "13_157");   // Westfarthing Businessman: [shire] companion who is not unique
					put("gaffer", "1_291");         // The Gaffer, Sam's Father: unique [shire] ally
					put("merchant", "7_97");        // Gondorian Merchant: unique [gondor] ally

					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3, damage +1
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table whose Ring-bearer is not Frodo: the burdens are Isildur's.
	protected VirtualTableScenario GetIsildurScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forfrodo", "103_107");
					put("frodo", "13_149");         // Frodo, Frenzied Fighter: the one Frodo with no Ring-bearer keyword
					put("merry", "1_302");          // Merry, Friend to Sam: unique [shire] companion, strength 3

					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				"9_33",                             // Isildur, Bearer of Heirlooms: unique [gondor] companion, strength 7, vitality 3
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForFrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: For Frodo
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a unique [gondor] or [shire] companion (except Frodo) to make that companion strength +1
		 * 	for each burden on Frodo (limit +3 unless you cannot spot more than 4 companions).  Remove this from
		 * 	the game.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forfrodo1");

		assertEquals("For Frodo", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAUniqueGondorOrShireCompanionButFrodoToMakeHimStrongerByFrodosBurdensAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var holbytla = scn.GetFreepsCard("holbytla");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var eowyn = scn.GetFreepsCard("eowyn");
		var knight = scn.GetFreepsCard("knight");
		var businessman = scn.GetFreepsCard("businessman");
		var gaffer = scn.GetFreepsCard("gaffer");
		var merchant = scn.GetFreepsCard("merchant");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, sam, smeagol, trollslayer, holbytla, eowyn, knight, businessman);
		scn.MoveAlliesToTable(gaffer, merchant);
		scn.MoveCardsToHand(forfrodo);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(savage);

		// The text goes by culture, not by race. Smeagol is a [shire] companion who is no Hobbit, and Pippin,
		// Trollslayer a [gondor] companion who is no Man. Merry, Master Holbytla is a Hobbit of neither
		// culture, and Eowyn a Man who is not a [gondor] card; beside her Merry is a Man as well.
		assertFalse(scn.IsRace(smeagol, Race.HOBBIT));
		assertFalse(scn.IsRace(trollslayer, Race.MAN));
		assertTrue(scn.IsRace(holbytla, Race.HOBBIT));
		assertTrue(scn.IsRace(holbytla, Race.MAN));

		// A Skirmish event: not playable in the assignment phase, with everything it needs on the table.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(forfrodo));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, savage);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(forfrodo));

		int discard = scn.GetFreepsDiscardCount();
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(trollslayer));

		scn.FreepsPlayCard(forfrodo);
		// In the skirmish or out of it: Aragorn is fighting, Sam, Smeagol and Pippin are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam, smeagol, trollslayer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, holbytla, eowyn, knight, businessman, gaffer, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		// 3 + 2 burdens
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(3, scn.GetStrength(trollslayer));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.REMOVED, forfrodo);
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoBurdenOnFrodoItCanStillBePlayedAndGivesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(forfrodo);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(forfrodo));

		scn.FreepsPlayCard(forfrodo);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetStrength(sam));
		assertInZone(Zone.REMOVED, forfrodo);
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAUniqueGondorOrShireCompanionOtherThanFrodoIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var businessman = scn.GetFreepsCard("businessman");
		var gaffer = scn.GetFreepsCard("gaffer");
		var merchant = scn.GetFreepsCard("merchant");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, sam, gimli, knight, businessman);
		scn.MoveAlliesToTable(gaffer, merchant);
		scn.MoveCardsToHand(forfrodo);
		// Both are vitality 4: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		// Frodo, a unique companion of another culture, a [gondor] and a [shire] companion who are not unique and
		// a unique ally of each culture could all exert.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(businessman));
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.IsExhausted(aragorn));
		assertTrue(scn.IsExhausted(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(forfrodo));

		scn.RemoveWoundsFromChar(sam, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(forfrodo));

		// Sam is the only companion who can pay, so nothing is asked.
		assertEquals(3, scn.GetStrength(sam));
		scn.FreepsPlayCard(forfrodo);
		assertEquals(3, scn.GetWoundsOn(sam));
		// 3 + 2 burdens
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(businessman));
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsUntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(forfrodo);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage },
				new PhysicalCardImpl[] { sam, runner }
		);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(forfrodo);
		scn.FreepsChooseCard(aragorn);
		// 8 + 1 burden
		assertEquals(9, scn.GetStrength(aragorn));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(aragorn));

		// 9 against the Savage's 5, not double: Aragorn wins with no wound but his exertion, and the Savage lives.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsResolveSkirmish(sam);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsTheBonusIsCappedAtThreeAndASecondCopyHasACapOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo1 = scn.GetFreepsCard("forfrodo1");
		var forfrodo2 = scn.GetFreepsCard("forfrodo2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var savage = scn.GetShadowCard("savage");
		// Frodo and these four: exactly 5 companions.
		scn.MoveCompanionsToTable(aragorn, sam, merry, pippin);
		scn.MoveCardsToHand(forfrodo1, forfrodo2);

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertEquals(4, scn.GetBurdens());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(forfrodo1);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// 8 + 3: four burdens, capped at +3
		assertEquals(11, scn.GetStrength(aragorn));
		assertInZone(Zone.REMOVED, forfrodo1);

		scn.ShadowPass();
		scn.FreepsPlayCard(forfrodo2);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// 8 + 3 + 3: a limit does not apply to another copy of the card
		assertEquals(14, scn.GetStrength(aragorn));
		assertInZone(Zone.REMOVED, forfrodo2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMoreThanFourCompanionsToSpotThereIsNoLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var gaffer = scn.GetFreepsCard("gaffer");
		var savage = scn.GetShadowCard("savage");
		// Frodo, Aragorn, Sam and Merry are 4 companions. Beside them are an ally and a fifth companion who is
		// hindered and so cannot be spotted.
		scn.MoveCompanionsToTable(aragorn, sam, merry, pippin);
		scn.MoveAlliesToTable(gaffer);
		scn.MoveCardsToHand(forfrodo);

		scn.StartGame();
		scn.AddBurdens(4);
		scn.HinderCard(pippin);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.IsHindered(pippin));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(forfrodo);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// 8 + 4 burdens
		assertEquals(12, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARingBearerWhoIsNotFrodoMayBeExertedAndHisBurdensAreNotOnFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsildurScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo");
		var isildur = scn.GetRingBearer();
		var frodo = scn.GetFreepsCard("frodo");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(frodo, merry);
		scn.MoveCardsToHand(forfrodo);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, runner);

		assertEquals("Isildur", isildur.getBlueprint().getTitle());
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(isildur));
		// 7, and 1 for The One Ring he bears
		assertEquals(8, scn.GetStrength(isildur));

		scn.FreepsPlayCard(forfrodo);
		assertTrue(scn.FreepsHasCardChoiceAvailable(isildur, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));

		scn.FreepsChooseCard(isildur);
		assertEquals(1, scn.GetWoundsOn(isildur));
		// The three burdens are Isildur's: Frodo bears none.
		assertEquals(8, scn.GetStrength(isildur));
		assertEquals(3, scn.GetStrength(merry));
		assertInZone(Zone.REMOVED, forfrodo);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WhileFrodoIsHinderedThereIsNoBurdenOnFrodoToCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forfrodo = scn.GetFreepsCard("forfrodo1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(forfrodo);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.HinderCard(frodo);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.IsHindered(frodo));
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsPlayCard(forfrodo);
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		// A hindered card cannot be told by its title, so there is no burden "on Frodo".
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInZone(Zone.REMOVED, forfrodo);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
