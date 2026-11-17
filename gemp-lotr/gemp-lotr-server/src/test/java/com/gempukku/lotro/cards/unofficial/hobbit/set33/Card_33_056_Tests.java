package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_056_Tests
{

	// The Hobbit table, on the Hobbit path with Great East Road as site 2 in place of The Troll Hoard 30_50.
	// Bilbo, Reliable Companion 30_46 is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Possessions: two hand weapons of two cultures, and one of no item class.
					put("axe", "31_1");          // Dwarven Axe: [dwarven], cost 1, bearer a [dwarven] character
					put("iron", "32_18");        // Iron-Forged Weapon: [esgaroth], cost 2, bearer a Man or a [dwarven] companion
					put("map", "32_12");         // Thrór's Map: [dwarven], cost 0, bearer a [dwarven] companion
					// Artifacts: one to the support area, one borne.
					put("arkenstone", "31_7");   // The Arkenstone: [dwarven], cost 2, support area
					put("sting", "30_47");       // Sting: [shire], cost 1, bearer Bilbo
					// Free Peoples cards of other types that could be played in the fellowship phase.
					put("dori", "30_8");         // Dori: [dwarven] follower, cost 1
					put("balin", "30_2");        // Balin, Brother of Dwalin: [dwarven] companion, cost 3
					put("blunt", "33_4");        // Blunt The Knives!: [dwarven] Fellowship event, cost 0
					// Thorin, Oakenshield: [dwarven] Dwarf companion, to bear the possessions.
					put("thorin", "30_19");
					// Goblin Patrol Troop: a minion with no text.  Eight copies lie on top of the draw deck, for the
					// reconcile of turn 1 to draw (a hand of eight), so that the cards beneath are still in the deck
					// when the offer is worked out at the start of turn 3.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_177");
					put("troop6", "1_177");
					put("troop7", "1_177");
					put("troop8", "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "30_49");
					put("site2", "33_56");
					put("site3", "30_51");
					put("site4", "30_52");
					put("site5", "30_53");
					put("site6", "30_54");
					put("site7", "30_55");
					put("site8", "30_56");
					put("site9", "30_57");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GreatEastRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Great East Road
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 2
		 * Shadow Number: 4
		 * Direction: none
		 * Game Text: <b>Forest</b>. At the start of your fellowship phase, you may play a possession or artifact from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Great East Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAPossessionOrArtifactMayBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var iron = scn.GetFreepsCard("iron");
		var map = scn.GetFreepsCard("map");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var sting = scn.GetFreepsCard("sting");
		var dori = scn.GetFreepsCard("dori");
		var balin = scn.GetFreepsCard("balin");
		var blunt = scn.GetFreepsCard("blunt");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				axe, iron, map, arkenstone, sting, dori, balin, blunt);
		// Player 1's second turn begins at Great East Road, its offer pending.
		scn.SkipToSite(2);

		assertEquals("33_56", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, axe, iron, map, arkenstone, sting, dori, balin, blunt);
		assertEquals(8, scn.GetFreepsDeckCount());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// Possessions of two cultures and item classes, and artifacts of two cultures, borne and unborne; not a
		// follower, a companion or an event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, iron, map, arkenstone, sting));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dori, balin, blunt));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(arkenstone);

		assertInZone(Zone.SUPPORT, arkenstone);
		assertInZone(Zone.DECK, axe, iron, map, sting, dori, balin, blunt);
		assertEquals(7, scn.GetFreepsDeckCount());
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningPlaysNothingAndTheDrawDeckIsNotLookedThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var thorin = scn.GetFreepsCard("thorin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				axe, arkenstone);
		scn.SkipToSite(2);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertInZone(Zone.DECK, axe, arkenstone);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DECK, axe, arkenstone);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoPossessionOrArtifactInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dori = scn.GetFreepsCard("dori");
		var balin = scn.GetFreepsCard("balin");
		var blunt = scn.GetFreepsCard("blunt");

		scn.StartGame();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				dori, balin, blunt);
		scn.SkipToSite(2);

		assertInZone(Zone.DECK, dori, balin, blunt);
		assertEquals("33_56", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		// A follower, a companion and an event, each of which could be played now: nothing the site could play.
		// A play from the draw deck, alone or beside the discard pile, with nothing it could play, is withheld
		// (Q-0019; the orchestrator on Q-0406 and Q-0418).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipPhaseBeginsThereAndNotOnceTheFellowshipHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arkenstone = scn.GetFreepsCard("arkenstone");
		var shadowSting = scn.GetShadowCard("sting");
		var shadowDori = scn.GetShadowCard("dori");
		var shadowBilbo = scn.GetShadowRingBearer();

		scn.StartGame();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				arkenstone);
		scn.SkipToSite(2);

		// Player 1's turn at Great East Road: he is offered, and declines.
		assertInZone(Zone.DECK, arkenstone);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Player 2's turn begins there too: it is his fellowship phase, and his offer.  His deck is laid now, with
		// the Troops on top for what he draws before then.
		scn.SetShadowDrawDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("troop3"), scn.GetShadowCard("troop4"), scn.GetShadowCard("troop5"), scn.GetShadowCard("troop6"), scn.GetShadowCard("troop7"), scn.GetShadowCard("troop8"),
				shadowSting, shadowDori);
		scn.SkipToOtherPlayersTurn();

		assertEquals("33_56", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, shadowSting, shadowDori);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowSting));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowDori));
		scn.ShadowChooseCardBPFromSelection(shadowSting);

		assertAttachedTo(shadowSting, shadowBilbo);
		assertInZone(Zone.DECK, shadowDori);

		// Player 1's next turn begins at Rivendell, with Great East Road behind: no offer.
		scn.SkipToOtherPlayersTurn();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
