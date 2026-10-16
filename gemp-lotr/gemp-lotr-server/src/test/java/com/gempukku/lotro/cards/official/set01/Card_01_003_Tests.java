package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike", "1_3");
					put("strike2", "1_3");

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; his skirmish ability is used only on purpose
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8; a maneuver ability only

					// Dwarven Axe: [dwarven] hand weapon, strength +2, bearer a Dwarf.  Its trigger answers a minion
					// losing to bearer, and its bearer here fights nobody.
					put("axe", "1_9");
					// Hand Axe: [dwarven] hand weapon, strength +1, bearer a Dwarf; an archery ability only.
					put("handaxe", "2_10");
					// Dwarven Armor: [dwarven] armor, bearer a Dwarf; "may not be overwhelmed unless his strength is tripled".
					put("armor", "1_8");
					// Iron-forged Weapon (Hobbit Draft Game): [esgaroth] hand weapon, strength +2, bearer a Man or a
					// [dwarven] companion.  The only hand weapon of another culture a Dwarf can bear.  Its skirmish
					// ability is used only on purpose.
					put("ironweapon", "32_18");

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1
					put("soldier1", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2; his skirmish ability is used only on purpose
					put("soldier2", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AxeStrikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Axe Strike
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Dwarf strength +2 (or +3 if bearing a [dwarven] hand weapon).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strike");

		assertEquals("Axe Strike", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CanBePlayedInASkirmishAndNotInTheAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(strike);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		assertInHand(strike);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(strike));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(strike));

		// Gimli, 6 with damage +1, beats the Savage, who survives with 2 wounds of 3.
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(strike);
		assertFalse(scn.FreepsPlayAvailable(strike));
	}

	@Test
	public void ADwarfWithNoDwarvenHandWeaponIsStrengthPlus2ForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var frodo = scn.GetRingBearer();
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCardsToHand(strike);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// Beside the Dwarf chosen, another Dwarf bears a [dwarven] hand weapon.
		scn.AttachCardsTo(grimir, axe);

		scn.StartGame();
		assertAttachedTo(axe, grimir);
		assertInHand(strike);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(guard, soldier1));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharAssignedAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		// 3 + 2 for the Dwarven Axe
		assertEquals(5, scn.GetStrength(grimir));

		scn.FreepsPlayCard(strike);
		// The Dwarf in the skirmish, the Dwarf waiting for his own, and the Dwarf ally in the support area;
		// not Aragorn or Frodo, companions who are no Dwarves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// Gimli bears no hand weapon: +2, not +3.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(grimir));
		assertInDiscard(strike);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: Gimli is still strengthened.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));

		// The Guard, 4, loses to the Soldier, 7.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(gimli);

		// A new skirmish phase: the bonus went with the last one.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier2));
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void ADwarfBearingADwarvenHandWeaponIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var handaxe = scn.GetFreepsCard("handaxe");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(strike);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, handaxe);

		scn.StartGame();
		assertAttachedTo(handaxe, guard);
		assertInHand(strike);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 4 + 1 for the Hand Axe
		assertEquals(5, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(strike);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		scn.FreepsChooseCard(guard);

		assertEquals(8, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(strike);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ADwarfBearingAHandWeaponOfAnotherCultureOrDwarvenArmorIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var strike2 = scn.GetFreepsCard("strike2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var ironweapon = scn.GetFreepsCard("ironweapon");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(strike, strike2);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, ironweapon);
		scn.AttachCardsTo(guard, armor);
		// The Dwarf who meets the condition, beside the two who do not.
		scn.AttachCardsTo(grimir, axe);

		scn.StartGame();
		assertAttachedTo(ironweapon, gimli);
		assertAttachedTo(armor, guard);
		assertAttachedTo(axe, grimir);
		assertInHand(strike);
		assertInHand(strike2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 6 + 2 for the Iron-forged Weapon
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(grimir));

		scn.FreepsPlayCard(strike);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		scn.FreepsChooseCard(gimli);
		// A hand weapon, but of another culture: +2.
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(grimir));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPlayCard(strike2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli, grimir));
		scn.FreepsChooseCard(guard);
		// A [dwarven] possession, but no hand weapon: +2.
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(grimir));
		assertInDiscard(strike);
		assertInDiscard(strike2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void MayBePlayedWithNoDwarfInPlayAndThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier1 = scn.GetShadowCard("soldier1");
		scn.MoveCardsToHand(strike);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInHand(strike);
		scn.MoveMinionsToTable(soldier1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier1);

		assertEquals(8, scn.GetStrength(aragorn));
		// Nothing in the text is a cost or a requirement, so the event can be played for nothing.
		assertTrue(scn.FreepsPlayAvailable(strike));

		scn.FreepsPlayCard(strike);
		assertInDiscard(strike);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
