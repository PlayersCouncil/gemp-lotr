package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("another", "12_40");
					put("slinker", "5_29");     // Sméagol, Slinker: [gollum] companion, strength 3; to play, add a burden
					put("oncelikeus", "103_116"); // Smeagol, Once Like Us: [shire] companion, strength 3; to play, add a burden
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion of another title, strength 3
					put("deagol", "13_46");     // Déagol, Fateful Finder: a [gollum] follower of another title
					put("gollum", "7_59");      // Gollum, Vile Creature: [gollum] minion, strength 5; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows-block path, where the site under the fellowship can be replaced with any site of an adventure deck.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("another", "12_40");
					put("another2", "12_40");
					put("slinker", "5_29");     // Sméagol, Slinker: [gollum] companion, strength 3
				}},
				// Site 1 Fangorn Glade (adds (1) for each Free Peoples weapon: there are none), site3 Crags of Emyn
				// Muil (wounds only when left in region 2), site4 Nurn (a bonus to its controller's hunters), site5
				// Sirannon Ruins (no text), site6 Buckland Homestead (a Shadow ability never used here).
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TheresAnotherWayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: There's Another Way
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Sméagol.
		 * 	Sméagol is strength +1.
		 * 	<b>Regroup:</b> Discard this condition to replace the fellowship's current site with a site from your
		 * 	adventure deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("another");

		assertEquals("There's Another Way", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnceGollumSmeagolIsSpottedAndMakesHimStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var another = scn.GetFreepsCard("another");
		var slinker = scn.GetFreepsCard("slinker");
		var deagol = scn.GetFreepsCard("deagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(another, slinker);
		scn.MoveCardsToSupportArea(deagol);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		// A [gollum] card of another title and a [gollum] character of another title: no Sméagol.
		assertFalse(scn.FreepsPlayAvailable(another));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(slinker);
		assertEquals(3, scn.GetStrength(slinker));
		assertEquals(4, scn.GetStrength(frodo));        // 3 + 1 for The Ruling Ring
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.FreepsPlayAvailable(another));

		scn.FreepsPlayCard(another);
		assertInZone(Zone.SUPPORT, another);
		assertEquals(4, scn.GetStrength(slinker));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnceShireSmeagolIsSpottedAndMakesHimStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var another = scn.GetFreepsCard("another");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(another, oncelikeus);

		scn.StartGame();

		// A [shire] companion of another title: no Sméagol.
		assertFalse(scn.FreepsPlayAvailable(another));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(oncelikeus);
		assertEquals(3, scn.GetStrength(oncelikeus));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsPlayAvailable(another));

		scn.FreepsPlayCard(another);
		assertInZone(Zone.SUPPORT, another);
		assertEquals(4, scn.GetStrength(oncelikeus));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsItToReplaceTheCurrentSiteWithOneOfYourOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var another = scn.GetFreepsCard("another");
		var another2 = scn.GetFreepsCard("another2");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCompanionsToTable(slinker);
		scn.MoveCardsToSupportArea(another, another2);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// Not a fellowship action.
		assertFalse(scn.FreepsActionAvailable(another));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		var ruins = scn.GetShadowSite("site5");
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertSame(ruins, scn.GetCurrentSite());
		scn.SkipToPhase(Phase.REGROUP);

		// The Free Peoples player's adventure deck holds two sites; the Shadow player's holds another.
		var crags = scn.GetFreepsSite("site3");
		var nurn = scn.GetFreepsSite("site4");
		var homestead = scn.GetShadowSite("site6");
		scn.SetFreepsAdventureDeck(crags, nurn);
		scn.SetShadowAdventureDeck(homestead);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(slinker));      // 3 + 1 for each copy
		assertInZone(Zone.SUPPORT, another, another2);
		assertTrue(scn.FreepsActionAvailable(another));

		scn.FreepsUseCardAction(another);
		// The copy used is discarded, and not the other.
		assertInZone(Zone.DISCARD, another);
		assertInZone(Zone.SUPPORT, another2);
		assertEquals(2, scn.FreepsGetBPChoiceCount());
		assertTrue(scn.FreepsHasBPChoiceAvailable(crags.getBlueprintId()));
		assertTrue(scn.FreepsHasBPChoiceAvailable(nurn.getBlueprintId()));
		assertFalse(scn.FreepsHasBPChoiceAvailable(homestead.getBlueprintId()));
		scn.FreepsChooseCardBPFromSelection(nurn);

		assertSame(nurn, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// The site replaced goes back to its owner's adventure deck.
		assertInZone(Zone.ADVENTURE_DECK, ruins);
		assertInZone(Zone.ADVENTURE_DECK, crags);
		// The copy discarded takes its bonus with it; the other's stays.
		assertEquals(4, scn.GetStrength(slinker));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
