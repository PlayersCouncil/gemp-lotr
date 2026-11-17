package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blitz", "13_169");
					put("vigilant", "11_205");   // Vigilant Uruk: [uruk-hai] Uruk-hai, cost 2, site 5; a strength count of its own only
					put("saruman", "12_144");    // Saruman, Agent of the Dark Lord: [uruk-hai] minion who is no Uruk-hai (a Wizard), no damage bonus;
					                             // a skirmish ability used only on purpose, and a heal of himself when a companion is killed
					put("sentry", "10_84");      // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 only
					put("alliance", "12_141");   // Dark Alliance: [uruk-hai] support-area condition; a response used only on purpose
					put("entranced", "13_163");  // Entranced Uruk: [uruk-hai] Uruk-hai, strength 9, damage +1; a bar on discarding its possessions only
					put("prowling", "11_136");   // Prowling Orc: [orc] Orc, no damage bonus; a skirmish ability used only on purpose

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man, unbound, strength 8, resistance 6; a maneuver ability only
					put("gimli", "11_8");        // Gimli, Lively Combatant: [dwarven] Dwarf, unbound, resistance 7; may draw when he wins a skirmish
					put("sam", "1_311");         // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound, strength 3, resistance 5
					put("thrarin", "1_27");      // Thrarin, Dwarven Smith: ally, strength 4; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukBlitzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Blitz
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: To play, spot an [uruk-hai] minion.<br>While the fellowship is in region 1, each Uruk-hai is <b>damage +1</b>.<br>While the fellowship is in region 2, each companion is strength -1.<br>While the fellowship is in region 3, each unbound companion is resistance -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blitz");

		assertEquals("Uruk Blitz", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blitz = scn.GetShadowCard("blitz");
		var vigilant = scn.GetShadowCard("vigilant");
		var sentry = scn.GetShadowCard("sentry");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(blitz, vigilant);
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, alliance);
		scn.MoveMinionsToTable(sentry);
		assertInPlay(sentry);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [sauron] Uruk-hai and an [uruk-hai] condition: no [uruk-hai] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(blitz));

		scn.ShadowPlayCard(vigilant);
		assertInPlay(vigilant);
		assertTrue(scn.ShadowPlayAvailable(blitz));
		scn.ShadowPlayCard(blitz);
		assertInPlay(blitz);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiMinionWhoIsNoUrukHaiIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blitz = scn.GetShadowCard("blitz");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(blitz);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		assertInPlay(saruman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(blitz));
		scn.ShadowPlayCard(blitz);
		assertInPlay(blitz);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion1EachUrukHaiIsDamagePlus1AndNothingElseChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var blitz = scn.GetShadowCard("blitz");
		var entranced = scn.GetShadowCard("entranced");
		var sentry = scn.GetShadowCard("sentry");
		var saruman = scn.GetShadowCard("saruman");
		var prowling = scn.GetShadowCard("prowling");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(entranced, sentry, saruman, prowling);

		// Site 3: the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(entranced, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));

		scn.MoveMinionsToTable(blitz);
		// Uruk-hai of two cultures, the Blitz itself among them.
		assertEquals(1, scn.GetKeywordCount(blitz, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(entranced, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
		// The [uruk-hai] minion who is no Uruk-hai, and an Orc.
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(prowling, Keyword.DAMAGE));

		// Neither of the other regions' texts.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InRegion2EachCompanionIsStrengthMinus1AndNoUrukHaiIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(thrarin);

		var blitz = scn.GetShadowCard("blitz");
		var entranced = scn.GetShadowCard("entranced");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(entranced);

		// Site 4: the first site of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		// Frodo 3 + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(sam));

		scn.MoveMinionsToTable(blitz);
		// Companions of each culture and race, the Ring-bearer among them.
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(sam));
		// An ally and the minions are no companions.
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(9, scn.GetStrength(entranced));
		assertEquals(12, scn.GetStrength(blitz));

		// Neither of the other regions' texts.
		assertEquals(1, scn.GetKeywordCount(entranced, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(blitz, Keyword.DAMAGE));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InRegion3EachUnboundCompanionIsResistanceMinus2AndNoCompanionIsWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);

		var blitz = scn.GetShadowCard("blitz");
		var entranced = scn.GetShadowCard("entranced");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(entranced);

		// Site 7: the first site of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(sam));

		scn.MoveMinionsToTable(blitz);
		// Unbound companions of two cultures and races.
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		// The Ring-bearer, and a Ring-bound companion who is not the Ring-bearer.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));

		// Neither of the other regions' texts.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(entranced, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(blitz, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
