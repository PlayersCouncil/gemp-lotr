package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("refuge", "12_21");

					put("legolas", "11_21");    // Legolas, Companion of the Ring: an Elf companion with no requirement to play; +2 only at a battleground or forest
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: an Elf ally, home 3F; a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion; a skirmish ability only

					// Cards held in hand for the cost.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("greenleaf", "1_50");   // Legolas, Greenleaf: an [elven] Elf companion
					put("galadriel", "1_45");   // Galadriel, Lady of Light: an [elven] Elf ally, a character who is no companion
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RefugeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Refuge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an Elf.<br><b>Fellowship:</b> Discard a companion from hand to heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("refuge");

		assertEquals("Refuge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var refuge = scn.GetFreepsCard("refuge");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(refuge, legolas);

		scn.StartGame();

		assertInHand(refuge);
		// Gimli and Frodo: no Elf to spot.
		assertFalse(scn.FreepsPlayAvailable(refuge));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsPlayAvailable(refuge));

		scn.FreepsPlayCard(refuge);
		assertInZone(Zone.SUPPORT, refuge);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyAloneIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var refuge = scn.GetFreepsCard("refuge");
		var golradir = scn.GetFreepsCard("golradir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(refuge);

		scn.StartGame();

		assertInHand(refuge);
		assertTrue(scn.FreepsPlayAvailable(refuge));

		scn.FreepsPlayCard(refuge);
		assertInZone(Zone.SUPPORT, refuge);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsACompanionFromHandToHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var refuge = scn.GetFreepsCard("refuge");
		var golradir = scn.GetFreepsCard("golradir");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var galadriel = scn.GetFreepsCard("galadriel");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToSupportArea(refuge);
		scn.MoveCardsToHand(aragorn, greenleaf, galadriel, cloak);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(golradir, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, refuge);
		assertInHand(aragorn, greenleaf, galadriel, cloak);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertTrue(scn.FreepsActionAvailable(refuge));

		scn.FreepsUseCardAction(refuge);
		// The cost: companions of two cultures, and not an ally or a possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, greenleaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel, cloak));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(aragorn);
		assertInDiscard(aragorn);
		assertInHand(greenleaf, galadriel, cloak);

		// The heal: wounded companions, and not the wounded ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(golradir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(frodo);
		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertInZone(Zone.SUPPORT, refuge);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var refuge = scn.GetFreepsCard("refuge");
		var galadriel = scn.GetFreepsCard("galadriel");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCardsToSupportArea(refuge);
		scn.MoveCardsToHand(galadriel, cloak);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, refuge);
		assertInHand(galadriel, cloak);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// An ally and a possession in hand, and no companion to discard.
		assertFalse(scn.FreepsActionAvailable(refuge));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoCompanionWoundedTheCompanionIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var refuge = scn.GetFreepsCard("refuge");
		var golradir = scn.GetFreepsCard("golradir");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToSupportArea(refuge);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(golradir, 1);
		scn.FreepsRefreshDecision();

		assertInHand(aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(golradir));
		// Only the ally is wounded: the action has a cost, so it is still offered.
		assertTrue(scn.FreepsActionAvailable(refuge));

		scn.FreepsUseCardAction(refuge);
		// The one companion in hand goes unasked, and there is no companion to heal.
		assertInDiscard(aragorn);
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
