package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fickle", "103_11");
					put("sam", "1_311");
					put("aragorn", "1_89"); // Strength 8, can beat Gollum
					put("glorfindel", "9_16"); // Strength 9, can beat Shelob

					put("gollum", "9_28"); // Gollum, Dark as Darkness - strength 5
					put("shelob", "8_26"); // Last Child of Ungoliant - strength 8
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FickleLoyaltiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Fickle Loyalties
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [shire] companions.
		* 	Each time Gollum loses a skirmish, make another minion strength -1 until the regroup phase.
		* 	Each time Shelob loses a skirmish, kill Gollum.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fickle");

		assertEquals("Fickle Loyalties", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FickleLoyaltiesRequires2ShireCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(fickle, sam);
		// Only Frodo (1 Shire companion) on table

		scn.StartGame();

		// Cannot play with only 1 Shire companion
		assertFalse(scn.FreepsPlayAvailable(fickle));

		// Play Sam (2nd Shire companion)
		scn.FreepsPlayCard(sam);

		// Now can play Fickle Loyalties
		assertTrue(scn.FreepsPlayAvailable(fickle));
		scn.FreepsPlayCard(fickle);

		assertInZone(Zone.SUPPORT, fickle);
	}

	@Test
	public void FickleLoyaltiesMakesAnotherMinionStrengthMinus1WhenGollumLosesSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(fickle);
		scn.MoveMinionsToTable(gollum, shelob);

		scn.StartGame();

		int shelobBaseStrength = scn.GetStrength(shelob);

		scn.SkipToAssignments();

		// Assign Aragorn (strength 8) to Gollum (strength 5)
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Verify strengths before skirmish resolves
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(shelobBaseStrength, scn.GetStrength(shelob));

		// Pass skirmish actions - Gollum loses
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Trigger fires - choose another minion (shelob auto-selected as only other minion)
		// Runner should now be -1 strength until regroup
		assertEquals(shelobBaseStrength - 1, scn.GetStrength(shelob));

		// Skip to regroup to verify modifier wears off
		scn.SkipToPhase(Phase.REGROUP);

		// Runner should be back to base strength
		assertEquals(shelobBaseStrength, scn.GetStrength(shelob));
	}

	@Test
	public void FickleLoyaltiesKillsGollumWhenShelobLosesSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fickle = scn.GetFreepsCard("fickle");
		var sam = scn.GetFreepsCard("sam");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.MoveCompanionsToTable(sam, glorfindel);
		scn.MoveCardsToSupportArea(fickle);
		scn.MoveMinionsToTable(gollum, shelob);

		scn.StartGame();

		scn.SkipToAssignments();

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		// Assign Glorfindel (strength 9) to Shelob (strength 8)
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(glorfindel);

		// Verify strengths before skirmish resolves
		assertEquals(9, scn.GetStrength(glorfindel));
		assertEquals(8, scn.GetStrength(shelob));

		// Pass skirmish actions - Shelob loses
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();

		// Trigger fires - Gollum is killed (goes to discard)
		assertInZone(Zone.DISCARD, gollum);
	}
}
