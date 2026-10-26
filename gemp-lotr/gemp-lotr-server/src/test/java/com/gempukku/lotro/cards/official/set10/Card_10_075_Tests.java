package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "10_75");
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
	public void AdvanceCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Advance Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each roaming [sauron] minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("captain");

		assertEquals("Advance Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachRoamingSauronMinionIsDamagePlus1AndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var hunter = scn.GetShadowCard("hunter");
		var guard = scn.GetShadowCard("guard");
		var lights = scn.GetShadowCard("lights");
		var party = scn.GetShadowCard("party");

		scn.MoveCardsToHand(captain);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(hunter, guard, lights, party);
		// Enough for the Captain's 3 and the 2 of roaming at site 4.
		scn.SetTwilight(10);
		// The fellowship moves to site 4.
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		// Site 6 and site 5 minions roam at site 4; Corpse Lights, of site 4, does not.
		assertTrue(scn.HasKeyword(hunter, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(party, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(lights, Keyword.ROAMING));
		// Without the Captain: only the printed bonuses.
		assertEquals(0, scn.GetKeywordCount(hunter, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lights, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));

		// Played from hand at site 4, where he roams.
		scn.ShadowPlayCard(captain);

		// The roaming [sauron] Orc and Uruk-hai, and the Captain himself (site 6, roaming), gain damage +1.
		assertTrue(scn.HasKeyword(captain, Keyword.ROAMING));
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(hunter, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// A [sauron] minion that is not roaming, and a roaming minion of another culture, do not.
		assertEquals(0, scn.GetKeywordCount(lights, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
