package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("parapet", "5_87");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant and no other text
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, home 4T; an ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be
					// spotted; his abilities used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose
					// A Path Appointed: a [gondor] condition that holds [gondor] tokens and is no machine; abilities used only on purpose
					put("path", "8_42");

					put("ram", "5_44");         // Battering Ram: an [isengard] machine in the support area; abilities used only on purpose
					put("ladder", "5_57");      // Scaling Ladder: another [isengard] machine; abilities used only on purpose
					put("marks1", "1_176");     // Goblin Marksman: a minion whose whole text is Archer (+1 to the minion archery total)
					put("marks2", "1_176");
					put("marks3", "1_176");
					put("marks4", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ParapetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Parapet
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. Plays to your support area.
		 * <b>Archery:</b> Spot 2 [rohan] Men to remove 2 tokens from a machine or to make the minion archery total -3. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("parapet");

		assertEquals("Parapet", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionAndAnAllyLetItRemoveTwoTokensFromTheMachineChosenAndThenItIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parapet = scn.GetFreepsCard("parapet");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var path = scn.GetFreepsCard("path");
		scn.MoveCardsToSupportArea(parapet, path);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);

		var ram = scn.GetShadowCard("ram");
		var ladder = scn.GetShadowCard("ladder");
		var marks1 = scn.GetShadowCard("marks1");
		scn.MoveCardsToSupportArea(ram, ladder);
		scn.AddTokensToCard(ram, 3);
		scn.AddTokensToCard(ladder, 2);
		scn.AddTokensToCard(path, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, parapet, path, guma, ram, ladder);
		assertEquals(3, scn.GetCultureTokensOn(ram));
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetCultureTokensOn(path));
		// Not a fellowship action.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(parapet));

		scn.MoveMinionsToTable(marks1);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		// The Guard (a companion) and Guma (an ally) are the two [rohan] Men.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(parapet));
		scn.FreepsUseCardAction(parapet);

		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Remove 2 tokens");
		// Either machine; not a condition that holds tokens and is no machine, nor Parapet itself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ram, ladder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path, parapet));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ram);

		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertInDiscard(parapet);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void TheOtherBranchMakesTheMinionArcheryTotalMinus3ForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parapet = scn.GetFreepsCard("parapet");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToSupportArea(parapet);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);

		var ladder = scn.GetShadowCard("ladder");
		var marks1 = scn.GetShadowCard("marks1");
		var marks2 = scn.GetShadowCard("marks2");
		var marks3 = scn.GetShadowCard("marks3");
		var marks4 = scn.GetShadowCard("marks4");
		scn.MoveCardsToSupportArea(ladder);
		scn.AddTokensToCard(ladder, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, parapet, guma, ladder);
		assertEquals(2, scn.GetCultureTokensOn(ladder));

		scn.MoveMinionsToTable(marks1, marks2, marks3, marks4);
		scn.SkipToPhase(Phase.ARCHERY);
		// Four Goblin Marksmen.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsUseCardAction(parapet);

		// A machine with 2 tokens is there: both are offered.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("archery total");

		// 4 - 3
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertInDiscard(parapet);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// Archery fire: the one wound of the minions' total goes on the Guard.
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		// The archery phase is over, and the minion archery total is 4 again.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithTheOnlyMachineHoldingOneTokenTheArcheryTotalIsLoweredUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parapet = scn.GetFreepsCard("parapet");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToSupportArea(parapet);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);

		var ladder = scn.GetShadowCard("ladder");
		var marks1 = scn.GetShadowCard("marks1");
		var marks2 = scn.GetShadowCard("marks2");
		var marks3 = scn.GetShadowCard("marks3");
		var marks4 = scn.GetShadowCard("marks4");
		scn.MoveCardsToSupportArea(ladder);
		scn.AddTokensToCard(ladder, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, parapet, guma, ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));

		scn.MoveMinionsToTable(marks1, marks2, marks3, marks4);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsUseCardAction(parapet);

		// Two tokens cannot be removed from a machine with one: only the archery total can be lowered, and nothing is asked.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertInDiscard(parapet);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWithOneRohanManBesideARohanHobbitAndAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var parapet = scn.GetFreepsCard("parapet");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(parapet);
		scn.MoveCompanionsToTable(guard, merry, boromir);

		var ram = scn.GetShadowCard("ram");
		var marks1 = scn.GetShadowCard("marks1");
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, parapet, ram);
		assertEquals(3, scn.GetCultureTokensOn(ram));

		scn.MoveMinionsToTable(marks1);
		scn.SkipToPhase(Phase.ARCHERY);
		// Two [rohan] companions: Merry is a Hobbit and no Man.  Boromir is a Man of [gondor].
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(parapet));
		assertInZone(Zone.SUPPORT, parapet, ram);
		assertEquals(3, scn.GetCultureTokensOn(ram));
	}
}
