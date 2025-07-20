package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haste", "103_4");
					put("gandalf", "1_364");
					put("wielder", "2_28"); // Maneuver event with no exert cost

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShowThemtheMeaningofHasteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Show Them the Meaning of Haste
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Exert Gandalf thrice (or spot Shadowfax and exert Gandalf once) to reveal a Free Peoples event from your hand.  Play that event 3 times, then remove it from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haste");

		assertEquals("Show Them the Meaning of Haste", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShowThemtheMeaningofHastePlaysANonExertingEventThrice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var wielder = scn.GetFreepsCard("wielder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(haste, wielder);
		scn.MoveCompanionsToTable(gandalf);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(wielder));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertInZone(Zone.HAND, wielder);

		scn.FreepsPlayCard(haste);
		assertFalse(scn.HasKeyword(gandalf, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		//Wielder was automatically chosen as the only valid target
		scn.ShadowDismissRevealedCards();

		scn.ShadowChooseNo(); //Remove (3) to prevent giving companion Defender +1
		scn.FreepsChooseCard(gandalf);
		scn.ShadowChooseNo(); //Remove (3) to prevent giving companion Defender +1
		scn.FreepsChooseCard(gandalf);
		scn.ShadowChooseNo(); //Remove (3) to prevent giving companion Defender +1
		scn.FreepsChooseCard(gandalf);

		assertEquals(3, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertInZone(Zone.REMOVED, wielder);
	}
}
