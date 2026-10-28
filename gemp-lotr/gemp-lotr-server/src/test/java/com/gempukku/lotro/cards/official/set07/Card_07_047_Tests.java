package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sharpen", "7_47");
					put("sharpen2", "7_47");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion, a strength bonus and no ability
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf; strength +2 only with 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("oathbreaker", "8_41"); // Oathbreaker: [gondor] Wraith companion, strength 6, enduring (unwounded here); a skirmish ability used only on purpose
					put("ranger", "4_122");     // Gondorian Ranger: [gondor] Man companion; site number of minions on a Ring-bound Man +2, which nothing reads
					put("knight", "5_36");      // Knight of Gondor: [gondor] Man companion; a fellowship ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; a "when you play" add (2), harmless cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SharpenYourSwordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sharpen Your Swords
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot Gandalf to make a [gondor] companion strength +1 for each [gondor] companion you spot (limit +4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sharpen");

		assertEquals("Sharpen Your Swords", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharpen = scn.GetFreepsCard("sharpen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A [gandalf] companion who is not Gandalf, and a [gondor] companion to make strong.
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(sharpen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(sharpen);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertInHand(sharpen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sharpen));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sharpen));
	}

	@Test
	public void MakesAGondorCompanionStrongerByTheNumberOfGondorCompanionsNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharpen = scn.GetFreepsCard("sharpen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn, pippin, oathbreaker);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sharpen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(oathbreaker));
		assertTrue(scn.FreepsPlayAvailable(sharpen));
		scn.FreepsPlayCard(sharpen);

		// The [gondor] companions are Aragorn, Pippin and Oathbreaker: none, or up to those 3.  Not the [gondor]
		// ally, nor Gandalf or Frodo.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		// The number comes filled in with all of them (F-0561), and fewer may be named.
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");
		assertEquals(3, scn.GetStrength(pippin));

		// A [gondor] companion, in this skirmish or in none, of any race; not the [gondor] ally or a companion of
		// another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin, oathbreaker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		// Two named of three: 3 + 2, not + 3.
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(oathbreaker));
		assertInDiscard(sharpen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusStopsAtFourAndASecondCopyGivesItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharpen = scn.GetFreepsCard("sharpen");
		var sharpen2 = scn.GetFreepsCard("sharpen2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(gandalf, aragorn, pippin, oathbreaker, ranger, knight);
		scn.MoveCardsToHand(sharpen, sharpen2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(sharpen);

		// Five [gondor] companions: the count itself is not capped, and comes filled in with all five.
		assertEquals(5, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(5), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("5");
		scn.FreepsChooseCard(aragorn);
		// Five named, limit +4: 8 + 4.
		assertEquals(12, scn.GetStrength(aragorn));

		scn.ShadowPass();
		scn.FreepsPlayCard(sharpen2);
		scn.FreepsChoose("5");
		scn.FreepsChooseCard(aragorn);
		// A limit does not apply to a different copy (CR 4.2 "limit"): 12 + 4.
		assertEquals(16, scn.GetStrength(aragorn));
		assertInDiscard(sharpen, sharpen2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsToTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharpen = scn.GetFreepsCard("sharpen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		scn.MoveCompanionsToTable(gandalf, aragorn, oathbreaker);
		scn.MoveCardsToHand(sharpen);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{oathbreaker, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(sharpen);
		// Aragorn and Oathbreaker: none or up to 2.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		scn.FreepsChooseCard(aragorn);

		// 8 + 1, still there at the next decision of the same skirmish.
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();

		// Aragorn won (9 to 5), and the Runner, of vitality 1, is gone.
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(oathbreaker);

		// The next skirmish phase: the +1 is gone.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharpen = scn.GetFreepsCard("sharpen");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(gandalf, avenger);
		// The [gondor] ally, who is no companion.
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sharpen);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, troop);
		scn.FreepsResolveSkirmish(avenger);

		// Gandalf: 6, +1 for the Hobbit companion Frodo.
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(6, scn.GetStrength(merchant));
		// Gandalf is spotted, so the event is playable; its target is no requirement.
		assertTrue(scn.FreepsPlayAvailable(sharpen));
		scn.FreepsPlayCard(sharpen);

		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(6, scn.GetStrength(merchant));
		assertInDiscard(sharpen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
