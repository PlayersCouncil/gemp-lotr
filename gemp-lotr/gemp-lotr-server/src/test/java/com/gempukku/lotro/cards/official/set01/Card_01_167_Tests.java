package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_167_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizens", "1_167");
					put("denizens2", "1_167");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troop2", "1_177");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" add (2), harmless cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is a Troll, strength 15; text on playing it only
					put("orcchief", "1_266");   // Orc Chieftain: a [sauron] Orc, strength 7; text on a weapon played on him only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, strength 3, vitality 4; a fellowship ability used only on purpose
					put("numenor", "5_37");     // Men of Numenor: Free Peoples skirmish event, cost 0: wound a minion bearing a [gondor] fortification
					put("wall", "5_33");        // City Wall: [gondor] fortification; its skirmish ability transfers it to a minion, and is not used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenizensEnragedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Denizens Enraged
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [moria] Orc strength +1 for each other [moria] Orc you spot (limit +4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denizens");

		assertEquals("Denizens Enraged", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndInASkirmishAnyMoriaOrcMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, troop, troll, orcchief);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(denizens));
		scn.ShadowPass();

		// Runner 1 fights Aragorn; Runner 2 is assigned to Sam, whose skirmish comes later; the Troop is assigned
		// to nobody.  The [moria] Troll and the [sauron] Orc are in Aragorn's skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{sam, runner2});
		scn.ShadowAssignToMinions(aragorn, troll, orcchief);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(denizens));
		scn.ShadowPlayCard(denizens);

		// A [moria] Orc, in this skirmish or another or none; not the [moria] Troll or the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, orcchief));
		assertEquals(1, scn.ShadowGetChoiceMin());
		// The game rests on the Shadow player's choice of the Orc.
		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void MakesAMoriaOrcStrongerByTheNumberOfOtherMoriaOrcsNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, troop, troll, orcchief);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{sam, runner2});
		scn.ShadowAssignToMinions(aragorn, troll, orcchief);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(runner1));
		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseCard(runner1);

		// Nothing is added before the player names how many he spots (CR 4.2 "spot").
		assertEquals(5, scn.GetStrength(runner1));
		// The other [moria] Orcs are Runner 2, assigned elsewhere, and the Troop, assigned to nobody: none, or up
		// to those 2.  Not the Runner chosen, the [moria] Troll or the [sauron] Orc.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		// The number comes filled in with both of them (F-0561), and fewer may be named.
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");

		// One named of the two: 5 + 1, not + 2.
		assertEquals(6, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(denizens);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStopsAtFourAndASecondCopyGivesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var denizens2 = scn.GetShadowCard("denizens2");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(denizens, denizens2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2, runner1, runner2, runner3, runner4, troll, orcchief);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseCard(troop);

		// Five other [moria] Orcs: the count itself is not capped.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		// Filled in with all five there are, not with the +4 the limit lets count (F-0561).
		assertEquals(Integer.valueOf(5), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("5");
		// Five named, limit +4: 13 + 4.
		assertEquals(17, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowPlayCard(denizens2);
		scn.ShadowChooseCard(troop);
		scn.ShadowChoose("5");
		// A limit does not apply to a different copy (CR 4.2 "limit"): 17 + 4.
		assertEquals(21, scn.GetStrength(troop));
		assertInDiscard(denizens, denizens2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsToTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseCard(troop);
		// One other [moria] Orc, the Runner: none or 1.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		// 13 + 1, still there at the next decision of the same skirmish.
		assertEquals(14, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The Troop won against Aragorn (14 to 8: wounded, not overwhelmed).
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(sam);

		// The next skirmish phase: the +1 is gone.
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStaysWhenACountedOrcDiesLaterInThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troop = scn.GetShadowCard("troop");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner1, runner2);
		// City Wall on a Runner, as its own ability would put it on a minion skirmishing a [gondor] Man.
		scn.AttachCardsTo(runner2, wall);
		assertAttachedTo(wall, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner1});
		scn.ShadowAssignToMinions(aragorn, runner2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseCard(troop);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");
		// Both Runners named: 13 + 2.
		assertEquals(15, scn.GetStrength(troop));

		// Men of Numenor wounds the Runner bearing City Wall, which has vitality 1.
		scn.FreepsPlayCard(numenor);
		assertInDiscard(runner2);
		// CR 4.2 "modifier": the amount is not worked out again.
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoMoriaOrcToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(denizens);

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
		assertTrue(scn.ShadowPlayAvailable(denizens));
		scn.ShadowPlayCard(denizens);

		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(denizens);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheOnlyMoriaOrcIsNoOtherOrcToItselfAndGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var runner = scn.GetShadowCard("runner1");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troll, orcchief);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowAssignToMinions(aragorn, troll, orcchief);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The one [moria] Orc, chosen with nothing asked: the Troll and the [sauron] Orc are not [moria] Orcs, and
		// the Runner is not another Orc to himself.  He stays at 5.
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.ShadowPlayAvailable(denizens));
		scn.ShadowPlayCard(denizens);

		assertEquals(5, scn.GetStrength(runner));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertInDiscard(denizens);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
