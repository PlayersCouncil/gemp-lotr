package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_284_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_284");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOverwhelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "7_284");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, the companion overwhelmed
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion in no skirmish, to take the threat wound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Response:</b> If a companion is overwhelmed during a skirmish involving a [sauron] Orc and threat wounds are about to be placed, remove a threat to assign this minion to the Ring-bearer (even if the Ring-bearer is already assigned).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Mordor Assassin", card.getBlueprint().getTitle());
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
	public void ResponseRemovesAThreatToAssignTheAssassinToTheRingBearerBeforeThreatWoundsArePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var assassin = scn.GetShadowCard("assassin");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		// Three companions hold the 2 threats.
		scn.AddThreats(2);
		scn.MoveMinionsToTable(assassin, troop);
		scn.SkipToAssignments();
		// The Free Peoples player may give Gimli one minion; the Shadow player gives him the one left over.
		scn.FreepsAssignToMinions(gimli, assassin);
		scn.ShadowAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.IsCharAssigned(frodo));
		scn.PassSkirmishActions();

		// 9 + 13 against 6: Gimli is overwhelmed and killed, and the 2 threats are about to become wounds.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));

		scn.ShadowChooseOptionalTrigger(assassin);
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		// One threat was the cost, paid before the threat wounds (CR 4.2 on Mordor Assassin), so one wound is
		// left to place and not two: the Free Peoples player puts it on either companion still alive.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// No second wound is asked for.  The next thing is the Ring-bearer's skirmish against the Assassin.
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
