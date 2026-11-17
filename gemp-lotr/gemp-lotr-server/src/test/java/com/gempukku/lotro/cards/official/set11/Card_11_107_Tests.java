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

public class Card_11_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orc", "11_107");
					put("orc2", "11_107");

					// Half-troll of Far Harad: an [orc] minion who is no Orc, vitality 2.  Its trigger at the start of
					// the assignment phase needs more companions than minions, never the case here.
					put("halftroll", "14_12");
					// Denizen of Khazad-dum: an [orc] Orc, strength 9, vitality 2.  Strength +2 only at an underground
					// site, and none is reached.
					put("denizen", "11_115");
					// Goblin Patrol Troop: a [moria] Orc, vitality 3, no text.
					put("troop", "1_177");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, resistance 6.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, resistance 6.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a Dwarf ally (resistance 0, as every ally has).  "Maneuver: Exert Thrarin to
					// allow him to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarbarousOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Barbarous Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert another [orc] minion to make this minion strength +2 (or +4 if this minion is skirmishing a companion who has resistance 4 or less).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orc");

		assertEquals("Barbarous Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertsAnotherOrcMinionToMakeItselfStrengthPlus2AgainstACompanionOfResistance5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		var halftroll = scn.GetShadowCard("halftroll");
		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(orc, orc2, halftroll, denizen, troop);
		// 6 less 1 burden.
		assertEquals(5, scn.GetResistance(aragorn));

		scn.SkipToAssignments();
		// Both Barbarous Orcs on Aragorn, the second put there by the Shadow player; the rest left out.
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.ShadowAssignToMinions(aragorn, orc2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(orc));
		assertEquals(7, scn.GetStrength(orc2));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(orc2));
		assertTrue(scn.ShadowActionAvailable(orc));
		scn.ShadowUseCardAction(orc);

		// Another [orc] minion: the other copy, an Orc and a Half-troll; not this minion, nor a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(orc2, halftroll, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orc, troop));
		scn.ShadowChooseCard(halftroll);

		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(orc));
		assertEquals(9, scn.GetStrength(orc));
		assertEquals(7, scn.GetStrength(orc2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlus4AgainstACompanionOfResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var orc = scn.GetShadowCard("orc");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(orc, denizen);
		// 6 less 2 burdens.
		assertEquals(4, scn.GetResistance(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(orc));
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.ShadowUseCardAction(orc);

		// The Denizen is the one other [orc] minion, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(11, scn.GetStrength(orc));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlus2OnlyAgainstAnAllyWhoHasNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var orc = scn.GetShadowCard("orc");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(orc, denizen);
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(thrarin));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(orc));
		scn.ShadowUseCardAction(orc);

		assertEquals(1, scn.GetWoundsOn(denizen));
		// Thrarin is a character of resistance 0, but no companion.
		assertEquals(9, scn.GetStrength(orc));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherSkirmishItIsPlus2AndTheBonusIsGoneInItsOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var orc = scn.GetShadowCard("orc");
		var halftroll = scn.GetShadowCard("halftroll");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(orc, halftroll, denizen);
		assertEquals(4, scn.GetResistance(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, denizen},
				new PhysicalCardImpl[]{gimli, orc});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(orc));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		scn.ShadowUseCardAction(orc);
		scn.ShadowChooseCard(halftroll);

		assertEquals(1, scn.GetWoundsOn(halftroll));
		// Assigned to Gimli, of resistance 4, but not yet skirmishing him.
		assertEquals(9, scn.GetStrength(orc));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);

		// A skirmish is a phase of its own: the bonus is gone.
		assertEquals(7, scn.GetStrength(orc));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyOtherOrcMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var orc = scn.GetShadowCard("orc");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, halftroll, troop);
		scn.AddWoundsToChar(halftroll, 1);
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Half-troll is exhausted, and the Goblin Patrol Troop, who could exert, is [moria].
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(orc));
		assertEquals(7, scn.GetStrength(orc));
	}
}
