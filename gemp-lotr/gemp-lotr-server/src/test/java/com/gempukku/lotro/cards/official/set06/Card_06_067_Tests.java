package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("journeyman", "6_67");
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
	public void IsengardJourneymanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Isengard Journeyman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Discard an [isengard] Orc to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var journeyman = scn.GetFreepsCard("journeyman");

		assertEquals("Isengard Journeyman", journeyman.getBlueprint().getTitle());
		assertNull(journeyman.getBlueprint().getSubtitle());
		assertFalse(journeyman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, journeyman.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, journeyman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, journeyman.getBlueprint().getCardType());
		assertEquals(Race.ORC, journeyman.getBlueprint().getRace());
		assertEquals(2, journeyman.getBlueprint().getTwilightCost());
		assertEquals(5, journeyman.getBlueprint().getStrength());
		assertEquals(3, journeyman.getBlueprint().getVitality());
		assertEquals(4, journeyman.getBlueprint().getSiteNumber());
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

		var journeyman = scn.GetShadowCard("journeyman");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(journeyman, worker, savage, runner);
		// Nobody is assigned to them, so all four are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, journeyman, worker, savage, runner);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(journeyman));
		scn.ShadowUseCardAction(journeyman);

		// Any [isengard] Orc pays, the Journeyman itself among them; not the [isengard] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(journeyman, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);

		assertInZone(Zone.DISCARD, worker);
		assertInZone(Zone.SHADOW_CHARACTERS, journeyman, savage, runner);

		// The Free Peoples player chooses the companion: any companion, exhausted or not, and no ally or minion.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, journeyman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// A wound and not an exertion: the exhausted Gimli is killed.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Used again with no other [isengard] Orc in play: the Journeyman pays with itself.
		assertTrue(scn.ShadowActionAvailable(journeyman));
		scn.ShadowUseCardAction(journeyman);
		assertInZone(Zone.DISCARD, journeyman);
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

		var journeyman = scn.GetShadowCard("journeyman");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.MoveMinionsToTable(journeyman, worker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, journeyman, worker);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(journeyman));
	}
}
