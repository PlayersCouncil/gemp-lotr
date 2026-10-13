package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow1", "1_41");
					put("bow2", "1_41");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, a ranger and no archer; a bonus against Nazgûl only
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally, no archer; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Elven Bow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be an Elf.<br>Bearer is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow1");

		assertEquals("Elven Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAnElfCompanionOrAllyAndMakesItsBearerAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow1 = scn.GetFreepsCard("bow1");
		var bow2 = scn.GetFreepsCard("bow2");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(bow1, bow2);

		scn.StartGame();

		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(celeborn, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow1));

		scn.FreepsPlayCard(bow1);
		// An Elf companion and an Elf ally; not a Man, not a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(arwen);
		assertAttachedTo(bow1, arwen);
		assertTrue(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(celeborn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));

		// Arwen bears a ranged weapon now: the second copy goes to the ally, who is then an archer too.
		scn.FreepsPlayCard(bow2);
		assertAttachedTo(bow2, celeborn);
		assertTrue(scn.HasKeyword(celeborn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
