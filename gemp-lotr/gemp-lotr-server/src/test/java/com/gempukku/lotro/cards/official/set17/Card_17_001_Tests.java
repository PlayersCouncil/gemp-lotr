package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_1");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli bearing Armor of Khazad, with three other [dwarven] possessions in the discard pile.
	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("khazad", "17_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession
					put("helm", "1_15");        // Gimli's Helm: [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmorofKhazadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Armor of Khazâd
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Dwarf.<br>Each time bearer is about to take a wound, you may add a threat to prevent that.<br><b>Regroup:</b> Discard this possession to take up to 2 other [dwarven] possessions into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Armor of Khazâd", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Take up to 2 other [dwarven] possessions into hand from your discard pile": none, one or two (the shape
	// of F-0033, Gwaihir 101_15).  The definition asked for exactly 2 whenever two or more were there.
	// F-0457.
	@Test
	public void UpToTwoPossessionsMayBeOneOrNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var khazad = scn.GetFreepsCard("khazad");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, khazad);
		scn.MoveCardsToDiscard(axe, armor, helm);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(khazad));
		scn.FreepsUseCardAction(khazad);

		// The three others are offered, and not the Armor of Khazad just discarded.
		assertInZone(Zone.DISCARD, khazad);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(axe);

		assertInZone(Zone.HAND, axe);
		assertInZone(Zone.DISCARD, khazad, armor, helm);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void ArmorofKhazadTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
