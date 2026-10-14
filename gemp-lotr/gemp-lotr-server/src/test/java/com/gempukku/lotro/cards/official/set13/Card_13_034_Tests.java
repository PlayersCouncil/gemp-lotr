package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("look", "13_34");
					put("look2", "13_34");
					// Gandalf, The Grey Wizard: a fellowship ability only
					put("gandalf", "1_364");
					// Ent Avenger: a [gandalf] companion who is no Gandalf, strength 6 (+2 only beside 3 Ents)
					put("avenger", "6_27");
					// Barliman Butterbur: a [gandalf] ally; a fellowship ability only
					put("barliman", "1_70");

					// [gandalf] cards of five kinds for the discard pile
					put("avenger2", "6_27");     // a companion
					put("glamdring", "1_75");    // Glamdring: a possession
					put("staff", "2_22");        // Gandalf's Staff: an artifact
					put("walkers", "1_79");      // The Nine Walkers: a condition
					put("wizard", "1_78");       // Mysterious Wizard: an event
					// A card of another culture for the discard pile
					put("knight", "5_35");       // Gondorian Knight
					// A [gandalf] card of a title that only the Shadow player's discard pile holds
					put("pipe", "1_74");         // Gandalf's Pipe

					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LookToMyComingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Look to My Coming
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 other [gandalf] cards in your discard pile to play this event from your discard pile. Then remove this event from the game.<br>Spot Gandalf to make your [gandalf] companion strength +3.
		*/

		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");

		assertEquals("Look to My Coming", look.getBlueprint().getTitle());
		assertNull(look.getBlueprint().getSubtitle());
		assertFalse(look.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, look.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, look.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, look.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(look, Timeword.SKIRMISH));
		assertEquals(2, look.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromTheDiscardPileByRemovingFourOtherGandalfCardsItMakesGandalfStrengthPlusThreeAndIsRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var look2 = scn.GetFreepsCard("look2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var walkers = scn.GetFreepsCard("walkers");
		var wizard = scn.GetFreepsCard("wizard");
		var knight = scn.GetFreepsCard("knight");
		var pipe = scn.GetShadowCard("pipe");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToDiscard(look, avenger2, glamdring, staff, walkers, wizard, knight);
		scn.MoveCardsToDiscard(pipe);
		scn.MoveCardsToHand(look2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gandalf, runner1},
				new PhysicalCardImpl[]{frodo, runner2});
		scn.FreepsResolveSkirmish(gandalf);

		assertInZone(Zone.DISCARD, look, avenger2, glamdring, staff, walkers, wizard, knight, pipe);
		assertInZone(Zone.HAND, look2);
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(look));

		scn.FreepsUseCardAction(look);
		// Five other [gandalf] cards, of five kinds, for the four. Not the event itself, a card of another
		// culture, or the [gandalf] card in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger2, glamdring, staff, walkers, wizard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(look, knight, pipe));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(avenger2, glamdring, staff, walkers);
		assertInZone(Zone.REMOVED, avenger2, glamdring, staff, walkers);
		assertInZone(Zone.DISCARD, wizard, knight, pipe);

		// Your [gandalf] companion: Gandalf in the skirmish and Ent Avenger in none; not Frodo (a [shire]
		// companion) or Barliman Butterbur (a [gandalf] ally).
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		// The copy played is removed from the game; the copy in hand stays there.
		assertInZone(Zone.REMOVED, look);
		assertInZone(Zone.HAND, look2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf, 10, beats his Runner. In the next skirmish, a phase of its own, he is 7 again.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(gandalf));
	}

	@Test
	public void NotPlayableFromTheDiscardPileWithOnlyThreeOtherGandalfCardsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var walkers = scn.GetFreepsCard("walkers");
		var wizard = scn.GetFreepsCard("wizard");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(gandalf);
		// Four [gandalf] cards with the event itself, and a fifth card of another culture.
		scn.MoveCardsToDiscard(look, glamdring, staff, walkers, knight);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);

		assertInZone(Zone.DISCARD, look, glamdring, staff, walkers, knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(look));

		// A fourth other [gandalf] card.
		scn.MoveCardsToDiscard(wizard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(look));
	}

	@Test
	public void NotPlayableWithoutGandalfAndFromHandItGoesToTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var look2 = scn.GetFreepsCard("look2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var walkers = scn.GetFreepsCard("walkers");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(look);
		// The second copy lies in the discard pile with four other [gandalf] cards.
		scn.MoveCardsToDiscard(look2, glamdring, staff, walkers, wizard);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, runner1);
		scn.FreepsResolveSkirmish(avenger);

		// A [gandalf] companion who is no Gandalf: neither copy is offered.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(look));
		assertFalse(scn.FreepsActionAvailable(look2));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(look));
		assertTrue(scn.FreepsActionAvailable(look2));

		// Played from hand: nothing is removed from the discard pile, and Ent Avenger, in the skirmish, is
		// chosen over Gandalf, in none.
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(look);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		scn.FreepsChooseCard(avenger);
		assertEquals(9, scn.GetStrength(avenger));
		assertEquals(7, scn.GetStrength(gandalf));
		assertInZone(Zone.DISCARD, look, look2, glamdring, staff, walkers, wizard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
