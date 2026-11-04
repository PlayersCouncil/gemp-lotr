package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("picket", "6_101");
					// Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: [dwarven] Dwarf companion; a skirmish ability only
					put("gimli", "1_13");
					// Legolas, Greenleaf: [elven] Elf companion; an archery ability only
					put("legolas", "1_50");
					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4; a strength bonus only
					put("gandalf", "1_72");
					// Bounder: [shire] Hobbit ally, home 2; a skirmish ability only
					put("bounder", "1_286");
					// Orophin, Lórien Bowman: [elven] Elf ally, home 6; an archery ability only
					put("orophin", "1_56");
					// Goblin Patrol Troop: [moria] Orc, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatePicketStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gate Picket
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Maneuver:</b> Exert this minion twice to make the Free Peoples player name 3 cultures. Wound each companion and ally not of a named culture.
		*/

		var scn = GetScenario();

		var picket = scn.GetFreepsCard("picket");

		assertEquals("Gate Picket", picket.getBlueprint().getTitle());
		assertNull(picket.getBlueprint().getSubtitle());
		assertTrue(picket.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, picket.getBlueprint().getSide());
		assertEquals(Culture.SAURON, picket.getBlueprint().getCulture());
		assertEquals(CardType.MINION, picket.getBlueprint().getCardType());
		assertEquals(Race.ORC, picket.getBlueprint().getRace());
		assertEquals(4, picket.getBlueprint().getTwilightCost());
		assertEquals(11, picket.getBlueprint().getStrength());
		assertEquals(3, picket.getBlueprint().getVitality());
		assertEquals(6, picket.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsTwiceAndWoundsEachCompanionAndAllyNotOfACultureTheFreePeoplesPlayerNames() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bounder = scn.GetFreepsCard("bounder");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn, gimli, legolas, gandalf);
		scn.MoveAlliesToTable(bounder, orophin);

		var picket = scn.GetShadowCard("picket");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Gandalf, vitality 4, is exhausted: the wound kills him.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(picket, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(picket));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(picket));
		scn.ShadowUseCardAction(picket);

		// Exerted twice.
		assertEquals(2, scn.GetWoundsOn(picket));
		// The Free Peoples player names the cultures (by choosing a character of each), not the Shadow player.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, legolas, gandalf, bounder, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(picket, troop));
		assertEquals(3, scn.FreepsGetChoiceMax());
		// [shire], [gondor] and [dwarven] named.
		scn.FreepsChooseCards(frodo, aragorn, gimli);

		// The [elven] and [gandalf] companions and the [elven] ally are wounded, the exhausted Gandalf killed.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertInZone(Zone.DEAD, gandalf);
		// The companions of a named culture, the [shire] ally, and the minions (of cultures not named) are not.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(picket));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NamingEveryCultureInPlayWoundsNobodyThoughTheExertionsArePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var picket = scn.GetShadowCard("picket");

		scn.StartGame();
		scn.MoveMinionsToTable(picket);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(picket));
		assertTrue(scn.ShadowActionAvailable(picket));
		scn.ShadowUseCardAction(picket);

		assertEquals(2, scn.GetWoundsOn(picket));
		// [shire] and [gondor]: every culture of a companion or ally in play.
		scn.FreepsChooseCards(frodo, aragorn);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedInTheShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var picket = scn.GetShadowCard("picket");

		scn.StartGame();
		scn.MoveMinionsToTable(picket);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(picket));
		assertFalse(scn.ShadowActionAvailable(picket));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenGatePicketCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var picket = scn.GetShadowCard("picket");

		scn.StartGame();
		scn.MoveMinionsToTable(picket);
		// Vitality 3 with a wound: 2 left, enough to exert once and not twice.
		scn.AddWoundsToChar(picket, 1);
		assertEquals(1, scn.GetWoundsOn(picket));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(picket));
		assertEquals(1, scn.GetWoundsOn(picket));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}
}
