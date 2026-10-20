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

public class Card_01_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("drums", "1_168");
					put("drums2", "1_168");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" add (2), harmless cheated in
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is a Troll, strength 15; text on playing it only
					put("orcchief", "1_266");   // Orc Chieftain: a [sauron] Orc, strength 7; text on a weapon played on him only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrumsintheDeepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Drums in the Deep
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [moria] Orc strength +2 (or +4 if skirmishing a Dwarf).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("drums");

		assertEquals("Drums in the Deep", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhaseAndIsPlayableInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drums = scn.GetShadowCard("drums");
		var runner = scn.GetShadowCard("runner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(drums));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(drums));
	}

	@Test
	public void MakesAMoriaOrcNotSkirmishingADwarfStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drums = scn.GetShadowCard("drums");
		var drums2 = scn.GetShadowCard("drums2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(drums, drums2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, troop, troll, orcchief);
		scn.SkipToAssignments();
		// Runner 1 fights Aragorn, a Man; Runner 2 is assigned to Gimli, whose skirmish comes later; the Troop
		// is assigned to nobody.  The [moria] Troll and the [sauron] Orc are in Aragorn's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{gimli, runner2});
		scn.ShadowAssignToMinions(aragorn, troll, orcchief);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertTrue(scn.ShadowPlayAvailable(drums));
		scn.ShadowPlayCard(drums);

		// A [moria] Orc, in this skirmish or another or none; not the [moria] Troll or the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, orcchief));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner1);

		// Skirmishing a Man: 5 + 2.
		assertEquals(7, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(drums);

		scn.FreepsPass();
		scn.ShadowPlayCard(drums2);
		scn.ShadowChooseCard(runner2);

		// Assigned to Gimli, but that skirmish has not begun: 5 + 2, not + 4.
		assertEquals(7, scn.GetStrength(runner2));
		assertEquals(7, scn.GetStrength(runner1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMoriaOrcSkirmishingADwarfCompanionIsPlusFourAndAnotherOrcIsPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drums = scn.GetShadowCard("drums");
		var drums2 = scn.GetShadowCard("drums2");
		var runner = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(drums, drums2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowPlayCard(drums);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		scn.ShadowChooseCard(runner);

		// Skirmishing a Dwarf: 5 + 4, in place of the +2 and not beside it.
		assertEquals(9, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowPlayCard(drums2);
		scn.ShadowChooseCard(troop);

		// The Troop skirmishes nobody, while the Runner skirmishes a Dwarf: 13 + 2.
		assertEquals(15, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(runner));
		assertInDiscard(drums, drums2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMoriaOrcSkirmishingADwarfAllyIsPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drums = scn.GetShadowCard("drums");
		var runner = scn.GetShadowCard("runner1");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin's own ability lets him take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.ShadowPass();
		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, runner);
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(drums);

		// The one [moria] Orc is chosen with nothing asked.  Skirmishing a Dwarf ally: 5 + 4.
		assertEquals(9, scn.GetStrength(runner));
		assertInDiscard(drums);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsToTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drums = scn.GetShadowCard("drums");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPlayCard(drums);
		scn.ShadowChooseCard(troop);

		// Still +2 at the next decision of the same skirmish.
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The Troop won against Aragorn (15 to 8: wounded, not overwhelmed).
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);

		// The next skirmish phase: the +2 is gone.
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoMoriaOrcToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drums = scn.GetShadowCard("drums");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(drums);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, orcchief);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troll);
		scn.ShadowAssignToMinions(aragorn, orcchief);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// A [moria] Troll and a [sauron] Orc, and no [moria] Orc to choose: the text has no requirement, so the
		// event is offered and played for nothing.
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(drums));
		scn.ShadowPlayCard(drums);

		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(drums);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
