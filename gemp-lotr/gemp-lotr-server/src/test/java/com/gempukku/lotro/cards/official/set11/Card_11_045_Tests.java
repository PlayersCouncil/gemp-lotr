package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_045_Tests
{

	// A Shadows-block path, the card's own block, whose sites take their numbers as they are laid: the Shadow player
	// lays each site the fellowship moves to, and any site of his adventure deck may take the place of one of his.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("astray", "11_45");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion; a regroup ability only
					put("gollum", "5_24");
					// Gollum, Half a Wraith Himself: a [wraith] minion; his texts answer a Nazgûl played, and an assignment ability
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion of another title; her text is about fierce skirmishes
					put("shelob", "8_26");
					// Sméagol, Poor Creature: the Free Peoples player's [gollum] companion; a skirmish ability used only on purpose
					put("smeagol", "6_45");
				}},
				new HashMap<>() {{
					put("glade", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to
					put("ruins", "18_138");     // Sirannon Ruins: River, no text
					put("crags", "11_234");     // Crags of Emyn Muil: wounds on a regroup move from it in region 2
					put("nurn", "17_148");      // Nurn: its text is about controlling it
					put("angle", "12_185");     // The Angle: a Shadow ability that plays a Nazgul
					put("homestead", "11_230"); // Buckland Homestead: a Shadow ability that plays a Nazgul
					put("emynmuil", "12_187");  // Emyn Muil: a Maneuver ability for a minion
					put("rauros", "17_146");    // Falls of Rauros: acts when moved to in region 2
					put("mineshaft", "13_185"); // Abandoned Mine Shaft: acts on a regroup move from it outside region 1
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void LedAstrayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Led Astray
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot Gollum to add (3) and exchange one of your sites on the adventure path with another site from your adventure deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("astray");

		assertEquals("Led Astray", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingGollumItAddsThreeAndExchangesOneOfTheShadowPlayersSitesWithOneFromHisAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var astray = scn.GetShadowCard("astray");
		var gollum = scn.GetShadowCard("gollum");

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");
		var crags = scn.GetShadowSite("crags");
		var angle = scn.GetShadowSite("angle");
		var homestead = scn.GetShadowSite("homestead");

		// Two moves in the first turn: the Shadow player lays Sirannon Ruins as site 2 and Crags of Emyn Muil as
		// site 3, both his own; site 1 is the Free Peoples player's.
		scn.StartGame(glade);
		scn.MoveCardsToHand(astray);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(crags);
		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(glade, scn.GetSite(1));
		assertSame(ruins, scn.GetSite(2));
		assertSame(crags, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, angle, homestead);
		int twilight = scn.GetTwilight();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(astray));

		scn.ShadowPlayCard(astray);
		assertEquals(twilight + 3, scn.GetTwilight());
		// His two sites on the path, the fellowship's current site among them; not the Free Peoples player's
		// site 1, nor a site in his adventure deck.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ruins, crags));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(glade, angle));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ruins);

		// Any site of his adventure deck may take its place.
		assertTrue(scn.ShadowHasBPChoice(angle));
		assertTrue(scn.ShadowHasBPChoice(homestead));
		scn.ShadowChooseCardBPFromSelection(angle);

		// His The Angle is site 2 now, and Sirannon Ruins is back in his adventure deck.
		assertSame(angle, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, angle);
		assertInZone(Zone.ADVENTURE_DECK, ruins, homestead);
		assertSame(glade, scn.GetSite(1));
		assertSame(crags, scn.GetSite(3));
		// The fellowship has not moved.
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(twilight + 3, scn.GetTwilight());
		assertInDiscard(astray);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteTheShadowPlayerControlsIsNotOneOfHisSitesOnTheAdventurePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var astray = scn.GetShadowCard("astray");
		var gollum = scn.GetShadowCard("gollum");

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");
		var crags = scn.GetShadowSite("crags");
		var emynmuil = scn.GetShadowSite("emynmuil");
		var angle = scn.GetShadowSite("angle");

		// The Shadow player lays Sirannon Ruins as site 2; after two turns of each player, both markers are past it
		// and he takes control of it.  Then he lays Emyn Muil as site 4 as the first player's third turn moves on.
		scn.StartGame(glade);
		scn.MoveCardsToHand(astray);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(crags);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(3, scn.ShadowGetSiteNumber());
		scn.ShadowTakeControlOfSite(ruins);
		assertTrue(scn.ShadowControls(ruins));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(emynmuil);
		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertSame(crags, scn.GetSite(3));
		assertSame(emynmuil, scn.GetSite(4));
		assertInZone(Zone.SUPPORT, ruins);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(astray));

		scn.ShadowPlayCard(astray);
		// His Crags of Emyn Muil and Emyn Muil are on the path; Sirannon Ruins, which he controls, is not, nor is
		// the Free Peoples player's site 1.
		assertTrue(scn.ShadowHasCardChoiceAvailable(crags, emynmuil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ruins, glade));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(crags);
		scn.ShadowChooseCardBPFromSelection(angle);

		assertSame(angle, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, crags);
		assertSame(emynmuil, scn.GetSite(4));
		assertTrue(scn.ShadowControls(ruins));
		assertInZone(Zone.SUPPORT, ruins);
		assertInDiscard(astray);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTheWraithGollumTheFellowshipsCurrentSiteMayBeExchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var astray = scn.GetShadowCard("astray");
		var wraithgollum = scn.GetShadowCard("wraithgollum");

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");
		var homestead = scn.GetShadowSite("homestead");

		scn.StartGame(glade);
		scn.MoveCardsToHand(astray);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.MoveMinionsToTable(wraithgollum);
		scn.ShadowRefreshDecision();

		assertSame(ruins, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, homestead);
		int twilight = scn.GetTwilight();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(astray));

		scn.ShadowPlayCard(astray);
		// Sirannon Ruins, the fellowship's site, is his only site on the path, and is taken unasked: the choice
		// of a site from his adventure deck is the first thing he is asked.
		assertTrue(scn.ShadowHasBPChoice(homestead));
		scn.ShadowChooseCardBPFromSelection(homestead);

		// The fellowship is at the new site 2, his Buckland Homestead.
		assertSame(homestead, scn.GetSite(2));
		assertSame(homestead, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, ruins);
		assertSame(glade, scn.GetSite(1));
		assertEquals(twilight + 3, scn.GetTwilight());
		assertInDiscard(astray);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithShelobAndSmeagolAndPlayableOnceGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var astray = scn.GetShadowCard("astray");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");

		scn.StartGame(glade);
		scn.MoveCardsToHand(astray);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		// A [gollum] minion of another title, and Sméagol, the same creature under the other title.
		scn.MoveMinionsToTable(shelob);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(astray));
		assertInZone(Zone.HAND, astray);

		scn.MoveMinionsToTable(gollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(astray));
	}
}
