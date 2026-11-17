package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_054_Tests
{

	// The Hobbit table: the Hobbit path, with Esgaroth as site 6, Bilbo, Reliable Companion 30_46 as the
	// Ring-bearer and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a Regroup ability, not used
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally; a Fellowship ability, not used
					put("weapon", "32_18");     // Iron-Forged Weapon: [esgaroth] possession that Dwalin could bear
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability, not used
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] Wizard ally; a Fellowship ability, not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion in play, a bearer for the Weapon
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void EsgarothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Esgaroth
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: none
		 * Game Text: <b>River</b>. <b>Sanctuary</b>. At the start of the regroup phase, you may play a [esgaroth] or [dwarven] ally from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Esgaroth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseAnEsgarothOrDwarvenAllyMayBePlayedFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var grimir = scn.GetFreepsCard("grimir");
		var weapon = scn.GetFreepsCard("weapon");
		var balin = scn.GetFreepsCard("balin");
		var radagast = scn.GetFreepsCard("radagast");
		var dwalin = scn.GetFreepsCard("dwalin");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(dwalin);
		// The turns on the way drew the small deck into the hand: these go back.
		scn.MoveCardsToTopOfDeck(bard, grimir, weapon, balin, radagast);
		assertInZone(Zone.DECK, bard, grimir, weapon, balin, radagast);
		assertInZone(Zone.FREE_CHARACTERS, dwalin);

		scn.FreepsPass();

		// At the start of the Shadow phase there, the phase before regroup with no minion in play: nothing.
		assertEquals("30_54", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// An ally of each culture; not a card of either culture that is no ally, nor an ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bard, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weapon, balin, radagast));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(grimir);

		assertInZone(Zone.SUPPORT, grimir);
		assertInZone(Zone.DECK, bard, weapon, balin, radagast);
		// Shuffled as Grimir leaves the deck that was looked through, and again once the look is over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSuchAllyInTheDrawDeckTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapon = scn.GetFreepsCard("weapon");
		var balin = scn.GetFreepsCard("balin");
		var radagast = scn.GetFreepsCard("radagast");
		var dwalin = scn.GetFreepsCard("dwalin");

		scn.StartGame();
		scn.MoveCardsToFreepsDiscard("bard", "grimir");
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable(dwalin);
		scn.MoveCardsToTopOfDeck(weapon, balin, radagast);
		assertInZone(Zone.DECK, weapon, balin, radagast);
		assertInZone(Zone.DISCARD, scn.GetFreepsCard("bard"), scn.GetFreepsCard("grimir"));

		scn.FreepsPass();
		scn.ShadowPass();

		// A possession, a [dwarven] companion and an ally of another culture in the deck, the allies it could play in
		// the discard pile: nothing the site could play.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418; F-0804).
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, weapon, balin, radagast);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedNothingIsPlayedOrShuffledAndThePlayerWhoseFellowshipArrivesNextIsOfferedItInTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var shadowBard = scn.GetShadowCard("bard");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToTopOfDeck(bard);
		scn.MoveCardsToTopOfDeck(shadowBard);
		assertInZone(Zone.DECK, bard, shadowBard);

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DECK, bard);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The first player stays at Esgaroth; on his own turn the second player's fellowship moves there from
		// site 5, and at the start of that regroup phase it is he who may play an ally, his Bard.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.MoveCardsToTopOfDeck(shadowBard);
		assertInZone(Zone.DECK, shadowBard);
		scn.ShadowPass();

		assertEquals("30_54", scn.GetCurrentSite().getBlueprintId());
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		scn.ShadowAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shadowBard);

		assertInZone(Zone.SUPPORT, shadowBard);
		assertTrue(scn.ShadowDecisionAvailable("Play Regroup action or Pass"));
	}
}
