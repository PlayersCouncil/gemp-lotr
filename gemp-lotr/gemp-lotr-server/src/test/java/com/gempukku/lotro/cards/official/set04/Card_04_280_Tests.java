package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_280_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herugrim", "4_280");
					// Théoden, Son of Thengel: [rohan] Man, cost 2; a regroup ability that needs a [rohan] ally, never used
					put("theoden", "4_292");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion who is not Théoden; Valiant and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLastOfHisHouseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herugrim", "4_280");
					// Theoden, Last of His House: [rohan] Man companion whose title is printed without the accent; his
					// strength bonus counts valiant companions assigned to skirmishes
					put("theoden", "102_55");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion who is not Théoden; Valiant and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HerugrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Herugrim
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be Théoden.
		 * He is damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herugrim");

		assertEquals("Herugrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnlyOnTheodenAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(theoden, herugrim);

		scn.StartGame();

		// Frodo and the Household Guard, a [rohan] Man, and no Théoden: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(herugrim));

		scn.FreepsPlayCard(theoden);
		assertTrue(scn.FreepsPlayAvailable(herugrim));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));

		// Théoden is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(herugrim);
		assertAttachedTo(herugrim, theoden);
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheodenWhoseTitleHasNoAccentCanBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLastOfHisHouseScenario();

		var herugrim = scn.GetFreepsCard("herugrim");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(herugrim);
		scn.MoveCompanionsToTable(theoden, guard);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(herugrim));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));

		// Théoden is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(herugrim);
		assertAttachedTo(herugrim, theoden);
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
