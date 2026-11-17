package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_100_Tests
{

	// Fellowship path: site 2 is Ettenmoors 1_331 (plains only), site 4 Balin's Tomb 1_343 (underground only).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rallying", "12_100");
					put("prowling", "11_136");  // Prowling Orc: [orc] Orc lurker, cost 3; a skirmish ability only
					// Horror of Harad: [orc] Half-troll lurker, cost 4; ambush, damage +1, fierce, muster and toil 2, no other text
					put("horror", "14_13");
					// Vigilant Uruk: [uruk-hai] Uruk-hai lurker, cost 2, site 5; a strength bonus only
					put("vigilant", "11_205");
					put("entrapping", "11_120"); // Entrapping Orc: [orc] Orc, no lurker, cost 3; an assignment ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, vitality 3; a skirmish ability
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Towers path: site 4 is White Rocks 4_346 (battleground only), site 6 Caves of Aglarond 4_352 (underground, sanctuary).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rallying", "12_100");
					put("prowling", "11_136");  // Prowling Orc: [orc] Orc lurker, cost 3; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Towers path with Uruk Camp 4_335 (battleground, plains; each Uruk-hai played exerts) as site 2.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rallying", "12_100");
					put("prowling", "11_136");  // Prowling Orc: [orc] Orc lurker, cost 3; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallyingOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Rallying Orc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each time you play another [orc] lurker at a battleground or underground site, you may make the Free Peoples player exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("rallying");

		assertEquals("Rallying Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtBalinsTombEachOrcLurkerPlayedLetsHimMakeTheFreePeoplesPlayerExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var horror = scn.GetShadowCard("horror");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(3);
		// Gimli exhausted: vitality 3, 2 wounds (laid after the sanctuary heal at site 3 was asked)
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling, horror);
		scn.SetTwilight(20);
		assertInHand(prowling, horror);
		scn.FreepsPass();

		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// An [orc] Orc lurker.
		scn.ShadowPlayCard(prowling);
		assertInZone(Zone.SHADOW_CHARACTERS, prowling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rallying));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player chooses: companions who can exert, not the exhausted one, the ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder, rallying, prowling));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Each time: an [orc] Half-troll lurker.  Its toil is asked first, and paid with nobody.
		scn.ShadowPlayCard(horror);
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertInZone(Zone.SHADOW_CHARACTERS, horror);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rallying));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtBalinsTombHisOwnPlayALurkerOfAnotherCultureAndAnOrcWhoIsNoLurkerAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rallying = scn.GetShadowCard("rallying");
		var vigilant = scn.GetShadowCard("vigilant");
		var entrapping = scn.GetShadowCard("entrapping");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(rallying, vigilant, entrapping);
		scn.SetTwilight(20);
		assertInHand(rallying, vigilant, entrapping);
		scn.FreepsPass();

		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Rallying Orc is himself an [orc] lurker: his own play is not "another".
		scn.ShadowPlayCard(rallying);
		assertInZone(Zone.SHADOW_CHARACTERS, rallying);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A lurker of [uruk-hai].
		scn.ShadowPlayCard(vigilant);
		assertInZone(Zone.SHADOW_CHARACTERS, vigilant);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [orc] Orc who is no lurker.
		scn.ShadowPlayCard(entrapping);
		assertInZone(Zone.SHADOW_CHARACTERS, entrapping);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtEttenmoorsAPlainsSiteAnOrcLurkerPlayedIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling);
		scn.SetTwilight(20);
		assertInHand(prowling);
		scn.FreepsPass();

		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(prowling);
		assertInZone(Zone.SHADOW_CHARACTERS, prowling);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNoCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		// The ally can exert, but is no companion.
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(3);
		// Frodo, the only companion, exhausted (laid after the sanctuary heal at site 3 was asked)
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(1, scn.GetVitality(frodo));
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling);
		scn.SetTwilight(20);
		assertInHand(prowling);
		scn.FreepsPass();

		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.ShadowPlayCard(prowling);
		assertInZone(Zone.SHADOW_CHARACTERS, prowling);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling);
		scn.SetTwilight(20);
		assertInHand(prowling);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(prowling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rallying));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtCavesOfAglarondAnUndergroundSanctuaryAnOrcLurkerPlayedIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling);
		scn.SetTwilight(20);
		assertInHand(prowling);
		scn.FreepsPass();

		assertEquals("Caves of Aglarond", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(prowling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rallying));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtWhiteRocksABattlegroundAnOrcLurkerPlayedIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling);
		scn.SetTwilight(20);
		assertInHand(prowling);
		scn.FreepsPass();

		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(prowling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rallying));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtUrukCampABattlegroundPlainsAnOrcLurkerPlayedIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var rallying = scn.GetShadowCard("rallying");
		var prowling = scn.GetShadowCard("prowling");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(rallying);
		scn.MoveCardsToHand(prowling);
		scn.SetTwilight(20);
		assertInHand(prowling);
		scn.FreepsPass();

		assertEquals("Uruk Camp", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(prowling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rallying));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
