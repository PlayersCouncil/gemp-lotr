package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaders", "7_112");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion; maneuver ability used only on purpose
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] companion; skirmish ability used only on purpose
					// Faramir, Son of Denethor: [gondor] companion, Ring-bound ranger; bars the OPPONENT's skirmish
					// events and abilities in his skirmishes, and is in none.
					put("faramir", "4_117");
					// Denethor, Steward of the City: [gondor] companion; his texts are a fellowship action used only
					// on purpose and threats and burdens added by Shadow cards (there are none).
					put("denethor", "7_85");
					// Aragorn, Defender of Rohan: a [rohan] Aragorn, valiant; hunter for [rohan] companions only
					// while the Ring-bearer is assigned.
					put("rohanaragorn", "17_93");
					// Boromir, My Brother: a [gondor] condition borne by Faramir, whose title begins with Boromir and is
					// not Boromir; a skirmish ability used only on purpose.
					put("brother", "4_111");
					put("knight", "5_36");        // Knight of Gondor: a [gondor] companion of another title; fellowship ability used only on purpose
					// Gondorian Merchant: the [gondor] ally (home 3T); a maneuver ability used only on purpose.
					put("merchant", "7_97");
					// A Path Appointed: a [gondor] support-area condition that carries [gondor] tokens; its abilities are
					// in the fellowship and regroup phases.
					put("path", "8_42");

					put("troop1", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NobleLeadersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Noble Leaders
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, place a [gondor] token here for each of the following characters
		 * 	you can spot: Aragorn, Boromir, Denethor or Faramir.
		 * 	Skirmish: Remove a token from here or discard this condition to make a [gondor] companion strength +1
		 * 	and damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaders");

		assertEquals("Noble Leaders", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideAragornAndBoromirItGetsTwoTokensAndDenethorPlayedLaterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaders = scn.GetFreepsCard("leaders");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var denethor = scn.GetFreepsCard("denethor");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.MoveCardsToHand(leaders, denethor);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(leaders));
		scn.FreepsPlayCard(leaders);

		// Aragorn and Boromir; the Knight of Gondor is a [gondor] companion of another title.  Nothing is asked.
		assertInZone(Zone.SUPPORT, leaders);
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The count was taken as the condition was played.
		scn.FreepsPlayCard(denethor);
		assertInPlay(denethor);
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedBesideTheRohanAragornDenethorAndFaramirItGetsThreeTokensAndNoneForTheConditionBoromirMyBrother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaders = scn.GetFreepsCard("leaders");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var denethor = scn.GetFreepsCard("denethor");
		var faramir = scn.GetFreepsCard("faramir");
		var brother = scn.GetFreepsCard("brother");
		scn.MoveCompanionsToTable(rohanaragorn, denethor, faramir);
		scn.AttachCardsTo(faramir, brother);
		scn.MoveCardsToHand(leaders);

		scn.StartGame();

		assertInPlay(brother);
		scn.FreepsPlayCard(leaders);

		// The [rohan] Aragorn, Denethor and Faramir.  Boromir, My Brother, a condition, is not counted.
		assertEquals(3, scn.GetCultureTokensOn(leaders));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenToMakeAGondorCompanionStrengthPlusOneAndDamagePlusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var leaders = scn.GetFreepsCard("leaders");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(leaders);
		scn.AddTokensToCard(leaders, 2);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{boromir, troop2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(leaders));

		scn.FreepsUseCardAction(leaders);
		assertTrue(scn.FreepsChoiceAvailable("Remove a token"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChoose("Remove a token");
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertInZone(Zone.SUPPORT, leaders);

		// Aragorn, who is skirmishing, Boromir, whose skirmish has not begun, and the Knight of Gondor, assigned to
		// no skirmish.  Not the [gondor] ally, nor Frodo, a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn loses to the 13 of the Troop; the next skirmish is Boromir's, a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));

		// With a token still on it, the ability is not offered in the regroup phase.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertFalse(scn.FreepsActionAvailable(leaders));
	}

	@Test
	public void SkirmishAbilityMayDiscardNobleLeadersInsteadThoughATokenIsHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaders = scn.GetFreepsCard("leaders");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(leaders);
		scn.AddTokensToCard(leaders, 1);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsUseCardAction(leaders);
		scn.FreepsChoose("Discard this condition");

		assertInDiscard(leaders);
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheAbilityDiscardsNobleLeadersAndLeavesTheTokenOfAnotherGondorCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaders = scn.GetFreepsCard("leaders");
		var path = scn.GetFreepsCard("path");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(leaders, path);
		scn.AddTokensToCard(path, 1);

		var troop = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetCultureTokensOn(leaders));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.FreepsActionAvailable(leaders));

		// Only the discard can be done, and it is done with no question.
		scn.FreepsUseCardAction(leaders);

		assertInDiscard(leaders);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
