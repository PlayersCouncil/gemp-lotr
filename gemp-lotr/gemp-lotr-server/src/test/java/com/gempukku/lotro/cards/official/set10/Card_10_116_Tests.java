package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("totgr", "10_116");
					put("sam", "1_311");

					put("sauron", "9_48");
					put("evil", "1_246"); //skirmish event
					put("spies", "2_91"); //response event
					put("frost1", "1_135");
					put("frost2", "1_135");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void TheTaleoftheGreatRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: The Tale of the Great Ring
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Tale</b>.
		 * <b>Skirmish:</b> Prevent a Hobbit from being overwhelmed unless a
		 * Shadow event is (or was) played during this skirmish. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("totgr");

		assertEquals("The Tale of the Great Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TOTGRPreventsOverwhelmWithNoEvents() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var totgr = scn.GetFreepsCard("totgr");
		scn.MoveCardsToSupportArea(totgr);
		scn.MoveCompanionToTable(sam);

		var sauron = scn.GetShadowCard("sauron");
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, sauron);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.FreepsActionAvailable(totgr));
		scn.FreepsUseCardAction(totgr);
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(24, scn.GetStrength(sauron));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		//Sam survived skirmish and now we're in the fierce assignment phase
		assertEquals(Zone.FREE_CHARACTERS, sam.getZone());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void TOTGRDoesNotPreventOverwhelmWithShadowSkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var totgr = scn.GetFreepsCard("totgr");
		scn.MoveCardsToSupportArea(totgr);
		scn.MoveCompanionToTable(sam);

		var sauron = scn.GetShadowCard("sauron");
		var evil = scn.GetShadowCard("evil");
		scn.MoveMinionsToTable(sauron);
		scn.MoveCardsToHand(evil);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, sauron);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.FreepsActionAvailable(totgr));
		scn.FreepsUseCardAction(totgr);
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(24, scn.GetStrength(sauron));

		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);
		scn.ShadowChoose("1");

		scn.PassCurrentPhaseActions();

		//Sam got overwhelmed and now we're in the fierce assignment phase
		assertEquals(Zone.DEAD, sam.getZone());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void TOTGRDoesNotPreventOverwhelmWithShadowResponseEvent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var totgr = scn.GetFreepsCard("totgr");
		scn.MoveCardsToSupportArea(totgr);
		scn.MoveCompanionToTable(sam);

		var sauron = scn.GetShadowCard("sauron");
		var spies = scn.GetShadowCard("spies");
		scn.MoveMinionsToTable(sauron);
		scn.MoveCardsToHand(spies);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, sauron);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.FreepsActionAvailable(totgr));
		scn.FreepsUseCardAction(totgr);
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(24, scn.GetStrength(sauron));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.ShadowPlayAvailable(spies));
		scn.ShadowPlayCard(spies);

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		//Sam got overwhelmed and now we're in the fierce assignment phase
		assertEquals(Zone.DEAD, sam.getZone());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void TOTGRPreventsOverwhelmEvenWhenStrengthReducedToZero() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var totgr = scn.GetFreepsCard("totgr");
		scn.MoveCardsToSupportArea(totgr);
		scn.MoveCompanionToTable(sam);

		var sauron = scn.GetShadowCard("sauron");
		var frost1 = scn.GetShadowCard("frost1");
		var frost2 = scn.GetShadowCard("frost2");
		scn.MoveMinionsToTable(sauron);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		var site2 = scn.GetCurrentSite();
		scn.AttachCardsTo(site2, frost1, frost2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, sauron);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.FreepsActionAvailable(totgr));
		scn.FreepsUseCardAction(totgr);
		scn.FreepsChooseCard(sam);
		assertEquals(0, scn.GetStrength(sam));
		assertEquals(24, scn.GetStrength(sauron));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		//Sam survived skirmish even with 0 strength and now we're in the fierce assignment phase
		assertEquals(Zone.FREE_CHARACTERS, sam.getZone());
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}
}
