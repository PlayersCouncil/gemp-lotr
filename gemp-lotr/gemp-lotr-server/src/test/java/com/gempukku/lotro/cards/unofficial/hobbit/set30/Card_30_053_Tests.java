package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_053_Tests
{

	// The Hobbit table: the Hobbit path, with Mirkwood as site 5, Bilbo, Reliable Companion 30_46 as the
	// Ring-bearer and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gandalf, The Grey: [gandalf] Wizard companion; a trigger on [gandalf] events and a Fellowship
					// ability, neither used.  Nothing of his text keeps him from being discarded.
					put("gandalf", "30_27");
					// Gandalf, Friend of the Shirefolk: a Gandalf from outside the Hobbit sets, with a strength
					// modifier only.
					put("shirefolk", "1_72");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; a strength modifier at 3 Ents only.
					put("avenger", "6_27");
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void MirkwoodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Mirkwood
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: none
		 * Game Text: <b>Forest</b>. When the fellowship moves to Mirkwood, discard Gandalf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Mirkwood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void MovingToMirkwoodDiscardsGandalfAndNoOtherGandalfCompanionAndMovingOnDiscardsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var shirefolk = scn.GetFreepsCard("shirefolk");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, avenger);

		assertEquals("30_52", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.FREE_CHARACTERS, gandalf, avenger);

		scn.FreepsPass();

		// Discarded as the fellowship arrives, with nothing asked; the Ent Avenger stays.
		assertEquals("30_53", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Gandalf in the fellowship at Mirkwood moves on with it to Esgaroth.
		scn.MoveCompanionsToTable(shirefolk);
		assertInZone(Zone.FREE_CHARACTERS, shirefolk);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("30_54", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.FREE_CHARACTERS, shirefolk, avenger);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GandalfStaysOnTheMoveToGoblinTownAndIsDiscardedOnTheRegroupMoveToMirkwood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shirefolk = scn.GetFreepsCard("shirefolk");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(shirefolk);

		assertInZone(Zone.FREE_CHARACTERS, shirefolk);

		scn.FreepsPass();

		assertEquals("30_52", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.FREE_CHARACTERS, shirefolk);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("30_53", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.DISCARD, shirefolk);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
