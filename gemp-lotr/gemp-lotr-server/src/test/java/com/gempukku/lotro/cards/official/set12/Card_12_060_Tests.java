package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "12_60");
					put("hillman2", "12_60");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, resistance 6; his maneuver ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, strength 3, resistance 5; his abilities are used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (resistance 0); Maneuver: exert him to take part in skirmishes until the regroup phase

					// Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits being
					// discarded or killed, and none is
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrazedHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Crazed Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Fierce</b>.<br>While this minion is in a fierce skirmish, it is strength +7 (or +10 if it is skirmishing a companion who has resistance 4 or less).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hillman");

		assertEquals("Crazed Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus7InHisOwnFierceSkirmishAndPlus10OnceHisCompanionIsAtResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, pursuer);
		scn.AddBurdens(1);
		// One burden: Sam 5 - 1 = 4, Aragorn 6 - 1 = 5, Frodo 10 - 1 = 9.
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(7, scn.GetStrength(hillman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, hillman}, new PhysicalCardImpl[]{sam, pursuer});
		scn.FreepsResolveSkirmish(frodo);

		// A normal skirmish: no bonus.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(hillman, frodo));
		assertEquals(7, scn.GetStrength(hillman));

		// The Hillman wins, 7 to 4 (Frodo 3 + 1 for The Ruling Ring), and the Pursuer beats Sam, 5 to 3: two
		// wounds, neither a kill, and both minions live on.
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hillman}, new PhysicalCardImpl[]{sam, pursuer});
		scn.FreepsResolveSkirmish(sam);

		// The Pursuer's fierce skirmish: the Hillman waits for his own and has no bonus, and the Pursuer has none.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer, sam));
		assertFalse(scn.IsCharSkirmishing(hillman));
		assertEquals(7, scn.GetStrength(hillman));
		assertEquals(5, scn.GetStrength(pursuer));

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);

		// His own fierce skirmish, against Aragorn at resistance 5; Sam, at 4, is in no skirmish of his.
		assertTrue(scn.IsCharSkirmishingAgainst(hillman, aragorn));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(14, scn.GetStrength(hillman));

		scn.AddBurdens(1);
		assertEquals(4, scn.GetResistance(aragorn));
		// 7 + 10
		assertEquals(17, scn.GetStrength(hillman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlusTenAgainstTheRingBearerAtResistance4ButOnlyPlusSevenAgainstAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var hillman2 = scn.GetShadowCard("hillman2");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(hillman, hillman2);
		scn.AddBurdens(6);
		// Six burdens: Frodo 10 - 6 = 4; an ally has resistance 0.
		assertEquals(6, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// Nobody is assigned in the first round; both Hillmen go on to the fierce round.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, hillman}, new PhysicalCardImpl[]{thrarin, hillman2});
		scn.FreepsResolveSkirmish(thrarin);

		// Against an ally of resistance 0, who is no companion: +7 only. The other Hillman waits for his own.
		assertTrue(scn.IsCharSkirmishingAgainst(hillman2, thrarin));
		assertEquals(14, scn.GetStrength(hillman2));
		assertEquals(7, scn.GetStrength(hillman));

		// Thrarin is overwhelmed, 4 to 14.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(hillman, frodo));
		// 7 + 10 against the Ring-bearer at resistance 4
		assertEquals(17, scn.GetStrength(hillman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
