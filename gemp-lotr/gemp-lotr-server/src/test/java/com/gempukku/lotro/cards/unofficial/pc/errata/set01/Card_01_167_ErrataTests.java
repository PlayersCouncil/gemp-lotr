package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_167_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizens", "51_167");
					put("denizens2", "51_167");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, vitality 3; no text
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc minion, strength 5, vitality 1; a "when you play" add (2), harmless cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll minion, strength 15; Damage +1, fierce, text on playing it only
					put("balrog", "12_79");     // The Balrog: [moria] Balrog minion, strength 17; Damage +1, and text for underground sites only
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc minion, strength 7; a regroup ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, strength 3, vitality 4; a fellowship ability used only on purpose
					put("numenor", "5_37");     // Men of Numenor: Free Peoples skirmish event, cost 0: wound a minion bearing a [gondor] fortification
					put("wall", "5_33");        // City Wall: [gondor] fortification; on a minion, strength -3
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
		 * Game Text: Choose one: make a [moria] minion strength +2, or make a [moria] Orc strength +1 for each other [moria] minion you can spot (limit +5).
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
	public void NotPlayableInTheAssignmentPhaseAndInASkirmishMakesAMoriaMinionOfAnyRaceStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troll = scn.GetShadowCard("troll");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, runner2, troll, servant);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(denizens));
		scn.ShadowPass();

		// The Goblin Runner is assigned to Sam, whose skirmish comes later; the other Goblin Runner is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner});
		scn.ShadowAssignToMinions(aragorn, troll, servant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(denizens));
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(denizens);
		assertEquals(twilight - 1, scn.GetTwilight());

		assertTrue(scn.ShadowChoiceAvailable("strength +2"));
		assertTrue(scn.ShadowChoiceAvailable("for each other"));
		scn.ShadowChooseOption("strength +2");

		// Any [moria] minion: an Orc, a Troll, one not in this skirmish, one in no skirmish at all; not an Orc of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troll, runner, runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertEquals(17, scn.GetStrength(troll));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(7, scn.GetStrength(servant));
		assertInDiscard(denizens);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheOtherChoiceMakesAMoriaOrcPlusOneForEachOtherMoriaMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troll = scn.GetShadowCard("troll");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, runner2, troll, servant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner});
		scn.ShadowAssignToMinions(aragorn, troll, servant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseOption("for each other");

		// A [moria] Orc, in this skirmish or not, assigned or not; not the [moria] Troll, not the [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner, runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		// The other [moria] minions are the Troll and the two Runners, the unassigned one counted too: 13 + 3.  Not the
		// Troop itself, not the [isengard] Orc.
		assertEquals(16, scn.GetStrength(troop));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFourOtherMoriaMinionsTheOrcIsPlusFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troop = scn.GetShadowCard("troop");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var troll = scn.GetShadowCard("troll");
		var balrog = scn.GetShadowCard("balrog");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner1, runner2, troll, balrog);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner1});
		scn.ShadowAssignToMinions(aragorn, runner2, troll, balrog);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseOption("for each other");
		scn.ShadowChooseCard(troop);

		// 13 + 4: two Runners, the Troll and the Balrog.
		assertEquals(17, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSixOtherMoriaMinionsTheOrcIsPlusFiveAndASecondCopyAddsFiveMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var denizens2 = scn.GetShadowCard("denizens2");
		var troop = scn.GetShadowCard("troop");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		var troll = scn.GetShadowCard("troll");
		var balrog = scn.GetShadowCard("balrog");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens, denizens2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner1, runner2, runner3, runner4, troll, balrog);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner1});
		scn.ShadowAssignToMinions(aragorn, runner2, runner3, runner4, troll, balrog);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseOption("for each other");
		scn.ShadowChooseCard(troop);
		// Six others, limit +5: 13 + 5.
		assertEquals(18, scn.GetStrength(troop));

		scn.FreepsPass();
		scn.ShadowPlayCard(denizens2);
		scn.ShadowChooseOption("for each other");
		scn.ShadowChooseCard(troop);
		// A limit does not apply to a different copy: 18 + 5.
		assertEquals(23, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMoriaOrcTheMinionChoiceIsWhatHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var troll = scn.GetShadowCard("troll");
		var balrog = scn.GetShadowCard("balrog");
		var servant = scn.GetShadowCard("servant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, balrog, servant);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troll}, new PhysicalCardImpl[]{sam, servant});
		scn.ShadowAssignToMinions(aragorn, balrog);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		// No [moria] Orc to make stronger: no choice of the two, straight to the [moria] minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, balrog));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		scn.ShadowChooseCard(balrog);

		assertEquals(19, scn.GetStrength(balrog));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(servant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EitherBonusLastsUntilTheEndOfTheSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denizens = scn.GetShadowCard("denizens");
		var denizens2 = scn.GetShadowCard("denizens2");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(denizens, denizens2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseOption("strength +2");
		scn.ShadowChooseCard(troop);
		assertEquals(15, scn.GetStrength(troop));
		scn.FreepsPass();
		scn.ShadowPass();
		// The Troop won its skirmish against Aragorn.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(sam);
		// The next skirmish phase: the +2 is gone.
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens2);
		scn.ShadowChooseOption("for each other");
		scn.ShadowChooseCard(troop);
		// One other [moria] minion, the Runner: 13 + 1.
		assertEquals(14, scn.GetStrength(troop));
		scn.FreepsPass();
		scn.ShadowPass();

		// The regroup phase: the +1 is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void TheBonusStaysWhenACountedMinionDiesLaterInThePhase() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.AttachCardsTo(runner2, wall);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{sam, runner1});
		scn.ShadowAssignToMinions(aragorn, runner2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(denizens);
		scn.ShadowChooseOption("for each other");
		scn.ShadowChooseCard(troop);
		// Two Runners: 13 + 2.
		assertEquals(15, scn.GetStrength(troop));

		// Men of Numenor wounds the Runner bearing City Wall, which has vitality 1.
		scn.FreepsPlayCard(numenor);
		assertInDiscard(runner2);
		// CR 4.2 "modifier": the amount is not worked out again.
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
