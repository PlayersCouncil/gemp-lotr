package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeking", "15_169");
					put("seeking2", "15_169");
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, hunter 6, no other text
					put("murderous", "11_195"); // Murderous Uruk: [uruk-hai] Uruk-hai, damage +1 and muster, no hunter

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, no hunter; a skirmish ability used only on purpose
					put("guardian", "17_7");    // Elven Guardian: [elven] Elf companion, hunter 1; a maneuver heal used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeekingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Seeking Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non- hunter character, this character is strength +1.)</helper><br>During a fierce skirmish involving this minion, it is strength +1 for each hunter you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seeking");

		assertEquals("Seeking Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void InHisFierceSkirmishHeIsStrengthPlus1ForEachHunterAndNotInHisRegularSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(aragorn, guardian);

		var seeking = scn.GetShadowCard("seeking");
		var butcher = scn.GetShadowCard("butcher");
		var murderous = scn.GetShadowCard("murderous");

		scn.StartGame();
		scn.MoveMinionsToTable(seeking, butcher, murderous);
		assertEquals(7, scn.GetStrength(seeking));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, seeking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// His regular skirmish: 7 + 1 for his own hunter 1 against Aragorn, who is no hunter, and no count.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, seeking));
		assertEquals(8, scn.GetStrength(seeking));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// 8 against 8: the tie goes to the Uruk.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(seeking));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, seeking);
		scn.FreepsResolveSkirmish(aragorn);

		// His fierce skirmish: 7 + 1 (hunter) + 3 for the hunters spotted: himself, the Butcher (a minion) and the
		// Elven Guardian (a companion); not the Murderous Uruk or Aragorn, who are no hunters.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, seeking));
		assertEquals(11, scn.GetStrength(seeking));
		assertEquals(5, scn.GetStrength(butcher));
		assertEquals(8, scn.GetStrength(murderous));
		scn.PassSkirmishActions();
		// 11 against 8: Aragorn loses again.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The fierce skirmish over, he is back to 7.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(seeking));
	}

	@Test
	public void OnlyHisOwnFierceSkirmishCountsAndEachCopyCountsTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var seeking = scn.GetShadowCard("seeking");
		var seeking2 = scn.GetShadowCard("seeking2");

		scn.StartGame();
		scn.MoveMinionsToTable(seeking, seeking2);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, seeking},
				new PhysicalCardImpl[]{gimli, seeking2});

		// The second copy's fierce skirmish: it is 7 + 1 (hunter, against Gimli) + 2 (the two copies are the
		// hunters spotted); the first copy, in a fierce skirmish still to come, keeps its 7.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, seeking2));
		assertEquals(10, scn.GetStrength(seeking2));
		assertEquals(7, scn.GetStrength(seeking));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		// 10 against 6.
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Its own: the first copy is 10, and the second, whose fierce skirmish is over, 7.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, seeking));
		assertEquals(10, scn.GetStrength(seeking));
		assertEquals(7, scn.GetStrength(seeking2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
