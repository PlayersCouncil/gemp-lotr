package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "7_36");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] companion; abilities only used on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] companion of cost 1, played to make three
					put("bounder", "1_286");    // Bounder: a [shire] ally, a Hobbit who is no companion
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion, not unique; +2 only with 3 Ents
					put("avenger2", "6_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gandalf, Defender of the West
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 9
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: While you cannot spot 3 companions of the same culture, Gandalf is strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Defender of the West", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void WithTwoCompanionsOfEachOfTwoCulturesGandalfIsStrengthMinus2UntilAThirdOfOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var avenger = scn.GetFreepsCard("avenger");
		// Frodo and Sam are [shire], Gandalf and the Ent Avenger [gandalf]: four companions, no three of one culture.
		scn.MoveCompanionsToTable(gandalf, sam, avenger);
		scn.MoveCardsToHand(merry);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));    // 9 - 2
		// The Ent Avenger is untouched: his printed 6.
		assertEquals(6, scn.GetStrength(avenger));

		scn.FreepsPlayCard(merry);
		// Frodo, Sam and Merry: three [shire] companions.
		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GandalfHimselfIsOneOfTheThreeCompanionsOfACulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var avenger2 = scn.GetFreepsCard("avenger2");
		// Gandalf and two Ent Avengers are three [gandalf] companions; Frodo is the only [shire] one.
		scn.MoveCompanionsToTable(gandalf, avenger, avenger2);

		scn.StartGame();

		assertEquals(9, scn.GetStrength(gandalf));
	}

	@Test
	public void AnAllyOfTheCultureIsNotACompanionOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo and Sam are two [shire] companions; Bounder makes three [shire] characters, but is an ally.
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));    // 9 - 2
	}
}
