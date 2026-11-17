package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_271_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axeman", "7_271");
					put("axeman2", "7_271");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), strength 9, damage +1
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), strength 5; his text needs 2 wounds on the Ring-bearer
					put("teeth", "5_110");      // Teeth of Mordor: a [sauron] card that is no minion (a support-area condition)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("hand1", "1_7");        // Dwarf Guard: cards for the Free Peoples hand, which decides initiative
					put("hand2", "1_7");
					put("hand3", "1_7");
					put("hand4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothAxemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Axeman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.
		 * <b>Skirmish:</b> Discard this minion to make a [sauron] minion strength +1 for each site you control (or +2 if you have initiative).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axeman");

		assertEquals("Gorgoroth Axeman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithoutInitiativeItDiscardsItselfToMakeASauronMinionStrengthPlusOneForEachSiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		var teeth = scn.GetShadowCard("teeth");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player, sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(axeman, chieftain, guard, hound);
		scn.MoveCardsToSupportArea(teeth);
		// Four cards in the Free Peoples hand: the Shadow has no initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		assertEquals(4, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertTrue(scn.ShadowControls(scn.GetSite(3)));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{frodo, hound});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.ShadowActionAvailable(axeman));

		scn.ShadowUseCardAction(axeman);

		assertInDiscard(axeman);
		// The [sauron] Orc in the skirmish and the [sauron] Uruk-hai in none; not the [wraith] Orc nor the [sauron] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, teeth));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		// 9 + 1 for each of his two sites; the Free Peoples player's site is not counted.
		assertEquals(11, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithInitiativeItIsPlusTwoForEachSiteYouControlUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		var chieftain = scn.GetShadowCard("chieftain");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(axeman, axeman2, chieftain, hound);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.SetFreepsHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowControls(scn.GetSite(1)));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{frodo, hound});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetStrength(chieftain));

		scn.ShadowUseCardAction(axeman);
		scn.ShadowChooseCard(chieftain);

		// 7 + 2 for his one site
		assertInDiscard(axeman);
		assertEquals(9, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();

		// The Chieftain is the one [sauron] minion left, so he is chosen with no question.
		scn.ShadowUseCardAction(axeman2);

		// 9 + 2 for each of his two sites
		assertInDiscard(axeman2);
		assertEquals(13, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		// Aragorn lost to 13 and took a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(frodo);
		// The next skirmish is a phase of its own: the Chieftain is back to his printed 7.
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoSiteOfItsOwnItIsStillOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		// The one site controlled is the Free Peoples player's.
		scn.FreepsTakeControlOfSite();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(axeman, chieftain, guard);
		// An empty Free Peoples hand: the Shadow has initiative, and 2 for each of no sites is still nothing.
		scn.SetFreepsHand();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsControls(scn.GetSite(1)));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.ShadowActionAvailable(axeman));

		scn.ShadowUseCardAction(axeman);
		scn.ShadowChooseCard(chieftain);

		assertInDiscard(axeman);
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseAndIsInTheSkirmishThatFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(axeman, chieftain);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(axeman));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(axeman));
	}
}
