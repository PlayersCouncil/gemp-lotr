package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_257_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("skirmisher", "1_257");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a Hobbit companion, vitality 4; strength +2 only with a weapon
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a Hobbit minion; his maneuver ability needs a site he controls
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSkirmisherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Skirmisher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Maneuver:</b> Remove (3) to exert a Hobbit (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("skirmisher");

		assertEquals("Morgul Skirmisher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityRemovesThreeToExertAHobbitWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var ted = scn.GetShadowCard("ted");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, pippin, merry, aragorn);
		scn.MoveAlliesToTable(bounder);
		// Merry, vitality 4, is exhausted.
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(skirmisher));
		assertEquals(4, scn.GetTwilight());

		scn.ShadowUseCardAction(skirmisher);

		assertEquals(1, scn.GetTwilight());
		// A Ring-bound Hobbit, a [gondor] Hobbit, a Hobbit ally and the Shadow player's own Hobbit minion; not the
		// Ring-bearer, a companion who is no Hobbit, or a Hobbit who cannot exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, pippin, bounder, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, merry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoHobbitButTheRingBearerAbleToExertItIsStillOfferedAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// The ability has a cost, so it is offered though nobody it could exert is there.
		assertTrue(scn.ShadowActionAvailable(skirmisher));

		scn.ShadowUseCardAction(skirmisher);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skirmisher = scn.GetShadowCard("skirmisher");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// Sam could exert; the pool is one short of the cost.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(skirmisher));
	}
}
