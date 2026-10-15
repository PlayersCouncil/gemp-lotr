package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_252_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_252");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Westemnet Hills 4_344 as site 4, the home of Guma (4T); the Herdsman's home is site 3,
	// Barrows of Edoras 4_337. CR 4.2 "ally": an ally is at his home site, the fellowship's only when it is there.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arms", "7_252");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; valiant and nothing else
					put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T, strength 4; a fellowship ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4; a regroup ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongArmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Strong Arms
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, spot a [rohan] Man. Plays on a site.<br>This site is a <b>plains</b>.<br>Each [rohan] Man is strength +1 at this site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Strong Arms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnTheFellowshipsSiteAnAllyWhoseHomeIsAnotherSiteIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var arms = scn.GetFreepsCard("arms");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, herdsman);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(arms);

		var site4 = scn.GetSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(herdsman));

		scn.FreepsPlayCard(arms);
		scn.FreepsChooseCard(site4);
		assertAttachedTo(arms, site4);

		// The companion and Guma, at his home site, are at Westemnet Hills with the fellowship; the Herdsman is at
		// Barrows of Edoras, his home.
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(herdsman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnAnAllysHomeSiteThatAllyIsStrengthenedWhileTheFellowshipIsAtAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var arms = scn.GetFreepsCard("arms");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma, herdsman);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(arms);

		var site3 = scn.GetSite(3);
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.FreepsPlayCard(arms);
		scn.FreepsChooseCard(site3);
		assertAttachedTo(arms, site3);

		// Barrows of Edoras is the Herdsman's home: he is at it, though the fellowship is at site 4 with the
		// companion and Guma.
		assertTrue(scn.HasKeyword(site3, Keyword.PLAINS));
		assertEquals(5, scn.GetStrength(herdsman));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void StrongArmsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
