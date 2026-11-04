package com.gempukku.lotro.cards.official.set17;

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

public class Card_17_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "17_115");
					put("staff", "4_174");       // Saruman's Staff: "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup phase"

					put("butcher", "17_122");    // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2, Hunter 6 and nothing else
					put("chasing", "15_157");    // Chasing Uruk: [uruk-hai] Uruk-hai, fierce, Hunter 1, strength 10; its own win trigger wants 4 hunters spotted
					put("feral", "11_183");      // Feral Uruk: [uruk-hai] Uruk-hai who is no hunter, strength 9, vitality 2; its text only at a battleground
					put("raiders", "1_158");     // Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("grishnakh", "17_71");   // Grishnakh: an [orc] hunter minion, strength 11, Hunter 2 (3 beside an [uruk-hai] minion); his ability used only on purpose
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc, strength 5, vitality 1

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter; his ability used only on purpose
					put("gimli", "15_5");        // Gimli, Eager Hunter: [dwarven] Dwarf companion, strength 6, vitality 3, Hunter 3 and damage +1
					put("guardian", "17_7");     // Elven Guardian: [elven] companion, Hunter 1; his texts need a wounded minion in his skirmish or the maneuver phase
					put("guard", "1_7");         // Dwarf Guard: [dwarven] companion, strength 4, vitality 2, no hunter
					put("barliman", "1_70");     // Barliman Butterbur: an ally, no hunter; his ability used only on purpose
					put("found", "18_2");        // Run Until Found: "Skirmish: Discard this condition from play or remove a [dwarven] token from here to make a [dwarven] companion gain hunter 1"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Saruman, Curunír
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Each time an [uruk-hai] hunter wins a skirmish, heal a different [uruk-hai] minion.
		 * <b>Skirmish:</b> Exert Saruman twice to make a companion lose the <b>hunter</b> keyword until the start of the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Curunír", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenAnUrukHaiHunterWinsAnotherWoundedUrukHaiMinionIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var butcher = scn.GetShadowCard("butcher");
		var feral = scn.GetShadowCard("feral");
		var raiders = scn.GetShadowCard("raiders");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, butcher, feral, raiders);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddWoundsToChar(butcher, 1);
		scn.AddWoundsToChar(feral, 1);
		scn.AddWoundsToChar(raiders, 1);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(1, scn.GetWoundsOn(feral));
		assertEquals(1, scn.GetWoundsOn(raiders));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, butcher);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 5 + 6 for hunting a non-hunter: 11 against 8.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Another [uruk-hai] minion, Saruman among them; not the winner, nor the [isengard] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, feral));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(butcher, raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(feral);

		assertEquals(0, scn.GetWoundsOn(feral));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiHunterLosingASkirmishHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var butcher = scn.GetShadowCard("butcher");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, butcher);
		scn.AddWoundsToChar(saruman, 1);
		assertEquals(1, scn.GetWoundsOn(saruman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, butcher);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// 13 against 11: Aragorn wins, short of double, and the Butcher lives with 1 wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(butcher));
		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionWhoIsNoHunterAndHuntersOfOtherCulturesWinningHealNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var grishnakh = scn.GetShadowCard("grishnakh");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, guard, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral, grishnakh, runner);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddWoundsToChar(feral, 1);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(feral));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, feral}, new PhysicalCardImpl[]{guard, grishnakh},
				new PhysicalCardImpl[]{gimli, runner});
		scn.ShadowDeclineAssignments();

		// The [uruk-hai] Uruk who is no hunter beats Aragorn, 9 against 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(feral));

		// The [orc] hunter minion overwhelms the Dwarf Guard: 11 + 3 against 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(feral));

		// The hunter companion beats the Runner: 6 + 3 against 5.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(feral));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceUrukHaiHunterWinningTwiceHealsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var chasing = scn.GetShadowCard("chasing");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, chasing);
		scn.AddWoundsToChar(saruman, 2);
		assertEquals(2, scn.GetWoundsOn(saruman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chasing);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 10 + 1 against 8.  Saruman is the one other [uruk-hai] minion, and is healed unasked.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(saruman));

		// The fierce skirmish: the Uruk beats Aragorn again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, chasing);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsSarumanTwiceToMakeACompanionLoseHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var butcher = scn.GetShadowCard("butcher");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guardian = scn.GetFreepsCard("guardian");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, guardian);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, butcher, feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(guardian, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher, Keyword.HUNTER));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);

		assertEquals(2, scn.GetWoundsOn(saruman));
		// Any companion, a hunter or not, in the skirmish or not; not a hunter minion, nor an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, guardian, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(butcher, barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(gimli, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(guardian, Keyword.HUNTER));
		assertEquals(6, scn.GetKeywordCount(butcher, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenSarumanCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral);
		// Vitality 4 with 2 wounds: one exertion left in him, not two.
		scn.AddWoundsToChar(saruman, 2);
		assertEquals(2, scn.GetWoundsOn(saruman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.HUNTER));
	}

	@Test
	public void TheCompanionIsStillNoHunterInALaterSkirmishAndIsAgainAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders, feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, feral});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(saruman);
		scn.ShadowChooseCard(gimli);
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));

		// The [isengard] Uruk-hai beats Aragorn, 9 against 8; no [uruk-hai] hunter wins.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The next skirmish, a phase of its own: Gimli is still no hunter, and has no +3 against the Uruk.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(gimli));

		// 9 against 6: Gimli loses and lives; at the regroup phase he is Hunter 3 again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.HUNTER));
	}

	@Test
	public void ACompanionMadeToLoseHunterCannotGainItAgainBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var found = scn.GetFreepsCard("found");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.MoveCardsToSupportArea(found);
		scn.AddTokensToCard(found, 1);
		assertInZone(Zone.SUPPORT, found);
		assertEquals(1, scn.GetCultureTokensOn(found));

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(saruman);
		scn.ShadowChooseCard(gimli);
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));

		// The control: the token pays for hunter 1 on the Dwarf Guard, who had none.
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.HUNTER));
		scn.FreepsUseCardAction(found);
		scn.FreepsChoose("Remove");
		scn.FreepsChooseCard(guard);
		assertEquals(0, scn.GetCultureTokensOn(found));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.HUNTER));
		scn.ShadowPass();

		// The same gain for Gimli, paid with the condition itself: refused while Saruman's loss holds.
		scn.FreepsUseCardAction(found);
		scn.FreepsChooseCard(gimli);
		assertInDiscard(found);
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffAHunterWinHealsNothingAndHisSkirmishAbilityStillWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var butcher = scn.GetShadowCard("butcher");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, butcher, feral);
		scn.AttachCardsTo(saruman, staff);
		scn.AddWoundsToChar(feral, 1);
		assertAttachedTo(staff, saruman);
		assertEquals(1, scn.GetWoundsOn(feral));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, butcher);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// His second sentence still works.
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		scn.ShadowChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));

		// 11 against 8: the hunter wins, and nobody is healed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(feral));
		// The Staff makes Saruman fierce, so the fierce assignments follow.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
