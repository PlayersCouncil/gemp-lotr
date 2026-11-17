package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halfling1", "8_18");
					put("halfling2", "8_18");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] companion; an archery ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion; stronger against a Nazgul only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit; stronger with a weapon only
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit (Ring-bound); an assignment ability
					// used only on purpose
					put("smeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					// Pippin, Trollslayer: a [gondor] Hobbit, no [shire] card; a skirmish ability used only on purpose
					put("pippin", "103_31");
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] ally; an archery ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] ally; a skirmish ability used only on purpose
					// Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");
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
					put("halfling1", "8_18");
					put("halfling2", "8_18");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound; a maneuver ability used only on purpose
					// Ent Avenger: an unbound [gandalf] Ent, no [gondor] Man; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Thrarin, Dwarven Smith: a [dwarven] ally; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					// Goblin Runner: [moria] Orc, strength 5; "When you play this minion, you may add (2)", never played
					put("runner", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NottheFirstHalflingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Not the First Halfling
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Exert Gandalf and either an [elven] companion or a [shire] companion to add a [gandalf] token here.<br><b>Skirmish:</b> Remove a [gandalf] token here to make a minion skirmishing an unbound companion strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halfling1");

		assertEquals("Not the First Halfling", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityExertsGandalfAndAnElvenCompanionToAddATokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halfling1 = scn.GetFreepsCard("halfling1");
		var halfling2 = scn.GetFreepsCard("halfling2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var orophin = scn.GetFreepsCard("orophin");
		var bounder = scn.GetFreepsCard("bounder");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, legolas, arwen, merry, smeagol, aragorn, pippin, avenger);
		scn.MoveAlliesToTable(orophin, bounder);
		scn.MoveCardsToSupportArea(halfling1, halfling2);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsActionAvailable(halfling1));

		// Gandalf is the only Gandalf, and exerts unasked; the Ent Avenger is a [gandalf] companion and no Gandalf.
		scn.FreepsUseCardAction(halfling1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, frodo, merry, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, pippin, orophin, bounder, avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoElvenCompanionAbleToExertAShireCompanionWhoIsNoHobbitPays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halfling1 = scn.GetFreepsCard("halfling1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, legolas, smeagol);
		scn.MoveCardsToSupportArea(halfling1);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Legolas, the one [elven] companion, cannot exert.
		scn.AddWoundsToChar(legolas, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsActionAvailable(halfling1));
		scn.FreepsUseCardAction(halfling1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas));
		scn.FreepsChooseCard(smeagol);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithGandalfExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halfling1 = scn.GetFreepsCard("halfling1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, legolas, avenger);
		scn.MoveCardsToSupportArea(halfling1);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Gandalf cannot exert.  The Ent Avenger, a [gandalf] companion, and Legolas can.
		scn.AddWoundsToChar(gandalf, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertFalse(scn.FreepsActionAvailable(halfling1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShireCompanionAbleToExertAnElvenCompanionPaysAndThenTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halfling1 = scn.GetFreepsCard("halfling1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var orophin = scn.GetFreepsCard("orophin");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, legolas, aragorn, pippin);
		scn.MoveAlliesToTable(orophin, bounder);
		scn.MoveCardsToSupportArea(halfling1);

		scn.StartGame();
		// Frodo (vitality 4), the one [shire] companion, is exhausted; Legolas (vitality 3) can exert once more.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(legolas, 1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		// Legolas is then the only one who can pay the second half, and exerts unasked.
		scn.FreepsUseCardAction(halfling1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetCultureTokensOn(halfling1));

		// Now no [elven] or [shire] companion can exert.  Aragorn, a [gondor] Hobbit, an [elven] ally and a [shire]
		// ally all can, and none of them pays.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertFalse(scn.FreepsActionAvailable(halfling1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenHereToMakeAMinionSkirmishingAnUnboundCompanionStrengthMinus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var halfling1 = scn.GetFreepsCard("halfling1");
		var halfling2 = scn.GetFreepsCard("halfling2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveCardsToSupportArea(halfling1, halfling2);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddTokensToCard(halfling1, 1);
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		scn.MoveMinionsToTable(savage, runner, troop);

		// A Skirmish ability: not in the assignment phase before.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(halfling1));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{avenger, troop});
		// The Shadow player puts the minion left over on Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));
		// The copy with no token here cannot pay, though the other copy holds one.
		assertFalse(scn.FreepsActionAvailable(halfling2));
		assertTrue(scn.FreepsActionAvailable(halfling1));

		scn.FreepsUseCardAction(halfling1);
		// Troop is assigned to the Ent Avenger, whose skirmish has not begun: it is not skirmishing him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		scn.FreepsChooseCard(savage);

		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn's 8 against 3 + 5 loses; in the Ent Avenger's skirmish, the next, the Uruk Savage is at its own 5 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(avenger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage));

		// The same ability against an unbound companion who is neither a Man nor [gondor]: the second copy, given its token.
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		scn.AddTokensToCard(halfling2, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetCultureTokensOn(halfling2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(halfling2));

		scn.FreepsUseCardAction(halfling2);
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertEquals(11, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityPaysForNothingWhenARingBoundCompanionOrAnAllyIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var halfling1 = scn.GetFreepsCard("halfling1");
		var halfling2 = scn.GetFreepsCard("halfling2");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(halfling1, halfling2);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddTokensToCard(halfling1, 1);
		scn.AddTokensToCard(halfling2, 1);
		assertEquals(1, scn.GetCultureTokensOn(halfling1));
		assertEquals(1, scn.GetCultureTokensOn(halfling2));
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{thrarin, runner});

		// Sam is Ring-bound: the token is paid and the Uruk Savage keeps its strength.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(halfling1));
		scn.FreepsUseCardAction(halfling1);
		assertEquals(0, scn.GetCultureTokensOn(halfling1));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Thrarin is an ally, never unbound: the same.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, runner));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(halfling2));
		scn.FreepsUseCardAction(halfling2);
		assertEquals(0, scn.GetCultureTokensOn(halfling2));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
