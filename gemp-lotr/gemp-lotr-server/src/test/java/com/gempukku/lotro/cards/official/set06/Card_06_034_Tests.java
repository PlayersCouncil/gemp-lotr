package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("roused1", "6_34");
					put("roused2", "6_34");

					// Ents whose strength nothing of their own changes (Ent Avenger 6_27 is left out: +2 at 3 Ents)
					put("onod1", "17_26");        // Woodland Onod: Ent companion, strength 7, vitality 4; a Skirmish ability used only on purpose
					put("onod2", "17_26");
					put("guardian1", "5_17");     // Forest Guardian: Ent companion, strength 8; unhasty, an Assignment ability used only on purpose
					put("guardian2", "5_17");
					put("treebeard", "4_104");    // Treebeard, Oldest Living Thing: Ent ally, strength 12; a Fellowship ability only
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent

					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; adds (2) when played, never played here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RousedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Roused
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Skirmish: Exert an Ent to make him strength +1 for each Ent you spot (limit +4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("roused1");

		assertEquals("Roused", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnEntToMakeHimStrengthPlus1ForEachEntSpottedUntilTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roused = scn.GetFreepsCard("roused1");
		var onod = scn.GetFreepsCard("onod1");
		var guardian = scn.GetFreepsCard("guardian1");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(onod, guardian, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(roused);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{onod, troop}, new PhysicalCardImpl[]{gandalf, runner});
		scn.FreepsResolveSkirmish(onod);

		assertEquals(0, scn.GetWoundsOn(onod));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(7, scn.GetStrength(onod));
		assertEquals(8, scn.GetStrength(guardian));
		assertTrue(scn.FreepsPlayAvailable(roused));

		scn.FreepsPlayCard(roused);
		// Any Ent may exert, in the skirmish or not, a companion or an ally; Gandalf is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(onod, guardian, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// The Guardian, who is in no skirmish: "him" is the Ent who exerted, not the one in the skirmish.
		scn.FreepsChooseCard(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(onod));
		assertEquals(0, scn.GetWoundsOn(treebeard));

		// Three Ents to spot, Onod among them, and not Gandalf; the player may spot fewer.
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		assertInDiscard(roused);
		// 8 + 2 spotted; the other Ents are not made stronger.
		assertEquals(10, scn.GetStrength(guardian));
		assertEquals(7, scn.GetStrength(onod));
		assertEquals(12, scn.GetStrength(treebeard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Onod, 7 against 13, loses and lives with 1 wound of 4; the bonus is gone in the next skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(1, scn.GetWoundsOn(onod));
		assertEquals(8, scn.GetStrength(guardian));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlus4ForEachCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roused1 = scn.GetFreepsCard("roused1");
		var roused2 = scn.GetFreepsCard("roused2");
		var onod1 = scn.GetFreepsCard("onod1");
		var onod2 = scn.GetFreepsCard("onod2");
		var guardian1 = scn.GetFreepsCard("guardian1");
		var guardian2 = scn.GetFreepsCard("guardian2");
		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(onod1, onod2, guardian1, guardian2);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(roused1, roused2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(onod1, troop);
		scn.FreepsResolveSkirmish(onod1);

		assertEquals(0, scn.GetWoundsOn(onod1));
		assertEquals(7, scn.GetStrength(onod1));

		scn.FreepsPlayCard(roused1);
		scn.FreepsChooseCard(onod1);
		// All five Ents may be spotted: the limit caps the bonus, not the count.
		assertEquals(5, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("5");
		// 7 + 4, not + 5.
		assertEquals(11, scn.GetStrength(onod1));
		assertEquals(1, scn.GetWoundsOn(onod1));

		// The limit is that copy's: a second copy in the same skirmish gives its own bonus.
		scn.ShadowPass();
		scn.FreepsPlayCard(roused2);
		scn.FreepsChooseCard(onod1);
		scn.FreepsChoose("3");
		// 11 + 3
		assertEquals(14, scn.GetStrength(onod1));
		assertEquals(2, scn.GetWoundsOn(onod1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileEveryEntIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var roused = scn.GetFreepsCard("roused1");
		var onod = scn.GetFreepsCard("onod1");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(onod, gandalf);
		scn.MoveAlliesToTable(treebeard);
		scn.MoveCardsToHand(roused);
		// Both Ents of vitality 4 exhausted; Gandalf, no Ent, could exert.
		scn.AddWoundsToChar(onod, 3);
		scn.AddWoundsToChar(treebeard, 3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(onod));
		assertEquals(3, scn.GetWoundsOn(treebeard));
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(onod, troop);
		scn.FreepsResolveSkirmish(onod);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(roused));
	}
}
