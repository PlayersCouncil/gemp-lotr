package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flurry", "2_5");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; a skirmish ability never used
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8; a maneuver ability never used

					// Dwarven Axe: [dwarven] hand weapon, strength +2; a minion that loses to its bearer discards from its deck
					put("axe", "1_9");
					put("axe2", "1_9");
					// Hand Axe: [dwarven] hand weapon, strength +1, borne beside 1 other hand weapon; an archery ability never used
					put("handaxe", "2_10");
					put("handaxe2", "2_10");
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession that is no weapon; no number changed

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not set off by a cheat
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlurryofBlowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Flurry of Blows
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Skirmish: Make a Dwarf strength +2 (or +4 and damage +1 if bearing 2 hand weapons).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flurry");

		assertEquals("Flurry of Blows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenDwarfStrengthPlus2ForTheSkirmishWhenHeBearsOneHandWeaponThoughAnotherBearsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flurry = scn.GetFreepsCard("flurry");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var axe2 = scn.GetFreepsCard("axe2");
		var handaxe2 = scn.GetFreepsCard("handaxe2");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Gimli bears two possessions, only one of them a hand weapon; Grimir, in no skirmish, bears two hand weapons.
		scn.AttachCardsTo(gimli, axe, armor);
		scn.AttachCardsTo(grimir, axe2, handaxe2);  // lint-ok: the Hand Axe may be borne in addition to 1 other hand weapon
		scn.MoveCardsToHand(flurry);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(axe2, grimir);
		assertAttachedTo(handaxe2, grimir);
		assertInHand(flurry);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(flurry);
		assertFalse(scn.FreepsPlayAvailable(flurry));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner},
				new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(gimli);

		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(axe2, grimir);
		assertAttachedTo(handaxe2, grimir);
		// 6 + 2 for the axe; 3 + 2 + 1 for the two axes
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(flurry));

		scn.FreepsPlayCard(flurry);
		// A Dwarf companion in the skirmish and a Dwarf ally in none; not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertInDiscard(flurry);
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(grimir));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The Dwarven Axe's required trigger on the Runner's loss comes with the skirmish's damage.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInDiscard(runner);

		// Gone in the next skirmish, a phase of its own.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ADwarfBearingTwoHandWeaponsIsMadeStrengthPlus4AndDamagePlus1ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flurry = scn.GetFreepsCard("flurry");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var handaxe = scn.GetFreepsCard("handaxe");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, axe, handaxe);  // lint-ok: the Hand Axe may be borne in addition to 1 other hand weapon
		scn.MoveCardsToHand(flurry);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		assertAttachedTo(handaxe, gimli);
		assertInHand(flurry);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner},
				new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(gimli);

		assertAttachedTo(axe, gimli);
		assertAttachedTo(handaxe, gimli);
		// 6 + 2 + 1
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(grimir));

		scn.FreepsPlayCard(flurry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		scn.FreepsChooseCard(gimli);

		// No choice of the smaller bonus is asked: the parenthesis is taken where it can be (F-0047).
		assertInDiscard(flurry);
		assertEquals(13, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The Dwarven Axe's required trigger on the Runner's loss comes with the skirmish's damage.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInDiscard(runner);

		// Both the strength and the damage bonus are gone in the next skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarfInPlayItIsStillPlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flurry = scn.GetFreepsCard("flurry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(flurry);

		scn.StartGame();
		assertInHand(flurry);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertInHand(flurry);
		assertTrue(scn.FreepsPlayAvailable(flurry));

		scn.FreepsPlayCard(flurry);
		assertInDiscard(flurry);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
