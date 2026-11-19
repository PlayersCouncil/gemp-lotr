package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "13_59");
					put("faramir", "12_47");    // Faramir, Dúnadan of Gondor: unbound [gondor] Man, resistance 7; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: unbound [gondor] Man, resistance 6, no text but Knight
					put("scout", "11_18");      // Elven Scout: unbound [elven] Elf, resistance 7; muster only while another Elf is spotted
					put("sam", "13_156");       // Sam, Bearer of Great Need: Ring-bound Hobbit, resistance 5 +1 per Hobbit spotted, so 7 beside Frodo
					// [gondor] support-area conditions that carry [gondor] tokens
					put("leaders", "7_112");    // Noble Leaders: a [gondor] token for each Aragorn, Boromir, Denethor or Faramir spotted as it is played
					put("heirs", "13_69");      // Heirs of Gondor: skirmish ability only
					put("garrison", "6_52");    // Garrison of Osgiliath: skirmish ability only
					put("path", "8_42");        // A Path Appointed: a [gondor] condition that holds tokens; here it holds none
					put("threatening", "13_103"); // Always Threatening: an [orc] condition that holds [orc] tokens
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("rider", "12_161");     // Black Rider: fierce [wraith] Nazgûl, strength 8, +1 for each companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Aragorn, Isildur's Heir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: Aragorn is strength +1 for each [gondor] card that has a culture token on it.<br>At the start of each assignment phase, you may exert Aragorn to make an unbound companion that has resistance 7 or more <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", aragorn.getBlueprint().getTitle());
		assertEquals("Isildur's Heir", aragorn.getBlueprint().getSubtitle());
		assertTrue(aragorn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, aragorn.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, aragorn.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, aragorn.getBlueprint().getCardType());
		assertEquals(Race.MAN, aragorn.getBlueprint().getRace());
		assertEquals(4, aragorn.getBlueprint().getTwilightCost());
		assertEquals(8, aragorn.getBlueprint().getStrength());
		assertEquals(4, aragorn.getBlueprint().getVitality());
		assertEquals(8, aragorn.getBlueprint().getResistance());
	}

	@Test
	public void AragornIsStrengthPlusOneOnceNobleLeadersIsPlayedWithAGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(leaders);

		scn.StartGame();
		assertInZone(Zone.HAND, leaders);
		assertEquals(8, scn.GetStrength(aragorn));

		// Noble Leaders spots Aragorn as it is played: one [gondor] token on it.
		scn.FreepsPlayCard(leaders);
		assertEquals(1, scn.GetCultureTokensOn(leaders));
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornCountsEachGondorCardWithATokenOnceAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var heirs = scn.GetFreepsCard("heirs");
		var garrison = scn.GetFreepsCard("garrison");
		var path = scn.GetFreepsCard("path");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCompanionsToTable(aragorn, faramir);
		scn.MoveCardsToSupportArea(heirs, garrison, path);
		scn.MoveCardsToSupportArea(threatening);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(threatening));
		// [gondor] cards with no token count for nothing.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(faramir));

		scn.AddTokensToCard(heirs, 1);
		assertEquals(9, scn.GetStrength(aragorn));

		// Garrison's two tokens make it one more card, not two; Always Threatening's [orc] tokens are on a card of
		// another culture, and A Path Appointed still has none.
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(threatening, 2);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(10, scn.GetStrength(aragorn));
		// The bonus is Aragorn's alone.
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheAssignmentPhaseHeExertsToMakeAnUnboundCompanionOfResistance7DefenderUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		var scout = scn.GetFreepsCard("scout");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, faramir, knight, scout, sam);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveMinionsToTable(runner1, runner2);

		// Not at the start of the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(faramir));
		assertEquals(6, scn.GetResistance(knight));
		assertEquals(7, scn.GetResistance(scout));
		// 5 + 1 for each of the two Hobbits, Frodo and Sam.
		assertEquals(7, scn.GetResistance(sam));
		assertEquals(10, scn.GetResistance(frodo));
		assertFalse(scn.HasKeyword(faramir, Keyword.DEFENDER));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Unbound and resistance 7 or more, of any culture and race: Aragorn himself (8), Faramir (7) and the Elf
		// Scout (7).  Not the Knight (6), or Frodo (10) or Sam (7), who are Ring-bound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, faramir, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo, sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(faramir);
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(knight, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{knight, runner2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		// In the second skirmish Faramir is still defender +1.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));

		scn.PassSkirmishActions();
		// Gone at the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(faramir, Keyword.DEFENDER));
	}

	@Test
	public void DeclinedNothingIsExertedAndNobodyIsDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);

		scn.StartGame();
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(faramir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);

		scn.StartGame();
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(faramir, Keyword.DEFENDER));
	}

	@Test
	public void WithNoCompanionOfResistance7TheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);

		scn.StartGame();
		// Two burdens: Aragorn 8 - 2 = 6, Faramir 7 - 2 = 5.
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveMinionsToTable(runner1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(faramir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFierceAssignmentPhaseOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);

		scn.StartGame();
		var rider = scn.GetShadowCard("rider");
		scn.MoveMinionsToTable(rider);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(faramir);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Aragorn 8 loses to the Rider, 8 + 3 for the companions, and takes a wound; the Rider is fierce.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The fierce assignment phase is an assignment phase.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		// Faramir's from the first assignment phase lasts until the regroup phase.
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
