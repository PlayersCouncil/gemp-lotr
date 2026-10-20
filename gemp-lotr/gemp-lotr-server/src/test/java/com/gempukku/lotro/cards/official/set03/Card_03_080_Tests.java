package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thing", "3_80");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability never used
					put("lord", "1_96");        // Boromir, Lord of Gondor: the other Boromir, vitality 3; ranger, and a text about being overwhelmed
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion who is no Boromir; a maneuver ability never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion to bear Dwarven Armor; a skirmish ability never used
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition (no possession) on a [gondor] companion, vitality +1
					put("cloak", "15_68");      // Ranger's Cloak: a [gondor] possession on a [gondor] Man, vitality +1; its texts are for skirmishes
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession; no text but its bearer
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession on a Dwarf; a text about being overwhelmed
					put("coat", "2_105");       // Mithril-coat: a [shire] artifact (no possession) on Frodo; its texts are for archery and skirmishes

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion; no text
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] Troll minion; damage +1, fierce, and texts about its play
					put("spear", "1_182");      // Goblin Spear: a Shadow possession on a [moria] minion; a text about a kill
					put("soldier", "1_271");    // Orc Soldier: a [sauron] minion; a skirmish ability never used
					put("relics", "1_195");     // Relics of Moria: a [moria] condition (no minion) in the support area; a Shadow ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuchaLittleThingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Such a Little Thing
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a [moria] minion to wound Boromir 3 times. The Free Peoples player may discard 2 Free Peoples possessions to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thing");

		assertEquals("Such a Little Thing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAMoriaMinionBoromirIsWoundedThreeTimesWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var cloak = scn.GetFreepsCard("cloak");
		var sword = scn.GetFreepsCard("sword");
		var thing = scn.GetShadowCard("thing");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, aragorn);
		// Boromir at vitality 4, so three wounds leave him alive and a fourth would not.
		scn.AttachCardsTo(boromir, saga);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(thing);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(4, scn.GetVitality(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(thing));

		scn.ShadowPlayCard(thing);
		// Two Free Peoples possessions are on the table, so he is asked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(3, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		// Aragorn, a [gondor] Man who is no Boromir, is not touched.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.ATTACHED, cloak, sword, saga);
		assertInZone(Zone.DISCARD, thing);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOneFreePeoplesPossessionNothingIsAskedAndTheWoundsKillTheOtherBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var thing = scn.GetShadowCard("thing");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(lord);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(thing);

		scn.StartGame();
		// A [moria] Troll is the only minion.
		scn.MoveMinionsToTable(troll);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lord));
		assertEquals(3, scn.GetVitality(lord));
		assertTrue(scn.ShadowPlayAvailable(thing));

		scn.ShadowPlayCard(thing);

		// One possession cannot pay for the prevention, so the Free Peoples player is not asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		// Three wounds on vitality 3 kill him; three exertions could not have been made.
		assertInZone(Zone.DEAD, lord);
		assertInZone(Zone.ATTACHED, sword);
		assertInZone(Zone.DISCARD, thing);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTwoFreePeoplesPossessionsToPreventTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var saga = scn.GetFreepsCard("saga");
		var cloak = scn.GetFreepsCard("cloak");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var coat = scn.GetFreepsCard("coat");
		var thing = scn.GetShadowCard("thing");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCompanionsToTable(boromir, aragorn, gimli);
		// Free Peoples possessions of three cultures on three bearers, Boromir among them.
		scn.AttachCardsTo(boromir, cloak);
		scn.AttachCardsTo(frodo, sword, coat);
		scn.AttachCardsTo(gimli, armor);
		// A Free Peoples condition on a companion.
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToHand(thing);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// A Shadow possession on the minion.
		scn.AttachCardsTo(troop, spear);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.ShadowPlayCard(thing);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The three Free Peoples possessions are offered; the artifact, the condition and the Shadow possession are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(cloak, sword, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(coat, saga, spear));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sword, armor);

		assertInZone(Zone.DISCARD, sword, armor);
		assertInZone(Zone.ATTACHED, cloak, coat, saga, spear);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DISCARD, thing);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAMoriaMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var thing = scn.GetShadowCard("thing");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var relics = scn.GetShadowCard("relics");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(thing);

		scn.StartGame();
		// A minion of another culture and a [moria] card that is no minion.
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToSupportArea(relics);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(thing));

		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(thing));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoBoromirInPlayItIsPlayedForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var sword = scn.GetFreepsCard("sword");
		var thing = scn.GetShadowCard("thing");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(thing);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Boromir is no requirement of the play.
		assertTrue(scn.ShadowPlayAvailable(thing));
		scn.ShadowPlayCard(thing);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.ATTACHED, cloak, sword);
		assertInZone(Zone.DISCARD, thing);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
