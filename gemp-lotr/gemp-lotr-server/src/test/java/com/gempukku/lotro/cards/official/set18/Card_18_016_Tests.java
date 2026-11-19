package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("miruvore", "18_16");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, vitality 3; stronger only against a Nazgul
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability never used here
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] Elf who is an ally; an archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MiruvoreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Miruvóre
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Add 2 threats to heal an [elven] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("miruvore");

		assertEquals("Miruvóre", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsTwoThreatsToHealOneWoundFromAnElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miruvore = scn.GetFreepsCard("miruvore");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, legolas, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(miruvore);
		// Two wounds on each, so that a heal of one reads 1
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(legolas, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(orophin, 2);
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInHand(miruvore);

		scn.StartGame();

		// Four companions: room for four threats.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(miruvore));
		scn.FreepsPlayCard(miruvore);

		// Two [elven] companions of different titles; not the [elven] ally, the [gondor] companion or the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(legolas);

		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInDiscard(miruvore);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilTwoThreatsCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miruvore = scn.GetFreepsCard("miruvore");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(miruvore);
		scn.AddWoundsToChar(arwen, 1);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInHand(miruvore);

		scn.StartGame();
		// Three companions and two threats: only one more threat can be added.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(miruvore));

		// One threat fewer: room for exactly two.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(miruvore));
		scn.FreepsPlayCard(miruvore);

		// Arwen is the one wounded [elven] companion, and is healed without a question.
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(miruvore);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedElvenCompanionTheThreatsAreAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miruvore = scn.GetFreepsCard("miruvore");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(miruvore);
		// Wounded: a [gondor] companion and an [elven] ally. The [elven] companion is not.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(orophin, 1);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertInHand(miruvore);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(miruvore));
		scn.FreepsPlayCard(miruvore);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertInDiscard(miruvore);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
