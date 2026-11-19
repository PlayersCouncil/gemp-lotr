package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_17_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throne", "17_105");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6; a maneuver ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("trooper", "3_99");     // Orc Trooper: a [sauron] Orc who is no Sauron; strength +1 for each card in the Shadow hand
					put("sauron", "13_140");    // Sauron, Dark Lord of Mordor: [sauron] Maia minion, printed Damage +1 and fierce; a discount on his own play only

					// Shadows-block sites, laid on the Fellowship path by the rig as the site after the fellowship's
					put("doom", "15_193");      // Mount Doom: "Until the end of the game, sites in this region cannot be replaced"
					put("foot", "18_135");      // Foot of Mount Doom: a site of another title; its text holds only while the fellowship is there
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThroneoftheDarkLordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Throne of the Dark Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 minions.<br>While you can spot Sauron, each other minion is <b>damage +1</b>.<br>While the fellowship is in the same region as Mount Doom, each companion is resistance -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("throne");

		assertEquals("Throne of the Dark Lord", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoMinionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetShadowCard("throne");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(throne);
		assertInHand(throne);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One minion, beside two characters who are companions.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(throne));

		// A second minion, of another culture and another race.
		scn.MoveMinionsToTable(brigand);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(throne));

		scn.ShadowPlayCard(throne);
		assertInZone(Zone.SUPPORT, throne);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileSauronCanBeSpottedEachOtherMinionIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetShadowCard("throne");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var trooper = scn.GetShadowCard("trooper");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(throne);
		assertInZone(Zone.SUPPORT, throne);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand, trooper);

		// No Sauron: the [sauron] Orc is not him.
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(trooper, Keyword.DAMAGE));

		scn.MoveMinionsToTable(sauron);

		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(trooper, Keyword.DAMAGE));
		// Sauron himself keeps his printed Damage +1: "each other minion".
		assertEquals(1, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
	}

	@Test
	public void EachCompanionIsResistanceMinus2WhileTheFellowshipIsInTheRegionOfMountDoomAtItOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetShadowCard("throne");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var doom = scn.GetShadowSite("doom");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(6);
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		scn.MoveCardsToSupportArea(throne);
		// Mount Doom becomes site 7, in region 3; the fellowship is at site 6, in region 2.
		scn.MoveCardToAdventurePath(doom);
		assertInZone(Zone.SUPPORT, throne);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));

		// At Mount Doom.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(doom, scn.GetCurrentSite());
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(aragorn));

		// At site 8, in the same region, but not at Mount Doom.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(4, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteOfAnotherTitleInTheRegionLowersNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetShadowCard("throne");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var foot = scn.GetShadowSite("foot");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(throne);
		// Foot of Mount Doom becomes site 8, in region 3 with the fellowship.
		scn.MoveCardToAdventurePath(foot);
		assertInZone(Zone.SUPPORT, throne);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.FreepsPass();
		assertEquals(foot, scn.GetCurrentSite());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
