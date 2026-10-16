package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slayer1", "3_97");
					put("slayer2", "3_97");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability, never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound and not the Ring-bearer; a fellowship ability and a response to Frodo's death
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability, never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSlayerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Slayer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Regroup:</b> Exert this minion to wound a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slayer1");

		assertEquals("Orc Slayer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsHimToWoundACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(thrarin);
		// Vitality 3: two wounds exhaust him, and a third kills him.
		scn.AddWoundsToChar(gimli, 2);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);

		assertEquals(2, scn.GetWoundsOn(gimli));

		// A Regroup ability: not offered in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer1));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.ShadowActionAvailable(slayer1));

		scn.ShadowUseCardAction(slayer1);
		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(slayer2));

		// Companions of three cultures, a Ring-bound one and an exhausted one among them; not the Ring-bearer, nor
		// an ally, nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, thrarin, slayer1, slayer2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// One wound, on the one chosen.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The other copy's own ability, on the exhausted companion: a wound, which kills him.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		assertEquals(1, scn.GetWoundsOn(slayer2));
		scn.ShadowChooseCard(gimli);
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedHeCannotUseTheAbilityWhileAnotherCopyCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer1, slayer2);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(slayer1, 1);

		assertEquals(1, scn.GetWoundsOn(slayer1));
		assertEquals(0, scn.GetWoundsOn(slayer2));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer1));
		assertTrue(scn.ShadowActionAvailable(slayer2));
	}

	@Test
	public void WithOnlyTheRingBearerAmongTheCompanionsTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var slayer = scn.GetShadowCard("slayer1");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.ShadowActionAvailable(slayer));

		scn.ShadowUseCardAction(slayer);

		// The exertion is paid, and nobody is wounded.
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
