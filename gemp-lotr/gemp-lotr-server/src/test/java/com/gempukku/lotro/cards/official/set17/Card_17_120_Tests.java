package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_120_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_120");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetControlledSitesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attacker", "17_120");
					// Duilin, Ranger From Blackroot Vale: [gondor] Man, strength 5, vitality 3. "While the
					// fellowship is at a forest or river site, Duilin is strength +3": a card that asks what
					// the fellowship's site is.
					put("duilin", "14_7");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Site 1 is Bag End (no keyword), site 2 Ettenmoors (plains), site 4 Balin's Tomb and
					// site 5 The Bridge of Khazad-dum (both underground and nothing else).
					// Ford of Bruinen: "River. Sanctuary."; its text is about a Nazgul played there.
					put("site3", "1_338");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandAttackerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Attacker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While this minion is assigned to a skirmish, each site on the adventure path gains each terrain keyword from each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("White Hand Attacker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileAssignedToASkirmishEachSiteOnThePathHasTheTerrainKeywordsOfTheSitesItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlledSitesScenario();

		var duilin = scn.GetFreepsCard("duilin");
		scn.MoveCompanionsToTable(duilin);

		var attacker = scn.GetShadowCard("attacker");

		// Both players' markers are at site 4, so the first three sites can be controlled; the fellowship then
		// moves on to site 5.
		scn.StartGame();
		scn.SkipToSite(4);
		var bagEnd = scn.GetSite(1);
		var ettenmoors = scn.GetSite(2);
		var ford = scn.GetSite(3);
		var tomb = scn.GetSite(4);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(attacker);
		scn.SkipToAssignments();

		var bridge = scn.GetCurrentSite();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetShadowControlledSiteCount());
		assertTrue(scn.IsSiteControlled(bagEnd));
		assertTrue(scn.IsSiteControlled(ettenmoors));
		assertTrue(scn.IsSiteControlled(ford));
		assertTrue(scn.HasKeyword(ettenmoors, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(ford, Keyword.RIVER));
		assertTrue(scn.HasKeyword(ford, Keyword.SANCTUARY));

		// The Attacker is in play and not assigned: the two sites still on the path are underground and
		// nothing more.
		assertTrue(scn.HasKeyword(bridge, Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(bridge, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(bridge, Keyword.RIVER));
		assertFalse(scn.HasKeyword(tomb, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(tomb, Keyword.RIVER));
		assertEquals(5, scn.GetStrength(duilin));

		scn.FreepsAssignToMinions(duilin, attacker);
		assertTrue(scn.IsCharAssigned(attacker));

		// Plains from Ettenmoors and river from Ford of Bruinen, on the fellowship's site and on the site
		// behind it; sanctuary is no terrain keyword and is not gained.
		assertTrue(scn.HasKeyword(bridge, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(bridge, Keyword.RIVER));
		assertFalse(scn.HasKeyword(bridge, Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(bridge, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(tomb, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(tomb, Keyword.RIVER));
		assertFalse(scn.HasKeyword(tomb, Keyword.SANCTUARY));
		// A controlled site is no longer a site on the adventure path, and gains nothing.
		assertFalse(scn.HasKeyword(bagEnd, Keyword.RIVER));
		assertFalse(scn.HasKeyword(ettenmoors, Keyword.RIVER));
		assertFalse(scn.HasKeyword(ford, Keyword.PLAINS));
		// The fellowship is at a river site now: 5 + 3.
		assertEquals(8, scn.GetStrength(duilin));

		// In the skirmish he was assigned to, the Attacker is assigned still.
		scn.FreepsResolveSkirmish(duilin);
		assertTrue(scn.HasKeyword(bridge, Keyword.RIVER));
		assertEquals(8, scn.GetStrength(duilin));

		// Duilin and the Attacker fight and both live: 8 against 8 goes to the Shadow player, and damage +1
		// makes it two wounds.
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(duilin));
		assertEquals(0, scn.GetWoundsOn(attacker));
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);

		// The skirmish is over and the Attacker is assigned to none: the keywords are gone again.
		assertFalse(scn.HasKeyword(bridge, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(bridge, Keyword.RIVER));
		assertFalse(scn.HasKeyword(tomb, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(tomb, Keyword.RIVER));
		assertEquals(5, scn.GetStrength(duilin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
