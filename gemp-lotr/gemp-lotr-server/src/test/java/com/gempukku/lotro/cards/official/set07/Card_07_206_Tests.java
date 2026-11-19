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

public class Card_07_206_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_206");
					// Winged and Ominous: a [wraith] support-area condition that holds [wraith] tokens; its abilities
					// are used only on purpose
					put("winged", "6_89");
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, strength 8; archery only at 3 burdens
					// Morgul Hound: [wraith] Orc, strength 5, vitality 2; +4 and fierce only at 2 wounds on the
					// Ring-bearer
					put("hound", "7_192");
					// Orc Chieftain: [sauron] Orc, strength 7; his text answers only a weapon played on him
					put("chieftain", "1_266");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], strength 12, fierce
					put("khamul", "103_61");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard1", "5_83");      // Household Guard: strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [wraith] minions that each beat a Household Guard without killing him.
	protected VirtualTableScenario GetTwoWinsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_206");
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul, strength 9; her text is about [wraith] conditions on companions
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, strength 8; archery only at 3 burdens
					put("guard1", "5_83");      // Household Guard: strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongholdofbrMinasMorgulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stronghold of Minas Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If your [wraith] minion wins a skirmish, place a [wraith] token here (limit 1 per site).<br><b>Regroup:</b> Spot 3 [wraith] tokens here to exhaust the Ring-bearer. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stronghold");

		assertEquals("Stronghold of Minas Morgul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "If your [wraith] minion wins a skirmish, place a [wraith] token here (limit 1 per site)": once for each
	// site in a turn (Q-0084, F-0146).  The definition counted by a regroup-phase limit; it is now the engine's
	// own count by site.  No board was found that tells the two apart: this test holds the limit, and passed
	// against the definition as it was.
	// F-0454.
	@Test
	public void OneTokenIsPlacedAtEachSiteHoweverManySkirmishesAreWonThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWinsScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var stronghold = scn.GetShadowCard("stronghold");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, creeper);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, lemenya}, new PhysicalCardImpl[]{guard2, creeper});

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
		scn.FreepsAssignToMinions(guard1, lemenya);
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
	public void OnlyTheWinOfYourWraithMinionIsAnsweredAndItsTokenGoesHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard1, guard2, aragorn);

		var stronghold = scn.GetShadowCard("stronghold");
		var winged = scn.GetShadowCard("winged");
		var chieftain = scn.GetShadowCard("chieftain");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToSupportArea(stronghold, winged);
		scn.AddTokensToCard(winged, 2);
		assertEquals(0, scn.GetCultureTokensOn(stronghold));
		assertEquals(2, scn.GetCultureTokensOn(winged));

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, khamul, creeper);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, chieftain}, new PhysicalCardImpl[]{aragorn, khamul},
				new PhysicalCardImpl[]{guard2, creeper});

		// A [sauron] Orc wins: not answered.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// A Nazgûl who is not [wraith] wins: not answered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(0, scn.GetCultureTokensOn(stronghold));

		// A [wraith] Orc wins: answered, and the token goes on this condition alone.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stronghold));
		assertEquals(2, scn.GetCultureTokensOn(winged));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
	}

	@Test
	public void AWraithMinionThatLosesASkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var stronghold = scn.GetShadowCard("stronghold");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);

		// Aragorn, 8, beats the Hound, 5 with no wound on the Ring-bearer; the Hound lives with 1 wound of 2.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(hound));
		assertInPlay(hound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertEquals(0, scn.GetCultureTokensOn(stronghold));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityWantsThreeTokensHereAndExhaustsTheRingBearerThenDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var stronghold = scn.GetShadowCard("stronghold");
		var winged = scn.GetShadowCard("winged");
		scn.MoveCardsToSupportArea(stronghold, winged);
		scn.AddTokensToCard(stronghold, 2);
		scn.AddTokensToCard(winged, 1);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		assertInZone(Zone.SUPPORT, stronghold);
		assertEquals(2, scn.GetCultureTokensOn(stronghold));
		assertEquals(1, scn.GetCultureTokensOn(winged));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Two here and a third elsewhere: not enough.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stronghold));

		scn.AddTokensToCard(stronghold, 1);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetCultureTokensOn(stronghold));
		assertTrue(scn.ShadowActionAvailable(stronghold));
		scn.ShadowUseCardAction(stronghold);

		// Frodo, vitality 4 with 1 wound, exerts twice more; Sam, who is not the Ring-bearer, is untouched.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsExhausted(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInDiscard(stronghold);
		assertEquals(1, scn.GetCultureTokensOn(winged));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheRingBearerAlreadyExhaustedTheAbilityStillDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stronghold = scn.GetShadowCard("stronghold");
		scn.MoveCardsToSupportArea(stronghold);
		scn.AddTokensToCard(stronghold, 3);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		assertInZone(Zone.SUPPORT, stronghold);
		assertEquals(3, scn.GetCultureTokensOn(stronghold));
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(stronghold));
		scn.ShadowUseCardAction(stronghold);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInDiscard(stronghold);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
