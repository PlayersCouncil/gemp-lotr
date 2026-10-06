package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guthwine", "102_49");

					put("eomer", "12_112");     // Eomer, Eored Leader: strength 7, vitality 3; a maneuver ability used only on purpose
					put("northman", "17_95");   // Eomer, Northman: strength 7, damage +1 in print
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6; Valiant is its whole text
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] companion with no text in play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man
					put("herdsman", "7_247");   // Rohirrim Herdsman: a [rohan] ally

					// Cards that bear culture tokens; none of them does anything unless used on purpose.
					put("doom", "8_86");        // Doom Drove Them: [rohan] condition
					put("freely", "13_127");    // Freely Across Our Land: [rohan] condition
					put("nonealive", "6_97");   // We Left None Alive: [rohan] condition, given no token here
					put("path", "8_42");        // A Path Appointed: [gondor] condition bearing [gondor] tokens

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuthwineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Gúthwinë, Eomer's Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [rohan] companion.
		 * 	If bearer is Eomer, he is damage +1 and each time he wins a skirmish, you may reinforce a [rohan] token.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guthwine");

		assertEquals("Gúthwinë", card.getBlueprint().getTitle());
		assertEquals("Eomer's Blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedForTwoOnEomerItMakesHimStrengthPlusTwoAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(guthwine);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(eomer));
		assertFalse(scn.HasKeyword(eomer, Keyword.DAMAGE));

		// Eomer is the only [rohan] companion, so it goes to him.
		scn.FreepsPlayCard(guthwine);

		assertEquals(2, scn.GetTwilight());
		assertAttachedTo(guthwine, eomer);
		assertEquals(9, scn.GetStrength(eomer));
		assertTrue(scn.HasKeyword(eomer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerMustBeARohanCompanionAndOnOneWhoIsNotEomerItGivesNoDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var dwarf = scn.GetFreepsCard("dwarf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(eomer, guard, dwarf, aragorn);
		scn.MoveAlliesToTable(herdsman);
		// A [rohan] companion who is no Man: every [rohan] companion in print is one.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, dwarf, null, Culture.ROHAN));
		scn.MoveCardsToHand(guthwine);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));

		scn.FreepsPlayCard(guthwine);

		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guard, dwarf));
		// A Man of another culture, a [rohan] ally, and the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, herdsman, frodo));

		scn.FreepsChooseCard(guard);

		assertAttachedTo(guthwine, guard);
		assertEquals(8, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		// Nor is Eomer damage +1 while another bears it.
		assertFalse(scn.HasKeyword(eomer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeEomerWinsASkirmishARohanTokenMayBeReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var doom = scn.GetFreepsCard("doom");
		var freely = scn.GetFreepsCard("freely");
		var nonealive = scn.GetFreepsCard("nonealive");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, guthwine);
		scn.MoveCardsToSupportArea(doom, freely, nonealive, path);
		// Two [rohan] cards with a [rohan] token each, a [rohan] card with none, and a [gondor] token.
		scn.AddTokensToCard(doom, 1);
		scn.AddTokensToCard(freely, 1);
		scn.AddTokensToCard(path, 1);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Fierce, so that Eomer fights it twice in one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();

		// 7 + 2 against 5: Eomer wins without overwhelming.
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsAssignAndResolve(eomer, savage);
		scn.PassSkirmishActions();

		// 1 wound for losing + 1 for Eomer's damage +1; the Savage has vitality 3.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guthwine));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, freely));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nonealive, path));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(doom);

		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertEquals(0, scn.GetCultureTokensOn(nonealive));
		assertEquals(1, scn.GetCultureTokensOn(path));

		// The fierce skirmish: he wins again, and may reinforce again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(eomer, savage);
		scn.PassFierceSkirmishActions();

		assertInDiscard(savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guthwine));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, freely));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nonealive, path));
		scn.FreepsChooseCard(freely);

		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(2, scn.GetCultureTokensOn(freely));
		assertEquals(0, scn.GetCultureTokensOn(nonealive));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnAnEomerWhoIsDamagePlusOneInPrintTheBonusesAddAndTheReinforcementMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var northman = scn.GetFreepsCard("northman");
		var doom = scn.GetFreepsCard("doom");
		var freely = scn.GetFreepsCard("freely");
		scn.MoveCompanionsToTable(northman);
		scn.MoveCardsToHand(guthwine);
		scn.MoveCardsToSupportArea(doom, freely);
		scn.AddTokensToCard(doom, 1);
		scn.AddTokensToCard(freely, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();

		assertEquals(1, scn.GetKeywordCount(northman, Keyword.DAMAGE));
		scn.FreepsPlayCard(guthwine);
		assertAttachedTo(guthwine, northman);
		assertEquals(2, scn.GetKeywordCount(northman, Keyword.DAMAGE));

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		// 7 + 2 against 5
		scn.FreepsAssignAndResolve(northman, runner);
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guthwine));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnotherRohanCompanionBearingItWinsNorWhenEomerWinsWithoutIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(guard, guthwine);
		scn.MoveCardsToSupportArea(doom);
		scn.AddTokensToCard(doom, 1);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{eomer, runner2});

		// The Household Guard, bearing it: 6 + 2 against 5.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(doom));

		// Eomer, without it: 7 against 5.
		scn.FreepsResolveSkirmish(eomer);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenEomerLosesNorWhenAnotherCompanionWinsBesideHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.AttachCardsTo(eomer, guthwine);
		scn.MoveCardsToSupportArea(doom);
		scn.AddTokensToCard(doom, 1);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{eomer, troop});

		// The Household Guard wins: 6 against 5.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(doom));

		// Eomer loses: 7 + 2 against 13.
		assertEquals(0, scn.GetWoundsOn(eomer));
		scn.FreepsResolveSkirmish(eomer);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(eomer));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenThereIsNoRohanTokenToReinforce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guthwine = scn.GetFreepsCard("guthwine");
		var eomer = scn.GetFreepsCard("eomer");
		var nonealive = scn.GetFreepsCard("nonealive");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, guthwine);
		// A [rohan] card that could bear tokens and has none, and a token of another culture.
		scn.MoveCardsToSupportArea(nonealive, path);
		scn.AddTokensToCard(path, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		scn.FreepsAssignAndResolve(eomer, runner);
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(guthwine));
		assertEquals(0, scn.GetCultureTokensOn(nonealive));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
