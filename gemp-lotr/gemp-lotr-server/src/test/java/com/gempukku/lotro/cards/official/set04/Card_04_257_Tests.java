package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_257_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("veterans1", "4_257");
					put("veterans2", "4_257");  // a second copy: not unique

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; vitality 4; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4; a modifier while he bears a weapon
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard, vitality 4; a strength modifier only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronVeteransStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Veterans
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br><b>Maneuver:</b> Exert this minion and spot 6 companions to make the Free Peoples player exert 2 companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("veterans1");

		assertEquals("Southron Veterans", card.getBlueprint().getTitle());
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
	public void ManeuverAbilityExertsThisMinionToMakeTheFreePeoplesPlayerExertTwoCompanionsOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veterans1 = scn.GetShadowCard("veterans1");
		var veterans2 = scn.GetShadowCard("veterans2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var celeborn = scn.GetFreepsCard("celeborn");
		// Exactly six companions, with an ally beside them.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry, gandalf);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(veterans1, veterans2);
		// Vitality 3 with 2 wounds: Gimli is exhausted and cannot exert.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(veterans1));
		assertEquals(0, scn.GetWoundsOn(veterans2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(veterans1));

		scn.ShadowUseCardAction(veterans1);
		// The copy used exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(veterans1));
		assertEquals(0, scn.GetWoundsOn(veterans2));
		// The Free Peoples player chooses two: companions of four cultures, the Ring-bearer and a Ring-bound one
		// among them; not an exhausted companion, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, aragorn, merry, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, celeborn, veterans1, veterans2));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, merry);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veterans1 = scn.GetShadowCard("veterans1");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(veterans1);
		// Six companions, each with one vitality left.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(merry, 3);
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(veterans1));
		assertTrue(scn.ShadowActionAvailable(veterans1));

		scn.ShadowUseCardAction(veterans1);
		// The cost is paid; nobody can exert, and nobody is asked for.
		assertEquals(1, scn.GetWoundsOn(veterans1));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyTheManeuverAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veterans1 = scn.GetShadowCard("veterans1");
		var gandalf = scn.GetFreepsCard("gandalf");
		// Five companions, and an ally, a character who is no companion.
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(veterans1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(veterans1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veterans1));

		// A sixth companion.
		scn.MoveCompanionsToTable(gandalf);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(veterans1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var veterans1 = scn.GetShadowCard("veterans1");
		var veterans2 = scn.GetShadowCard("veterans2");
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry", "gandalf");

		scn.StartGame();
		scn.MoveMinionsToTable(veterans1, veterans2);
		// Vitality 3 with 2 wounds: exhausted, with six companions to spot.
		scn.AddWoundsToChar(veterans1, 2);
		assertEquals(2, scn.GetWoundsOn(veterans1));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(veterans1));
		assertEquals(0, scn.GetWoundsOn(veterans2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(veterans1));
		// The other copy, able to exert, is offered its own.
		assertTrue(scn.ShadowActionAvailable(veterans2));

		scn.RemoveWoundsFromChar(veterans1, 2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(veterans1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
