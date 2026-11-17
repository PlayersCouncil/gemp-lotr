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

public class Card_07_314_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_314");
					// Great Siege-towers: a [sauron] engine condition that holds [sauron] tokens; its abilities are
					// used only on purpose
					put("towers", "7_281");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1, no other text
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, strength 8; archery only at 3 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard1", "5_83");      // Household Guard: strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [sauron] minions that each beat a Household Guard without killing him.
	protected VirtualTableScenario GetTwoWinsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_314");
					put("chieftain1", "1_266"); // Orc Chieftain: [sauron] Orc, strength 7; his text answers a weapon played on him
					put("chieftain2", "1_266");
					put("guard1", "5_83");      // Household Guard: strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongholdofCirithUngolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stronghold of Cirith Ungol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Response:</b> If your [sauron] minion wins a skirmish, place a [sauron] token here (limit 1 per site).<br><b>Regroup:</b> Add (2) for each token here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stronghold");

		assertEquals("Stronghold of Cirith Ungol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "If your [sauron] minion wins a skirmish, place a [sauron] token here (limit 1 per site)": once for each
	// site in a turn (Q-0084, F-0146).  The definition counted by a regroup-phase limit; it is now the engine's
	// own count by site.  No board was found that tells the two apart: this test holds the limit, and passed
	// against the definition as it was.
	// F-0455.
	@Test
	public void OneTokenIsPlacedAtEachSiteHoweverManySkirmishesAreWonThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWinsScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var stronghold = scn.GetShadowCard("stronghold");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain1, chieftain2);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, chieftain1}, new PhysicalCardImpl[]{guard2, chieftain2});

		// The first win at site 2: offered, and a token placed.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stronghold));

		// The second win at the same site: not offered.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertEquals(1, scn.GetCultureTokensOn(stronghold));

		// A second move in the same turn, and a win at site 3: offered again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(guard1, chieftain1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(guard1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(stronghold));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheWinOfYourSauronMinionIsAnsweredAndItsTokenGoesHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var stronghold = scn.GetShadowCard("stronghold");
		var towers = scn.GetShadowCard("towers");
		var creeper = scn.GetShadowCard("creeper");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToSupportArea(stronghold, towers);
		scn.AddTokensToCard(towers, 2);
		assertEquals(0, scn.GetCultureTokensOn(stronghold));
		assertEquals(2, scn.GetCultureTokensOn(towers));

		scn.StartGame();
		scn.MoveMinionsToTable(creeper, sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, creeper}, new PhysicalCardImpl[]{guard2, sentry});

		// A [wraith] Orc wins, 8 to 6: not answered.  It comes first, so that the limit cannot be what keeps it out.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(0, scn.GetCultureTokensOn(stronghold));

		// A [sauron] Uruk-hai wins, 8 to 6, damage +1: answered, and the token goes on this condition alone.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stronghold));
		assertEquals(2, scn.GetCultureTokensOn(towers));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASauronMinionThatLosesASkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var stronghold = scn.GetShadowCard("stronghold");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);

		// Aragorn, 8, beats the Chieftain, 7; the Chieftain lives with 1 wound of 2.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInPlay(chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertEquals(0, scn.GetCultureTokensOn(stronghold));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityAddsTwoForTheOneTokenHereAndDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetShadowCard("stronghold");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(stronghold, towers);
		scn.AddTokensToCard(stronghold, 1);
		scn.AddTokensToCard(towers, 2);
		assertEquals(1, scn.GetCultureTokensOn(stronghold));
		assertEquals(2, scn.GetCultureTokensOn(towers));
		assertInZone(Zone.SUPPORT, stronghold);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(3);

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(stronghold));
		scn.ShadowUseCardAction(stronghold);

		// 3 + 2 for the one token here; the two on Great Siege-towers are not counted.
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(stronghold);
		assertEquals(2, scn.GetCultureTokensOn(towers));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityAddsFourForTwoTokensHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetShadowCard("stronghold");
		scn.MoveCardsToSupportArea(stronghold);
		scn.AddTokensToCard(stronghold, 2);
		assertEquals(2, scn.GetCultureTokensOn(stronghold));
		assertInZone(Zone.SUPPORT, stronghold);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(3);

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetTwilight());
		scn.ShadowUseCardAction(stronghold);

		// 3 + 2 for each of the two tokens
		assertEquals(7, scn.GetTwilight());
		assertInDiscard(stronghold);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stronghold = scn.GetShadowCard("stronghold");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(stronghold);
		scn.AddTokensToCard(stronghold, 1);
		assertEquals(1, scn.GetCultureTokensOn(stronghold));
		assertInZone(Zone.SUPPORT, stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stronghold));
		assertInZone(Zone.SUPPORT, stronghold);
		assertEquals(1, scn.GetCultureTokensOn(stronghold));
	}
}
