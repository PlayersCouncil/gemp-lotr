package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_024_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Shadow phase of
	// turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer: a minion of site 3 or more
	// played there pays the roaming 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("influence", "33_24");
					put("influence2", "33_24");

					// Alfrid, Lickspittle: unique [men] Man, cost 2, site 6; a trigger on an [esgaroth] ally assigned,
					// a maneuver play from the discard pile and a skirmish ability, none of them met or used
					put("alfrid", "32_25");
					// Laketown Patrol: [men] Man, cost 4, site 6, fierce; its play trigger is never met, and its bar on
					// weapon bonuses reaches no card here
					put("patrol", "33_27");
					// Southron Explorer: [raider] Man, cost 2, site 4; a regroup ability not used
					put("explorer", "4_250");
					// Spear of the Guard: a [men] possession, no minion; never played
					put("spear", "33_28");
					// Goblin Runner: [moria] Orc, a minion of another culture and race; never played
					put("runner", "31_30");
					// Archer of Harad: a [men] Man, archer, cost 3; its archery text reaches no phase here
					put("archer", "11_67");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	// The same table with the Shadow player's draw deck left empty once both cards are placed.
	protected VirtualTableScenario GetEmptyDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("influence", "33_24");
					put("alfrid", "32_25");     // Alfrid, Lickspittle: [men] Man; his texts not met or used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AlfridsInfluenceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Alfrid's Influence
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: The site number of each [men] Man is -3.
		 * <b>Shadow:</b> Remove (1) to discard the bottom card of your draw deck. If it is a minion, play a [men] Man from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("influence");

		assertEquals("Alfrid's Influence", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheSiteNumberOfEachMenManIsMinus3InPlayAndInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var influence = scn.GetShadowCard("influence");
		var alfrid = scn.GetShadowCard("alfrid");
		var patrol = scn.GetShadowCard("patrol");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(alfrid, explorer);
		scn.MoveCardsToHand(patrol);
		assertInZone(Zone.HAND, patrol);
		assertEquals(6, scn.GetMinionSiteNumber(alfrid));
		assertEquals(6, scn.GetMinionSiteNumber(patrol));
		assertEquals(4, scn.GetMinionSiteNumber(explorer));

		scn.MoveCardsToSupportArea(influence);
		assertInZone(Zone.SUPPORT, influence);
		// [men] Men of two titles, one in play and one in hand; not a Man of another culture.
		assertEquals(3, scn.GetMinionSiteNumber(alfrid));
		assertEquals(3, scn.GetMinionSiteNumber(patrol));
		assertEquals(4, scn.GetMinionSiteNumber(explorer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowActionRemoves1ToDiscardTheBottomCardAndPlaysNothingForACardThatIsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var influence = scn.GetShadowCard("influence");
		var alfrid = scn.GetShadowCard("alfrid");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(influence);
		scn.MoveCardsToDiscard(alfrid);
		// A [men] card that is no minion lies at the bottom.
		scn.MoveCardsToBottomOfDeck(spear);
		assertInZone(Zone.DECK, spear);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		var freepsBottom = scn.GetFreepsBottomOfDeck();
		int deckCount = scn.GetShadowDeckCount();
		assertInZone(Zone.SUPPORT, influence);
		assertInZone(Zone.DISCARD, alfrid);
		assertSame(spear, scn.GetShadowBottomOfDeck());
		assertEquals(3, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(influence));
		scn.ShadowUseCardAction(influence);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, spear);
		assertEquals(deckCount - 1, scn.GetShadowDeckCount());
		// Only the Shadow player's own draw deck.
		assertSame(freepsBottom, scn.GetFreepsBottomOfDeck());
		// Nothing played: Alfrid stays in the discard pile.
		assertInZone(Zone.DISCARD, alfrid);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithNoTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var influence = scn.GetShadowCard("influence");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(influence);
		assertInZone(Zone.SUPPORT, influence);
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(influence));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(influence));
	}

	@Test
	public void AMinionAtTheBottomPlaysAMenManFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var influence = scn.GetShadowCard("influence");
		var influence2 = scn.GetShadowCard("influence2");
		var alfrid = scn.GetShadowCard("alfrid");
		var patrol = scn.GetShadowCard("patrol");
		var explorer = scn.GetShadowCard("explorer");
		var runner = scn.GetShadowCard("runner");
		var freepsArcher = scn.GetFreepsCard("archer");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(influence);
		scn.MoveCardsToDiscard(alfrid, patrol, explorer, influence2, freepsArcher);
		// The Shadow player's own Archer of Harad stays in his draw deck, so an Archer offered would be the other player's.
		// A minion of another culture and race lies at the bottom.
		scn.MoveCardsToBottomOfDeck(runner);
		assertInZone(Zone.DECK, runner);
		// 6 left after the (1): Laketown Patrol at exactly 4 + 2 roaming (site 6 - 3 against site 2).
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, influence);
		assertInZone(Zone.DISCARD, alfrid);
		assertInZone(Zone.DISCARD, patrol);
		assertInZone(Zone.DISCARD, explorer);
		assertInZone(Zone.DISCARD, influence2);
		assertInZone(Zone.DISCARD, freepsArcher);
		assertSame(runner, scn.GetShadowBottomOfDeck());
		assertEquals(7, scn.GetTwilight());

		scn.ShadowUseCardAction(influence);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.DISCARD, runner);
		// [men] Men of two titles; not a Man of another culture, nor a [men] card that is no Man, nor the minion just
		// discarded, nor the other player's [men] Man in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(alfrid, patrol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(explorer, influence2, runner, freepsArcher));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(alfrid);
		assertInZone(Zone.SHADOW_CHARACTERS, alfrid);
		assertInZone(Zone.DISCARD, patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheShadowActionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEmptyDeckScenario();

		var influence = scn.GetShadowCard("influence");
		var alfrid = scn.GetShadowCard("alfrid");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(influence);
		scn.MoveCardsToDiscard(alfrid);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, influence);
		assertInZone(Zone.DISCARD, alfrid);
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(influence));
		scn.ShadowUseCardAction(influence);
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, alfrid);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
