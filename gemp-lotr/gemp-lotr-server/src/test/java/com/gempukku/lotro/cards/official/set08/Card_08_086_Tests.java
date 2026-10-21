package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doom1", "8_86");
					put("doom2", "8_86");       // a second copy, to show which copy "here" is
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion, vitality 4 (a Man only beside 3 [rohan]
					// companions or Éowyn); his trigger answers ANOTHER [rohan] Man exerting, his skirmish ability is
					// used only on purpose
					put("merry", "103_85");
					// Rohirrim Herdsman: a [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 to a [gondor] Man, so that the Merchant can exert
					// Oathbreaker: a [gondor] companion who is a Wraith, no Man; a skirmish ability used only on purpose
					put("oath", "8_41");
					// Pippin, Trollslayer: a [gondor] Hobbit companion, no [shire] card; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("smeagol", "103_116");
					put("bounder", "1_286");    // Bounder: a [shire] ally; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doom1", "8_86");
					put("doom2", "8_86");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					// Ent Avenger: a [gandalf] Ent companion; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");
					// Thrarin, Dwarven Smith: a [dwarven] ally; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					// Goblin Runner: [moria] Orc, strength 5, vitality 1 (exhausted); "When you play this minion, you may
					// add (2)", never played
					put("runner", "1_178");
					put("cguard", "10_81");     // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DoomDroveThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Doom Drove Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Exert a [rohan] companion and either a [gondor] Man or a [shire] companion to add a [rohan] token here.<br><b>Skirmish:</b> Remove a [rohan] token here to exert a minion skirmishing a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("doom1");

		assertEquals("Doom Drove Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityExertsARohanCompanionAndAGondorManOrAShireCompanionToAddATokenToThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var oath = scn.GetFreepsCard("oath");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(doom1, doom2);
		scn.MoveCompanionsToTable(guard, merry, knight, oath, pippin, smeagol);
		scn.MoveAlliesToTable(herdsman, merchant, bounder);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(2, scn.GetVitality(merchant));
		assertTrue(scn.FreepsActionAvailable(doom1));
		scn.FreepsUseCardAction(doom1);

		// The [rohan] companion: a Man and a Hobbit; not a [rohan] ally, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, knight, frodo, smeagol, oath, pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// Then a [gondor] Man (a companion and an ally) or a [shire] companion (the Ring-bearer and one who is no
		// Hobbit), both alternatives on offer at once; not a [gondor] companion who is no Man, a [gondor] Hobbit, a
		// [shire] ally, nor a Man of [rohan].
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant, frodo, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(oath, pippin, bounder, guard, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(smeagol);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShireCompanionAbleToExertAGondorManPaysAndThenTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doom1 = scn.GetFreepsCard("doom1");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var oath = scn.GetFreepsCard("oath");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCompanionsToTable(guard, knight, oath, pippin);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Frodo, vitality 4, exhausted; the Knight, vitality 3, can exert once more.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(knight, 1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(oath));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.FreepsActionAvailable(doom1));
		scn.FreepsUseCardAction(doom1);

		// The Guard is the only [rohan] companion, and the [gondor] alternative the only one that can be paid: both
		// exert with nothing asked.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetCultureTokensOn(doom1));

		// Now neither a [gondor] Man nor a [shire] companion can exert, though the Guard can, and so can
		// Oathbreaker, Pippin and the Bounder.
		assertFalse(scn.FreepsActionAvailable(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGondorManAbleToExertAShireCompanionPaysAndAnExhaustedRohanCompanionStopsTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doom1 = scn.GetFreepsCard("doom1");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveAlliesToTable(herdsman);

		scn.StartGame();
		// The Knight, vitality 3, exhausted; the Guard, vitality 3, can exert once more.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.FreepsActionAvailable(doom1));
		scn.FreepsUseCardAction(doom1);

		// The [shire] alternative is the only one that can be paid, and is.
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetCultureTokensOn(doom1));

		// The Guard, the one [rohan] companion, is now exhausted: Frodo can still exert, and so can the Herdsman, a
		// [rohan] ally.
		assertFalse(scn.FreepsActionAvailable(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenHereToExertAMinionSkirmishingACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveCardsToSupportArea(doom1, doom2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var cguard = scn.GetShadowCard("cguard");

		scn.StartGame();
		// The first copy gained its token by its fellowship ability in an earlier turn.
		scn.AddTokensToCard(doom1, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		scn.MoveMinionsToTable(savage, troop, runner, cguard);

		// A Skirmish ability: not in the assignment phase before.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(doom1));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{avenger, cguard});
		// The Shadow player puts the minions left over on Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, troop, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(cguard));
		// The copy with no token here cannot pay, though the other copy holds one.
		assertFalse(scn.FreepsActionAvailable(doom2));
		assertTrue(scn.FreepsActionAvailable(doom1));

		scn.FreepsUseCardAction(doom1);
		// Minions of two cultures and races skirmishing Aragorn; not the Runner, exhausted (vitality 1), nor the
		// Cirith Ungol Guard, assigned to the Ent Avenger, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, cguard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(cguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Ent Avenger's skirmish, the next: the same ability against a companion who is neither a Man nor
		// [gondor], on the second copy, given its token.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(avenger);
		assertTrue(scn.IsCharSkirmishingAgainst(avenger, cguard));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		scn.AddTokensToCard(doom2, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertTrue(scn.FreepsActionAvailable(doom2));

		scn.FreepsUseCardAction(doom2);
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetWoundsOn(cguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityPaysForNothingWhenTheMinionIsSkirmishingAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var doom1 = scn.GetFreepsCard("doom1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(doom1);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddTokensToCard(doom1, 1);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);

		// Thrarin is an ally, no companion: the token is paid and the Uruk Savage is not exerted.
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, savage));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(doom1));
		scn.FreepsUseCardAction(doom1);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
