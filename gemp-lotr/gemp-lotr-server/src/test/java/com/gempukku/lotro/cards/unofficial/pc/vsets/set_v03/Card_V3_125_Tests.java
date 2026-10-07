package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_125_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo (strength 3), who bears The Ruling Ring (strength +1).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("gimli", "1_13");          // Gimli, Son of Gloin: strength 6; a third companion, for a threat limit of 3
					put("bilbo", "1_284");         // Bilbo, Retired Adventurer: [shire] ally, strength 2
					put("resistance", "7_121");    // Stout Resistance: skirmish event, "Add a threat to make a [gondor] Man strength +3."

					put("savage", "1_151");        // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3
					put("runner", "1_178");        // Goblin Runner: [moria] Orc, strength 5
					put("troop", "1_177");         // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "103_125");       // Muster of Gorgoroth
					put("site9", "7_363");         // Slag Mounds: adds a burden when a companion is killed, nothing else
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MusterofGorgorothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Muster of Gorgoroth
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. Each minion is strength +1 for each threat you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Muster of Gorgoroth", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void WhileTheFellowshipIsHereEachMinionIsStrongerByTheNumberOfThreatsAndNobodyElseIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveMinionsToTable(savage, runner);
		scn.AddThreats(2);

		// At site 7, with 2 threats on the dead pile, everyone has the strength printed.
		assertEquals(2, scn.GetThreats());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(2, scn.GetStrength(bilbo));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPass();
		assertEquals("Muster of Gorgoroth", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetThreats());

		// Minions of two cultures and two races: 5 + 2 threats
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(runner));
		// A minion that comes into play after the fellowship arrived: 13 + 2 threats
		scn.MoveMinionsToTable(troop);
		assertEquals(15, scn.GetStrength(troop));

		// Companions, the Ring-bearer and an ally are as they were.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(2, scn.GetStrength(bilbo));
		assertEquals(4, scn.GetStrength(frodo));

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachThreatAddedMakesEveryMinionOneStrongerAndWithNoThreatsNoMinionIsStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var resistance = scn.GetFreepsCard("resistance");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		// Three companions with the Ring-bearer, so that a third threat can be added.
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(resistance);
		scn.MoveMinionsToTable(savage, troop);

		scn.FreepsPass();
		assertEquals("Muster of Gorgoroth", scn.GetCurrentSite().getBlueprint().getTitle());

		// Here with no threat to spot: the strength printed.
		assertEquals(0, scn.GetThreats());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		scn.AddThreats(2);
		// 5 + 2 threats, 13 + 2 threats
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(15, scn.GetStrength(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(15, scn.GetStrength(troop));
		assertTrue(scn.FreepsPlayAvailable(resistance));

		// Stout Resistance adds a third threat as its cost: the minion in the skirmish and the one out of
		// it are each 1 stronger at once.
		scn.FreepsPlayCard(resistance);
		assertEquals(3, scn.GetThreats());
		// 5 + 3 threats, 13 + 3 threats
		assertEquals(8, scn.GetStrength(savage));
		assertEquals(16, scn.GetStrength(troop));
		// 8 + 3 for Stout Resistance; the threats give him nothing
		assertEquals(11, scn.GetStrength(aragorn));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnTheSameThreatsMakeNoMinionStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(savage, troop);
		scn.AddThreats(2);

		scn.FreepsPass();
		assertEquals("Muster of Gorgoroth", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetThreats());
		// 5 + 2 threats, 13 + 2 threats
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(15, scn.GetStrength(troop));

		// Nobody is assigned, and the fellowship moves again: the minions stay in play for the second move.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetStrength(savage));
		assertEquals(15, scn.GetStrength(troop));
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The same two minions, with the same 2 threats still on the dead pile.
		assertEquals(2, scn.GetThreats());
		assertInPlay(savage, troop);
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
	}
}
