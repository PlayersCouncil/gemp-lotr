package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("level", "7_92");
					put("level2", "7_92");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, no knight, vitality 4; a maneuver ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man knight, vitality 3, no other text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] knight who is a Hobbit, no Man; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man, valiant, no other text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; an ability that needs initiative
					put("narsil", "9_34");      // Narsil: vitality +1, so the Merchant can exert; a response to its bearer's wound only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FirstLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: First Level
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Strength: -1
		 * Game Text: <b>Fortification</b>.<br><b>Maneuver:</b> Exert a [gondor] Man or spot a knight to transfer this condition from your support area to a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("level");

		assertEquals("First Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	@Test
	public void ExertingAGondorManTransfersItFromTheSupportAreaToAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var level = scn.GetFreepsCard("level");
		var level2 = scn.GetFreepsCard("level2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(level, level2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, level, level2);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(level));

		scn.FreepsUseCardAction(level);
		// Both halves can be paid (Pippin is a knight), so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("knight"));
		scn.FreepsChooseOption("Exert");

		// Who exerts: a [gondor] Man, companion or ally; not Pippin ([gondor], no Man), the Household Guard
		// (a Man, [rohan]) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Where it goes: either minion; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// This copy went; the other is still in the support area.
		assertAttachedTo(level, troop);
		assertInZone(Zone.SUPPORT, level2);
		assertTrue(scn.GetAttachedCards(brigand).isEmpty());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingAKnightWhoIsNoManTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var level = scn.GetFreepsCard("level");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveCardsToSupportArea(level);

		scn.StartGame();
		// Vitality 4 with 3 wounds: the one [gondor] Man cannot exert, so only the spot can be paid.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, level);
		assertTrue(scn.FreepsActionAvailable(level));

		scn.FreepsUseCardAction(level);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		scn.FreepsChooseCard(brigand);

		assertAttachedTo(level, brigand);
		assertTrue(scn.GetAttachedCards(troop).isEmpty());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SpottingAnExhaustedGondorManKnightTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var level = scn.GetFreepsCard("level");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(level);

		scn.StartGame();
		// Vitality 3 with 2 wounds: he cannot exert, but he is still a knight to spot.
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, level);
		assertTrue(scn.FreepsActionAvailable(level));

		// The one minion takes it, unasked.
		scn.FreepsUseCardAction(level);
		assertAttachedTo(level, troop);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGondorManAbleToExertAndNoKnight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var level = scn.GetFreepsCard("level");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(level);

		scn.StartGame();
		// Aragorn exhausted; the Household Guard could exert, but is a [rohan] Man; neither is a knight.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInZone(Zone.SUPPORT, level);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(level));
	}

	@Test
	public void ACopyBorneByAMinionIsNotOfferedWhileOneInTheSupportAreaIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var level = scn.GetFreepsCard("level");
		var level2 = scn.GetFreepsCard("level2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(level2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, brigand);
		scn.AttachCardsTo(troop, level);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn can exert; "from your support area" is what keeps the borne copy from moving on.
		assertAttachedTo(level, troop);
		assertInZone(Zone.SUPPORT, level2);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(level));
		assertTrue(scn.FreepsActionAvailable(level2));
	}
}
