package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_304_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noble", "1_304");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability used only on purpose
					// Sam, Son of Hamfast: a [shire] Hobbit companion; his abilities are used only on purpose
					put("sam", "1_311");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in);
					// no wound on him, so his enduring adds nothing; a skirmish ability used only on purpose
					put("pippin", "103_31");
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, vitality 2; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability used only on purpose
					// Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3, vitality 4; maneuver and
					// regroup abilities used only on purpose
					put("ted", "18_75");
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc minion with no text
					put("runner", "1_178");     // Goblin Runner: an Orc minion, strength 5, vitality 1; its text answers only its play
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNoHobbitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noble", "1_304");
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: an Elf, the Ring-bearer, so that no Hobbit is in play; her only text
				// reads an artifact or The One Ring she bears
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NobleIntentionsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Noble Intentions
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert a companion (except a Hobbit) to make a Hobbit strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("noble");

		assertEquals("Noble Intentions", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsACompanionWhoIsNoHobbitToMakeAHobbitStrength3More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noble = scn.GetFreepsCard("noble");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var barliman = scn.GetFreepsCard("barliman");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, sam, pippin);
		scn.MoveAlliesToTable(barliman, bounder);
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, ted);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, troop}, new PhysicalCardImpl[]{sam, ted});
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// Frodo 3, +1 from The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.FreepsPlayAvailable(noble));

		scn.FreepsPlayCard(noble);
		// The cost: a companion who is no Hobbit, of either culture and race.  Not a Hobbit companion, not an ally
		// who is no Hobbit, not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, pippin, frodo, barliman, bounder, troop, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The Hobbit: the Ring-bearer in this skirmish, Hobbit companions of two cultures in no skirmish or another,
		// a Hobbit ally, and the Shadow player's Hobbit minion.  Not a character who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, barliman, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);
		assertEquals(7, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertInDiscard(noble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoHobbitInPlayItIsStillPlayedAndTheCompanionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoHobbitScenario();

		var noble = scn.GetFreepsCard("noble");
		var troop = scn.GetShadowCard("troop");
		var galadriel = scn.GetRingBearer();
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		assertInHand(noble);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(galadriel, troop);
		scn.FreepsResolveSkirmish(galadriel);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		// Galadriel 3, +1 from The Ruling Ring
		assertEquals(4, scn.GetStrength(galadriel));
		assertTrue(scn.FreepsPlayAvailable(noble));

		scn.FreepsPlayCard(noble);
		// Galadriel, the only companion and no Hobbit, exerts with nothing asked, and there is no Hobbit to make
		// stronger: the event is played all the same.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(4, scn.GetStrength(galadriel));
		assertInDiscard(noble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsOnlyForTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noble = scn.GetFreepsCard("noble");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{frodo, runner2});
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetStrength(sam));
		scn.FreepsPlayCard(noble);
		// Aragorn is the only companion who is no Hobbit, and exerts with nothing asked.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(sam);
		// Still in force at the Shadow player's next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(sam));

		// Sam 6 against the Runner's 5: the Runner, of vitality 1, is killed.
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DISCARD, runner);

		// Each skirmish is a phase of its own: in Frodo's, Sam is himself again.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(sam));
	}

	@Test
	public void CannotBePlayedWhileTheOnlyCompanionWhoIsNoHobbitIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noble = scn.GetFreepsCard("noble");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(noble);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  Frodo, Sam and Barliman can.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInHand(noble);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(noble);
		assertFalse(scn.FreepsPlayAvailable(noble));
	}
}
