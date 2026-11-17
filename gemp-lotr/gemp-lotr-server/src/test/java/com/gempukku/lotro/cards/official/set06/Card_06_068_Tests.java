package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mechanics", "6_68");
					put("mechanics2", "6_68");
					put("forger", "3_56");      // Isengard Forger: an [isengard] Orc of another title; a Shadow ability only
					put("rider", "5_67");       // Warg-rider: an [isengard] Orc of a third title; fierce while not exhausted
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is an Uruk-hai, no other text
					put("runner", "1_178");     // Goblin Runner: an Orc of [moria]; its play trigger is not set off by a cheat
					// Iron Fist of the Orc: an [isengard] condition that Orcs a regroup action discards are stacked on
					put("fist", "6_64");
					// Library of Orthanc: an [isengard] artifact, another type of [isengard] card, that takes cards stacked
					put("library", "9_39");
					// Web: a [gollum] possession whose regroup ability stacks the Shadow player's Orc on it
					put("web", "8_30");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit; strength while bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit; a bar on discarding tales
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardMechanicsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Isengard Mechanics
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Discard an [isengard] Orc to make the Free Peoples player wound a companion (or 2 companions if you spot 6 companions).<br><b>Regroup:</b> If this minion is stacked on an [isengard] card, spot an [isengard] Orc and discard this minion to add (3).
		*/

		var scn = GetScenario();

		var mechanics = scn.GetFreepsCard("mechanics");

		assertEquals("Isengard Mechanics", mechanics.getBlueprint().getTitle());
		assertNull(mechanics.getBlueprint().getSubtitle());
		assertFalse(mechanics.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mechanics.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, mechanics.getBlueprint().getCulture());
		assertEquals(CardType.MINION, mechanics.getBlueprint().getCardType());
		assertEquals(Race.ORC, mechanics.getBlueprint().getRace());
		assertEquals(6, mechanics.getBlueprint().getTwilightCost());
		assertEquals(11, mechanics.getBlueprint().getStrength());
		assertEquals(4, mechanics.getBlueprint().getVitality());
		assertEquals(4, mechanics.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsAnIsengardOrcToWoundTwoCompanionsWithSixAndOneWithFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var barliman = scn.GetFreepsCard("barliman");
		// Six companions, the Ring-bearer among them.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, merry, pippin);
		scn.MoveAlliesToTable(barliman);
		// Gimli, vitality 3, is exhausted: a wound kills him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		var mechanics = scn.GetShadowCard("mechanics");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(mechanics, forger, savage, runner);
		// Nobody is assigned to them, so all four are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, mechanics, forger, savage, runner);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mechanics));
		int pool = scn.GetTwilight();
		scn.ShadowUseCardAction(mechanics);

		// Any [isengard] Orc pays, the Mechanics itself among them; not the [isengard] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mechanics, forger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(forger);

		assertInZone(Zone.DISCARD, forger);
		assertInZone(Zone.SHADOW_CHARACTERS, mechanics, savage, runner);

		// With six companions the Free Peoples player wounds two of them: no ally and no minion.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, boromir, merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, mechanics));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, aragorn);

		// A wound and not an exertion: the exhausted Gimli is killed.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Used again with five companions and an ally: one companion. No other [isengard] Orc is in play, so the
		// Mechanics pays with itself.
		assertTrue(scn.ShadowActionAvailable(mechanics));
		scn.ShadowUseCardAction(mechanics);
		assertInZone(Zone.DISCARD, mechanics);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir, merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WoundAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mechanics = scn.GetShadowCard("mechanics");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		scn.MoveMinionsToTable(mechanics, forger);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, mechanics, forger);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mechanics));
	}

	@Test
	public void StackedOnAnIsengardCardItIsDiscardedToAdd3WhileAnIsengardOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mechanics = scn.GetShadowCard("mechanics");
		var mechanics2 = scn.GetShadowCard("mechanics2");
		var forger = scn.GetShadowCard("forger");
		var fist = scn.GetShadowCard("fist");
		var library = scn.GetShadowCard("library");
		scn.MoveCardsToSupportArea(fist, library);
		scn.StackCardsOn(fist, mechanics);
		scn.StackCardsOn(library, mechanics2);

		scn.StartGame();
		assertInZone(Zone.STACKED, mechanics, mechanics2);
		scn.MoveMinionsToTable(forger);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(mechanics));
		assertTrue(scn.ShadowActionAvailable(mechanics2));
		scn.ShadowUseCardAction(mechanics);

		// The Mechanics used goes from Iron Fist to the discard pile; the other copy stays on the Library. The Orc
		// spotted is only spotted, and nobody is wounded: the stacked card's first ability is out of force.
		assertEquals(pool + 3, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mechanics);
		assertInZone(Zone.STACKED, mechanics2);
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertEquals(1, scn.GetStackedCards(library).size());
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Stacked on an [isengard] artifact it works as well.
		assertTrue(scn.ShadowActionAvailable(mechanics2));
		scn.ShadowUseCardAction(mechanics2);
		assertEquals(pool + 6, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mechanics2);
		assertEquals(0, scn.GetStackedCards(library).size());
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwilightAbilityIsNotOfferedStackedOnACardOfAnotherCultureNorInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mechanics = scn.GetShadowCard("mechanics");
		var mechanics2 = scn.GetShadowCard("mechanics2");
		var forger = scn.GetShadowCard("forger");
		var web = scn.GetShadowCard("web");
		scn.MoveCardsToSupportArea(web);
		scn.StackCardsOn(web, mechanics);

		scn.StartGame();
		assertInZone(Zone.STACKED, mechanics);
		// The other Mechanics is a minion in play, beside the Forger, which can be spotted.
		scn.MoveMinionsToTable(mechanics2, forger);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.STACKED, mechanics);
		assertInZone(Zone.SHADOW_CHARACTERS, mechanics2, forger);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mechanics));

		// In play, the Mechanics offers its first ability alone: taken, it asks for an [isengard] Orc to discard and
		// adds nothing.
		assertTrue(scn.ShadowActionAvailable(mechanics2));
		int pool = scn.GetTwilight();
		scn.ShadowUseCardAction(mechanics2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(mechanics2, forger));
		scn.ShadowChooseCard(forger);
		assertInZone(Zone.DISCARD, forger);
		assertInZone(Zone.SHADOW_CHARACTERS, mechanics2);
		assertEquals(pool, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwilightAbilityIsNotOfferedUntilAnIsengardOrcInPlayIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mechanics = scn.GetShadowCard("mechanics");
		var forger = scn.GetShadowCard("forger");
		var rider = scn.GetShadowCard("rider");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var fist = scn.GetShadowCard("fist");
		var library = scn.GetShadowCard("library");
		scn.MoveCardsToSupportArea(fist, library);
		scn.StackCardsOn(fist, mechanics);
		// An [isengard] Orc that is stacked is not in play, and cannot be spotted.
		scn.StackCardsOn(library, forger);

		scn.StartGame();
		assertInZone(Zone.STACKED, mechanics, forger);
		// An [isengard] Uruk-hai and a [moria] Orc in play.
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mechanics));

		// An [isengard] Orc comes into play.
		scn.MoveMinionsToTable(rider);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mechanics));
		int pool = scn.GetTwilight();
		scn.ShadowUseCardAction(mechanics);
		assertEquals(pool + 3, scn.GetTwilight());
		assertInZone(Zone.DISCARD, mechanics);
		assertInZone(Zone.SHADOW_CHARACTERS, rider, savage, runner);
		assertInZone(Zone.STACKED, forger);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
