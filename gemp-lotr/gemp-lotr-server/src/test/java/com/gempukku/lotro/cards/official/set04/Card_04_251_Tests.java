package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_251_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighter1", "4_251");
					put("fighter2", "4_251");   // a second copy: not unique

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; vitality 4; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4; a modifier while he bears a weapon
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronFighterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Fighter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br><b>Maneuver:</b> Exert this minion and spot 5 companions to make the Free Peoples player exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fighter1");

		assertEquals("Southron Fighter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsThisMinionToMakeTheFreePeoplesPlayerExertACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var celeborn = scn.GetFreepsCard("celeborn");
		// Exactly five companions, with an ally beside them.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter1, fighter2);
		// Vitality 3 with 2 wounds: Gimli is exhausted and cannot exert.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(fighter1));
		assertEquals(0, scn.GetWoundsOn(fighter2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fighter1));

		scn.ShadowUseCardAction(fighter1);
		// The copy used exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(fighter1));
		assertEquals(0, scn.GetWoundsOn(fighter2));
		// The Free Peoples player chooses: companions of three cultures, the Ring-bearer and a Ring-bound one among
		// them; not an exhausted companion, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, celeborn, fighter1, fighter2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter1 = scn.GetShadowCard("fighter1");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter1);
		// Five companions, each with one vitality left.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(fighter1));
		assertTrue(scn.ShadowActionAvailable(fighter1));

		scn.ShadowUseCardAction(fighter1);
		// The cost is paid; nobody can exert, and nobody is asked for.
		assertEquals(1, scn.GetWoundsOn(fighter1));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFourCompanionsAndAnAllyTheManeuverAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter1 = scn.GetShadowCard("fighter1");
		var merry = scn.GetFreepsCard("merry");
		// Four companions, and an ally, a character who is no companion.
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(fighter1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fighter1));

		// A fifth companion.
		scn.MoveCompanionsToTable(merry);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(fighter1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fighter1 = scn.GetShadowCard("fighter1");
		var fighter2 = scn.GetShadowCard("fighter2");
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter1, fighter2);
		// Vitality 3 with 2 wounds: exhausted, with five companions to spot.
		scn.AddWoundsToChar(fighter1, 2);
		assertEquals(2, scn.GetWoundsOn(fighter1));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(fighter1));
		assertEquals(0, scn.GetWoundsOn(fighter2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fighter1));
		// The other copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(fighter2));

		scn.RemoveWoundsFromChar(fighter1, 2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(fighter1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
