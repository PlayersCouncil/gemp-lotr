package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanguard", "10_63");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, the other minion that stays unassigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability only
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT hand weapon on a [gondor] Man; its response is not used
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability only
					put("axe", "1_9");          // Dwarven Axe: a POSSESSION hand weapon on a Dwarf; its trigger needs a skirmish
					put("sting", "1_313");      // Sting: a hand weapon that Frodo, Ring-bound, bears; its ability is not used
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, unbound; +3 only against a Nazgul
					put("bow", "1_41");         // Elven Bow: a RANGED weapon on an Elf (makes her an archer: laid after the archery phase)
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, unbound; a skirmish ability only
					put("armor", "1_92");       // Armor: a possession on a Man that is no weapon
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a Hobbit ALLY; a fellowship ability only
					put("sword", "1_299");      // Hobbit Sword: a hand weapon on a Hobbit, here the ally; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulVanguardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Morgul Vanguard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Assignment:</b> Assign this minion to an unbound companion bearing a hand weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vanguard");

		assertEquals("Morgul Vanguard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilityAssignsItToAnUnboundCompanionBearingAHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var sting = scn.GetFreepsCard("sting");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, gimli, arwen, boromir);
		scn.MoveAlliesToTable(bilbo);

		var vanguard = scn.GetShadowCard("vanguard");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(boromir, armor);
		scn.AttachCardsTo(bilbo, sword);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(sting, frodo);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(sword, bilbo);
		scn.MoveMinionsToTable(vanguard, troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The bow would make Arwen an archer, so it goes on once the archery phase is over.
		scn.AttachCardsTo(arwen, bow);
		assertAttachedTo(bow, arwen);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(vanguard));
		assertTrue(scn.ShadowActionAvailable(vanguard));

		scn.ShadowUseCardAction(vanguard);
		// An artifact hand weapon and a possession one, on companions of two cultures and races.  Not Frodo, who is
		// Ring-bound; not Arwen, whose weapon is ranged; not Boromir, whose item is no weapon; not Bilbo, an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, arwen, boromir, bilbo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// This minion, and not the other one.
		assertTrue(scn.IsCharAssignedAgainst(gimli, vanguard));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedUntilAnUnboundCompanionBearsAHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var sting = scn.GetFreepsCard("sting");
		var arwen = scn.GetFreepsCard("arwen");
		var bow = scn.GetFreepsCard("bow");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveAlliesToTable(bilbo);

		var vanguard = scn.GetShadowCard("vanguard");

		scn.StartGame();
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(boromir, armor);
		scn.AttachCardsTo(bilbo, sword);
		assertAttachedTo(sting, frodo);
		assertAttachedTo(armor, boromir);
		assertAttachedTo(sword, bilbo);
		scn.MoveMinionsToTable(vanguard);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.AttachCardsTo(arwen, bow);
		assertAttachedTo(bow, arwen);
		scn.FreepsPass();

		// A hand weapon on a Ring-bound companion and on an ally, a ranged weapon, an item that is no weapon, and
		// Aragorn bearing nothing: nobody the ability could assign it to.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(vanguard));

		scn.AttachCardsTo(aragorn, narsil);
		assertAttachedTo(narsil, aragorn);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(vanguard));
	}

	@Test
	public void OnceItIsAssignedItIsNotOfferedAgainThoughAnotherSuchCompanionIsUnassigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var vanguard = scn.GetShadowCard("vanguard");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(gimli, axe);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(axe, gimli);
		scn.MoveMinionsToTable(vanguard);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(vanguard));
		scn.ShadowUseCardAction(vanguard);
		scn.ShadowChooseCard(gimli);
		assertTrue(scn.IsCharAssignedAgainst(gimli, vanguard));
		assertFalse(scn.IsCharAssigned(aragorn));

		// An assigned minion cannot be assigned again (CR 4.2 "assignment phase"): not offered, though Aragorn
		// still bears Narsil and is unassigned.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(vanguard));
	}
}
