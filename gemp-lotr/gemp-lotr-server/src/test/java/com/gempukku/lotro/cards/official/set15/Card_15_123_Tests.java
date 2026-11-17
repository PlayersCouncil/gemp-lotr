package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "15_123");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, cost 2, valiant and nothing else
					put("guard2", "5_83");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, cost 1; a fellowship ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Éowyn) can be spotted
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; a skirmish ability used only on purpose

					put("shield", "15_141");    // Sturdy Shield: a [rohan] possession
					put("villagers", "4_273");  // Fight for the Villagers: a [rohan] skirmish event

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text (no hunter)
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Éomer, Horsemaster
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Valiant</b>. <b>Hunter 2</b>. <helper>(While skirmishing a non-hunter character, this character is strength +2.)</helper>
		 * 	While you can spot a [rohan] Man, Éomer's twilight cost is -1.
		 * 	<b>Skirmish:</b> Discard a [rohan] card from hand to make Éomer strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", card.getBlueprint().getTitle());
		assertEquals("Horsemaster", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(eomer, guard2);

		scn.StartGame();

		// Éomer's own cost only: the second Household Guard in hand reads his printed 2.
		assertEquals(2, scn.GetTwilightCost(eomer));
		assertEquals(2, scn.GetTwilightCost(guard2));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);
		// 3 printed - 1
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		// One [rohan] companion only, so Merry is no Man.
		scn.MoveCompanionsToTable(merry, boromir);
		scn.MoveCardsToHand(eomer, guma);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(guma);
		assertInZone(Zone.SUPPORT, guma);
		assertEquals(2, scn.GetTwilightCost(eomer));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(eomer);
		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsARohanCardFromHandToMakeEomerStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var shield = scn.GetFreepsCard("shield");
		var villagers = scn.GetFreepsCard("villagers");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveCardsToHand(guard2, shield, villagers, boromir);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		assertInHand(guard2, shield, villagers, boromir);
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eomer, troop },
				new PhysicalCardImpl[] { guard, troop2 }
		);
		scn.FreepsResolveSkirmish(eomer);

		// 7 + hunter 2 against a minion who is no hunter
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);

		// A [rohan] character, possession and event; not the [gondor] Boromir.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, shield, villagers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(shield);

		assertInDiscard(shield);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(guard2, villagers, boromir);
		assertEquals(11, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 11 against 13: he loses and takes a wound. In the next skirmish, which he is not in, he is back at 7
		// (9 if the +2 had lasted).
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(eomer));
		scn.FreepsResolveSkirmish(guard);
		assertEquals(7, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedWhileEomerIsNotAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveCardsToHand(shield);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(shield);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertFalse(scn.IsCharAssigned(eomer));
		assertEquals(7, scn.GetStrength(eomer));
		assertInHand(shield);
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);

		assertInDiscard(shield);
		assertEquals(9, scn.GetStrength(eomer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoRohanCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(boromir);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(boromir);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, troop);
		scn.FreepsResolveSkirmish(eomer);

		assertInHand(boromir);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eomer));
	}
}
