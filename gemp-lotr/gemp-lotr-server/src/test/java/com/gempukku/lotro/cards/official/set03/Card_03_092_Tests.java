package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("massing", "3_92");

					// The minions.
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, vitality 2; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("saruman", "3_68");     // Saruman, Keeper of Isengard: [isengard] Wizard minion; may not be assigned to a skirmish

					// Companions of the four races, and others.
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, no archer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man
					put("guardsman", "5_83");   // Household Guard: [rohan] Man, valiant
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent, no Wizard
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit, no Man

					// Allies of three of the races: not in the fellowship.
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, a fellowship ability only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally, no archer
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: Man ally, a maneuver ability only

					// Two more cards of other titles for the draw deck, neither of them played.
					put("dwarfguard", "1_7");   // Dwarf Guard
					put("merry", "1_303");      // Merry, From O'er the Brandywine
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MassingintheEastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Massing in the East
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a [sauron] minion to discard a card from the top of the Free Peoples player's draw deck for each of these races you can spot in the fellowship: Dwarf, Elf, Man, and Wizard.
		*/

		var scn = GetScenario();

		var massing = scn.GetFreepsCard("massing");

		assertEquals("Massing in the East", massing.getBlueprint().getTitle());
		assertNull(massing.getBlueprint().getSubtitle());
		assertFalse(massing.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, massing.getBlueprint().getSide());
		assertEquals(Culture.SAURON, massing.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, massing.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(massing, Timeword.REGROUP));
		assertEquals(1, massing.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsACardFromTheTopOfTheDeckForEachOfTheFourRacesAndNotForASecondCompanionOfARace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var lights = scn.GetShadowCard("lights");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		// The Free Peoples player's own copies of six cards of six titles, for his draw deck.
		var d1 = scn.GetFreepsCard("dwarfguard");
		var d2 = scn.GetFreepsCard("merry");
		var d3 = scn.GetFreepsCard("troop");
		var d4 = scn.GetFreepsCard("brigand");
		var d5 = scn.GetFreepsCard("saruman");
		var d6 = scn.GetFreepsCard("lights");
		scn.MoveCompanionsToTable(gimli, arwen, aragorn, boromir, gandalf);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		assertInZone(Zone.HAND, massing);
		scn.MoveMinionsToTable(soldier, lights, troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetDrawDeck(VirtualTableScenario.P1, d1, d2, d3, d4, d5, d6);
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(d1, scn.GetFreepsTopOfDeck());
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowPlayAvailable(massing));

		scn.ShadowPlayCard(massing);
		// The cost: a [sauron] Orc and a [sauron] Wraith, and not the [moria] Orc.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, lights));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(troop));
		// A Dwarf, an Elf, a Man and a Wizard: four cards, not five for five such companions (two are Men).
		assertInDiscard(d1, d2, d3, d4);
		assertEquals(discard + 4, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(d5, scn.GetFreepsTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInDiscard(massing);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AlliesAMinionAndAGondorHobbitAreNotCountedBesideAWizardCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var brigand = scn.GetShadowCard("brigand");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var grimir = scn.GetFreepsCard("grimir");
		var elrond = scn.GetFreepsCard("elrond");
		var albert = scn.GetFreepsCard("albert");
		var d1 = scn.GetFreepsCard("dwarfguard");
		var d2 = scn.GetFreepsCard("merry");
		var d3 = scn.GetFreepsCard("troop");
		// Frodo and Sam are Hobbits, Pippin a [gondor] Hobbit; Gandalf the one Wizard.
		scn.MoveCompanionsToTable(gandalf, sam, pippin);
		// A Dwarf, an Elf and a Man, none of them in the fellowship.
		scn.MoveAlliesToTable(grimir, elrond, albert);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		assertInZone(Zone.HAND, massing);
		assertInZone(Zone.SUPPORT, grimir, elrond, albert);
		// A Man minion of the Shadow player's own.
		scn.MoveMinionsToTable(soldier, brigand);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetDrawDeck(VirtualTableScenario.P1, d1, d2, d3);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(d1, scn.GetFreepsTopOfDeck());
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(massing));
		scn.ShadowPlayCard(massing);

		assertEquals(1, scn.GetWoundsOn(soldier));
		// The Wizard alone: one card.
		assertInDiscard(d1);
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(d2, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWizardMinionAndAGandalfEntAreNotCountedBesideARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var saruman = scn.GetShadowCard("saruman");
		var guardsman = scn.GetFreepsCard("guardsman");
		var avenger = scn.GetFreepsCard("avenger");
		var d1 = scn.GetFreepsCard("dwarfguard");
		var d2 = scn.GetFreepsCard("merry");
		var d3 = scn.GetFreepsCard("troop");
		// A [rohan] Man, and a [gandalf] companion who is an Ent.
		scn.MoveCompanionsToTable(guardsman, avenger);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		assertInZone(Zone.HAND, massing);
		// A Wizard who is the Shadow player's minion.
		scn.MoveMinionsToTable(soldier, saruman);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetDrawDeck(VirtualTableScenario.P1, d1, d2, d3);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(d1, scn.GetFreepsTopOfDeck());
		int discard = scn.GetFreepsDiscardCount();
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(massing));
		scn.ShadowPlayCard(massing);

		assertEquals(1, scn.GetWoundsOn(soldier));
		// The Man alone: one card.
		assertInDiscard(d1);
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(d2, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		assertInZone(Zone.HAND, massing);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(massing));

		scn.ShadowPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(massing));
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		assertInZone(Zone.HAND, massing);
		// The [moria] Orc beside him is fresh.
		scn.MoveMinionsToTable(soldier, troop);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(soldier));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(massing));
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyItIsStillPlayedAndTheMinionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		assertInZone(Zone.HAND, massing);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetDrawDeck(VirtualTableScenario.P1);
		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.ShadowPlayAvailable(massing));

		// Aragorn, a Man, is in the fellowship; Orc Soldier is the one [sauron] minion and exerts unasked.
		scn.ShadowPlayCard(massing);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInDiscard(massing);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
