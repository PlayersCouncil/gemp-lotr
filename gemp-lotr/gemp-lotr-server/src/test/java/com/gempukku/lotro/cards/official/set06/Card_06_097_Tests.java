package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alive", "6_97");
					// Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					put("guard1", "5_83");
					put("guard2", "5_83");
					// Rohirrim Herdsman: [rohan] Man ally, home 3T; "To play, spot 2 [rohan] Men", so he is cheated in;
					// a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Boromir, Son of Denethor: a [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("boromir", "1_97");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3, and no Man unless 3 [rohan]
					// companions or Eowyn can be spotted; his trigger answers a [rohan] Man exerting, and his skirmish
					// ability is used only on purpose
					put("merry", "103_85");
					// Doom Drove Them: another [rohan] support-area condition, which gives itself [rohan] tokens; its
					// abilities are used only on purpose
					put("doom1", "8_86");
					put("doom2", "8_86");
					// Goblin Runner: [moria] Orc, strength 5, vitality 1, so always exhausted; its play trigger is not
					// reached by a cheat
					put("runner1", "1_178");
					put("runner2", "1_178");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alive", "6_97");
					// Rohirrim Herdsman: [rohan] Man ally, strength 4, vitality 2, home 3T, so he may be assigned at
					// site 3 of this path
					put("herdsman", "7_247");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeLeftNoneAliveStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: We Left None Alive
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area. When you play this condition, you may spot a [rohan] Man to place 2 [rohan] tokens here.<br><b>Skirmish:</b> Discard this condition or remove a [rohan] token from here to make a [rohan] Man skirmishing an exhausted minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alive");

		assertEquals("We Left None Alive", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedBesideARohanManCompanionMayPlaceTwoTokensHereAndAnotherCardPlayedLaterSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var guard1 = scn.GetFreepsCard("guard1");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		scn.MoveCompanionsToTable(guard1);
		scn.MoveCardsToSupportArea(doom1);
		scn.MoveCardsToHand(alive, doom2);

		scn.StartGame();
		scn.AddTokensToCard(doom1, 1);

		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertInZone(Zone.HAND, alive);

		scn.FreepsPlayCard(alive);
		assertInZone(Zone.SUPPORT, alive);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alive));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(alive));
		// "Here": the other [rohan] condition gets none.
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another card played once this one is in play is not "this condition".
		scn.FreepsPlayCard(doom2);
		assertInZone(Zone.SUPPORT, doom2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManAllyIsEnoughToSpotAndDecliningPlacesNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(alive);

		scn.StartGame();
		scn.FreepsPlayCard(alive);

		// The ally is the only [rohan] Man there is.
		assertInZone(Zone.SUPPORT, alive);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alive));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(alive));
		assertInZone(Zone.SUPPORT, alive);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyAGondorManAndARohanHobbitToSpotNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(boromir, merry);
		scn.MoveCardsToHand(alive);

		scn.StartGame();
		scn.FreepsPlayCard(alive);

		// Boromir is a Man of another culture; Merry, beside one [rohan] companion (himself) and no Eowyn, is a
		// [rohan] Hobbit and no Man.
		assertInZone(Zone.SUPPORT, alive);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(alive));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenHereToMakeTheRohanManSkirmishingAnExhaustedMinionStrongerForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var doom1 = scn.GetFreepsCard("doom1");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(alive, doom1);

		scn.StartGame();
		scn.AddTokensToCard(alive, 2);
		scn.AddTokensToCard(doom1, 1);
		assertInZone(Zone.SUPPORT, alive);
		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, runner1},
				new PhysicalCardImpl[]{guard2, runner2}
		);
		scn.FreepsResolveSkirmish(guard1);

		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(alive));

		scn.FreepsUseCardAction(alive);
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token from here"));
		scn.FreepsChooseOption("token from here");

		// The first Guard is the one [rohan] Man skirmishing, and is made strength +2 unasked; the second, assigned
		// to the other exhausted Runner, is not skirmishing yet.
		assertEquals(1, scn.GetCultureTokensOn(alive));
		assertInZone(Zone.SUPPORT, alive);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(8, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard2);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(6, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(alive);
		scn.FreepsChooseOption("token from here");
		assertEquals(0, scn.GetCultureTokensOn(alive));
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheConditionIsDiscardedUnaskedToMakeAnAllySkirmishingAnExhaustedMinionStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var alive = scn.GetFreepsCard("alive");
		var herdsman = scn.GetFreepsCard("herdsman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, alive);
		// The fellowship moves to site 3, the Herdsman's home.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(herdsman, savage);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetCultureTokensOn(alive));
		assertEquals(4, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(alive));

		scn.FreepsUseCardAction(alive);
		// Only the discard can be paid, and nothing is asked.
		assertInZone(Zone.DISCARD, alive);
		assertEquals(6, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDoesNothingForAGondorManOrARohanHobbitSkirmishingAnExhaustedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(boromir, merry);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddTokensToCard(alive, 2);
		assertEquals(2, scn.GetCultureTokensOn(alive));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, runner1},
				new PhysicalCardImpl[]{merry, runner2}
		);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(alive));

		// A Man of another culture: the cost is paid for nothing.
		scn.FreepsUseCardAction(alive);
		scn.FreepsChooseOption("token from here");
		assertEquals(1, scn.GetCultureTokensOn(alive));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(alive));

		// A [rohan] Hobbit who is no Man: the same.
		scn.FreepsUseCardAction(alive);
		scn.FreepsChooseOption("token from here");
		assertEquals(0, scn.GetCultureTokensOn(alive));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityDoesNothingForARohanManSkirmishingAMinionThatIsNotExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var guard1 = scn.GetFreepsCard("guard1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard1);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddTokensToCard(alive, 2);
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 1 wound: 2 left, one wound short of exhausted.
		scn.AddWoundsToChar(savage, 1);
		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard1, savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertEquals(6, scn.GetStrength(guard1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(alive));

		scn.FreepsUseCardAction(alive);
		scn.FreepsChooseOption("token from here");
		assertEquals(1, scn.GetCultureTokensOn(alive));
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alive = scn.GetFreepsCard("alive");
		var guard1 = scn.GetFreepsCard("guard1");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard1);
		scn.MoveCardsToSupportArea(alive);

		scn.StartGame();
		scn.AddTokensToCard(alive, 2);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetCultureTokensOn(alive));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(alive));
	}
}
