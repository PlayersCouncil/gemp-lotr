package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_011_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("honour", "102_11");
					put("haldir", "102_8");
					put("troop", "56_22");
					put("than", "54_85");
					put("veowyn", "4_270");
					put("arwen", "1_30");
					put("bow", "1_41");
					put("ring", "3_23"); //to ensure artifacts are not selectable
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ToHonourThatAllegianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: To Honour That Allegiance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Exert a valiant companion (or spot 2 valiant Elves) to take a valiant [elven] companion or [elven] possession into hand from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("honour");

		assertEquals("To Honour That Allegiance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToHonourThatAllegianceCanExertAValiantNonElfCompanionToTakeAValiantElfIntoHandFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var honour = scn.GetFreepsCard("honour");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var than = scn.GetFreepsCard("than");
		var veowyn = scn.GetFreepsCard("veowyn");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCardToHand(honour);
		scn.FreepsMoveCharToTable(veowyn, troop);

		scn.StartGame();

		assertEquals(Zone.FREE_CHARACTERS, veowyn.getZone());
		assertEquals(Zone.FREE_CHARACTERS, troop.getZone());
		assertEquals(Zone.DECK, than.getZone());
		assertEquals(Zone.DECK, haldir.getZone());
		assertEquals(Zone.DECK, bow.getZone());
		assertEquals(0, scn.GetWoundsOn(veowyn));
		assertTrue(scn.FreepsPlayAvailable(honour));

		scn.FreepsPlayCard(honour);
		scn.FreepsChooseCard(veowyn); //to exert
		scn.FreepsDismissRevealedCards();
		assertEquals(3, scn.FreepsGetSelectableCount()); //Haldir, Thandronen, bow

		scn.FreepsChooseCardBPFromSelection(haldir);
		assertEquals(Zone.HAND, haldir.getZone());
		assertEquals(Zone.DECK, bow.getZone());
		assertEquals(1, scn.GetWoundsOn(veowyn));
	}

	@Test
	public void ToHonourThatAllegianceCanExertAValiantNonElfCompanionToTakeAnElvenPossessionIntoHandFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var honour = scn.GetFreepsCard("honour");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var than = scn.GetFreepsCard("than");
		var veowyn = scn.GetFreepsCard("veowyn");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCardToHand(honour);
		scn.FreepsMoveCharToTable(veowyn, troop);

		scn.StartGame();

		assertEquals(Zone.FREE_CHARACTERS, veowyn.getZone());
		assertEquals(Zone.FREE_CHARACTERS, troop.getZone());
		assertEquals(Zone.DECK, than.getZone());
		assertEquals(Zone.DECK, haldir.getZone());
		assertEquals(Zone.DECK, bow.getZone());
		assertEquals(0, scn.GetWoundsOn(veowyn));
		assertTrue(scn.FreepsPlayAvailable(honour));

		scn.FreepsPlayCard(honour);
		scn.FreepsChooseCard(veowyn); //to exert
		scn.FreepsDismissRevealedCards();
		assertEquals(3, scn.FreepsGetSelectableCount()); //Haldir, Thandronen, bow

		scn.FreepsChooseCardBPFromSelection(bow);
		assertEquals(Zone.DECK, haldir.getZone());
		assertEquals(Zone.HAND, bow.getZone());
		assertEquals(1, scn.GetWoundsOn(veowyn));
	}

	@Test
	public void ToHonourThatAllegianceCanSpot2ValiantElvesToTakeAValiantElfIntoHandFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var honour = scn.GetFreepsCard("honour");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var than = scn.GetFreepsCard("than");
		var veowyn = scn.GetFreepsCard("veowyn");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCardToHand(honour);
		scn.FreepsMoveCharToTable(veowyn, troop, than);

		scn.StartGame();

		assertEquals(Zone.FREE_CHARACTERS, veowyn.getZone());
		assertEquals(Zone.FREE_CHARACTERS, troop.getZone());
		assertEquals(Zone.FREE_CHARACTERS, than.getZone());
		assertEquals(Zone.DECK, haldir.getZone());
		assertEquals(Zone.DECK, bow.getZone());
		assertTrue(scn.FreepsPlayAvailable(honour));

		scn.FreepsPlayCard(honour);
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.FreepsGetSelectableCount()); //Haldir, bow

		scn.FreepsChooseCardBPFromSelection(haldir);
		assertEquals(Zone.HAND, haldir.getZone());
		assertEquals(Zone.DECK, bow.getZone());
		assertEquals(0, scn.GetWoundsOn(veowyn));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(troop));
	}

	@Test
	public void ToHonourThatAllegianceCanSpot2ValiantElvesToTakeAnElvenPossessionIntoHandFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var honour = scn.GetFreepsCard("honour");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var than = scn.GetFreepsCard("than");
		var veowyn = scn.GetFreepsCard("veowyn");
		var bow = scn.GetFreepsCard("bow");
		scn.FreepsMoveCardToHand(honour);
		scn.FreepsMoveCharToTable(veowyn, troop, than);

		scn.StartGame();

		assertEquals(Zone.FREE_CHARACTERS, veowyn.getZone());
		assertEquals(Zone.FREE_CHARACTERS, troop.getZone());
		assertEquals(Zone.FREE_CHARACTERS, than.getZone());
		assertEquals(Zone.DECK, haldir.getZone());
		assertEquals(Zone.DECK, bow.getZone());
		assertTrue(scn.FreepsPlayAvailable(honour));

		scn.FreepsPlayCard(honour);
		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.FreepsGetSelectableCount()); //Haldir, bow

		scn.FreepsChooseCardBPFromSelection(bow);
		assertEquals(Zone.DECK, haldir.getZone());
		assertEquals(Zone.HAND, bow.getZone());
		assertEquals(0, scn.GetWoundsOn(veowyn));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(troop));
	}
}
