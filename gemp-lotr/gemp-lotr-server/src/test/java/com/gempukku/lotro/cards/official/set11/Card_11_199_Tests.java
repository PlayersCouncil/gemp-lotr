package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_199_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("relentless", "11_199");
					put("relentless2", "11_199");
					put("bred", "1_121");       // Bred for Battle: [isengard] Shadow skirmish event, "Exert an Uruk-hai to make it strength +3"

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4;
					                            // his maneuver ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6, vitality 3; his skirmish
					                            // ability is used only on purpose
					put("gamling", "5_82");     // Gamling, Warrior of Rohan: [rohan] Man companion, strength 6; "Skirmish: Play a
					                            // [rohan] possession on Gamling to make him strength +2 and damage +1"
					put("spear", "4_288");      // Rider's Spear: [rohan] hand weapon possession, cost 1, on a [rohan] Man
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] skirmish event, a [gondor] companion +2
					put("noble", "1_304");      // Noble Intentions: [shire] skirmish event, exert a companion (not a Hobbit)
					                            // to make a Hobbit strength +3
					put("axestrike", "1_3");    // Axe Strike: [dwarven] skirmish event, a Dwarf strength +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RelentlessUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Relentless Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time a Free Peoples event is played during a skirmish involving this minion, this minion is strength -2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("relentless");

		assertEquals("Relentless Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachFreePeoplesEventPlayedInItsSkirmishMakesItStrengthMinus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var relentless2 = scn.GetShadowCard("relentless2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var swordarm = scn.GetFreepsCard("swordarm");
		var noble = scn.GetFreepsCard("noble");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(swordarm, noble, axestrike);

		scn.StartGame();
		scn.MoveMinionsToTable(relentless, relentless2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, relentless},
				new PhysicalCardImpl[]{gimli, relentless2});
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(14, scn.GetStrength(relentless));
		assertEquals(14, scn.GetStrength(relentless2));
		assertEquals(6, scn.GetStrength(gimli));

		// A [dwarven] event in Gimli's skirmish: the copy he fights is -2; this one, assigned to Aragorn and
		// waiting, is in no skirmish yet and keeps its strength.
		scn.FreepsPlayCard(axestrike);
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(12, scn.GetStrength(relentless2));
		assertEquals(14, scn.GetStrength(relentless));

		// 12 against 8: Gimli loses and takes the two wounds of damage +1, of his vitality 3.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(aragorn);

		// The copy's -2 lasts into this later skirmish.
		assertEquals(12, scn.GetStrength(relentless2));
		assertEquals(14, scn.GetStrength(relentless));

		// A [gondor] event: -2.
		scn.FreepsPlayCard(swordarm);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(12, scn.GetStrength(relentless));
		assertEquals(12, scn.GetStrength(relentless2));

		// A second event in the same skirmish, a [shire] one: -2 again.  Aragorn, the one companion who can,
		// exerts for it, and Frodo is the Hobbit made +3.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPass();
		scn.FreepsPlayCard(noble);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(10, scn.GetStrength(relentless));
		assertEquals(12, scn.GetStrength(relentless2));

		// 10 against 10: Aragorn loses, and takes two wounds (3 of his vitality 4).
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// At the regroup phase both are as printed again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(14, scn.GetStrength(relentless));
		assertEquals(14, scn.GetStrength(relentless2));
	}

	@Test
	public void AShadowEventAndAFreePeoplesCardThatIsNoEventPlayedInItsSkirmishDoNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var bred = scn.GetShadowCard("bred");
		var gamling = scn.GetFreepsCard("gamling");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(gamling);
		scn.MoveCardsToHand(spear);
		scn.MoveCardsToHand(bred);
		assertInHand(spear);
		assertInHand(bred);

		scn.StartGame();
		scn.MoveMinionsToTable(relentless);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gamling, relentless);
		scn.FreepsResolveSkirmish(gamling);

		assertEquals(14, scn.GetStrength(relentless));
		assertEquals(0, scn.GetWoundsOn(relentless));
		assertEquals(6, scn.GetStrength(gamling));

		// A Free Peoples possession played in the skirmish, by Gamling's ability: no event.
		scn.FreepsUseCardAction(gamling);
		assertAttachedTo(spear, gamling);
		// 6 + 2 for the spear + 2 for his ability.
		assertEquals(10, scn.GetStrength(gamling));
		assertEquals(14, scn.GetStrength(relentless));

		// A Shadow event played in the skirmish: its own +3 and nothing more.
		scn.ShadowPlayCard(bred);
		assertEquals(1, scn.GetWoundsOn(relentless));
		assertEquals(17, scn.GetStrength(relentless));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
