package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_121_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "52_121");
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion with no text that matters, strength 4
				}},
				// Site 2 Ettenmoors (plains, Shadow number 2), 3 Rivendell Valley (0), 4 Balin's Tomb (underground, 3),
				// 5 The Bridge of Khazad-dum (underground, 6), 6 Galadriel's Glade (3)
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Gimli, Dwarf of the Mountain-race
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Damage +1.
		 * Each underground site's Shadow number is -1. While the fellowship is at an underground site, Gimli is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Dwarf of the Mountain-race", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFor2HeLowersTheShadowNumberOfEachUndergroundSiteBy1AndOfNoOtherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(gimli);
		scn.SetTwilight(0);

		var ettenmoors = scn.GetFreepsSite(2);
		var balinsTomb = scn.GetFreepsSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		assertEquals(3, scn.GetTwilightCost(balinsTomb));

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		// Balin's Tomb is underground; Ettenmoors is not
		assertEquals(2, scn.GetTwilightCost(balinsTomb));
		assertEquals(2, scn.GetTwilightCost(ettenmoors));

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetTwilightCost(scn.GetCurrentSite()));
		// 2 for Gimli + 2 companions + 5 for the Bridge (6 - 1)
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus1OnlyWhileTheFellowshipIsAtAnUndergroundSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);

		scn.StartGame();
		scn.SkipToSite(3);

		// Rivendell Valley is not underground
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		scn.SkipToSite(5);
		assertEquals(7, scn.GetStrength(gimli));

		scn.SkipToSite(6);
		// Galadriel's Glade is not underground
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
