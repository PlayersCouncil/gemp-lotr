package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "5_96");
					put("orc", "1_266");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "5_96");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he bears Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand.  Remove (1) for
					// each Orc revealed."  How a Free Peoples player reveals a card from a Shadow player's hand.
					put("glamdring", "1_75");
					put("savage", "1_151");     // Uruk Savage: a second card in the Shadow hand, and no Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EyeofBaradDurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Eye of Barad-Dûr
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a companion or ally skirmishing a [sauron] Orc strength -1 for each Ring-bound companion.<br><b>Response:</b> If a Free Peoples player reveals this card from your hand, discard this card to add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eye");

		assertEquals("Eye of Barad-Dûr", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EyeofBaradDurSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToHand(eye);
		scn.MoveMinionsToTable(orc);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, orc);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(eye));
	}

	@Test
	public void ResponseIsPlayedWhenAFreePeoplesPlayerRevealsItFromHandAndAddsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, savage);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(glamdring));

		scn.FreepsUseCardAction(glamdring);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The Shadow hand is shown to the Free Peoples player, the Eye in it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eye, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		scn.FreepsDismissRevealedCards();

		// The response is the event played from hand (CR 4.2, the ruling on this card: it "must be played in
		// order to use its response text"; "discard this card to" is superfluous).
		assertTrue(scn.ShadowPlayAvailable(eye));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(eye);

		assertEquals(2, scn.GetBurdens());
		assertInDiscard(eye);
		assertInHand(savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
