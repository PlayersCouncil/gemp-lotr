package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "4_122");
					put("boromir", "0_65");     // Boromir, Steward's Heir: a Ring-bound Man of another title, keywords only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man; a maneuver ability used only on purpose
					put("sam", "4_315");        // Sam, Frodo's Gardener: a Ring-bound Hobbit who is not the Ring-bearer
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, site 4, strength 5; "when you play" does nothing when cheated in
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, site 3, no text
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, site 5, damage +1 and nothing else
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site 4, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorianRangerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gondorian Ranger
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.<br>The site number of each minion assigned to skirmish a Ring-bound Man is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger");

		assertEquals("Gondorian Ranger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void EachMinionAssignedToARingBoundManIsSiteNumberPlus2UntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(ranger, boromir, aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand, savage1, savage2, troop);
		scn.SkipToAssignments();

		// Not yet assigned: the printed numbers.
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertEquals(3, scn.GetMinionSiteNumber(brigand));
		assertEquals(5, scn.GetMinionSiteNumber(savage1));
		assertEquals(5, scn.GetMinionSiteNumber(savage2));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { ranger, runner },
				new PhysicalCardImpl[] { boromir, brigand },
				new PhysicalCardImpl[] { aragorn, savage2 },
				new PhysicalCardImpl[] { sam, troop }
		);
		// The Shadow player puts the minion left over on Boromir: two minions on one Ring-bound Man.
		scn.ShadowAssignToMinions(boromir, savage1);

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		// On the Ranger himself (an Orc), and on another Ring-bound Man (a Man and an Uruk-hai): +2.
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		assertEquals(5, scn.GetMinionSiteNumber(brigand));
		assertEquals(7, scn.GetMinionSiteNumber(savage1));
		// On an unbound Man, and on a Ring-bound Hobbit: no change.
		assertEquals(5, scn.GetMinionSiteNumber(savage2));
		assertEquals(4, scn.GetMinionSiteNumber(troop));

		scn.FreepsResolveSkirmish(ranger);
		// Still assigned while that skirmish is fought.
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		scn.PassSkirmishActions();
		// 5 against 5: the Runner wins, lives, and is assigned no longer.
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(4, scn.GetMinionSiteNumber(runner));
		assertEquals(5, scn.GetMinionSiteNumber(brigand));
		assertEquals(7, scn.GetMinionSiteNumber(savage1));
	}
}
