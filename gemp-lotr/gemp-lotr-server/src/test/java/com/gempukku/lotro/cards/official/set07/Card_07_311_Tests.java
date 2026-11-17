package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_311_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "7_311");
					// Gorgoroth Pillager: a besieger Orc, strength 10, vitality 2; its text works only while it is stacked on a site
					put("pillager", "7_275");
					// Great Hill Troll: a besieger who is a Troll, printed fierce; its trigger answers only its own play
					put("troll", "8_102");
					// Uruk Besieger: an [isengard] Uruk-hai with the word in its title and not the keyword; its abilities are used only on purpose
					put("uruk", "4_180");
					// Orc Chieftain: a [sauron] Orc who is no besieger; its text answers a weapon played on it
					put("chieftain", "1_266");

					// Three cards for the Shadow hand, of different sides, cultures and types: a [sauron] condition,
					// a [moria] minion and a Free Peoples companion.
					put("summons", "1_242");    // The Dark Lord's Summons
					put("runner", "1_178");     // Goblin Runner
					put("guard", "1_7");        // Dwarf Guard

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SiegeCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Siege Commander
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.
		 * <b>Skirmish:</b> Discard 2 cards from hand to make a besieger <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var commander = scn.GetFreepsCard("commander");

		assertEquals("Siege Commander", commander.getBlueprint().getTitle());
		assertNull(commander.getBlueprint().getSubtitle());
		assertTrue(commander.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, commander.getBlueprint().getSide());
		assertEquals(Culture.SAURON, commander.getBlueprint().getCulture());
		assertEquals(CardType.MINION, commander.getBlueprint().getCardType());
		assertEquals(Race.ORC, commander.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(commander, Keyword.BESIEGER));
		assertEquals(5, commander.getBlueprint().getTwilightCost());
		assertEquals(12, commander.getBlueprint().getStrength());
		assertEquals(3, commander.getBlueprint().getVitality());
		assertEquals(5, commander.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityDiscardsTwoCardsToMakeABesiegerFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var commander = scn.GetShadowCard("commander");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var uruk = scn.GetShadowCard("uruk");
		var chieftain = scn.GetShadowCard("chieftain");
		var summons = scn.GetShadowCard("summons");
		var runner = scn.GetShadowCard("runner");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(summons, runner, guard);
		assertInHand(summons, runner, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(commander, pillager, troll, uruk, chieftain);
		scn.SkipToAssignments();
		// Aragorn takes the Commander; every other minion is left unassigned.
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(commander));
		scn.ShadowUseCardAction(commander);

		// The cost: any 2 cards of the hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(summons, runner, guard));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(summons, guard);
		assertInDiscard(summons, guard);
		assertInHand(runner);
		assertEquals(1, scn.GetShadowHandCount());

		// A besieger: the Commander himself, an unassigned besieger Orc and a besieger Troll who is fierce already;
		// not an Uruk-hai with the word in his title only, nor a [sauron] Orc who is no besieger.
		assertTrue(scn.ShadowHasCardChoiceAvailable(commander, pillager, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		scn.ShadowChooseCard(pillager);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(commander, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));

		// 12 against 8: Aragorn loses.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Pillager is still fierce for the fierce round, and is fought there.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pillager));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(commander));
		scn.FreepsAssignToMinions(aragorn, pillager);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));

		// 10 against 8: Aragorn loses again.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// Gone with the regroup phase; the Troll's printed fierce stays.
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedAtTheShadowPlayersAssignmentActions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var commander = scn.GetShadowCard("commander");
		var summons = scn.GetShadowCard("summons");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(summons, runner);
		assertInHand(summons, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(commander));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var commander = scn.GetShadowCard("commander");
		var summons = scn.GetShadowCard("summons");
		scn.MoveCardsToHand(summons);
		assertInHand(summons);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, commander);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(commander));
		assertFalse(scn.HasKeyword(commander, Keyword.FIERCE));
	}
}
