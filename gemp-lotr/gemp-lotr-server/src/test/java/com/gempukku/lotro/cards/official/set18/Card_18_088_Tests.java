package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_088_Tests
{

	// The Fellowship path: site 2 (Ettenmoors) is a plains and neither a battleground nor underground; site 4
	// (Balin's Tomb) is underground and no sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "18_88");
					put("patrol1", "1_177");    // Goblin Patrol Troop: a [moria] Orc minion with no text
					put("patrol2", "1_177");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer, for a fellowship archery total of 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 2 (Uruk Camp) is a battleground and a plains, site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "18_88");
					put("patrol1", "1_177");    // Goblin Patrol Troop: a [moria] Orc minion with no text
					put("patrol2", "1_177");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer, for a fellowship archery total of 1
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf to exert for Baruk Khazâd
					put("baruk", "5_5");        // Baruk Khazâd: Maneuver event, exert a Dwarf to wound a minion
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path: site 3 (The Dimholt) is underground and a sanctuary.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "18_88");
					put("patrol1", "1_177");    // Goblin Patrol Troop: a [moria] Orc minion with no text
					put("patrol2", "1_177");
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer, for a fellowship archery total of 1
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishDefenderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Defender
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While the fellowship is at a battleground or underground site, this minion cannot take wounds in the archery phase.
		*/

		var scn = GetScenario();

		var defender = scn.GetFreepsCard("defender");

		assertEquals("Orkish Defender", defender.getBlueprint().getTitle());
		assertNull(defender.getBlueprint().getSubtitle());
		assertFalse(defender.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, defender.getBlueprint().getSide());
		assertEquals(Culture.ORC, defender.getBlueprint().getCulture());
		assertEquals(CardType.MINION, defender.getBlueprint().getCardType());
		assertEquals(Race.ORC, defender.getBlueprint().getRace());
		assertEquals(3, defender.getBlueprint().getTwilightCost());
		assertEquals(8, defender.getBlueprint().getStrength());
		assertEquals(2, defender.getBlueprint().getVitality());
		assertEquals(4, defender.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtABattlegroundItTakesAManeuverWoundButNoArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var defender = scn.GetShadowCard("defender");
		var patrol1 = scn.GetShadowCard("patrol1");
		var patrol2 = scn.GetShadowCard("patrol2");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(baruk);
		assertInHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, patrol1, patrol2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Uruk Camp.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// In the maneuver phase, at the same battleground, the Defender can be wounded.
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(defender, patrol1, patrol2));
		scn.FreepsChooseCard(defender);
		assertEquals(1, scn.GetWoundsOn(defender));

		// The fellowship's one archery wound may go on either Goblin Patrol Troop, and not on the Defender.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol1, patrol2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(defender));
		scn.ShadowChooseCard(patrol1);

		assertEquals(1, scn.GetWoundsOn(patrol1));
		assertEquals(0, scn.GetWoundsOn(patrol2));
		assertEquals(1, scn.GetWoundsOn(defender));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtABattlegroundThatIsNoPlainsItTakesNoArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var defender = scn.GetShadowCard("defender");
		var patrol1 = scn.GetShadowCard("patrol1");
		var patrol2 = scn.GetShadowCard("patrol2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(defender, patrol1, patrol2);
		assertEquals(0, scn.GetWoundsOn(defender));
		scn.SkipToArcheryWounds();

		// White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol1, patrol2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(defender));
		scn.ShadowChooseCard(patrol2);

		assertEquals(1, scn.GetWoundsOn(patrol2));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteItTakesNoArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var patrol1 = scn.GetShadowCard("patrol1");
		var patrol2 = scn.GetShadowCard("patrol2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(defender, patrol1, patrol2);
		assertEquals(0, scn.GetWoundsOn(defender));
		scn.SkipToArcheryWounds();

		// Balin's Tomb.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol1, patrol2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(defender));
		scn.ShadowChooseCard(patrol1);

		assertEquals(1, scn.GetWoundsOn(patrol1));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtAnUndergroundSanctuaryItTakesNoArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var defender = scn.GetShadowCard("defender");
		var patrol1 = scn.GetShadowCard("patrol1");
		var patrol2 = scn.GetShadowCard("patrol2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(defender, patrol1, patrol2);
		assertEquals(0, scn.GetWoundsOn(defender));
		scn.SkipToArcheryWounds();

		// The Dimholt.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol1, patrol2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(defender));
		scn.ShadowChooseCard(patrol1);

		assertEquals(1, scn.GetWoundsOn(patrol1));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherItTakesAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var patrol1 = scn.GetShadowCard("patrol1");
		var patrol2 = scn.GetShadowCard("patrol2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, patrol1, patrol2);
		assertEquals(0, scn.GetWoundsOn(defender));
		scn.SkipToArcheryWounds();

		// Ettenmoors: a plains, neither a battleground nor underground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertTrue(scn.ShadowHasCardChoiceAvailable(defender, patrol1, patrol2));
		scn.ShadowChooseCard(defender);

		assertEquals(1, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(patrol1));
		assertEquals(0, scn.GetWoundsOn(patrol2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
