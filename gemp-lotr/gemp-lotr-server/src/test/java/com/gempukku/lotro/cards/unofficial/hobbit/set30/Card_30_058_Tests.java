package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_058_Tests
{

	// The Hobbit table, on the Hobbit path with the Free Peoples Pack's sites 1 and 2 in place of Bag End and The
	// Troll Hoard: Bilbo's Kitchen is site 1, where the game begins, and Trollshaw Forest 30_59 (whose text is at
	// the start of the maneuver phase) site 2.  Bilbo, Reliable Companion 30_46 is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Bifur, Inarticulate: [dwarven] follower of race Dwarf, cost 1.  Aid and a fierce-win trigger.
					put("bifur", "30_5");
					// Roäc, Son of Carc: [dwarven] follower of race Bird, cost 1.  Aid and a maneuver ability.
					put("roac", "32_8");
					// Old Thrush, Messenger Bird: a [shire] follower, cost 1.  Aid and a maneuver ability.
					put("thrush", "31_42");
					// Balin, Brother of Dwalin: a [dwarven] companion, cost 3, nothing needed to play him.
					put("balin", "30_2");
					// Thorin, Oakenshield: a [dwarven] Dwarf companion, vitality 4, to pay the exertion.
					put("thorin", "30_19");
					// Bard, The Bowman: [esgaroth] Man ally, vitality 3: a character who can exert and is no companion.
					put("bard", "30_22");
					// Goblin Patrol Troop: a card with no text, laid on top of a draw deck so that the reconciles
					// before a later turn do not take the cards its offer is to find.
					for (int i = 1; i <= 8; i++)
						put("troop" + i, "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "30_58");
					put("site2", "30_59");
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
	public void BilbosKitchenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bilbo's Kitchen
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: none
		 * Game Text: At the start of your fellowship phase, you may exert a companion to play a [dwarven] follower from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Bilbo's Kitchen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseExertsACompanionToPlayADwarvenFollowerFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard);

		// The game begins at Bilbo's Kitchen, its offer pending.
		scn.StartGame();
		scn.SetFreepsDrawDeck(bifur, roac, thrush, balin);

		assertEquals("30_58", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SUPPORT, bard);
		assertInZone(Zone.DECK, bifur, roac, thrush, balin);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();

		// Either companion may exert; the ally may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.FreepsDismissRevealedCards();

		// [dwarven] followers of two races; not a follower of another culture, nor a [dwarven] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush, balin));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(bifur);

		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.DECK, roac, thrush, balin);
		assertEquals(3, scn.GetFreepsDeckCount());
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyPlaysNothingAndTheDrawDeckIsNotLookedThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.SetFreepsDrawDeck(bifur, roac);

		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.DECK, bifur, roac);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertInZone(Zone.DECK, bifur, roac);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenEveryCompanionIsExhaustedThoughAnAllyCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bifur = scn.GetFreepsCard("bifur");
		var thorin = scn.GetFreepsCard("thorin");
		var bard = scn.GetFreepsCard("bard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard);
		// Vitality 4 with 3 wounds: both companions exhausted; Bard is not.
		scn.AddWoundsToChar(thorin, 3);
		scn.AddWoundsToChar(bilbo, 3);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(bard));

		scn.StartGame();
		scn.SetFreepsDrawDeck(bifur);

		assertEquals("30_58", scn.GetCurrentSite().getBlueprintId());
		assertInZone(Zone.DECK, bifur);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenFollowerInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrush = scn.GetFreepsCard("thrush");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();

		// The deck is laid before the game begins: the offer is worked out as the first fellowship phase begins.
		scn.SetFreepsDrawDeck(thrush, balin);
		scn.StartGame();

		assertInZone(Zone.DECK, thrush, balin);
		assertEquals(2, scn.GetFreepsDeckCount());
		// A [shire] follower and a [dwarven] companion: nothing the site could play.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// no companion exerts for a look that finds nothing.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipPhaseBeginsThereAndNotOnceTheFellowshipHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowBifur = scn.GetShadowCard("bifur");
		var shadowThrush = scn.GetShadowCard("thrush");
		var shadowBilbo = scn.GetShadowRingBearer();

		scn.StartGame();

		// Player 1's turn at Bilbo's Kitchen: he is offered, and declines.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		scn.SetShadowDrawDeck(scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("troop3"), scn.GetShadowCard("troop4"), scn.GetShadowCard("troop5"), scn.GetShadowCard("troop6"), scn.GetShadowCard("troop7"), scn.GetShadowCard("troop8"),
				shadowBifur, shadowThrush);

		// Player 2's turn begins there too: it is his fellowship phase, and his offer.
		scn.SkipToOtherPlayersTurn();

		assertEquals("30_58", scn.GetCurrentSite().getBlueprintId());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertInZone(Zone.DECK, shadowBifur, shadowThrush);
		assertEquals(0, scn.GetWoundsOn(shadowBilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowAcceptOptionalTrigger();
		// His own Bilbo, his one companion, exerts unasked.
		assertEquals(1, scn.GetWoundsOn(shadowBilbo));
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowBifur));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shadowThrush));
		scn.ShadowChooseCardBPFromSelection(shadowBifur);

		assertInZone(Zone.SUPPORT, shadowBifur);
		assertInZone(Zone.DECK, shadowThrush);

		// Player 1's next turn begins at site 2, with Bilbo's Kitchen behind: no offer.
		scn.SkipToOtherPlayersTurn();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
