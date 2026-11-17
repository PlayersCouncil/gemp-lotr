package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_201_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinel", "11_201");
					put("sentinel2", "11_201");
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] Wizard minion, strength 8, vitality 4;
					                            // each companion is resistance -1; his skirmish ability is used only on purpose
					put("feral", "11_183");     // Feral Uruk: an [uruk-hai] Uruk-hai, strength 9, vitality 2; its text is only at a
					                            // battleground, and no site of this path is one
					put("cuguard", "10_81");    // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, vitality 2, damage +1 and no other text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; his ability is used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("longifell", "4_97");   // Long I Fell: Skirmish: spot Gandalf to prevent all wounds to him; the Shadow player may make you wound a minion to prevent this
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentinelUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sentinel Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Response:</b> If an [uruk-hai] minion is about to take a wound in a skirmish, exert or discard this minion to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentinel");

		assertEquals("Sentinel Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertsOrDiscardsThisCopyToPreventTheWoundOfAnUrukhaiMinionAmongTheLosersOfASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var sentinel2 = scn.GetShadowCard("sentinel2");
		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var cuguard = scn.GetShadowCard("cuguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(sentinel, sentinel2, saruman, feral, cuguard);
		// Aragorn, 8 + 30, beats the three together, 8 + 9 + 9 = 26, and does not double them: all three lose
		// and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 30));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);
		scn.ShadowAssignToMinions(aragorn, feral, cuguard);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(38, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel2));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(feral));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		scn.ShadowChooseOptionalTrigger(sentinel);
		// Both costs can be paid: the Shadow player is asked which.
		assertEquals(List.of("Exert this minion", "Discard this minion"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert this minion");
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel2));
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel2);

		// The wound to be prevented is an [uruk-hai] minion's, of either race, and not the [sauron] Uruk-hai's; the
		// second copy, an [uruk-hai] minion with no wound coming, is not offered either.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, feral));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cuguard, sentinel2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		// The Uruk's wound is still to come: offered again, and this time this copy is discarded to prevent it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		scn.ShadowChooseOptionalTrigger(sentinel);
		scn.ShadowChoose("Discard this minion");
		assertInDiscard(sentinel);
		assertEquals(0, scn.GetWoundsOn(sentinel2));
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel2);

		// Feral Uruk's was the one [uruk-hai] wound left: prevented unasked.  Nothing left to prevent: the second
		// copy is not offered for the Guard's wound.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sentinel2));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(feral));
		assertEquals(1, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(sentinel2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnswersTheWoundToItselfAndIsDiscardedToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(sentinel);
		// Aragorn, 8 + 5, beats the Sentinel's 8 and does not double it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentinel);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(sentinel));
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel);
		scn.PassSkirmishActions();

		// "An [uruk-hai] minion" takes in this one: it is offered for its own wound.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		scn.ShadowChooseOptionalTrigger(sentinel);
		assertEquals(List.of("Exert this minion", "Discard this minion"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Discard this minion");

		// Discarded, not killed, and the wound it was to take is prevented: it would otherwise have stayed in play.
		assertInDiscard(sentinel);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnswersAWoundToAnUrukhaiMinionWaitingForItsSkirmishDuringAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var feral = scn.GetShadowCard("feral");
		var cuguard = scn.GetShadowCard("cuguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var longifell = scn.GetFreepsCard("longifell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(longifell);

		scn.StartGame();
		scn.MoveMinionsToTable(sentinel, feral, cuguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, cuguard}, new PhysicalCardImpl[]{aragorn, feral});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		// Feral Uruk waits for his skirmish with Aragorn.
		assertTrue(scn.IsCharAssigned(feral));
		assertEquals(0, scn.GetWoundsOn(feral));
		assertEquals(0, scn.GetWoundsOn(sentinel));
		scn.FreepsPlayCard(longifell);
		// The Shadow player has the Free Peoples player wound a minion, to prevent the spell.
		scn.ShadowChooseYes();
		scn.FreepsChooseCard(feral);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		scn.ShadowChooseOptionalTrigger(sentinel);
		scn.ShadowChoose("Exert this minion");
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(feral));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASauronUrukhaiLosingAloneIsNotAnsweredAndAnUrukhaiWizardLosingAloneIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var saruman = scn.GetShadowCard("saruman");
		var cuguard = scn.GetShadowCard("cuguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(sentinel, saruman, cuguard);
		// Aragorn, 8 + 5, beats the Guard's 9, and Gandalf, 6 + 2 (his own text, for the Hobbit and the Man beside
		// him) + 1, Saruman's 8; neither doubles his minion.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, cuguard}, new PhysicalCardImpl[]{gandalf, saruman});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(cuguard));
		assertEquals(0, scn.GetWoundsOn(sentinel));
		scn.PassSkirmishActions();

		// The [sauron] Uruk-hai's wound is not answered, with the cost payable.
		assertEquals(1, scn.GetWoundsOn(cuguard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		assertEquals(0, scn.GetWoundsOn(sentinel));

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		scn.ShadowChooseOptionalTrigger(sentinel);
		scn.ShadowChoose("Exert this minion");
		assertEquals(1, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedItIsDiscardedToPreventTheWoundWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(sentinel, feral);
		// Vitality 3 with 2 wounds: exhausted, so only the discard can be paid.
		scn.AddWoundsToChar(sentinel, 2);
		assertEquals(2, scn.GetWoundsOn(sentinel));
		// Aragorn, 8 + 5, beats Feral Uruk's 9 and does not double it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(feral));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		scn.ShadowChooseOptionalTrigger(sentinel);

		assertInDiscard(sentinel);
		assertEquals(0, scn.GetWoundsOn(feral));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnArcheryWoundIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetShadowCard("sentinel");
		var feral = scn.GetShadowCard("feral");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(sentinel, feral);
		scn.SkipToArcheryWounds();

		assertEquals(0, scn.GetWoundsOn(sentinel));
		assertEquals(0, scn.GetWoundsOn(feral));
		// The fellowship's archery wound, placed by the Shadow player.
		scn.ShadowChooseCard(feral);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(sentinel));
		assertEquals(1, scn.GetWoundsOn(feral));
		assertEquals(0, scn.GetWoundsOn(sentinel));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
