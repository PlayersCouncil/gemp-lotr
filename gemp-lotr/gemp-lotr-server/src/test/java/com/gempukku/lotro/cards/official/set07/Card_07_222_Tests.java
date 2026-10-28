package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_222_Tests
{

	// The Towers path, Déor's own block (home 3T); the skirmishes are fought at its site 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deor", "7_222");

					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("guard2", "5_83");
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4; a regroup ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Eowyn) can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("brigand1", "4_10");    // Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text
					put("brigand2", "4_10");

					// Four cards of different kinds for the Free Peoples hand, which decides who has initiative and pays the cost.
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("valor", "1_103");      // Elendil's Valor: a Maneuver event
					put("chieftain", "1_266");  // Orc Chieftain: a Shadow card
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Déor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 3T
		 * Game Text: <b>Villager</b>.<br><b>Skirmish:</b> If you have initiative, discard a card from hand to make a [rohan] Man strength +1.
		*/

		var scn = GetScenario();

		var deor = scn.GetFreepsCard("deor");

		assertEquals("Déor", deor.getBlueprint().getTitle());
		assertNull(deor.getBlueprint().getSubtitle());
		assertTrue(deor.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, deor.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, deor.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, deor.getBlueprint().getCardType());
		assertEquals(Race.MAN, deor.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(deor, Keyword.VILLAGER));
		assertEquals(1, deor.getBlueprint().getTwilightCost());
		assertEquals(4, deor.getBlueprint().getStrength());
		assertEquals(2, deor.getBlueprint().getVitality());
		assertTrue(deor.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
	}

	@Test
	public void WithInitiativeSkirmishAbilityDiscardsACardFromHandToMakeARohanManStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deor = scn.GetFreepsCard("deor");
		var guard1 = scn.GetFreepsCard("guard1");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand1 = scn.GetShadowCard("brigand1");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var valor = scn.GetFreepsCard("valor");
		var chieftain = scn.GetFreepsCard("chieftain");
		scn.MoveCompanionsToTable(guard1, merry, aragorn);
		scn.MoveAlliesToTable(deor, herdsman);
		// Exactly four cards in the Free Peoples hand: he has initiative.
		scn.MoveCardsToHand(gimli, armor, valor, chieftain);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(gimli, armor, valor, chieftain);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, brigand1);
		scn.FreepsResolveSkirmish(guard1);

		assertTrue(scn.IsCharSkirmishingAgainst(guard1, brigand1));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(4, scn.GetStrength(deor));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsActionAvailable(deor));

		scn.FreepsUseCardAction(deor);
		// Any card from hand pays: a companion, a possession, an event and a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, armor, valor, chieftain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(valor);
		assertInDiscard(valor);
		assertEquals(3, scn.GetFreepsHandCount());

		// [rohan] Men in and out of the skirmish, a companion and two allies, Déor himself among them; not Merry,
		// a [rohan] Hobbit (two [rohan] companions only), Aragorn, a [gondor] Man, or the [dunland] Man he fights.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard1, herdsman, deor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn, brigand1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard1);

		assertEquals(7, scn.GetStrength(guard1));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(4, scn.GetStrength(deor));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Three cards left: the Shadow has initiative, and the ability is no longer offered, with a card to pay.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsActionAvailable(deor));
		assertEquals(7, scn.GetStrength(guard1));
	}

	@Test
	public void TheBonusLastsTheSkirmishPhaseItIsMadeIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deor = scn.GetFreepsCard("deor");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveAlliesToTable(deor);
		scn.MoveCardsToHand(gimli, scn.GetFreepsCard("armor"), scn.GetFreepsCard("valor"), scn.GetFreepsCard("chieftain"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, brigand1}, new PhysicalCardImpl[]{guard2, brigand2});
		scn.FreepsResolveSkirmish(guard1);

		// The second Guard, who waits for his own skirmish, is made strength +1 during the first's.
		assertEquals(6, scn.GetStrength(guard2));
		scn.FreepsUseCardAction(deor);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(guard2);
		assertEquals(7, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(guard1));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guard2));

		// 6 against 8: the first Guard loses and takes a wound.  In the next skirmish phase the bonus is gone.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(6, scn.GetStrength(guard2));
		scn.FreepsResolveSkirmish(guard2);
		assertTrue(scn.IsCharSkirmishingAgainst(guard2, brigand2));
		assertEquals(6, scn.GetStrength(guard2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
