package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_333_ErrataTests
{
	// The Fellowship block path with this Midgewater Moors as site 2.  Each test moves there from site 1.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, cost 4, vitality 4; a maneuver ability
					put("guard", "1_7");          // Dwarf Guard: [dwarven] companion, vitality 2; needs a Dwarf to be played, so cheated in
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: [elven] ally, vitality 4; heals site 3 allies at the start of the turn
					put("fodder1", "1_7");        // Dwarf Guards held in the Free Peoples hand, to be discarded
					put("fodder2", "1_7");

					put("backstabber", "1_174");  // Goblin Backstabber: [moria] Orc minion, cost 1, site 4; damage +2 beside another [moria] Orc
					put("savage", "1_151");       // Uruk Savage: [isengard] minion, cost 2, site 5; damage +1 only
					put("party", "1_158");        // Uruk-hai Raiding Party: [isengard] minion, cost 4, site 5; damage +1 only
					put("spear", "1_182");        // Goblin Spear: [moria] possession, cost 1, on a [moria] minion; acts only when its bearer kills
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "51_333");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MidgewaterMoorsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Midgewater Moors
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Plains.</b> Each time a minion is played, the Free Peoples player must exert a companion or
		 * discard a card from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Midgewater Moors", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachTimeAMinionIsPlayedTheFreePeoplesPlayerChoosesToExertACompanionOrDiscardACardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var fodder2 = scn.GetFreepsCard("fodder2");
		var backstabber = scn.GetShadowCard("backstabber");
		var savage = scn.GetShadowCard("savage");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveAlliesToTable(elrond);
		// The Dwarf Guard is exhausted.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveCardsToHand(fodder1, fodder2);
		scn.MoveCardsToHand(backstabber, savage, spear);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 2 is played from the Shadow player's adventure deck: his copy of this card.
		assertEquals("51_333", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetFreepsHandCount());

		// A [moria] minion: the Free Peoples player chooses, and chooses to exert.
		scn.ShadowPlayCard(backstabber);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		assertTrue(scn.FreepsChoiceAvailable("Exert a companion"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a card from hand"));
		scn.FreepsChooseOption("Exert a companion");

		// A companion who can exert, the Ring-bearer included; not the exhausted Guard, not the ally, not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, elrond, backstabber));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] minion: asked again, and this time the card from hand.
		scn.ShadowPlayCard(savage);
		assertTrue(scn.FreepsChoiceAvailable("Exert a companion"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a card from hand"));
		scn.FreepsChooseOption("Discard a card from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(fodder1, fodder2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(fodder1);

		assertInZone(Zone.DISCARD, fodder1);
		assertInZone(Zone.HAND, fodder2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow card that is not a minion sets nothing off.
		scn.ShadowPlayCard(spear);
		assertAttachedTo(spear, backstabber);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenOnlyOneCanBeDoneItIsDoneWithoutAskingWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var fodder1 = scn.GetFreepsCard("fodder1");
		var backstabber = scn.GetShadowCard("backstabber");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		// Frodo is exhausted and Aragorn can exert once more.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveCardsToHand(backstabber, savage);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertEquals("51_333", scn.GetCurrentSite().getBlueprintId());

		// No cards in hand: the exertion, with no choice between the two.
		assertEquals(0, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(backstabber);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Every companion exhausted and one card in hand: it is discarded, with no choice between the two.
		scn.MoveCardsToHand(fodder1);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.DISCARD, fodder1);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenNeitherCanBeDoneNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(aragorn);
		// Both companions are exhausted, and the Free Peoples hand is empty.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(party);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		assertEquals("51_333", scn.GetCurrentSite().getBlueprintId());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(party);
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionPlayedHereSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("51_333", scn.GetCurrentSite().getBlueprintId());

		int hand = scn.GetFreepsHandCount();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(aragorn));

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
