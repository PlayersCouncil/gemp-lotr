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

public class Card_11_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "11_75");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion, strength 14, with no text but toil
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3; his abilities are used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("courageous", "15_75"); // Courageous Easterling: Skirmish: if skirmishing, discard it to play a [men] minion from the discard pile
					put("standard", "11_84");   // Harad Standard-bearer: a [men] Man minion, strength 10; its ability is used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; his ability is used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingHostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Easterling Host
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper>
		 * 	<b>Skirmish:</b> Exert Easterling Host and make it strength -2 until the regroup phase to make each other [men] minion strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Easterling Host", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityPaysAWoundAndTwoStrengthForPlusOneOnEachOtherMenMinionUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(host, legion, ted, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, legion}, new PhysicalCardImpl[]{gimli, troop});
		// The Host and Ted stay out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(host));
		assertEquals(13, scn.GetStrength(host));
		assertEquals(14, scn.GetStrength(legion));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowActionAvailable(host));

		scn.ShadowUseCardAction(host);
		assertEquals(1, scn.GetWoundsOn(host));
		assertEquals(11, scn.GetStrength(host));
		// Each other [men] minion, in this skirmish or in none, of either race; not the [moria] Orc.
		assertEquals(15, scn.GetStrength(legion));
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(13, scn.GetStrength(troop));

		// Legion 15 beats Aragorn 8.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next skirmish is a phase of its own: both still hold.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(host));
		assertEquals(15, scn.GetStrength(legion));
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(13, scn.GetStrength(troop));

		// The Troop, 13 against 6, overwhelms Gimli; then the regroup phase.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(13, scn.GetStrength(host));
		assertEquals(14, scn.GetStrength(legion));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedTheHostCannotUseTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var legion = scn.GetShadowCard("legion");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(host, legion);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(host, 2);
		assertEquals(2, scn.GetWoundsOn(host));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(host));
		assertEquals(14, scn.GetStrength(legion));
	}

	@Test
	public void AMenMinionThatComesIntoPlayAfterTheUseIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var legion = scn.GetShadowCard("legion");
		var courageous = scn.GetShadowCard("courageous");
		var standard = scn.GetShadowCard("standard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(host, legion, courageous);
		scn.MoveCardsToDiscard(standard);
		assertInDiscard(standard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, courageous);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// Enough for the Standard-bearer: 4, and 2 for roaming at site 2.
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(host);
		assertEquals(6, scn.GetStrength(courageous));
		assertEquals(15, scn.GetStrength(legion));
		scn.FreepsPass();

		// The Easterling, skirmishing, is discarded to play the Standard-bearer from the discard pile.
		scn.ShadowUseCardAction(courageous);
		assertInZone(Zone.SHADOW_CHARACTERS, standard);
		assertInDiscard(courageous);
		// Printed strength: the ability reached only the [men] minions on the table when it was used.
		assertEquals(10, scn.GetStrength(standard));
		assertEquals(15, scn.GetStrength(legion));
		assertEquals(11, scn.GetStrength(host));
		// Aragorn's skirmish has no minion left, and the Standard-bearer is fierce: the fierce assignments follow.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
