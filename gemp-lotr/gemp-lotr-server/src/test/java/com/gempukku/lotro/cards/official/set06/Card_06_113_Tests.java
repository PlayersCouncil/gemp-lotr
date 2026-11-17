package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "6_113");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally, no companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of another culture
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Merry, Impatient Hobbit
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Regroup:</b> Discard Merry to heal a [gandalf] companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Impatient Hobbit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void RegroupAbilityDiscardsMerryToHealAGandalfCompanionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(avenger, 1);
		scn.AddWoundsToChar(barliman, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);

		// Merry goes to the discard pile, not the dead pile (CR 4.2 "discard").
		assertInDiscard(merry);
		// Each wounded [gandalf] companion; not the [gandalf] ally or the [gondor] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		// One wound of two healed.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionWoundedItIsStillOfferedAndMerryIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, gandalf, aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsUseCardAction(merry);

		assertInDiscard(merry);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
