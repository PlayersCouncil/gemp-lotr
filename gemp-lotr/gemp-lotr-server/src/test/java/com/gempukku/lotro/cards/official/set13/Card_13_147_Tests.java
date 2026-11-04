package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faith", "13_147");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability and a response when Frodo dies
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of another culture
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("keptsafe", "6_111");   // Kept Safe: a [shire] condition that holds [shire] tokens; its response answers a wound, and none is taken
					put("homestead", "13_152"); // Humble Homestead: a [shire] condition (to play, spot 2 Hobbits); its tokens count forests and dwellings, and the path has none
					put("runner", "1_178");     // Goblin Runner: a minion whose text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaithinFriendshipStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Faith in Friendship
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, add a [shire] token here.<br>At the start of the regroup phase, you may discard this from play or remove a token from here to heal a [shire] companion.
		*/

		var scn = GetScenario();

		var faith = scn.GetFreepsCard("faith");

		assertEquals("Faith in Friendship", faith.getBlueprint().getTitle());
		assertNull(faith.getBlueprint().getSubtitle());
		assertTrue(faith.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, faith.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, faith.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, faith.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(faith, Keyword.SUPPORT_AREA));
		assertEquals(1, faith.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedItTakesAShireTokenAndAnotherShireConditionPlayedAfterItAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faith = scn.GetFreepsCard("faith");
		var sam = scn.GetFreepsCard("sam");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var homestead = scn.GetFreepsCard("homestead");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(keptsafe);
		scn.AddTokensToCard(keptsafe, 2);
		scn.MoveCardsToHand(faith, homestead);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertInZone(Zone.HAND, faith, homestead);

		scn.FreepsPlayCard(faith);
		assertInZone(Zone.SUPPORT, faith);
		assertEquals(1, scn.GetCultureTokensOn(faith));
		// "Here": Kept Safe keeps its two.
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));

		// Another [shire] condition played after it: the trigger is on its own play.
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(1, scn.GetCultureTokensOn(faith));
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtEachRegroupPhaseItRemovesATokenOrDiscardsItselfToHealAShireCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faith = scn.GetFreepsCard("faith");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, smeagol, merry, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(faith);
		scn.AddTokensToCard(faith, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, faith);
		assertEquals(1, scn.GetCultureTokensOn(faith));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faith));
		scn.FreepsAcceptOptionalTrigger();
		// With a token here both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this from play"));
		assertTrue(scn.FreepsChoiceAvailable("Remove a token from here"));
		scn.FreepsChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(faith));
		assertInZone(Zone.SUPPORT, faith);

		// A wounded [shire] companion: Sam (a Hobbit) and Smeagol (no Hobbit).  Not Bounder, a [shire] ally,
		// Aragorn, a companion of another culture, or Merry, who has no wound to heal.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, aragorn, merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The regroup phase after a second move: offered again.  No token is left, so the discard is the one cost,
		// paid with nothing asked.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faith));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, faith);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, smeagol));
		scn.FreepsChooseCard(smeagol);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfTheManeuverPhaseAndDeclinedAtTheRegroupPhaseNothingChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faith = scn.GetFreepsCard("faith");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(faith);
		scn.AddTokensToCard(faith, 1);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, faith);
		assertEquals(1, scn.GetCultureTokensOn(faith));
		assertEquals(1, scn.GetWoundsOn(sam));
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(faith));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faith));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(faith));
		assertInZone(Zone.SUPPORT, faith);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoShireCompanionWoundedTheTokenIsStillPaidAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faith = scn.GetFreepsCard("faith");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(faith);
		scn.AddTokensToCard(faith, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(bounder, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, faith);
		assertEquals(1, scn.GetCultureTokensOn(faith));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(faith));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(faith));
		assertInZone(Zone.SUPPORT, faith);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
