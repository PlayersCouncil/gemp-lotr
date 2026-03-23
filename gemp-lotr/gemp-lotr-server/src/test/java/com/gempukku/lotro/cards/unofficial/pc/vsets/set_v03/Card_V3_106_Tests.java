package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flotsam", "103_106");
					put("flotsam2", "103_106");     // Second copy (pipeweed, discard target)
					put("pipe", "1_285");           // Bilbo's Pipe (unique, bearer must be Hobbit)
					put("pipeweed1", "1_300");      // Longbottom Leaf (pipeweed)
					put("pipeweed2", "1_305");      // Old Toby (pipeweed)
					put("runner", "1_178");         // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlotsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Flotsam
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: Pipeweed. Bearer must bear a pipe.
		 * 	When you play this possession, you may shuffle up to 2 pipes or pipeweed
		 * 	from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flotsam");

		assertEquals("Flotsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FlotsamRequiresBearerWithPipe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var flotsam = scn.GetFreepsCard("flotsam");
		var pipe = scn.GetFreepsCard("pipe");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");

		scn.MoveCardsToHand(flotsam, pipe);
		scn.MoveCardsToDiscard(pipeweed1);  // Ensure trigger has a target

		scn.StartGame();

		// No one has a pipe — Flotsam cannot be played
		assertFalse(scn.FreepsPlayAvailable(flotsam));

		// Play Bilbo's Pipe — auto-attaches to Frodo (only Hobbit)
		scn.FreepsPlayCard(pipe);

		// Now Flotsam can be played (Frodo bears a pipe)
		assertTrue(scn.FreepsPlayAvailable(flotsam));
		scn.FreepsPlayCard(flotsam);
		// Auto-attaches to Frodo (only character with a pipe)

		assertAttachedTo(flotsam, frodo);
	}

	@Test
	public void FlotsamOnPlayShufflesPipesAndPipeweedFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var pipe = scn.GetFreepsCard("pipe");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToHand(flotsam);
		scn.MoveCardsToDiscard(pipeweed1, pipeweed2);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsDiscardCount());
		int deckBefore = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(flotsam);
		scn.FreepsAcceptOptionalTrigger();

		// Choose both pipeweed cards to shuffle back
		scn.FreepsChooseCards(pipeweed1, pipeweed2);

		// Both left the discard and went to the draw deck
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(deckBefore + 2, scn.GetFreepsDeckCount());
	}

	@Test
	public void FlotsamOnPlayTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var pipe = scn.GetFreepsCard("pipe");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToHand(flotsam);
		scn.MoveCardsToDiscard(pipeweed1, pipeweed2);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(flotsam);
		scn.FreepsDeclineOptionalTrigger();

		// Cards remain in discard
		assertEquals(2, scn.GetFreepsDiscardCount());
	}

	@Test
	public void FlotsamOnPlayTriggerLimitedToTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var flotsam2 = scn.GetFreepsCard("flotsam2");
		var pipe = scn.GetFreepsCard("pipe");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToHand(flotsam);
		scn.MoveCardsToDiscard(pipeweed1, pipeweed2, flotsam2);  // 3 valid targets

		scn.StartGame();

		assertEquals(3, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(flotsam);
		scn.FreepsAcceptOptionalTrigger();

		// All 3 are valid targets
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipeweed1, pipeweed2, flotsam2));

		// Choose 2 of the 3 (limit enforced by "up to 2")
		scn.FreepsChooseCards(pipeweed1, pipeweed2);

		// Only 2 shuffled; flotsam2 remains in discard
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(flotsam2);
	}

	@Test
	public void FlotsamHasNoSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("flotsam");
		var pipe = scn.GetFreepsCard("pipe");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(frodo, flotsam);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// During skirmish, Flotsam should NOT have an available action (errata removed it)
		assertFalse(scn.FreepsActionAvailable(flotsam));
	}
}
