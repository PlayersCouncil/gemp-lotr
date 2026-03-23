package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_004_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Bilbo's Pipe (1_285): FP Shire Possession, Pipe, targets hobbit, twilight 1
		put("pipe", "1_285");
		// Old Toby (1_305): FP Shire Possession, Pipeweed, support area, twilight 1
		put("weed1", "1_305");
		put("weed2", "1_305");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_4", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_4"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_4
		 * Type: MetaSite
		 * Game Text: Fellowship: Play a pipe to play a pipeweed from your draw deck (or vice versa).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_4", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void PlayPipeToPlayPipeweedFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: Play a pipe from hand → play a pipeweed from draw deck.
		// Only pipe in hand, so it auto-selects as the cost.
		// Pipe auto-attaches to Frodo (only hobbit).
		// Then deck search for pipeweed.

		var scn = GetFreepsScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");

		scn.MoveCardsToHand(pipe);
		// weed1 stays in deck

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(scn.GetFreepsCard("mod")));
		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));

		// Pipe auto-selected (only pipe/pipeweed in hand), auto-attached to Frodo (only hobbit)
		// Now searching deck for pipeweed — dismiss revealed cards, then choose
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(weed1);

		assertInZone(Zone.SUPPORT, weed1);
		assertInZone(Zone.ATTACHED, pipe);
	}

	@Test
	public void PlayPipeweedToPlayPipeFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: Play a pipeweed from hand → play a pipe from draw deck.

		var scn = GetFreepsScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");

		scn.MoveCardsToHand(weed1);
		// pipe stays in deck

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(scn.GetFreepsCard("mod")));
		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));

		// Weed auto-selected (only pipe/pipeweed in hand), plays to support area
		scn.FreepsDeclineOptionalTrigger(); //old toby's text
		// Now searching deck for pipe — dismiss revealed cards, then choose
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(pipe);

		// Pipe auto-attaches to Frodo
		assertInZone(Zone.ATTACHED, pipe);
		assertInZone(Zone.SUPPORT, weed1);
	}

	@Test
	public void ChoosesBetweenPipeAndPipeweedInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: With both pipe and pipeweed in hand, player chooses which to play as cost.

		var scn = GetFreepsScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var weed1 = scn.GetFreepsCard("weed1");
		var weed2 = scn.GetFreepsCard("weed2");

		scn.MoveCardsToHand(pipe, weed1);
		// weed2 stays in deck

		scn.StartGame();

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));

		// Should offer choice between pipe and weed1
		assertTrue(scn.FreepsHasCardChoiceAvailable(pipe, weed1));

		// Choose pipe → should then search for pipeweed
		scn.FreepsChooseCard(pipe);

		// Pipe auto-attaches to Frodo
		// Now searching deck for pipeweed
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(weed2);

		assertInZone(Zone.ATTACHED, pipe);
		assertInZone(Zone.SUPPORT, weed2);
	}

	@Test
	public void CannotUseWithoutPipeOrPipeweedInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: No pipe or pipeweed in hand = ability not available.

		var scn = GetFreepsScenario();

		// Don't put any pipes or pipeweeds in hand

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(scn.GetFreepsCard("mod")));
	}

	@Test
	public void ShadowCopyDoesNotWorkForFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_4: Shadow's copy should not give FP the ability.

		var scn = GetShadowScenario();

		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCardsToHand(pipe);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(scn.GetShadowCard("mod")));
	}
}
