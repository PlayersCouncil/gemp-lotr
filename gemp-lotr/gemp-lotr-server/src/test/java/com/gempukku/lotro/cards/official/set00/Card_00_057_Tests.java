package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_00_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "0_57");
					put("radagast", "9_26");    // Radagast, The Brown: move limit +1; at each regroup move the Shadow player may discard 2 to draw 2
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion who is no Radagast
					put("follower", "13_38");   // Radagast, Tender of Beasts: a [gandalf] follower titled Radagast; followers bear no cards
					// Cards for the Free Peoples draw deck:
					put("guard1", "1_7");       // Dwarf Guard: companion, no text in play
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RadagastsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Radagast's Staff
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Staff
		 * Strength: 1
		 * Vitality: 1
		 * Game Text: Bearer must be Radagast.<br>Each time the fellowship moves during the regroup phase, you may draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Radagast's Staff", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void BearerMustBeRadagast() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(staff, radagast);

		scn.StartGame();
		// With Gandalf the only Wizard, the Staff has no bearer.
		assertInHand(staff);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(staff));

		scn.FreepsPlayCard(radagast);
		assertInZone(Zone.FREE_CHARACTERS, radagast);
		assertTrue(scn.FreepsPlayAvailable(staff));

		// Radagast is the one bearer it can have, so nobody is asked.
		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, radagast);
		assertEquals(0, scn.GetAttachedCards(gandalf).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFollowerRadagastCannotBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var follower = scn.GetFreepsCard("follower");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(follower);
		scn.MoveCardsToHand(staff);
		assertInZone(Zone.SUPPORT, follower);

		scn.StartGame();
		// A follower is no character and can bear no card (CR 4.2 "follower"), in the support area or borne.
		assertInHand(staff);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(staff));

		scn.AttachCardsTo(gandalf, follower);
		scn.FreepsRefreshDecision();
		assertAttachedTo(follower, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(staff));
	}

	@Test
	public void EachMoveDuringTheRegroupPhaseMayDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);
		scn.AttachCardsTo(radagast, staff);
		assertAttachedTo(staff, radagast);

		scn.StartGame();
		// The Shadow player is kept with no cards, so Radagast's own regroup-move offer to him is never made.
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowHandCount());
		// Radagast's move limit +1: two moves in the regroup phase.
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// The move that ends the fellowship phase draws nothing.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Each time: the second move of the regroup phase offers it again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDrawMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);
		scn.AttachCardsTo(radagast, staff);
		assertAttachedTo(staff, radagast);

		scn.StartGame();
		scn.SetShadowDrawDeck();
		scn.FreepsPass();
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
