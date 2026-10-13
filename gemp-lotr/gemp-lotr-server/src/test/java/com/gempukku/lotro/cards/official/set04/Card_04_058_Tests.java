package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alliance", "4_58");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an archery ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion; to play, spot an Elf (cheated in)
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf who is an ally, not a companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf
					put("runner", "1_178");     // Goblin Runner: a minion for the maneuver phase; cheated in
					// Hobbit Swords: possessions with no text, so that the draw deck has cards to draw.
					put("sword1", "1_299");
					put("sword2", "1_299");
					put("sword3", "1_299");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllianceReforgedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Alliance Reforged
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Tale.
		 * Fellowship: Draw a card for each Elf companion you spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alliance");

		assertEquals("Alliance Reforged", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoElfCompanionsThePlayerNamesHowManyToSpotAndDrawsThatMany() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(alliance);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(alliance));
		scn.FreepsPlayCard(alliance);

		// Legolas and Arwen; not Orophin (an Elf ally), Aragorn or Frodo (companions who are no Elves).
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		// One is named, and one card drawn, not two.
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(alliance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithThreeElfCompanionsSpottedItDrawsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, arwen, haldir, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(alliance);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		scn.FreepsPlayCard(alliance);

		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		// Three draws, within the Rule of 4.
		scn.FreepsChoose("3");
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(alliance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedInTheFellowshipPhaseAndNotTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(alliance);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(alliance));

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(alliance));
		assertInHand(alliance);
	}
}
