package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("griffo", "101_51");

					put("bounder", "1_286");    // Bounder: a [shire] ally that is not unique, (1), home site 2; a skirmish ability
					put("maggot", "1_288");     // Farmer Maggot: a unique [shire] ally, (1), home site 1; heals Merry or Pippin
					put("gaffer", "1_291");     // The Gaffer: a unique [shire] ally, (1); Bag End plays him from the draw deck
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] companion, (1)
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally, (1), with nothing to spot to play him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GriffoBoffinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Griffo Boffin, Uncouth Fellow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: Each time you play a [shire] ally, you may add (1) to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("griffo");

		assertEquals("Griffo Boffin", card.getBlueprint().getTitle());
		assertEquals("Uncouth Fellow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilightAndHisOwnPlayOffersTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var griffo = scn.GetFreepsCard("griffo");
		scn.MoveCardsToHand(griffo);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(griffo));

		scn.FreepsPlayCard(griffo);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, griffo);
		// He is a [shire] ally himself, and is in play by the time his being played is responded to.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(griffo));
		scn.FreepsAcceptOptionalTrigger();

		// 1 to play him + 1 for the card
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachShireAllyPlayedMayAddOneTwilightToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var griffo = scn.GetFreepsCard("griffo");
		var bounder = scn.GetFreepsCard("bounder");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveAlliesToTable(griffo);
		scn.MoveCardsToHand(bounder, maggot);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		var first = scn.GetFreepsTopOfDeck();
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, first);

		// A [shire] ally that is not unique, whose home is site 2.
		scn.FreepsPlayCard(bounder);

		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(griffo));
		scn.FreepsAcceptOptionalTrigger();

		// 1 for the Bounder + 1 for the card
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, first);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		var second = scn.GetFreepsTopOfDeck();
		assertInZone(Zone.DECK, second);

		// A unique [shire] ally, whose home is site 1.
		scn.FreepsPlayCard(maggot);

		assertInZone(Zone.SUPPORT, maggot);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(griffo));
		scn.FreepsAcceptOptionalTrigger();

		// 2 + 1 for Farmer Maggot + 1 for the card
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, first, second);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningAddsNoTwilightAndDrawsNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var griffo = scn.GetFreepsCard("griffo");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(griffo);
		scn.MoveCardsToHand(bounder);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(bounder);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(griffo));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, bounder);
		// 1 for the Bounder and no more
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireAllyPlayedFromTheDrawDeckOffersTheDrawToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var griffo = scn.GetFreepsCard("griffo");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveAlliesToTable(griffo);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertInZone(Zone.DECK, gaffer);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());

		// Bag End: "Fellowship: Exert a Hobbit to play The Gaffer from your draw deck."
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(frodo);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gaffer);

		assertInZone(Zone.SUPPORT, gaffer);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(griffo));
		scn.FreepsAcceptOptionalTrigger();

		// 1 for The Gaffer + 1 for the card
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		// The Gaffer and the card drawn
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireCompanionOrAnAllyOfAnotherCultureOffersNothing() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var griffo = scn.GetFreepsCard("griffo");
		var merry = scn.GetFreepsCard("merry");
		var grimir = scn.GetFreepsCard("grimir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(griffo);
		scn.MoveCardsToHand(merry, grimir, bounder);
		// Every [shire] ally is a Hobbit, so the [dwarven] ally is made one: it is the culture that the text names.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateFreepsFilter("name(Grimir)"), null, Race.HOBBIT));

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(grimir);

		assertInZone(Zone.SUPPORT, grimir);
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		// On the same board a [shire] ally does offer it.
		scn.FreepsPlayCard(bounder);

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(griffo));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
