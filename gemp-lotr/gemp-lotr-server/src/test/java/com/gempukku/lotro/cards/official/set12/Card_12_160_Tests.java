package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worthy", "12_160");
					put("bloodthirsty", "11_178"); // Bloodthirsty Uruk: [uruk-hai] Uruk-hai; stronger beside an exhausted companion only
					put("saruman", "17_116");    // Saruman, Master of the White Hand: [uruk-hai] Wizard minion; companions resistance -1
					put("guard", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai; Damage +1 is its whole text
					put("alliance", "12_141");   // Dark Alliance: a card in the Shadow hand for muster to discard; a response only
					put("murderous", "11_195");  // Murderous Uruk: [uruk-hai] Uruk-hai printed with muster; no other text
					put("drawn", "12_141");      // a second Dark Alliance, put on top of the Shadow draw deck for muster to draw
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WorthyofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Worthy of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of the regroup phase, you may remove (1) to make an [uruk-hai] minion gain <b>muster</b> until the end of the regroup phase. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetScenario();

		var worthy = scn.GetFreepsCard("worthy");

		assertEquals("Worthy of Mordor", worthy.getBlueprint().getTitle());
		assertNull(worthy.getBlueprint().getSubtitle());
		assertFalse(worthy.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, worthy.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, worthy.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, worthy.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(worthy, Keyword.SUPPORT_AREA));
		assertEquals(0, worthy.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesOneToGiveAnUrukHaiMinionMusterUsedInThatRegroupPhaseAndGoneOnceItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worthy = scn.GetShadowCard("worthy");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var alliance = scn.GetShadowCard("alliance");
		var drawn = scn.GetShadowCard("drawn");
		scn.MoveCardsToSupportArea(worthy);
		scn.MoveCardsToHand(alliance);
		assertInZone(Zone.SUPPORT, worthy);
		assertInHand(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(bloodthirsty, saruman, guard);
		scn.MoveCardsToTopOfDeck(drawn);
		assertEquals(drawn, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, drawn);
		// No assignment is made: the minions stand into the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		int pool = scn.GetTwilight();
		assertTrue(pool > 0);
		assertFalse(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worthy));
		scn.ShadowChooseOptionalTrigger(worthy);
		assertEquals(pool - 1, scn.GetTwilight());

		// [uruk-hai] minions of two races are offered; an Uruk-hai of another culture and this [uruk-hai] condition are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bloodthirsty, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, worthy));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(bloodthirsty);
		assertTrue(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(saruman, Keyword.MUSTER));

		// CR 4.2 "muster" acts at the start of the regroup phase, when the keyword was gained: it is used there.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Use Muster"));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(alliance);
		assertInZone(Zone.DISCARD, alliance);
		assertInHand(drawn);
		assertEquals(1, scn.GetShadowHandCount());

		// Still there at the regroup actions and the move decision, the last of the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		scn.SkipToMovementDecision();
		assertTrue(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));

		// The regroup move ends the regroup phase: in the Shadow phase at site 3 the minion has it no more.
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInPlay(bloodthirsty);
		assertFalse(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesThePoolAndGivesNoMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worthy = scn.GetShadowCard("worthy");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToSupportArea(worthy);
		scn.MoveCardsToHand(alliance);
		assertInZone(Zone.SUPPORT, worthy);
		assertInHand(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(bloodthirsty);
		scn.SkipToPhase(Phase.REGROUP);

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worthy));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(pool, scn.GetTwilight());
		assertFalse(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		// With a card in hand, muster would be offered if the minion had it.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable("Use Muster"));
		assertInHand(alliance);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worthy = scn.GetShadowCard("worthy");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		scn.MoveCardsToSupportArea(worthy);
		assertInZone(Zone.SUPPORT, worthy);

		scn.StartGame();
		scn.MoveMinionsToTable(bloodthirsty);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(worthy));
		assertFalse(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worthy = scn.GetShadowCard("worthy");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		scn.MoveCardsToSupportArea(worthy);
		assertInZone(Zone.SUPPORT, worthy);

		scn.StartGame();
		scn.MoveMinionsToTable(bloodthirsty);
		scn.FreepsPass();
		assertTrue(scn.GetTwilight() > 0);
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(worthy));
		assertFalse(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAgainAtTheSecondRegroupPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worthy = scn.GetShadowCard("worthy");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		scn.MoveCardsToSupportArea(worthy);
		assertInZone(Zone.SUPPORT, worthy);

		scn.StartGame();
		scn.MoveMinionsToTable(bloodthirsty);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worthy));
		scn.ShadowChooseOptionalTrigger(worthy);
		assertTrue(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertFalse(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		scn.SkipToPhase(Phase.REGROUP);

		int pool = scn.GetTwilight();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worthy));
		scn.ShadowChooseOptionalTrigger(worthy);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.HasKeyword(bloodthirsty, Keyword.MUSTER));
		// The Shadow player drew a hand on reconciling at the first move: his muster is offered, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Use Muster"));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// A keyword given to a card that has it already is still an offer (Q-0245: A).
	@Test
	public void TheOfferIsMadeAndTheTwilightRemovedWhenTheOnlyUrukHaiMinionHasMusterAlready() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worthy = scn.GetShadowCard("worthy");
		var murderous = scn.GetShadowCard("murderous");
		scn.MoveCardsToSupportArea(worthy);
		assertInZone(Zone.SUPPORT, worthy);

		scn.StartGame();
		scn.MoveMinionsToTable(murderous);
		scn.SkipToPhase(Phase.REGROUP);

		int pool = scn.GetTwilight();
		assertTrue(scn.HasKeyword(murderous, Keyword.MUSTER));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worthy));
		scn.ShadowChooseOptionalTrigger(worthy);
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.HasKeyword(murderous, Keyword.MUSTER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
