package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stands", "7_95");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no other text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; to play, spot a [gondor] companion
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ally, no companion; an ability that needs initiative
					put("guard", "5_83");       // Household Guard: a [rohan] companion, valiant, no other text
					put("parapet", "5_87");     // Parapet: a [rohan] fortification
					put("rally", "5_38");       // Rally Point: a [gondor] condition that is no fortification
					put("filler", "1_7");       // Dwarf Guard: a card laid on top of the Free Peoples draw deck
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFortificationScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stands", "7_95");
					put("citadel", "5_32");     // Citadel of the Stars: a [gondor] fortification
					put("wall", "5_33");        // City Wall: a [gondor] fortification
					put("spear", "7_108");      // Knight's Spear: a [gondor] possession, and no fortification
					put("guard", "1_7");        // Dwarf Guard: the one card of the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorStillStandsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondor Still Stands
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 [gondor] companions.<br>While no opponent controls a site, wound a minion at the start of each regroup phase.<br><b>Regroup:</b> Discard this condition to place a [gondor] fortification from your discard pile on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stands");

		assertEquals("Gondor Still Stands", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsItselfToPlaceAGondorFortificationFromYourDiscardPileOnTopOfYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFortificationScenario();

		var stands = scn.GetFreepsCard("stands");
		var citadel = scn.GetFreepsCard("citadel");
		var wall = scn.GetFreepsCard("wall");
		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(stands);
		scn.MoveCardsToDiscard(citadel, wall, spear);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stands);
		assertInZone(Zone.DISCARD, citadel, wall, spear);
		// No minion is in play, so the wound of its own trigger at the start of the regroup phase falls on nobody.
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SUPPORT, stands);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertTrue(scn.FreepsActionAvailable(stands));

		scn.FreepsUseCardAction(stands);
		assertInZone(Zone.DISCARD, stands);
		// The two fortifications; not the [gondor] possession, nor Gondor Still Stands itself, which lies there now.
		assertTrue(scn.FreepsHasCardChoiceAvailable(citadel, wall));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spear, stands));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(citadel);

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(citadel, scn.GetFreepsTopOfDeck());
		assertEquals(guard, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, wall, spear, stands);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilThreeGondorCompanionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stands = scn.GetFreepsCard("stands");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, knight, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(stands, pippin);

		scn.StartGame();

		// Two [gondor] companions: the Merchant is [gondor] but an ally, the Household Guard a companion but [rohan].
		assertInZone(Zone.HAND, stands, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stands));

		// Pippin, a [gondor] companion who is a Hobbit, makes three.
		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.FreepsPlayAvailable(stands));
		scn.FreepsPlayCard(stands);
		assertInZone(Zone.SUPPORT, stands);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WoundsAMinionAtTheStartOfEachRegroupPhaseWhileNoOpponentControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stands = scn.GetFreepsCard("stands");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stands);

		scn.StartGame();
		scn.SkipToSite(2);
		// A site the card's own player controls is no site an opponent controls.
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SUPPORT, stands);
		scn.MoveMinionsToTable(troop, brigand);

		// Nothing at the start of the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Nobody is assigned, so both minions are still there in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		// Either minion; no companion. The Brigand has 1 vitality and is killed by the wound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(brigand);
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, and a second regroup phase in the same turn: the Troop, the one minion left, is wounded.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoMinionIsWoundedWhileAnOpponentControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stands = scn.GetFreepsCard("stands");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(stands);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, stands);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhaseAndWithNoGondorFortificationInThePilePlacesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stands = scn.GetFreepsCard("stands");
		var parapet = scn.GetFreepsCard("parapet");
		var rally = scn.GetFreepsCard("rally");
		var filler = scn.GetFreepsCard("filler");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(stands);
		scn.MoveCardsToDiscard(parapet, rally);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(filler);
		assertEquals(filler, scn.GetFreepsTopOfDeck());
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, stands);
		assertInZone(Zone.DISCARD, parapet, rally);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(stands));

		// At the start of the regroup phase the one minion takes the trigger's wound, unasked.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(1, scn.GetWoundsOn(troop));

		var deckCount = scn.GetFreepsDeckCount();
		assertEquals(filler, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(stands));

		// Parapet is a fortification of another culture, Rally Point a [gondor] card that is none: nothing is placed.
		scn.FreepsUseCardAction(stands);
		assertInZone(Zone.DISCARD, stands, parapet, rally);
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertEquals(filler, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
