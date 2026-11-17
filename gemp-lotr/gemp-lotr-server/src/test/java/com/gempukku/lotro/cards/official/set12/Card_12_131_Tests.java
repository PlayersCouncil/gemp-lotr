package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stand", "12_131");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, +2 while he bears a weapon
					put("merry", "1_303");
					put("sword", "1_299");      // Hobbit Sword: [shire] hand weapon, strength +2, bearer must be a Hobbit
					// Bounder: [shire] Hobbit ally, home 2F, strength 2; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; a fellowship ability and a
					// response to Frodo's death, neither used here
					put("sam", "1_311");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability not used
					put("aragorn", "1_89");
					// Ted Sandyman, Chief's Men's Ally: [men] Hobbit minion, strength 3, vitality 4; his maneuver ability
					// needs a site his player controls and his regroup ability is used only on purpose
					put("ted", "18_75");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; "When you play" only
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StandTogetherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Stand Together
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Hobbit wins a skirmish, he or she gains <b>muster</b> until the end of the turn. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stand");

		assertEquals("Stand Together", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachHobbitWhoWinsASkirmishGainsMusterAndNoOtherCharacterDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ted = scn.GetShadowCard("ted");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		scn.MoveCardsToSupportArea(stand);
		scn.MoveCompanionsToTable(merry, sam, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		assertAttachedTo(sword, merry);
		scn.FreepsPass();
		// Site 2: Bounder's home, where he may be assigned.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(ted, runner1, runner2, runner3);
		// Bounder's 2 raised to 6 by hand, so that he beats a Runner's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{bounder, runner2},
				new PhysicalCardImpl[]{aragorn, runner3}, new PhysicalCardImpl[]{sam, ted});

		// Merry, a Hobbit companion: 3 + 2 for the Sword + 2 while he bears a weapon, against 5.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(7, scn.GetStrength(merry));
		assertFalse(scn.HasKeyword(merry, Keyword.MUSTER));
		scn.PassSkirmishActions();
		// The trigger is ordered with the skirmish's own result.
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.HasKeyword(merry, Keyword.MUSTER));

		// Bounder, a Hobbit ally.
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(6, scn.GetStrength(bounder));
		assertFalse(scn.HasKeyword(bounder, Keyword.MUSTER));
		scn.PassSkirmishActions();
		// The trigger is ordered with the skirmish's own result.
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.HasKeyword(bounder, Keyword.MUSTER));

		// Aragorn wins too, but he is no Hobbit.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner3);
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));

		// Ted Sandyman, the Shadow player's Hobbit minion, wins on the tie (3 against 3); Sam, a Hobbit who loses
		// and lives, gains nothing.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(ted));
		assertFalse(scn.HasKeyword(ted, Keyword.MUSTER));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.HasKeyword(ted, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(sam, Keyword.MUSTER));

		// Frodo, a Hobbit in no skirmish, has none either; Merry and Bounder keep theirs.
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(merry, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(bounder, Keyword.MUSTER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MusterLastsUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stand = scn.GetFreepsCard("stand");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(stand);
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);

		scn.StartGame();
		assertAttachedTo(sword, merry);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		assertFalse(scn.HasKeyword(merry, Keyword.MUSTER));
		scn.PassSkirmishActions();
		// The trigger is ordered with the skirmish's own result.
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.HasKeyword(merry, Keyword.MUSTER));

		// Still his after the regroup move, in the Shadow phase at the next site.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(merry, Keyword.MUSTER));

		// Gone once the turn is over.
		scn.SkipToOtherPlayersTurn();
		assertFalse(scn.HasKeyword(merry, Keyword.MUSTER));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
