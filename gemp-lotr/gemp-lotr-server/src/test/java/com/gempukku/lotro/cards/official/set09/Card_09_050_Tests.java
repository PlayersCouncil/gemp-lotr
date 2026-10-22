package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knows", "9_50");
					put("knows2", "9_50");
					// Merry, From O'er the Brandywine: [shire] Hobbit, strength 3, vitality 4; +2 only while he bears a
					// weapon, and he bears none here
					put("merry", "1_303");
					// Merry, Master Holbytla: a [rohan] Hobbit, strength 3, vitality 4; enduring and valiant, a Man only
					// beside Eowyn or 3 [rohan] companions; his abilities are used only on purpose
					put("rohanmerry", "103_85");
					// Pippin, Trollslayer: a [gondor] Hobbit, strength 3, vitality 4; enduring, so he is never wounded
					// here; his skirmish ability is used only on purpose
					put("gondorpippin", "103_31");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, strength 3, vitality 4; his text is about tales
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit, strength 3, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is a Man, vitality 4
					put("bounder", "1_286");    // Bounder: a Hobbit ally, strength 2, vitality 2; his skirmish ability is used only on purpose
					// Escape: [shire] condition, cost 0, vitality +1, bearer must be an unbound Hobbit; its skirmish
					// ability is used only on purpose
					put("escape", "4_300");

					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("seeker", "4_195");     // Uruk Seeker: fierce, strength 6; stronger only against a bearer of a search card
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, vitality 4; his
					// abilities are used only on purpose
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EveryoneKnowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Everyone Knows
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Each unbound Hobbit is strength +1 for each vitality over 3 he or she has.
		 * 	<b>Skirmish:</b> Prevent an unbound Hobbit from being overwhelmed unless his or her strength is tripled. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knows");

		assertEquals("Everyone Knows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUnboundHobbitIsStrengthPlusOneForEachVitalityOverThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knows = scn.GetFreepsCard("knows");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("gondorpippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var escape = scn.GetFreepsCard("escape");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(escape);

		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		// No Hobbit ally has more than 3 vitality in print: Bounder is raised to 4 by hand, so that only the
		// word "unbound" keeps him out.
		scn.ApplyAdHocModifier(new VitalityModifier(null, bounder, null, 2));

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(4, scn.GetVitality(merry));
		assertEquals(4, scn.GetVitality(pippin));
		assertEquals(4, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(4, scn.GetVitality(bounder));
		assertEquals(4, scn.GetVitality(ted));

		scn.MoveCardsToSupportArea(knows);
		scn.FreepsRefreshDecision();

		// Vitality 4 is one over 3: the [shire] and the [gondor] unbound Hobbit are +1.  Not the Ring-bound Sam or
		// Frodo, the companion who is no Hobbit, the Hobbit ally or the Hobbit minion.
		assertEquals(4, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));

		// Escape gives Merry vitality +1: two over 3.
		scn.FreepsPlayCard(escape);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(escape, merry);
		assertEquals(5, scn.GetVitality(merry));
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnUnboundHobbitWithThreeVitalityOrLessGetsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knows = scn.GetFreepsCard("knows");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("gondorpippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveCardsToSupportArea(knows);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(4, scn.GetStrength(merry));

		// Each wound takes a vitality away: 3 left is none over 3.
		scn.AddWoundsToChar(merry, 1);
		assertEquals(3, scn.GetVitality(merry));
		assertEquals(3, scn.GetStrength(merry));

		// 2 left is no penalty either.
		scn.AddWoundsToChar(merry, 1);
		assertEquals(2, scn.GetVitality(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(pippin));
	}

	@Test
	public void SkirmishAbilityKeepsAnUnboundHobbitBeatenByDoubleFromBeingOverwhelmedAndDiscardsThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knows = scn.GetFreepsCard("knows");
		var knows2 = scn.GetFreepsCard("knows2");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(knows, knows2);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertInZone(Zone.SUPPORT, knows, knows2);

		// Not an assignment action.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(knows));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(pippin, brigand);
		scn.FreepsResolveSkirmish(pippin);

		// 3 + 1 for each copy
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		assertTrue(scn.FreepsActionAvailable(knows));

		// Pippin is the only unbound Hobbit, so he is chosen unasked.
		scn.FreepsUseCardAction(knows);

		// The copy used is discarded, the other stays, and with it +1.
		assertInZone(Zone.DISCARD, knows);
		assertInZone(Zone.SUPPORT, knows2);
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(3, scn.GetOverwhelmMultiplier(pippin));

		scn.ShadowPass();
		scn.FreepsPass();

		// 8 against 4 is double and less than triple: beaten and wounded, not overwhelmed.
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyUnboundHobbitMayBeChosenAndOnlyTheOneChosenIsProtected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knows = scn.GetFreepsCard("knows");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("rohanmerry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(pippin, merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(knows);

		var brigand = scn.GetShadowCard("brigand");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, ted);
		assertInZone(Zone.SUPPORT, knows);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, brigand);
		// Ted is left over, and the Shadow player assigns him to nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(4, scn.GetStrength(pippin));
		scn.FreepsUseCardAction(knows);

		// The unbound Hobbit skirmishing and an unbound [rohan] one in no skirmish.  Not the Ring-bound Sam or
		// the Ring-bearer, the Hobbit ally, the Hobbit minion, or an unbound companion who is a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, bounder, ted, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		// The condition is gone, and with it his +1.
		assertInZone(Zone.DISCARD, knows);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(brigand));

		scn.ShadowPass();
		scn.FreepsPass();

		// Merry was protected, not Pippin: 8 against 3 overwhelms him.
		assertInZone(Zone.DEAD, pippin);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheProtectionEndsWithTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knows = scn.GetFreepsCard("knows");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(knows);

		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(seeker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, seeker);
		scn.FreepsResolveSkirmish(pippin);

		// Pippin is the only unbound Hobbit, so he is chosen unasked.
		scn.FreepsUseCardAction(knows);
		// 6 against 3, the condition gone: double.
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(3, scn.GetOverwhelmMultiplier(pippin));
		scn.ShadowPass();
		scn.FreepsPass();

		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));

		// The fierce round is another skirmish phase: the same Pippin against the same Seeker, unprotected.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, seeker);
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(2, scn.GetOverwhelmMultiplier(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassFierceSkirmishActions();

		// Still protected he would have a second wound and be alive.
		assertInZone(Zone.DEAD, pippin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitTheAbilityOnlyDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knows = scn.GetFreepsCard("knows");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(knows);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertInZone(Zone.SUPPORT, knows);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);

		// Its second sentence still does something, so it is offered (shapes, "Count every sentence").
		assertTrue(scn.FreepsActionAvailable(knows));
		scn.FreepsUseCardAction(knows);

		// Nobody to choose: the condition is discarded, and nobody's multiplier changes.
		assertInZone(Zone.DISCARD, knows);
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
