package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_050_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The skirmishes are
	// at Erebor (site 7, a mountain, where Smaug stays in play), reached by SkipToSite(6) and the move; the minions
	// are laid in its Shadow phase.  A doubt is the Hobbit game's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dissension", "32_50");
					put("dissension2", "32_50");

					// Smaug, The Golden: [smaug] Dragon, 17/5, damage +2, fierce; discarded if not at a mountain;
					// a regroup ability, not used.
					put("smaug", "30_66");
					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire.
					put("runner", "31_30");
					// Orkish Pursuer: [sauron] Orc, 8/3, a minion able to exert twice; a trigger on its own win and a
					// regroup ability, neither reached.
					put("pursuer", "32_42");

					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability.
					put("balin", "30_2");
					// Nori, Of the Blue Mountains: [dwarven] Dwarf companion, 7/3; a maneuver ability.
					put("nori", "30_15");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, 7/4; Thorin costs 2 less, and he cannot
					// be discarded beside 2 [dwarven] followers.  Mirkwood (site 5) discards every Gandalf as the
					// fellowship moves there, so he is laid after that move.
					put("gandalf", "30_26");
					// Dain Ironfoot: [dwarven] Dwarf ally; Thorin is strength +1 (no Thorin is laid); a start-of-regroup
					// trigger that needs a [dwarven] follower (none is laid).
					put("dain", "32_2");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DissensionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dissension
		 * Unique: False
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may exert Smaug twice to play this event from your discard pile.<br>Spot X doubts to make a [dwarven] companion strength -X.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dissension");

		assertEquals("Dissension", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotThreeDoubtsToMakeADwarvenCompanionStrengthMinus3UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dissension = scn.GetShadowCard("dissension");
		var smaug = scn.GetShadowCard("smaug");
		var runner = scn.GetShadowCard("runner");
		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(smaug, runner);
		scn.MoveCardsToHand(dissension);
		scn.AddBurdens(3);
		assertInZone(Zone.FREE_CHARACTERS, balin, nori, gandalf);
		assertInZone(Zone.SUPPORT, dain);
		assertInHand(dissension);
		assertEquals(3, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, smaug}, new PhysicalCardImpl[]{nori, runner});
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));
		assertTrue(scn.ShadowPlayAvailable(dissension));

		scn.ShadowPlayCard(dissension);

		// X is the Shadow player's count of the doubts, from 0 to all of them, filled in with all of them.
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("3");

		// A [dwarven] companion in this skirmish and one in a skirmish still to come; not a companion of another
		// culture, nor a [dwarven] ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, nori));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, bilbo, dain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nori);

		assertEquals(4, scn.GetStrength(nori));
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(3, scn.GetBurdens());
		assertInDiscard(dissension);
		// Still in force at the decision that follows, in the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(nori));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(nori);

		// Nori's own skirmish is a phase of its own: the -3 is gone.
		assertEquals(7, scn.GetStrength(nori));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SpotOneDoubtToMakeADwarvenCompanionStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dissension = scn.GetShadowCard("dissension");
		var smaug = scn.GetShadowCard("smaug");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToHand(dissension);
		scn.AddBurdens(1);
		assertInHand(dissension);
		assertEquals(1, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(7, scn.GetStrength(balin));

		scn.ShadowPlayCard(dissension);
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		scn.ShadowChooseCard(balin);

		assertEquals(6, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));
		assertInDiscard(dissension);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarvenCompanionDissensionIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dissension = scn.GetShadowCard("dissension");
		var smaug = scn.GetShadowCard("smaug");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToHand(dissension);
		scn.AddBurdens(2);
		assertInZone(Zone.SUPPORT, dain);
		assertInHand(dissension);
		assertEquals(2, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, smaug);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.ShadowPlayAvailable(dissension));

		scn.ShadowPlayCard(dissension);
		// The doubts are spotted all the same, and then no companion can be chosen.
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(7, scn.GetStrength(gandalf));
		assertInDiscard(dissension);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableFromHandOrDiscardPileInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dissension = scn.GetShadowCard("dissension");
		var dissension2 = scn.GetShadowCard("dissension2");
		var smaug = scn.GetShadowCard("smaug");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToHand(dissension);
		scn.MoveCardsToDiscard(dissension2);
		scn.AddBurdens(2);
		assertInHand(dissension);
		assertInDiscard(dissension2);
		assertEquals(0, scn.GetWoundsOn(smaug));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dissension));
		assertFalse(scn.ShadowActionAvailable(dissension2));
	}

	@Test
	public void ExertingSmaugTwicePlaysItFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dissension = scn.GetShadowCard("dissension");
		var smaug = scn.GetShadowCard("smaug");
		var pursuer = scn.GetShadowCard("pursuer");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, pursuer);
		scn.MoveCardsToDiscard(dissension);
		scn.AddBurdens(2);
		assertInDiscard(dissension);
		assertEquals(2, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, smaug}, new PhysicalCardImpl[]{nori, pursuer});
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dissension));

		scn.ShadowUseCardAction(dissension);

		// Smaug, and not the other minion able to exert twice.
		assertEquals(2, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.ShadowChoose("2");
		scn.ShadowChooseCard(balin);

		assertEquals(5, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(nori));
		assertInDiscard(dissension);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableFromTheDiscardPileWhileSmaugCannotExertTwiceThoughACopyInHandIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dissension = scn.GetShadowCard("dissension");
		var dissension2 = scn.GetShadowCard("dissension2");
		var smaug = scn.GetShadowCard("smaug");
		var pursuer = scn.GetShadowCard("pursuer");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveMinionsToTable(smaug, pursuer);
		// Vitality 5 with 3 wounds: 2 left, one exertion short of two.
		scn.AddWoundsToChar(smaug, 3);
		scn.MoveCardsToDiscard(dissension);
		scn.MoveCardsToHand(dissension2);
		scn.AddBurdens(1);
		assertEquals(3, scn.GetWoundsOn(smaug));
		assertInDiscard(dissension);
		assertInHand(dissension2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, smaug);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dissension));
		assertTrue(scn.ShadowPlayAvailable(dissension2));
		assertEquals(3, scn.GetWoundsOn(smaug));
		assertEquals(0, scn.GetWoundsOn(pursuer));
	}
}
