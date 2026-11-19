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

public class Card_06_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("udun", "6_106");
					put("udun2", "6_106");
					// Morgul Hunter: [sauron] Orc, cost 5, strength 9 +1 for each companion you can spot
					put("hunter", "1_256");
					put("hunter2", "1_256");
					// Troll of Cirith Gorgor: [sauron] Troll, cost 7, site 6; to play, spot a [sauron] minion; a strength
					// bonus for possessions only (none is in play)
					put("cirith", "10_101");
					// Beastly Olog-hai: [orc] Troll; its ability is an assignment ability, used only on purpose
					put("olog", "11_108");

					// The [sauron] cards for the skirmish ability's cost: a minion, a possession and a condition
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] hand weapon
					put("few", "1_260");        // The Number Must Be Few: [sauron] support-area condition
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, damage +1; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; a play trigger only, and it is cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollofUdunStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Troll of Udûn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 14
		 * Vitality: 5
		 * Site Number: 6
		 * Game Text: <b>Fierce</b>.<br>While you can spot a Troll, this minion's twilight cost is -4.<br><b>Skirmish:</b> Discard a [sauron] card from hand to make this minion strength +1.
		*/

		var scn = GetScenario();

		var udun = scn.GetFreepsCard("udun");

		assertEquals("Troll of Udûn", udun.getBlueprint().getTitle());
		assertNull(udun.getBlueprint().getSubtitle());
		assertFalse(udun.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, udun.getBlueprint().getSide());
		assertEquals(Culture.SAURON, udun.getBlueprint().getCulture());
		assertEquals(CardType.MINION, udun.getBlueprint().getCardType());
		assertEquals(Race.TROLL, udun.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(udun, Keyword.FIERCE));
		assertEquals(10, udun.getBlueprint().getTwilightCost());
		assertEquals(14, udun.getBlueprint().getStrength());
		assertEquals(5, udun.getBlueprint().getVitality());
		assertEquals(6, udun.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwilightCostIsFourLessOnceATrollIsPlayedAndNeitherAnOrcNorATrollInHandLowersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var udun = scn.GetShadowCard("udun");
		var hunter = scn.GetShadowCard("hunter");
		var hunter2 = scn.GetShadowCard("hunter2");
		var cirith = scn.GetShadowCard("cirith");
		scn.MoveCardsToHand(udun, hunter2, cirith);
		assertInHand(udun, hunter2, cirith);

		scn.StartGame();
		scn.MoveMinionsToTable(hunter);
		// 7 + 2 roaming for Troll of Cirith Gorgor at site 2, and 10 - 4 + 2 roaming for this minion after it: 17, of
		// which the move to Ettenmoors adds 3 (its Shadow number 2, and 1 for Frodo).
		scn.SetTwilight(14);

		scn.FreepsPass();
		assertEquals(17, scn.GetTwilight());

		// A [sauron] Orc in play and a Troll in hand, no Troll in play: the printed 10; the others in hand at their own.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(10, scn.GetTwilightCost(udun));
		assertEquals(5, scn.GetTwilightCost(hunter2));
		assertEquals(7, scn.GetTwilightCost(cirith));

		scn.ShadowPlayCard(cirith);
		assertInZone(Zone.SHADOW_CHARACTERS, cirith);
		assertEquals(8, scn.GetTwilight());

		// A Troll in play: 10 - 4; the other minion in hand is as it was.
		assertEquals(6, scn.GetTwilightCost(udun));
		assertEquals(5, scn.GetTwilightCost(hunter2));
		assertTrue(scn.ShadowPlayAvailable(udun));

		scn.ShadowPlayCard(udun);
		// 8 - (10 - 4 + 2 roaming)
		assertInZone(Zone.SHADOW_CHARACTERS, udun);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcTrollAloneMakesItsTwilightCostFourLessAndNoOtherTrollsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var udun = scn.GetShadowCard("udun");
		var hunter2 = scn.GetShadowCard("hunter2");
		var cirith = scn.GetShadowCard("cirith");
		var olog = scn.GetShadowCard("olog");
		scn.MoveCardsToHand(udun, hunter2, cirith);
		assertInHand(udun, hunter2, cirith);

		scn.StartGame();
		assertEquals(10, scn.GetTwilightCost(udun));

		scn.MoveMinionsToTable(olog);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(6, scn.GetTwilightCost(udun));
		// Another minion and another Troll in hand keep their own costs.
		assertEquals(5, scn.GetTwilightCost(hunter2));
		assertEquals(7, scn.GetTwilightCost(cirith));
	}

	@Test
	public void SkirmishAbilityDiscardsASauronCardFromHandToMakeItStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var udun = scn.GetShadowCard("udun");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var few = scn.GetShadowCard("few");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(soldier, scimitar, few, troop);
		assertInHand(soldier, scimitar, few, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(udun, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, udun}, new PhysicalCardImpl[]{gimli, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(14, scn.GetStrength(udun));
		assertEquals(4, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(udun));

		scn.ShadowUseCardAction(udun);

		// A [sauron] minion, possession and condition are offered; a [moria] card is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, scimitar, few));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(scimitar);

		assertInDiscard(scimitar);
		assertInHand(soldier, few, troop);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(15, scn.GetStrength(udun));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 15 against Aragorn's 8: he loses and takes a wound, short of double.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next skirmish is a phase of its own: the bonus is gone.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(14, scn.GetStrength(udun));

		// Out of his own skirmish he may still use it: the text names no skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(udun));
	}

	@Test
	public void OnlyTheCopyThatUsesItIsStrengthenedThoughBothAreInOneSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var udun = scn.GetShadowCard("udun");
		var udun2 = scn.GetShadowCard("udun2");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(soldier);
		assertInHand(soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(udun, udun2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, udun);
		scn.ShadowAssignToMinions(aragorn, udun2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(14, scn.GetStrength(udun));
		assertEquals(14, scn.GetStrength(udun2));

		// The one [sauron] card in hand is discarded unasked.
		scn.ShadowUseCardAction(udun);

		assertInDiscard(soldier);
		assertEquals(15, scn.GetStrength(udun));
		assertEquals(14, scn.GetStrength(udun2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoSauronCardInHandTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var udun = scn.GetShadowCard("udun");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(troop);
		assertInHand(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(udun);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, udun);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Only a [moria] card in hand: nothing can pay.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(udun));
	}
}
