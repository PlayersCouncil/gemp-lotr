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

public class Card_01_201_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("territory", "1_201");
					put("territory2", "1_201");
					put("territory3", "1_201");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" add (2), harmless cheated in
					put("runner2", "1_178");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is a Troll, strength 15; text on playing it only
					put("orcchief", "1_266");   // Orc Chieftain: a [sauron] Orc, strength 7; text on a weapon played on him only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man who is no archer, strength 8; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf archer companion, strength 6; an archery ability used only on purpose
					put("garrison", "8_36");    // Garrison of Gondor: a [gondor] Man archer companion, strength 8; to play, spot 3 [gondor] companions (cheated in)
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] Elf archer ALLY, strength 3; an archery ability on Uruk-hai only
					// One Whom Men Would Follow: exert Aragorn to make an ally strength +2 and take part in archery fire and skirmishes until the regroup phase
					put("onewhom", "1_109");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnfamiliarTerritoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Unfamiliar Territory
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [moria] Orc strength +2 (or +4 if skirmishing an archer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("territory");

		assertEquals("Unfamiliar Territory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedBeforeTheSkirmishesAndMakesAMoriaOrcFightingNoArcherStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var territory = scn.GetShadowCard("territory");
		var territory2 = scn.GetShadowCard("territory2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToHand(territory, territory2);

		scn.StartGame();
		assertInHand(territory, territory2);
		// The Troop alone is on the table for the archery phase, and takes Legolas's archery wound.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.MoveMinionsToTable(runner1, runner2, troll, orcchief);
		scn.FreepsPass();

		// Not in the assignment phase, the phase before the skirmishes.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(territory));
		scn.ShadowPass();

		// Runner 1 fights Aragorn, who is no archer; Runner 2 is assigned to Legolas, an archer whose skirmish
		// comes later; the Troop is assigned to nobody.  The [moria] Troll and the [sauron] Orc are in Aragorn's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{legolas, runner2});
		scn.ShadowAssignToMinions(aragorn, troll, orcchief);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(territory));
		scn.ShadowPlayCard(territory);

		// A [moria] Orc, in this skirmish or another or none; not the [moria] Troll or the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, orcchief));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner1);

		// Skirmishing a companion who is no archer, with an archer in play elsewhere: 5 + 2.
		assertEquals(7, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(territory);

		scn.FreepsPass();
		scn.ShadowPlayCard(territory2);
		scn.ShadowChooseCard(runner2);

		// Assigned to Legolas, but that skirmish has not begun: 5 + 2, not + 4.
		assertEquals(7, scn.GetStrength(runner2));
		assertEquals(7, scn.GetStrength(runner1));
		assertInDiscard(territory2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMoriaOrcSkirmishingAnArcherCompanionIsPlusFourUntilTheSkirmishEndsAndAnOrcInNoSkirmishPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var territory = scn.GetShadowCard("territory");
		var territory2 = scn.GetShadowCard("territory2");
		var territory3 = scn.GetShadowCard("territory3");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(legolas, garrison);
		scn.MoveCardsToHand(territory, territory2, territory3);

		scn.StartGame();
		assertInHand(territory, territory2, territory3);
		// The Troop alone is on the table for the archery phase, and takes both archers' wounds (vitality 3).
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(2, scn.GetWoundsOn(troop));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.PassAssignmentActions();

		// Archer companions of two cultures and races: Legolas's skirmish first, the Garrison's after it.
		// The Troop is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, runner1}, new PhysicalCardImpl[]{garrison, runner2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(legolas);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowPlayCard(territory);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, troop));
		scn.ShadowChooseCard(runner1);

		// Skirmishing an archer: 5 + 4, in place of the +2 and not beside it.
		assertEquals(9, scn.GetStrength(runner1));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowPlayCard(territory2);
		scn.ShadowChooseCard(troop);

		// The Troop skirmishes nobody, while another [moria] Orc skirmishes an archer: 13 + 2.
		assertEquals(15, scn.GetStrength(troop));
		// Still +4 at the next decision of the same skirmish.
		assertEquals(9, scn.GetStrength(runner1));
		assertInDiscard(territory, territory2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Runner 1 (9) beats Legolas (6).  In the Garrison's skirmish, the next skirmish phase, the bonuses are gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(garrison);
		assertTrue(scn.IsCharSkirmishingAgainst(garrison, runner2));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowPlayCard(territory3);
		scn.ShadowChooseCard(runner2);

		// Skirmishing a [gondor] Man who is an archer: 5 + 4.
		assertEquals(9, scn.GetStrength(runner2));
		assertInDiscard(territory3);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMoriaOrcSkirmishingAnArcherAllyIsPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var territory = scn.GetShadowCard("territory");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var onewhom = scn.GetFreepsCard("onewhom");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(onewhom, territory);

		scn.StartGame();
		assertInHand(onewhom, territory);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// One Whom Men Would Follow lets Orophin, an archer ally away from his home site, skirmish.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetStrength(orophin));
		scn.FreepsPlayCard(onewhom);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetStrength(orophin));
		scn.ShadowPass();
		scn.FreepsPass();

		// Orophin now takes part in archery fire: the Troop takes his wound.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.FreepsAssignToMinions(orophin, troop);
		scn.FreepsResolveSkirmish(orophin);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowPlayCard(territory);

		// The one [moria] Orc is chosen with nothing asked.  Skirmishing an archer ally: 13 + 4.
		assertEquals(17, scn.GetStrength(troop));
		assertInDiscard(territory);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoMoriaOrcToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var territory = scn.GetShadowCard("territory");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(territory);

		scn.StartGame();
		assertInHand(territory);
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
		assertTrue(scn.ShadowPlayAvailable(territory));
		scn.ShadowPlayCard(territory);

		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(territory);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
