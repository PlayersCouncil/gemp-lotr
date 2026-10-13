package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("west", "7_23");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, vitality 3; an archery ability only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally, vitality 4; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf
					put("gimli", "1_13");       // Gimli, Son of Glóin: a fourth companion, so that 4 threats can lie
					// The Free Peoples draw deck is these three cards and nothing else.
					put("strike", "1_3");       // Axe Strike: a [dwarven] event
					put("lorien", "1_53");      // Lórien Elf: an [elven] companion
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntotheWestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Into the West
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Exert an Elf to reveal the top card of your draw deck. If it is an [elven] card, remove 3 threats. Place the revealed card beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Into the West", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfToRevealTheTopCardOfYourDrawDeckAndPlacesItBeneath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var west = scn.GetFreepsCard("west");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(west);
		scn.MoveCompanionsToTable(legolas, aragorn, gimli);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// The last card named is the top card: Axe Strike, then Lórien Elf, then Dwarf Guard.
		scn.MoveCardsToTopOfDeck(guard, lorien, strike);
		// Four companions hold 4 threats: one more than an [elven] card takes away.
		scn.AddThreats(4);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(strike, scn.GetFreepsTopOfDeck());
		assertEquals(4, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsActionAvailable(west));

		scn.FreepsUseCardAction(west);
		// An Elf: the companion and the ally, and nobody else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elrond));

		// The top card is shown to both players, and the card under it to neither.
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(lorien, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(strike, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(lorien, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// Axe Strike is no [elven] card: the threats stay.  It lies beneath the deck, and the second card is the top.
		assertEquals(4, scn.GetThreats());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(strike, scn.GetFreepsBottomOfDeck());
		assertEquals(lorien, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The card now on top is [elven]: a second use, paid for by the other Elf, removes 3 of the 4 threats.
		scn.ShadowPass();
		scn.FreepsUseCardAction(west);
		scn.FreepsChooseCard(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lorien, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(lorien, scn.GetFreepsBottomOfDeck());
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
