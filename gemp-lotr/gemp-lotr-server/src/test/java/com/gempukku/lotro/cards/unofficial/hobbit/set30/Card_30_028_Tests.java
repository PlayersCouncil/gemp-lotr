package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_028_Tests
{

	// The Hobbit table: the Hobbit path (site 5 is Mirkwood 30_53, "When the fellowship moves to Mirkwood,
	// discard Gandalf") and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "30_28");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					put("kili", "30_12");       // Kili, Brother of Fili: [dwarven] Dwarf companion; an archery ability only
					put("dain", "32_2");        // Dáin Ironfoot: [dwarven] Dwarf ally; a regroup trigger only
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower, a Dwarf; a fierce-win trigger only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Dwarf; +2 only with 3 Ents
					put("despair", "7_148");    // Fierce in Despair: Regroup event, cost 7, "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)"
					put("axeman1", "4_224");    // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens
					put("axeman2", "4_224");
					// Dark Sorcery: [wraith] condition, "The first sentence of Gandalf's game text does not apply"
					put("sorcery", "33_61");
					// Goblin Patrol Troop: a card with no text, for a draw deck with no Dwarf companion in it
					put("card1", "1_177");
					put("card2", "1_177");
					// More of them, laid on top of a draw deck so that the hands drawn before a later turn do not take
					// the Dwarf companion its offer is to find.
					for (int i = 1; i <= 8; i++)
						put("troop" + i, "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Gandalf, Leader of the Company
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Wise</b>.<br>Gandalf cannot be discarded.<br>At the start of each of your turns, you may add a doubt to play a Dwarf companion from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Leader of the Company", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void GandalfIsNotDiscardedOnTheMoveToMirkwood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowDiscardOfACompanionOffersTheDwarvesAndNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var kili = scn.GetFreepsCard("kili");
		var despair = scn.GetShadowCard("despair");
		scn.MoveCardsToHand(despair);
		assertInZone(Zone.HAND, despair);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, balin, kili);

		scn.SkipToPhase(Phase.REGROUP);
		// Two [raider] Men for Fierce in Despair to spot, standing in the regroup phase as minions that lived
		// through the skirmishes do.
		scn.MoveMinionsToTable(scn.GetShadowCard("axeman1"), scn.GetShadowCard("axeman2"));
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);
		// The Ring-bearer is excepted by the event, and Gandalf by his own text.
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, kili));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, scn.GetRingBearer()));
		scn.ShadowChooseCard(balin);

		assertInZone(Zone.DISCARD, balin);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, kili);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheStartOfHisOwnersTurnHeMayAddADoubtToPlayADwarfCompanionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var kili = scn.GetFreepsCard("kili");
		var dain = scn.GetFreepsCard("dain");
		var bifur = scn.GetFreepsCard("bifur");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.DECK, balin, kili, dain, bifur, avenger);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		// The deck is looked through: Dwarf companions of two titles may be played; not a Dwarf ally, a Dwarf
		// follower, nor a companion who is no Dwarf.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, kili));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, bifur, avenger));
		scn.FreepsChooseCardBPFromSelection(kili);

		assertInZone(Zone.FREE_CHARACTERS, kili);
		assertInZone(Zone.DECK, balin, dain, bifur, avenger);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAtTheStartOfTheTurnBeforeTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
	}

	@Test
	public void DecliningTheOfferAddsNoDoubtAndLeavesTheDeckUnlooked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.DECK, balin);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInZone(Zone.DECK, balin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarfCompanionInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var dain = scn.GetFreepsCard("dain");
		var bifur = scn.GetFreepsCard("bifur");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		// No Dwarf companion in the draw deck: a Dwarf ally, a Dwarf follower and a companion who is no Dwarf.
		scn.SetFreepsDrawDeck(dain, bifur, avenger, scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"));
		assertInZone(Zone.DECK, dain, bifur, avenger);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on
		// Q-0406 and Q-0418): no doubt is paid for a look that finds nothing.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DECK, dain, bifur, avenger);
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferComesAgainAtHisOwnersNextTurnAndNotAtTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// A Dwarf companion kept in the deck for the turns to come: the offer is made only with one there.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				scn.GetFreepsCard("balin"));

		// Turn 2, the other player's: nothing is offered to Gandalf's owner.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		// Turn 3, his owner's, at site 2: offered again.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyByDarkSorceryHeIsDiscardedAtMirkwoodAndHisStartOfTurnOfferIsStillMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");

		var sorcery = scn.GetShadowCard("sorcery");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(sorcery);
		assertInZone(Zone.SUPPORT, sorcery);

		// Dark Sorcery leaves his start-of-turn offer alone.
		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		// A Dwarf companion kept in the deck for the turns to come: the offer is made only with one there.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"), scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"), scn.GetFreepsCard("troop6"), scn.GetFreepsCard("troop7"), scn.GetFreepsCard("troop8"),
				scn.GetFreepsCard("balin"));

		scn.SkipToSite(4);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.SUPPORT, sorcery);
		// Turn 7, at site 4: the offer again, declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
