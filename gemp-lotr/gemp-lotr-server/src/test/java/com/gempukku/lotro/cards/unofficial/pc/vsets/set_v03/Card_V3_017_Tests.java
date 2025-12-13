package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("get", "103_17");
					put("shelob", "8_26"); // Strength 8
					put("web", "8_30"); // Gollum item for stacking
					put("pass", "103_12"); // Pass of the Spider - another Gollum item
					put("web2", "8_30"); // Second Web for discard test
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("runner5", "1_178");

					put("aragorn", "1_89"); // Strength 8, tie goes to Shadow
					put("glorfindel", "9_16"); // Strength 9, can beat Shelob
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UngoliantsGetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ungoliant's Get
		 * Unique: true
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time Shelob loses a skirmish, you may play a [gollum] item from your discard pile.
		* 	Response: If Shelob wins a skirmish and there are 4 minions stacked on [gollum] items, hinder this to make her <b>relentless</b> until the regroup phase <i>(she participates in 1 extra round of skirmishes after fierce)</i>. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("get");

		assertEquals("Ungoliant's Get", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void UngoliantsGetPlaysGollumItemFromDiscardWhenShelobLoses() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web2 = scn.GetShadowCard("web2");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(get);
		scn.MoveMinionsToTable(shelob);
		scn.MoveCardsToDiscard(web2);

		scn.StartGame();
		scn.SkipToAssignments();

		assertInZone(Zone.DISCARD, web2);

		// Assign Glorfindel (strength 9) to Shelob (strength 8) - Shelob loses
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassCurrentPhaseActions();

		// Shelob loses, trigger should fire
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Web should be played from discard
		assertInZone(Zone.SUPPORT, web2);
	}

	@Test
	public void UngoliantsGetCanDeclinePlayFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web2 = scn.GetShadowCard("web2");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(get);
		scn.MoveMinionsToTable(shelob);
		scn.MoveCardsToDiscard(web2);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Web should remain in discard
		assertInZone(Zone.DISCARD, web2);
	}

	@Test
	public void UngoliantsGetMakesShelobRelentlessWhenSheWinsWith4StackedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var pass = scn.GetShadowCard("pass");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(get, web, pass);
		scn.MoveMinionsToTable(shelob);
		// Stack 2 minions on Web, 2 on Pass = 4 total
		scn.StackCardsOn(web, runner1, runner2);
		scn.StackCardsOn(pass, runner3, runner4);

		scn.StartGame();
		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(get));
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));

		// Assign Aragorn (strength 8) to Shelob (strength 8) - tie goes to Shadow, Shelob wins
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Shelob wins, Response should be available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Get should be hindered, Shelob should be relentless
		assertTrue(scn.IsHindered(get));
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));
	}

	@Test
	public void UngoliantsGetResponseNotAvailableWith3StackedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var pass = scn.GetShadowCard("pass");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(get, web, pass);
		scn.MoveMinionsToTable(shelob);
		// Stack only 3 minions total
		scn.StackCardsOn(web, runner1, runner2);
		scn.StackCardsOn(pass, runner3);

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign Aragorn to Shelob - Shelob wins
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Shelob wins, but only 3 stacked minions - Response NOT available
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(get));
		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
	}

	@Test
	public void UngoliantsGetResponseNotAvailableWhenAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(get, web);
		scn.MoveMinionsToTable(shelob);
		scn.StackCardsOn(web, runner1, runner2, runner3, runner4);

		scn.StartGame();

		// Pre-hinder the Get
		scn.HinderCard(get);
		assertTrue(scn.IsHindered(get));

		scn.SkipToAssignments();

		// Assign Aragorn to Shelob - Shelob wins
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Shelob wins with 4 stacked, but Get is already hindered - Response NOT available
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void UngoliantsGetRelentlessWearsOffAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var get = scn.GetShadowCard("get");
		var shelob = scn.GetShadowCard("shelob");
		var web = scn.GetShadowCard("web");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(get, web);
		scn.MoveMinionsToTable(shelob);
		scn.StackCardsOn(web, runner1, runner2, runner3, runner4);

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign Aragorn to Shelob - Shelob wins
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Accept trigger to make Shelob relentless
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.HasKeyword(shelob, Keyword.RELENTLESS));

		// Skip to Regroup phase - relentless should wear off
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.HasKeyword(shelob, Keyword.RELENTLESS));
	}
}
