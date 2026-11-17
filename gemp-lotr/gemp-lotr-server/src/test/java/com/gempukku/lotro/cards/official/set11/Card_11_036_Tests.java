package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_036_Tests
{
	// The Fellowship path: site 2, Ettenmoors, is a plains and no battleground.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inspiration", "11_36");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus by the races in the fellowship
					// Saruman, The White: a [gandalf] Wizard who is an ally (a Hobbit-set card, laid under Q-0312 as the
					// one card that tells "a [gandalf] Wizard" from a [gandalf] Wizard companion).  Gandalf is
					// defender +1 beside him; his abilities are never used here.
					put("sarumanally", "32_24");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; strength +2 only beside 3 Ents
					put("blacktraitor", "4_173"); // Saruman, Black Traitor: an [isengard] Wizard minion who may not be assigned
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("madril", "11_62");     // Madril, Ranger of Ithilien: a companion; a fellowship ability that needs a forest
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, in hand only
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition, in hand only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: a minion card, in the Free Peoples hand only
					put("detachment", "11_74"); // Detachment of Haradrim: [men] Man, strength 12, vitality 3; a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with a site 2 that is a battleground.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inspiration", "11_36");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus by the races in the fellowship
					put("madril", "11_62");     // Madril, Ranger of Ithilien: a companion, in hand only
					put("avenger", "6_27");     // Ent Avenger: a companion, in hand only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InspirationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Inspiration
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a [gandalf] Wizard to reveal your hand and make a minion strength -3 for each companion in your hand (or -4 for each if the fellowship is at a battleground site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("inspiration");

		assertEquals("Inspiration", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealsTheHandAndMakesAMinionStrengthMinus3ForEachOfTheTwoCompanionsInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var madril = scn.GetFreepsCard("madril");
		var avenger = scn.GetFreepsCard("avenger");
		var elrond = scn.GetFreepsCard("elrond");
		var saga = scn.GetFreepsCard("saga");
		var freepsRunner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		// Two companions in the Free Peoples hand, beside an ally, a condition and a minion card.
		scn.MoveCardsToHand(inspiration, madril, avenger, elrond, saga, freepsRunner);
		// Companions in the other player's hand are not "your hand": three of them, so that his hand would
		// read otherwise.
		var shadowMadril = scn.GetShadowCard("madril");
		var shadowAvenger = scn.GetShadowCard("avenger");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(shadowMadril, shadowAvenger, shadowAragorn);

		var troop = scn.GetShadowCard("troop");
		var detachment = scn.GetShadowCard("detachment");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, detachment);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{aragorn, detachment});
		scn.FreepsResolveSkirmish(gandalf);

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(detachment));
		assertTrue(scn.FreepsPlayAvailable(inspiration));
		scn.FreepsPlayCard(inspiration);

		// The hand is shown to the other player, and its owner is shown nothing.
		assertEquals(5, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(madril, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(freepsRunner, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// The minion may be any minion: the one Gandalf fights, and one whose skirmish with Aragorn waits.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, detachment));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Two companions in hand: 13 - 3 - 3.  The ally, the condition, the minion card and the other
		// player's companions count for nothing.
		assertEquals(7, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(detachment));
		assertInDiscard(inspiration);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneCompanionInHandAMinionInNoSkirmishYetIsMinus3UntilTheSkirmishPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var sarumanally = scn.GetFreepsCard("sarumanally");
		var aragorn = scn.GetFreepsCard("aragorn");
		var madril = scn.GetFreepsCard("madril");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, madril);
		// The only [gandalf] Wizard in play is an ally.
		scn.MoveAlliesToTable(sarumanally);
		scn.MoveCardsToHand(inspiration, avenger);

		var troop = scn.GetShadowCard("troop");
		var detachment = scn.GetShadowCard("detachment");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, detachment);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, detachment}, new PhysicalCardImpl[]{madril, troop});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsPlayAvailable(inspiration));
		scn.FreepsPlayCard(inspiration);
		scn.ShadowDismissRevealedCards();
		scn.FreepsChooseCard(troop);

		// One companion in hand: 13 - 3, on the minion whose skirmish with Madril is still to come.
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(detachment));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn's 8 loses to 12.  Each skirmish is a phase of its own, and in Madril's the Troop is 13 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(madril);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGandalfWizardBesideAGandalfCompanionWhoIsNoWizardAndAWizardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var avenger = scn.GetFreepsCard("avenger");
		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(inspiration, madril);
		assertInZone(Zone.HAND, inspiration);

		var troop = scn.GetShadowCard("troop");
		var blacktraitor = scn.GetShadowCard("blacktraitor");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, blacktraitor);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, troop);
		scn.FreepsResolveSkirmish(avenger);

		assertInZone(Zone.HAND, inspiration);
		assertFalse(scn.FreepsPlayAvailable(inspiration));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundTheMinionIsMinus4ForEachCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var gandalf = scn.GetFreepsCard("gandalf");
		var madril = scn.GetFreepsCard("madril");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(inspiration, madril, avenger);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(inspiration);
		scn.ShadowDismissRevealedCards();

		// Two companions in hand at a battleground: 13 - 4 - 4.
		assertEquals(5, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WhileOnlyTheShadowPlayersOwnFellowshipIsAtTheBattlegroundTheMinionIsMinus3ForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var inspiration = scn.GetFreepsCard("inspiration");
		var gandalf = scn.GetFreepsCard("gandalf");
		var madril = scn.GetFreepsCard("madril");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Each player has moved once, to Uruk Camp, and it is the first player's turn again.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(inspiration, madril, avenger);
		scn.MoveMinionsToTable(troop);
		// The first player's fellowship moves on to site 3, a sanctuary and no battleground.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// "The fellowship" is the one that moved this turn.  The Shadow player's own is still at the battleground.
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.BATTLEGROUND));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(inspiration);
		scn.ShadowDismissRevealedCards();

		// Two companions in hand away from a battleground: 13 - 3 - 3.
		assertEquals(7, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
