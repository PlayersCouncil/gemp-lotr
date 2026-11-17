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

public class Card_01_188_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("longdark", "1_188");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; +2 only while skirmishing an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ALLY, home 3F; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its (2) only when played
					put("sneak", "1_181");      // Goblin Sneak: [moria] Orc, strength 5; its text only when played
					put("cavetroll", "1_165");  // Cave Troll of Moria: [moria] Troll, strength 15, no Orc; its other text is about playing it
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; its text answers a weapon played on it
					put("pinned", "1_192");     // Pinned Down: another [moria] condition in the support area; its text needs a [moria] archer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("longdark", "1_188");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, no mount
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheLongDarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Long Dark
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	When the fellowship moves to site 4 or 5, add (2) for each Dwarf companion.
		 * 	<b>Skirmish:</b> Discard this condition to make your [moria] Orc strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("longdark");

		assertEquals("The Long Dark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MovingToSite4AddsTwoForEachDwarfCompanionAndNoneForADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 3 for Balin's Tomb + 3 companions + 2 for each of the 2 Dwarf companions (not Grimir, an ally; not Frodo)
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToSite5AddsTwoForTheOneDwarfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// 6 for The Bridge of Khazad-dum + 2 companions + 2 for the Dwarf companion
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToSite3AddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 0 for Rivendell Valley + 2 companions, and nothing for Gimli
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToSite6AddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 3 for Galadriel's Glade + 2 companions, and nothing for Gimli
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToSite4OfTheTowersPathAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var longdark = scn.GetShadowCard("longdark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		// 3 for White Rocks (no mounted companion) + 2 companions, and nothing for Gimli
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToSite5OfTheTowersPathAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var longdark = scn.GetShadowCard("longdark");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// 7 for Deeping Wall + 2 companions, and nothing for Gimli
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsTheLongDarkToMakeYourMoriaOrcAnywhereStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var pinned = scn.GetShadowCard("pinned");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(longdark, pinned);
		assertInZone(Zone.SUPPORT, longdark, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, sneak, cavetroll, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{frodo, runner});
		// The Sneak, the Cave Troll and the Orc Chieftain are left over, and stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(sneak));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(7, scn.GetStrength(chieftain));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(longdark));
		scn.ShadowUseCardAction(longdark);
		// [moria] Orcs in this skirmish, in another and in none; not a [moria] Troll, a [sauron] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner, sneak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cavetroll, chieftain, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertEquals(15, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(sneak));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertEquals(7, scn.GetStrength(chieftain));
		// The Long Dark is discarded, and the other [moria] condition stays.
		assertInDiscard(longdark);
		assertInZone(Zone.SUPPORT, pinned);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMoriaOrcInPlayTheSkirmishActionIsStillOfferedAndTheLongDarkIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(longdark);
		assertInZone(Zone.SUPPORT, longdark);

		scn.StartGame();
		// A [moria] Troll and a [sauron] Orc: no [moria] Orc to choose.
		scn.MoveMinionsToTable(cavetroll, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		// The Cave Troll is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(15, scn.GetStrength(cavetroll));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(longdark));
		scn.ShadowUseCardAction(longdark);

		// The cost is paid and the strength goes to nobody.
		assertInDiscard(longdark);
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(15, scn.GetStrength(cavetroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheStrengthBonusLastsToTheEndOfTheSkirmishPhaseAndNotIntoTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(longdark);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{frodo, runner});
		scn.FreepsResolveSkirmish(aragorn);

		// The Runner waits for his own skirmish, the second of the turn.
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsPass();
		scn.ShadowUseCardAction(longdark);
		scn.ShadowChooseCard(runner);
		assertEquals(7, scn.GetStrength(runner));

		// Still in force at the last decision of the skirmish phase it was made in.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(runner));

		// Gone when the next skirmish phase begins, and in his own skirmish.
		scn.ShadowPass();
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runner));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseTheSkirmishActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var longdark = scn.GetShadowCard("longdark");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(longdark);
		assertInZone(Zone.SUPPORT, longdark);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(longdark));
		assertInZone(Zone.SUPPORT, longdark);
	}
}
