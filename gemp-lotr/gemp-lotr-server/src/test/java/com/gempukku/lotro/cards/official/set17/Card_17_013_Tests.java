package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("world", "17_13");

					put("arwen", "1_30");           // Arwen, Daughter of Elrond: Elf companion, vitality 3; +3 only against a Nazgûl
					put("calaglin", "3_10");        // Calaglin, Elf of Lórien: Elf ally, home 6F, vitality 3; a Regroup ability used only on purpose
					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk: a Wizard companion, no Elf; a strength count only
					put("lorien", "1_53");          // Lórien Elf: only a card for the draw deck to hold
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWorldAheadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: The World Ahead
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 5
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot an Elf.
		 * Each time the fellowship moves to a region one site, add a threat.
		 * Each time the fellowship moves to a region two site, heal an Elf.
		 * Each time the fellowship moves to a region three site, draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("world");

		assertEquals("The World Ahead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var world = scn.GetFreepsCard("world");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(world, arwen);

		scn.StartGame();

		// Frodo and Gandalf: companions, but no Elf.
		assertInZone(Zone.HAND, world);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(world));

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(world));
		scn.FreepsPlayCard(world);
		assertInZone(Zone.SUPPORT, world);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsSpottedToPlayItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var world = scn.GetFreepsCard("world");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);
		scn.MoveCardsToHand(world);

		scn.StartGame();

		assertInZone(Zone.HAND, world);
		assertTrue(scn.FreepsPlayAvailable(world));
		scn.FreepsPlayCard(world);
		assertInZone(Zone.SUPPORT, world);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveToARegionOneSiteAddsAThreatAndDoesNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var world = scn.GetFreepsCard("world");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(arwen, gandalf);
		scn.MoveCardsToSupportArea(world);
		scn.AddWoundsToChar(arwen, 1);

		scn.StartGame();

		// Frodo, Arwen and Gandalf: room for three threats.
		assertInZone(Zone.SUPPORT, world);
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(arwen));
		int hand = scn.GetFreepsHandCount();

		// Site 1 to site 2.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetThreats());
		// No heal and no draw at a region one site.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(hand, scn.GetFreepsHandCount());

		// A second move in the turn, site 2 to site 3, adds another.
		scn.SkipToMovementDecision();
		assertEquals(hand, scn.GetFreepsHandCount());
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveToARegionTwoSiteHealsAnElfAndAddsNoThreatAndDrawsNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var world = scn.GetFreepsCard("world");
		var arwen = scn.GetFreepsCard("arwen");
		var calaglin = scn.GetFreepsCard("calaglin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(arwen, gandalf);
		scn.MoveAlliesToTable(calaglin);

		scn.StartGame();
		scn.SkipToSite(3);
		// Laid at site 3, after the moves of the skip; wounded after the turn's sanctuary heal.
		scn.MoveCardsToSupportArea(world);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(calaglin, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, world);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetThreats());
		int hand = scn.GetFreepsHandCount();

		// Site 3 to site 4.
		scn.FreepsPass();
		// An Elf companion and an Elf ally, both wounded; not a wounded companion who is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, calaglin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(arwen);
		assertEquals(4, scn.GetCurrentSiteNumber());
		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// No threat and no draw at a region two site, with room for threats.
		assertEquals(0, scn.GetThreats());
		assertEquals(hand, scn.GetFreepsHandCount());

		// A second move in the turn, site 4 to site 5, heals again.
		scn.SkipToMovementDecision();
		assertEquals(hand, scn.GetFreepsHandCount());
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, calaglin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		scn.FreepsChooseCard(calaglin);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(calaglin));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetThreats());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveToARegionThreeSiteDrawsACardAndHealsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var world = scn.GetFreepsCard("world");
		var arwen = scn.GetFreepsCard("arwen");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(arwen, gandalf);

		scn.StartGame();
		scn.SkipToSite(6);
		// Laid at site 6, after the moves of the skip; wounded after the turn's sanctuary heal.
		scn.MoveCardsToSupportArea(world);
		scn.AddWoundsToChar(arwen, 1);
		// The reconciles of the skip have drawn the deck dry: two cards for it to draw.
		var calaglin = scn.GetFreepsCard("calaglin");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCardsToTopOfDeck(calaglin, lorien);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, calaglin);
		assertInZone(Zone.DECK, lorien);
		assertInZone(Zone.SUPPORT, world);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetThreats());
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// Site 6 to site 7, Anduin Confluence, whose own move trigger (every ally discarded) is ordered with this one.
		scn.FreepsPass();
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("World Ahead");
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// No heal and no threat at a region three site.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetThreats());

		// A second move in the turn, site 7 to site 8, draws again.
		scn.SkipToMovementDecision();
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, calaglin);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
