package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_18_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "18_71");
					put("beast", "17_40");      // Beast of War: [men] mount, bearer must be a [men] Man
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl with no text below 2 burdens
					put("steed", "1_208");      // Black Steed: a [ringwraith] mount, bearer must be a Nazgûl
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakilCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Mûmakil Commander, Bold and Grim
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While at a battleground, this minion is strength +2.<br>Each mounted Man is strength +1.<br><b>Regroup:</b> Shuffle any number of your [men] mounts in play into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Mûmakil Commander", card.getBlueprint().getTitle());
		assertEquals("Bold and Grim", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithoutAMenMountOfHisInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.MoveMinionsToTable(commander, lemenya);
		// A mount of his, but not a [men] one.
		scn.AttachCardsTo(lemenya, steed);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// Ketura on Q-0112: the ability requires a mount in play, since with none it would only shuffle the deck.
		assertFalse(scn.ShadowActionAvailable(commander));
	}

	@Test
	public void RegroupAbilityShufflesAnyNumberOfHisMenMountsIntoHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var beast = scn.GetShadowCard("beast");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.MoveMinionsToTable(commander, lemenya);
		scn.AttachCardsTo(commander, beast);
		scn.AttachCardsTo(lemenya, steed);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(commander));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// "Any number": none at all is a choice, and the deck is shuffled all the same (Ketura on Q-0112).
		scn.ShadowUseCardAction(commander);
		assertTrue(scn.ShadowHasCardChoiceAvailable(beast));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards();

		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(commander, beast.getAttachedTo());
		assertEquals(lemenya, steed.getAttachedTo());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(commander));
		scn.ShadowUseCardAction(commander);
		scn.ShadowChooseCards(beast);

		assertInZone(Zone.DECK, beast);
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(lemenya, steed.getAttachedTo());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
