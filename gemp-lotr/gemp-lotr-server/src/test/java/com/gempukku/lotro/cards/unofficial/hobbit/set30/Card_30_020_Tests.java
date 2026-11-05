package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_020_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The fellowship phase
	// of turn 1 is at Bag End (site 1), whose Fellowship action plays a [dwarven] follower from hand.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kinsfolk", "30_20");

					// Followers. Bifur: [dwarven] Dwarf. Roäc: [dwarven] Bird. Dori: [dwarven] Dwarf (the Shadow
					// player's copy, in his own discard pile). Gwaihir: a [gandalf] Eagle.
					put("bifur", "30_5");
					put("bifur2", "30_5");
					put("roac", "32_8");
					put("dori", "30_8");
					put("gwaihir", "31_15");
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ToMeOMyKinsfolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: To me! O my kinsfolk!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Play a [dwarven] follower from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kinsfolk");

		assertEquals("To me! O my kinsfolk!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysADwarvenFollowerOfEitherRaceFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kinsfolk = scn.GetFreepsCard("kinsfolk");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		var shadowDori = scn.GetShadowCard("dori");
		scn.MoveCardsToHand(kinsfolk);
		scn.MoveCardsToDiscard(bifur, roac, gwaihir, axe, shadowDori);

		scn.StartGame(false);
		assertInHand(kinsfolk);
		assertInDiscard(bifur, roac, gwaihir, axe, shadowDori);

		assertTrue(scn.FreepsPlayAvailable(kinsfolk));
		scn.FreepsPlayCard(kinsfolk);

		// A [dwarven] Dwarf and a [dwarven] Bird; not a [gandalf] follower, a [dwarven] possession, or the other
		// player's [dwarven] follower in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, axe, shadowDori));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(roac);

		assertInZone(Zone.SUPPORT, roac);
		assertInDiscard(kinsfolk, bifur, gwaihir, axe, shadowDori);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableWithNoDwarvenFollowerInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kinsfolk = scn.GetFreepsCard("kinsfolk");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		var shadowDori = scn.GetShadowCard("dori");
		scn.MoveCardsToHand(kinsfolk);
		scn.MoveCardsToDiscard(gwaihir, axe, shadowDori);

		scn.StartGame(false);
		assertInHand(kinsfolk);
		assertInDiscard(gwaihir, axe, shadowDori);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(kinsfolk));
	}

	@Test
	public void NotPlayableWhenTheOnlyDwarvenFollowerThereIsACopyOfOneInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kinsfolk = scn.GetFreepsCard("kinsfolk");
		var bifur = scn.GetFreepsCard("bifur");
		var bifur2 = scn.GetFreepsCard("bifur2");
		scn.MoveCardsToHand(kinsfolk);
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToDiscard(bifur2);

		scn.StartGame(false);
		assertInHand(kinsfolk);
		assertInZone(Zone.SUPPORT, bifur);
		assertInDiscard(bifur2);

		// Bifur is unique and already in play: the copy in the discard pile cannot be played.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(kinsfolk));
	}
}
