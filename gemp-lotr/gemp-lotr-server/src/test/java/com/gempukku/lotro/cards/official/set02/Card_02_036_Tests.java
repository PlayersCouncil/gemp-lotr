package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "2_36");
					put("ranger2", "2_36");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], Gandalf signet; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Gandalf signet; a skirmish ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: Aragorn signet; not overwhelmed unless his strength is tripled
					put("barliman", "1_70");    // Barliman Butterbur: an ally; a fellowship ability used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanAragornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "2_36");

					// Aragorn, Defender of Rohan: the Aragorn of another culture, no signet; his text needs the
					// Ring-bearer assigned, and he is not
					put("aragorn", "17_93");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire], Aragorn signet; +2 only while he bears a weapon

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoMereRangerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: No Mere Ranger
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert Aragorn to make another companion strength +2 (or +3 if that companion has the Aragorn signet).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger1");

		assertEquals("No Mere Ranger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAragornToMakeAnotherCompanionPlusTwoOrPlusThreeWithTheAragornSignetForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(ranger1, ranger2);
		assertInHand(ranger1, ranger2);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{boromir, troop2});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertInHand(ranger1, ranger2);
		assertTrue(scn.FreepsPlayAvailable(ranger1));

		scn.FreepsPlayCard(ranger1);
		// Aragorn, the only Aragorn, exerts unasked. Any companion but him, in this skirmish or not; no ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, boromir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(ranger1);
		// Gimli has the Gandalf signet: +2, though Boromir beside him has the Aragorn signet.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		// Boromir, who has the Aragorn signet, is assigned to the skirmish after this one.
		scn.FreepsPlayCard(ranger2);
		scn.FreepsChooseCard(boromir);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(ranger2);
		assertEquals(10, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(gimli));

		// Gimli loses, 8 to 13, and his skirmish is over: so is what the events gave.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AragornDefenderOfRohanPaysTheExertionAndIsNotTheAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanAragornScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToHand(ranger);
		assertInHand(ranger);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertInHand(ranger);
		assertTrue(scn.FreepsPlayAvailable(ranger));

		scn.FreepsPlayCard(ranger);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(ranger);
		// A [shire] Hobbit with the Aragorn signet: 3 + 3
		assertEquals(6, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileAragornIsExhaustedThoughAnotherGondorCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveCardsToHand(ranger1);
		assertInHand(ranger1);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop1);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(ranger1);
		assertFalse(scn.FreepsPlayAvailable(ranger1));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
	}
}
