package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("plodder", "6_69");
					// Isengard Worker: an [isengard] Orc of another title. Its regroup ability exerts it, and is
					// never used
					put("worker", "3_62");
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is an Uruk-hai, no other text
					put("runner", "1_178");     // Goblin Runner: an Orc of [moria]; its play trigger is not set off by a cheat

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardPlodderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Isengard Plodder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Discard an [isengard] Orc to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var plodder = scn.GetFreepsCard("plodder");

		assertEquals("Isengard Plodder", plodder.getBlueprint().getTitle());
		assertNull(plodder.getBlueprint().getSubtitle());
		assertFalse(plodder.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, plodder.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, plodder.getBlueprint().getCulture());
		assertEquals(CardType.MINION, plodder.getBlueprint().getCardType());
		assertEquals(Race.ORC, plodder.getBlueprint().getRace());
		assertEquals(3, plodder.getBlueprint().getTwilightCost());
		assertEquals(7, plodder.getBlueprint().getStrength());
		assertEquals(3, plodder.getBlueprint().getVitality());
		assertEquals(4, plodder.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsAnIsengardOrcToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		// Gimli, vitality 3, is exhausted: a wound kills him.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		var plodder = scn.GetShadowCard("plodder");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(plodder, worker, savage, runner);
		// Nobody is assigned to them, so all four are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, plodder, worker, savage, runner);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(plodder));
		scn.ShadowUseCardAction(plodder);

		// Any [isengard] Orc pays, the Plodder itself among them; not the [isengard] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(plodder, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);

		assertInZone(Zone.DISCARD, worker);
		assertInZone(Zone.SHADOW_CHARACTERS, plodder, savage, runner);

		// The Free Peoples player chooses the companion: any companion, exhausted or not, and no ally or minion.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, plodder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// A wound and not an exertion: the exhausted Gimli is killed.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Used again with no other [isengard] Orc in play: the Plodder pays with itself.
		assertTrue(scn.ShadowActionAvailable(plodder));
		scn.ShadowUseCardAction(plodder);
		assertInZone(Zone.DISCARD, plodder);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var plodder = scn.GetShadowCard("plodder");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.MoveMinionsToTable(plodder, worker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, plodder, worker);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(plodder));
	}
}
