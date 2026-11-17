package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_262_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aldor", "4_262");
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, a villager; a regroup ability only
					put("deor", "7_222");       // Déor: [rohan] Man ally, a villager of another title; a skirmish ability only
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man companion who is no villager; a regroup ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AldorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Aldor, Soldier of Edoras
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 2
		 * Site Number: 3T & 6T
		 * Game Text: Archery: Exert Aldor and spot a villager to make the fellowship archery total +1.
		*/

		var scn = GetScenario();

		var aldor = scn.GetFreepsCard("aldor");

		assertEquals("Aldor", aldor.getBlueprint().getTitle());
		assertEquals("Soldier of Edoras", aldor.getBlueprint().getSubtitle());
		assertTrue(aldor.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, aldor.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, aldor.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, aldor.getBlueprint().getCardType());
		assertEquals(Race.MAN, aldor.getBlueprint().getRace());
		assertEquals(1, aldor.getBlueprint().getTwilightCost());
		assertEquals(3, aldor.getBlueprint().getStrength());
		assertEquals(2, aldor.getBlueprint().getVitality());
		assertTrue(aldor.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
		assertTrue(aldor.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 6)));
	}

	@Test
	public void ArcheryAbilityExertsAldorToMakeTheFellowshipArcheryTotalPlus1ForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldor = scn.GetFreepsCard("aldor");
		var herdsman = scn.GetFreepsCard("herdsman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(aldor, herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// No Free Peoples archer: the fellowship archery total is 0.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aldor));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aldor));

		scn.FreepsUseCardAction(aldor);

		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(aldor));
		// The villager is only spotted.
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The bonus lasts the archery phase, and the Troop takes the fellowship's one archery wound.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithoutAVillagerAndIsOnceDeorIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldor = scn.GetFreepsCard("aldor");
		var deor = scn.GetFreepsCard("deor");
		var theoden = scn.GetFreepsCard("theoden");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(aldor);
		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Two [rohan] Men, Théoden and Aldor himself, and neither is a villager.
		assertEquals(0, scn.GetWoundsOn(aldor));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aldor));

		scn.MoveAlliesToTable(deor);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(aldor));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsUseCardAction(aldor);

		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetWoundsOn(aldor));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileAldorIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldor = scn.GetFreepsCard("aldor");
		var herdsman = scn.GetFreepsCard("herdsman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(aldor, herdsman);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(aldor, 1);
		assertEquals(1, scn.GetWoundsOn(aldor));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aldor));
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
