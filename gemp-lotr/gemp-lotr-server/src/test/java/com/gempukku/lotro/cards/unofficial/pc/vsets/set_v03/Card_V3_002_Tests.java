package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_V3_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jetsam", "103_2");
					put("gandalf", "1_364");
					put("gandalfspipe", "1_74");
					put("frodospipe", "3_107");
					put("bladetip", "1_209");
					put("treebeard", "6_37");
					put("oldtoby1", "1_305");
					put("oldtoby2", "1_305");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JetsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Jetsam
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Pipeweed. Bearer must bear a pipe. When you play this, you may discard a Shadow card on bearer.
		* 	When this is discarded by a pipe, add 1 to the number of pipes you can spot this phase and make your character lose <b>unhasty</b> until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jetsam");

		assertEquals("Jetsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void JetsamRequiresBearerToHavePipe() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var jetsam = scn.GetFreepsCard("jetsam");
		var frodospipe = scn.GetFreepsCard("frodospipe");

		scn.MoveCardsToHand(jetsam, frodospipe);

		scn.StartGame();

		// Cannot play Jetsam without a pipe on Frodo
		assertFalse(scn.FreepsPlayAvailable(jetsam));

		// Play Frodo's Pipe first
		scn.FreepsPlayCard(frodospipe);
		assertAttachedTo(frodospipe, frodo);

		// Now Jetsam can be played
		assertTrue(scn.FreepsPlayAvailable(jetsam));
		scn.FreepsPlayCard(jetsam);
		assertAttachedTo(jetsam, frodo);
	}

	@Test
	public void JetsamOptionallyDiscardsShadowCardOnBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var jetsam = scn.GetFreepsCard("jetsam");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var bladetip = scn.GetShadowCard("bladetip");

		scn.AttachCardsTo(frodo, frodospipe, bladetip);
		scn.MoveCardsToHand(jetsam);

		scn.StartGame();

		assertAttachedTo(bladetip, frodo);

		scn.FreepsPlayCard(jetsam);

		// Should have optional trigger to discard Shadow card
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Should be able to choose Blade Tip
		scn.FreepsChooseCard(bladetip);

		assertEquals(Zone.DISCARD, bladetip.getZone());
		assertAttachedTo(jetsam, frodo);
	}

	@Test
	public void JetsamCanDeclineShadowCardDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var jetsam = scn.GetFreepsCard("jetsam");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var bladetip = scn.GetShadowCard("bladetip");

		scn.AttachCardsTo(frodo, frodospipe, bladetip);
		scn.MoveCardsToHand(jetsam);

		scn.StartGame();

		assertAttachedTo(bladetip, frodo);

		scn.FreepsPlayCard(jetsam);

		// Decline optional trigger
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Blade Tip should remain attached
		assertAttachedTo(bladetip, frodo);
		assertAttachedTo(jetsam, frodo);
	}

	@Test
	public void JetsamIncreasesSpottedPipeCountWhenDiscardedByPipe() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var jetsam = scn.GetFreepsCard("jetsam");
		var oldtoby1 = scn.GetFreepsCard("oldtoby1");
		var oldtoby2 = scn.GetFreepsCard("oldtoby2");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(oldtoby1);
		scn.AttachCardsTo(gandalf, gandalfspipe);
		scn.MoveCardsToHand(jetsam, oldtoby2);

		scn.StartGame();

		// First use: Only 1 pipe (Gandalf's Pipe), should spot 1
		scn.FreepsUseCardAction(gandalfspipe);
		// Old Toby auto-selected as only pipeweed
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// Play Jetsam and another pipeweed
		scn.FreepsPlayCard(oldtoby2);
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPlayCard(jetsam);
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(jetsam, gandalf);

		// Second use: Discard Jetsam, should spot 2 (1 pipe + Jetsam modifier)
		scn.FreepsUseCardAction(gandalfspipe);
		scn.FreepsChooseCard(jetsam); // Choose which pipeweed to discard
		assertEquals(Zone.DISCARD, jetsam.getZone());
		assertEquals(2, scn.FreepsGetChoiceMax()); // 1 real pipe + 1 from Jetsam
		scn.FreepsChoose("2");

		// Third use: Jetsam in discard, modifier persists this phase, still spot 2
		scn.FreepsUseCardAction(gandalfspipe);
		// Old Toby auto-selected as only remaining pipeweed
		assertEquals(2, scn.FreepsGetChoiceMax()); // Modifier still active this phase
		scn.FreepsChoose("2");
	}

	@Test
	public void JetsamRemovesUnhastyWhenDiscardedByPipe() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var jetsam = scn.GetFreepsCard("jetsam");

		scn.MoveCompanionsToTable(gandalf, treebeard);
		scn.AttachCardsTo(gandalf, gandalfspipe, jetsam);

		scn.StartGame();

		assertTrue(scn.HasKeyword(treebeard, Keyword.UNHASTY));

		// Use Gandalf's Pipe to discard Jetsam
		scn.FreepsUseCardAction(gandalfspipe);
		// Jetsam auto-selected as only pipeweed
		assertEquals(Zone.DISCARD, jetsam.getZone());

		// Choose number of burdens to remove
		scn.FreepsChoose("1");

		// Should have choice to remove unhasty from a character
		scn.FreepsChooseCard(treebeard);

		assertFalse(scn.HasKeyword(treebeard, Keyword.UNHASTY));

		// Verify it returns at end of turn
		scn.SkipCurrentSite();

		// At start of next turn, unhasty returns
		assertTrue(scn.HasKeyword(treebeard, Keyword.UNHASTY));
	}
}
