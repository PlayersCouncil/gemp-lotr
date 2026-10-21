package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe1", "2_10");
					put("axe2", "2_10");
					put("axe3", "2_10");
					put("dwaxe1", "1_9");     // Dwarven Axe: a [dwarven] hand weapon for a Dwarf, with no "in addition" text
					put("dwaxe2", "1_9");
					put("gimli", "1_13");     // Gimli, Son of Gloin: Dwarf companion; his skirmish ability is used only on purpose
					put("guard1", "1_7");     // Dwarf Guard: Dwarf companion, not unique; its text is about its own play
					put("guard2", "1_7");
					put("grimir", "1_17");    // Grimir, Dwarven Elder: Dwarf ally; his fellowship ability is used only on purpose
					put("aragorn", "1_89");   // Aragorn, Ranger of the North: a companion who is no Dwarf
					put("troop", "1_177");    // Goblin Patrol Troop: a minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HandAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hand Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +1
		 * Game Text: Bearer must be a Dwarf.
		 * This weapon may be borne in addition to 1 other hand weapon.
		 * Archery: Discard this weapon to make the fellowship archery total +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe1");

		assertEquals("Hand Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBeBorneByADwarfCompanionOrAllyAndByNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe1");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(axe);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		// A Dwarf companion and a Dwarf ally; not a Man, nor the Hobbit Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(axe, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MayBeBorneBesideOneOtherHandWeaponWhicheverComesFirstButNotBesideTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var axe3 = scn.GetFreepsCard("axe3");
		var dwaxe1 = scn.GetFreepsCard("dwaxe1");
		var dwaxe2 = scn.GetFreepsCard("dwaxe2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, guard1, guard2);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, dwaxe1);
		scn.AttachCardsTo(guard1, axe2);
		scn.MoveCardsToHand(axe1, axe3, dwaxe2);

		scn.StartGame();
		assertAttachedTo(dwaxe1, gimli);
		assertAttachedTo(axe2, guard1);

		// Beside another hand weapon (Gimli's Dwarven Axe) and beside another Hand Axe (the first Dwarf Guard's).
		scn.FreepsPlayCard(axe1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard1, guard2, grimir));
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(axe1, gimli);
		assertAttachedTo(dwaxe1, gimli);

		// The other way round: a plain hand weapon may join a Hand Axe already borne.  Gimli, with two hand weapons,
		// has no room, while the two Dwarves with one or none are offered in the same choice.
		scn.FreepsPlayCard(dwaxe2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, guard2, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(guard1);
		assertAttachedTo(dwaxe2, guard1);
		assertAttachedTo(axe2, guard1);

		// A Hand Axe is borne beside 1 other hand weapon, not 2: Gimli and the first Guard each bear two already.
		scn.FreepsPlayCard(axe3);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, guard1));
		scn.FreepsChooseCard(guard2);
		assertAttachedTo(axe3, guard2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityDiscardsThisAxeToMakeTheFellowshipArcheryTotalOneMoreForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe1, axe2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The phase before: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(axe1));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(axe1));
		// Nobody in the fellowship is an archer.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertAttachedTo(axe1, gimli);

		scn.FreepsUseCardAction(axe1);
		// The axe used is the one discarded; the other copy stays on Gimli and keeps its own ability.
		assertInDiscard(axe1);
		assertAttachedTo(axe2, gimli);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(axe2));
		scn.FreepsPass();
		scn.ShadowPass();

		// The one archery wound goes to the only minion; the +1 lasted the archery phase and no longer.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.FreepsActionAvailable(axe2));
	}
}
