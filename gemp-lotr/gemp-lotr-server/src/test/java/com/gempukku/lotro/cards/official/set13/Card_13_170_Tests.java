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

public class Card_13_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("distractor1", "13_170");
					put("distractor2", "13_170");
					put("uruk1", "11_204");     // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14; damage +1 and toil only
					put("uruk2", "11_204");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard; his skirmish ability is never used
					put("saruman", "17_116");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: his maneuver ability is never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a second companion
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally who can be made to skirmish
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession on Aragorn, damage +1
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession on a companion; minion archery total -1
					put("cloak2", "1_42");
					put("ring", "6_55");        // Ring of Barahir: a [gondor] ARTIFACT on Aragorn; a start-of-turn heal, attached after it
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession on a Dwarf
					put("toby", "1_305");       // Old Toby: a Free Peoples possession in the support area, borne by nobody
					// Spider Poison: a Shadow ([gollum]) possession a companion can bear; its bearer exerts when assigned
					put("poison", "8_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukDistractorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Distractor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Skirmish:</b> If this minion is not assigned to a skirmish, you may exert it to discard a possession borne by
		 * 	a companion skirmishing another [uruk-hai] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("distractor1");

		assertEquals("Uruk Distractor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsItToDiscardAPossessionBorneByACompanionSkirmishingAnUrukHaiMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		var cloak = scn.GetFreepsCard("cloak");
		var cloak2 = scn.GetFreepsCard("cloak2");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, sword, cloak, poison);
		scn.AttachCardsTo(gandalf, cloak2);
		scn.MoveCardsToSupportArea(toby);

		var distractor1 = scn.GetShadowCard("distractor1");
		var distractor2 = scn.GetShadowCard("distractor2");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, ring);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(ring, aragorn);
		assertAttachedTo(cloak2, gandalf);
		assertInZone(Zone.SUPPORT, toby);

		scn.MoveMinionsToTable(distractor1, distractor2, uruk1, uruk2);
		scn.SkipToAssignments();
		// Aragorn skirmishes the first Tyrannical Uruk now; Gandalf the second, later. Both Distractors stand apart.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk1},
				new PhysicalCardImpl[]{gandalf, uruk2}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(distractor1));
		assertEquals(0, scn.GetWoundsOn(distractor2));
		assertTrue(scn.ShadowActionAvailable(distractor1));
		scn.ShadowUseCardAction(distractor1);
		// It exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(distractor1));
		assertEquals(0, scn.GetWoundsOn(distractor2));

		// Possessions of three cultures, of either side, borne by Aragorn; not his artifact, nor Gandalf's cloak (his
		// skirmish has not begun), nor Old Toby, which nobody bears.
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, cloak, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, cloak2, toby));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertInDiscard(sword);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(ring, aragorn);
		assertAttachedTo(cloak2, gandalf);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ReachesACompanionSkirmishingAnUrukHaiWizardButNotOneSkirmishingASauronUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		var cloak = scn.GetFreepsCard("cloak");
		var cloak2 = scn.GetFreepsCard("cloak2");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, sword, cloak);
		scn.AttachCardsTo(gandalf, cloak2);

		var distractor1 = scn.GetShadowCard("distractor1");
		var distractor2 = scn.GetShadowCard("distractor2");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(cloak2, gandalf);

		scn.MoveMinionsToTable(distractor1, distractor2, saruman, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, saruman},
				new PhysicalCardImpl[]{gandalf, guard}
		);
		scn.ShadowDeclineAssignments();

		// Aragorn skirmishes Saruman, an [uruk-hai] minion who is no Uruk-hai.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(distractor1));
		scn.ShadowUseCardAction(distractor1);
		assertEquals(1, scn.GetWoundsOn(distractor1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, cloak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cloak2));
		scn.ShadowChooseCard(cloak);
		assertInDiscard(cloak);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Gandalf skirmishes the Cirith Ungol Guard, a [sauron] Uruk-hai: the ability is offered and paid for, and
		// nothing is discarded.
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(distractor2));
		assertTrue(scn.ShadowActionAvailable(distractor2));
		scn.ShadowUseCardAction(distractor2);
		assertEquals(1, scn.GetWoundsOn(distractor2));
		assertAttachedTo(cloak2, gandalf);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APossessionBorneByAnAllySkirmishingAnUrukHaiMinionIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, armor);

		var distractor1 = scn.GetShadowCard("distractor1");
		var uruk1 = scn.GetShadowCard("uruk1");

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, thrarin);

		scn.MoveMinionsToTable(distractor1, uruk1);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, uruk1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(distractor1));
		assertTrue(scn.ShadowActionAvailable(distractor1));
		scn.ShadowUseCardAction(distractor1);
		// Thrarin is an ally, and Aragorn is in no skirmish: the exertion is paid, and nothing is discarded.
		assertEquals(1, scn.GetWoundsOn(distractor1));
		assertAttachedTo(armor, thrarin);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileItIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, sword);

		var distractor1 = scn.GetShadowCard("distractor1");
		var distractor2 = scn.GetShadowCard("distractor2");
		var uruk1 = scn.GetShadowCard("uruk1");

		scn.StartGame();
		assertAttachedTo(sword, aragorn);

		scn.MoveMinionsToTable(distractor1, distractor2, uruk1);
		scn.SkipToAssignments();
		// The first Distractor is assigned to Gandalf, its skirmish still to come; the second to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk1},
				new PhysicalCardImpl[]{gandalf, distractor1}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(distractor1));
		assertTrue(scn.ShadowActionAvailable(distractor2));

		// In its own skirmish as well.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(distractor1));
		assertTrue(scn.ShadowActionAvailable(distractor2));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedOnceItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var distractor1 = scn.GetShadowCard("distractor1");
		var distractor2 = scn.GetShadowCard("distractor2");
		var uruk1 = scn.GetShadowCard("uruk1");

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		scn.MoveMinionsToTable(distractor1, distractor2, uruk1);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(distractor1, 1);
		assertEquals(1, scn.GetWoundsOn(distractor1));
		assertEquals(0, scn.GetWoundsOn(distractor2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(distractor1));
		assertTrue(scn.ShadowActionAvailable(distractor2));
	}
}
