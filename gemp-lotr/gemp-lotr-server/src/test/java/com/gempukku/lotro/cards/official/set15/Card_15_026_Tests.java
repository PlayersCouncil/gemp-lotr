package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruviel", "15_26");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; +3 only against a Nazgul
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, vitality 3; a Fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a Maneuver ability used only on purpose
					put("dinendal", "15_12");   // Dinendal, Mirkwood Archer: another [elven] follower, Aid (3); exerts no [elven] bearer

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UruvielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruviel, Woodland Maid
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype: 
		 * Resistance: 1
		 * Game Text: <b>Aid</b> – (1). <helper>(At the start of the maneuver phase, you may add (1) to transfer this to a companion.)</helper><br>Each time you transfer this to a companion, you may exert an Elf to make the fellowship's current site a <b>forest</b> until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruviel");

		assertEquals("Uruviel", card.getBlueprint().getTitle());
		assertEquals("Woodland Maid", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
		assertEquals(1, scn.GetAidTwilightCost(card));
	}

	@Test
	public void TransferredByItsAidItMayExertAnElfToMakeTheSiteAForestUntilTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(uruviel);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var site2 = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, uruviel);
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
		int pool = scn.GetTwilight();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(pool + 1, scn.GetTwilight());
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(uruviel, aragorn);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Optional trigger"));
		scn.FreepsAcceptOptionalTrigger();

		// An Elf, companion or ally; not the Men.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(celeborn);

		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.HasKeyword(site2, Keyword.FOREST));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Still a forest in the regroup phase, and after the fellowship has moved on; the site moved to is not made one.
		scn.SkipToMovementDecision();
		assertTrue(scn.HasKeyword(site2, Keyword.FOREST));
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		var site3 = scn.GetCurrentSite();
		assertTrue(scn.ShadowDecisionAvailable("Play Shadow action or Pass"));
		assertTrue(scn.HasKeyword(site2, Keyword.FOREST));
		assertFalse(scn.HasKeyword(site3, Keyword.FOREST));

		// The next turn: gone.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
	}

	@Test
	public void DeclinedNoElfExertsAndTheSiteIsNoForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(uruviel);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, uruviel);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var site2 = scn.GetCurrentSite();
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(uruviel, arwen);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Optional trigger"));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherElvenFollowerTransferredOrItsOwnAidDeclinedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var dinendal = scn.GetFreepsCard("dinendal");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(uruviel, dinendal);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, uruviel, dinendal);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var site2 = scn.GetCurrentSite();
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dinendal, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Aid"));
		scn.FreepsChooseOptionalTrigger(dinendal, "Aid");
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(dinendal, arwen);

		// Dinendal's transfer sets nothing of Uruviel's off; her own Aid is still on offer, and is declined.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Optional trigger"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Aid"));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, uruviel);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheOnlyElfExhaustedNothingIsOfferedOnTheTransfer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToSupportArea(uruviel);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, uruviel);
		// Vitality 3 with 2 wounds; Aragorn, who is no Elf, could still exert.
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var site2 = scn.GetCurrentSite();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(uruviel, aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Optional trigger"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(site2, Keyword.FOREST));
	}

	@Test
	public void TransferredAgainInASecondManeuverPhaseItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(uruviel);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, uruviel);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var site2 = scn.GetCurrentSite();
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.HasKeyword(site2, Keyword.FOREST));

		// Nobody is assigned to the Troop; the fellowship moves on, and Uruviel is still borne.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		var site3 = scn.GetCurrentSite();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(uruviel, aragorn);
		assertFalse(scn.HasKeyword(site3, Keyword.FOREST));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(uruviel, arwen);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel, "Optional trigger"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(celeborn);

		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.HasKeyword(site3, Keyword.FOREST));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
