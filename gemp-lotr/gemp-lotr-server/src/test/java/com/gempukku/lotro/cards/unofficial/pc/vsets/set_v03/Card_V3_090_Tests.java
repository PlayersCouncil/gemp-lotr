package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herb", "103_90");       // Teach Me Your Herb-lore
					put("frodospipe", "3_107");  // Frodo's Pipe - plays on Frodo
					put("gandalfspipe", "1_74"); // Gandalf's Pipe - plays on Gandalf
					put("aragornspipe", "1_91"); // Aragorn's Pipe - plays on Aragorn

					put("aragorn", "1_89");
					put("gandalf", "1_364");     // Gandalf, The Grey Wizard
					put("theoden", "4_292");     // Théoden, King of the Golden Hall
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TeachMeYourHerbloreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Teach Me Your Herb-lore
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Play up to 2 pipes from your draw deck.  If you cannot spot Theoden, add a burden for each pipe played.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herb");

		assertEquals("Teach Me Your Herb-lore", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void TeachMePlays2PipesFromDeckAndAdds2BurdensWithoutTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(herb);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(frodospipe);

		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragornspipe);

		assertAttachedTo(frodospipe, scn.GetRingBearer());
		assertAttachedTo(aragornspipe, aragorn);
		assertEquals(2, scn.GetBurdens());
	}

	@Test
	public void TeachMeAddsNoBurdensWhenTheodenPresent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var theoden = scn.GetFreepsCard("theoden");

		scn.MoveCardsToHand(herb);
		scn.MoveCompanionsToTable(aragorn, theoden);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(frodospipe);

		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragornspipe);

		assertAttachedTo(frodospipe, scn.GetRingBearer());
		assertAttachedTo(aragornspipe, aragorn);
		assertEquals(0, scn.GetBurdens());
	}

	@Test
	public void TeachMeAdds1BurdenWhenOnly1PipeInDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");

		scn.MoveCardsToHand(herb);
		scn.MoveCardsToDiscard(aragornspipe, gandalfspipe);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(frodospipe);

		scn.DismissRevealedCards();  // Second reveal still happens, just empty

		assertAttachedTo(frodospipe, scn.GetRingBearer());
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void TeachMeAddsNoBurdensWhenNoPipesInDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");

		scn.MoveCardsToHand(herb);
		scn.MoveCardsToDiscard(frodospipe, aragornspipe, gandalfspipe);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(herb);
		scn.DismissRevealedCards();  // First reveal - nothing there
		scn.DismissRevealedCards();  // Second reveal - still nothing

		assertEquals(0, scn.GetBurdens());
		assertInDiscard(herb);
	}

	@Test
	public void TeachMeCanDeclineToPlayPipeWhenAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herb = scn.GetFreepsCard("herb");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToHand(herb);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.FreepsPlayCard(herb);
		scn.DismissRevealedCards();

		// Decline first pipe
		scn.FreepsPass();

		scn.DismissRevealedCards();

		// Decline second pipe
		scn.FreepsChoose("");

		// No pipes played, no burdens
		assertInZone(Zone.DECK, frodospipe);
		assertInZone(Zone.DECK, aragornspipe);
		assertEquals(0, scn.GetBurdens());
	}
}
