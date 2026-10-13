package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_365_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_365");
					put("spear", "4_288");      // Rider's Spear: a [rohan] possession, cost 1; bearer must be a [rohan] Man
					put("mount", "4_287");      // Rider's Mount: a [rohan] possession, cost 1; bearer must be a [rohan] Man
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Théoden, Lord of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Fellowship:</b> Play a [rohan] possession on a [rohan] companion to heal that companion (limit once per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("Lord of the Mark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TheodenTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedAgainInTheTurnItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(card);
		scn.MoveCardsToHand(spear, mount);
		scn.AddWoundsToChar(card, 1);

		scn.StartGame();

		scn.FreepsUseCardAction(card);
		// Theoden is the only [rohan] companion, so he is the bearer and the one healed.
		scn.FreepsChooseCard(spear);
		assertEquals(Zone.ATTACHED, spear.getZone());
		assertEquals(0, scn.GetWoundsOn(card));
		assertEquals(1, scn.GetTwilight());

		// A wound put back by the rig, and a second possession in hand that could be played: everything a
		// second use would need.
		scn.AddWoundsToChar(card, 1);

		// Limit once per turn: a limit on uses, so the ability is not offered again (Ketura on Q-0093).
		assertFalse(scn.FreepsActionAvailable(card));
		assertEquals(Zone.HAND, mount.getZone());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
