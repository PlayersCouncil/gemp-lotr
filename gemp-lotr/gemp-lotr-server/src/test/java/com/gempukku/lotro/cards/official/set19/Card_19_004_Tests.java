package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "19_4");
					put("gimli", "19_2");       // Gimli, Opinionated Guide: a fellowship companion, vitality 3
					put("swarms1", "1_183");    // Goblin Swarms: a Shadow condition in the support area; its texts need a [moria] Orc winning a skirmish or stacked on it
					put("swarms2", "1_183");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThatsTwoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: That's Two!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot Gimli and exert a fellowship companion to have each Shadow player discard one of his or her Shadow cards from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("That's Two!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ThatsTwoTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AHinderedShadowCardMayBeTheOneTheShadowPlayerDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(card);

		var hindered = scn.GetShadowCard("swarms1");
		var unhindered = scn.GetShadowCard("swarms2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hindered, unhindered);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098:
		// "one of his or her Shadow cards" may be a hindered one).
		scn.HinderCard(hindered);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.IsHindered(hindered));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(card));

		// Gimli is the one fellowship companion, and exerts.
		scn.FreepsPlayCard(card);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The Shadow player picks among his Shadow cards in play, the hindered condition among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hindered, unhindered, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(hindered);

		assertInZone(Zone.DISCARD, hindered);
		assertInZone(Zone.SUPPORT, unhindered);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
