package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "11_54");

					put("sword", "1_299");      // Hobbit Sword: a card for the Free Peoples hand; no text but its bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Aragorn, Strider
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ranger</b>.<br>Each time the fellowship moves, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Strider", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void EachMoveOfTheFellowshipAddsOneTheRegroupMoveAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// To Ettenmoors: 2 for its Shadow number + 2 for Frodo and Aragorn + 1 for Aragorn's text.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// To Rivendell Valley: 0 for its Shadow number + 2 for the companions + 1 for Aragorn's text.
		assertEquals(pool + 3, scn.GetTwilight());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		// A card in hand, so that the Free Peoples player is asked to reconcile and the turn does not end at once.
		scn.SetFreepsHand(sword);
		int pool = scn.GetTwilight();

		scn.FreepsChooseToStay();
		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}
}
