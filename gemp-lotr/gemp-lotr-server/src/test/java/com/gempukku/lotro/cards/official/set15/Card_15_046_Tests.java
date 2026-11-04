package com.gempukku.lotro.cards.official.set15;

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

public class Card_15_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fish", "15_46");
					put("fish2", "15_46");
					// Sméagol, Poor Creature: [gollum] companion, Ring-bound, strength 3, vitality 4; to play, add a burden
					// (cheated in); his skirmish ability is not used
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol, strength 3 (cheated in); an assignment ability only
					put("shiresmeagol", "103_116");
					// Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; his abilities are used only on purpose
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: an unbound companion
					put("merry", "1_303");
					// Bilbo, Retired Adventurer: a Hobbit ally, and so not Ring-bound; his ability is not used
					put("bilboally", "1_284");
					// White Hand Destroyer: fierce, strength 10, vitality 3 (hunter only for sites controlled)
					put("destroyer", "17_124");
					// Goblin Runner: strength 5, vitality 1 (its (2) is not met: cheated in)
					put("runner", "1_178");
					// Uruk Savage: strength 5, vitality 3, damage +1
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NiceFishStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Nice Fish
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time Sméagol wins a skirmish, place a [gollum] token here.<br><b>Regroup:</b> Heal a Ring-bound companion for each token here. Discard this condition.
		*/

		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");

		assertEquals("Nice Fish", fish.getBlueprint().getTitle());
		assertNull(fish.getBlueprint().getSubtitle());
		assertFalse(fish.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, fish.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, fish.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, fish.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(fish, Keyword.SUPPORT_AREA));
		assertEquals(1, fish.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeSmeagolWinsASkirmishEachCopyPlacesAGollumTokenOnItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var fish2 = scn.GetFreepsCard("fish2");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(fish, fish2);

		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish, fish2);
		scn.MoveMinionsToTable(destroyer);
		// Strength by hand, so that Sméagol beats the Destroyer (10) twice without killing him (vitality 3).
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 8));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, destroyer);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertEquals(0, scn.GetCultureTokensOn(fish2));
		assertEquals(11, scn.GetStrength(smeagol));

		scn.PassSkirmishActions();
		// The token and the wound for losing come at once; the order is the Free Peoples player's.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(destroyer));
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetCultureTokensOn(fish2));

		// The Destroyer is fierce: a second skirmish, and a second win.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(smeagol, destroyer);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(destroyer));
		assertEquals(2, scn.GetCultureTokensOn(fish));
		assertEquals(2, scn.GetCultureTokensOn(fish2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShireSmeagolWinningPlacesATokenToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(fish);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish);
		scn.MoveMinionsToTable(runner);
		// Strength by hand, so that Sméagol (3) beats the Runner (5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, runner);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(0, scn.GetCultureTokensOn(fish));

		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInDiscard(runner);
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotSmeagolWinningASkirmishPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToSupportArea(fish);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish);
		scn.MoveMinionsToTable(runner);
		// Strength by hand, so that Sam (3) beats the Runner (5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetCultureTokensOn(fish));

		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SmeagolLosingASkirmishPlacesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(fish);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish);
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(0, scn.GetCultureTokensOn(fish));

		// The Savage (5, damage +1) beats Sméagol (3): 2 wounds, and he lives with vitality 4.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityHealsARingBoundCompanionForEachTokenOneOfThemTwiceAndDiscardsThisCopyOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var fish2 = scn.GetFreepsCard("fish2");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var bilboally = scn.GetFreepsCard("bilboally");
		scn.MoveCompanionsToTable(smeagol, sam, merry);
		scn.MoveCardsToSupportArea(fish, fish2, bilboally);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish, fish2, bilboally);
		scn.AddTokensToCard(fish, 2);
		scn.AddTokensToCard(fish2, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.AddWoundsToChar(merry, 1);
		scn.AddWoundsToChar(bilboally, 1);
		assertEquals(2, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetCultureTokensOn(fish2));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(bilboally));

		// No minions: on to the regroup phase at site 2, which is no sanctuary.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(fish));
		scn.FreepsUseCardAction(fish);

		// Ring-bound companions: Frodo, Sam and Sméagol. Merry is unbound, Bilbo an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, bilboally));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		// The second heal may go to the same companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, smeagol));
		scn.FreepsChooseCard(sam);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(bilboally));

		assertInDiscard(fish);
		assertInZone(Zone.SUPPORT, fish2);
		assertEquals(1, scn.GetCultureTokensOn(fish2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneTokenItHealsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(fish);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish);
		scn.AddTokensToCard(fish, 1);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.SkipToPhase(Phase.REGROUP);
		// Sam is the one wounded companion: healed unasked.
		scn.FreepsUseCardAction(fish);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(fish);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoTokenItIsStillUsedAndOnlyDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(fish);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(fish));
		scn.FreepsUseCardAction(fish);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInDiscard(fish);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fish = scn.GetFreepsCard("fish");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(fish);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fish);
		scn.AddTokensToCard(fish, 1);
		scn.AddWoundsToChar(sam, 1);
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fish));
	}
}
