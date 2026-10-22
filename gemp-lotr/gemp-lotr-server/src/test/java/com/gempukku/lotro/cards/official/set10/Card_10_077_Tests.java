package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regular", "10_77");
					put("hunter", "1_256");     // Morgul Hunter: [sauron] Orc, site 6; strength +1 for each companion
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, site 6; "Damage +1." is its whole text
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, site 4; a skirmish ability used only on purpose
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, site 5; "Damage +1." is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AdvanceRegularStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Advance Regular
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each roaming [sauron] minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("regular");

		assertEquals("Advance Regular", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachRoamingSauronMinionIsFierceAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var hunter = scn.GetShadowCard("hunter");
		var guard = scn.GetShadowCard("guard");
		var lights = scn.GetShadowCard("lights");
		var party = scn.GetShadowCard("party");

		scn.MoveCardsToHand(regular);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(hunter, guard, lights, party);
		// Enough for the Regular's 3 and the 2 of roaming at site 4.
		scn.SetTwilight(10);
		// The fellowship moves to site 4.
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		// Site 6 and site 5 minions roam at site 4; Corpse Lights, of site 4, does not.
		assertTrue(scn.HasKeyword(hunter, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(party, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lights, Keyword.ROAMING));
		// Without the Regular, none of them is fierce.
		assertFalse(scn.HasKeyword(hunter, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(guard, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(lights, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(party, Keyword.FIERCE));

		// Played from hand at site 4, where he roams.
		scn.ShadowPlayCard(regular);

		// The roaming [sauron] Orc and Uruk-hai, and the Regular himself (site 6, roaming), are fierce.
		assertTrue(scn.HasKeyword(regular, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(regular, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(hunter, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(guard, Keyword.FIERCE));
		// A [sauron] minion that is not roaming, and a roaming minion of another culture, are not.
		assertFalse(scn.HasKeyword(lights, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(party, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
