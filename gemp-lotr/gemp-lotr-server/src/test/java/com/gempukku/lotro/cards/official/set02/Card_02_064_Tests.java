package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scrabbler1", "2_64");
					put("scrabbler2", "2_64");
					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc minion, no text

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability
					// Mallorn-trees: [elven] condition in the support area; a fellowship ability only
					put("mallorn", "1_54");
					// The Last Alliance of Elves and Men: [elven] condition borne by a [gondor] Man; bearer is
					// strength +1 for each Elf
					put("alliance", "1_49");
					put("cloak", "1_42");         // Elven Cloak: [elven] possession on a companion; minion archery total -1
					put("lord", "1_21");          // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRoamingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Scrabbler: cost 1, site number 4
					put("scrabbler1", "2_64");
					put("scrabbler2", "2_64");
					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, site number 4, no text
					// Cave Troll of Moria: a [moria] minion who is no Orc (a Troll), cost 10, site number 4; to play,
					// spot a [moria] Orc; -3 only at an underground site
					put("troll", "1_165");
					// Orc Chieftain: a [sauron] Orc, cost 2, site number 6; his text answers only a weapon played on him
					put("chieftain", "1_266");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinScrabblerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Goblin Scrabbler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 4
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may discard an [elven] condition.<br>The roaming penalty for each [moria] Orc you play is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scrabbler1");

		assertEquals("Goblin Scrabbler", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedMayDiscardAnElvenConditionAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var mallorn = scn.GetFreepsCard("mallorn");
		var alliance = scn.GetFreepsCard("alliance");
		var cloak = scn.GetFreepsCard("cloak");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(mallorn, lord);

		var scrabbler1 = scn.GetShadowCard("scrabbler1");
		var scrabbler2 = scn.GetShadowCard("scrabbler2");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(scrabbler1, scrabbler2, patrol);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, alliance, cloak);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Offered as it is played, and declined: nothing is discarded.
		scn.ShadowPlayCard(scrabbler1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scrabbler1));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, scrabbler1);
		assertInZone(Zone.SUPPORT, mallorn, lord);
		assertInZone(Zone.ATTACHED, alliance, cloak);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [moria] Orc played while it is in play: nothing is offered.
		scn.ShadowPlayCard(patrol);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second copy makes its own offer, and the first none.
		scn.ShadowPlayCard(scrabbler2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scrabbler2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scrabbler1));
		scn.ShadowAcceptOptionalTrigger();

		// An [elven] condition in the support area and one a companion bears; not an [elven] possession, nor a
		// condition of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, alliance));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cloak, lord, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(alliance);

		assertInZone(Zone.DISCARD, alliance);
		assertInZone(Zone.SUPPORT, mallorn, lord);
		assertInZone(Zone.ATTACHED, cloak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoElvenConditionInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord);

		var scrabbler = scn.GetShadowCard("scrabbler1");
		scn.MoveCardsToHand(scrabbler);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, cloak);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(scrabbler);
		assertInZone(Zone.SHADOW_CHARACTERS, scrabbler);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, lord);
		assertInZone(Zone.ATTACHED, cloak);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoriaOrcPlayedAfterItPaysOneLessForRoamingAndTheScrabblerItselfPaysThePenaltyInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var scrabbler1 = scn.GetShadowCard("scrabbler1");
		var scrabbler2 = scn.GetShadowCard("scrabbler2");
		var patrol = scn.GetShadowCard("patrol");
		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(scrabbler1, scrabbler2, patrol, troll, chieftain);

		scn.StartGame();
		scn.SetTwilight(27);
		scn.FreepsPass();

		// Site 2: every one of these minions is roaming here, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 27 set + 1 companion + 2 for Ettenmoors
		assertEquals(30, scn.GetTwilight());

		scn.ShadowPlayCard(scrabbler1);
		// 1 to play + 2 roaming: its own text is not in force while it is being played
		assertEquals(27, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scrabbler1);

		scn.ShadowPlayCard(troll);
		// A [moria] minion who is no Orc: 10 to play + 2 roaming
		assertEquals(15, scn.GetTwilight());
		assertTrue(scn.HasKeyword(troll, Keyword.ROAMING));

		scn.ShadowPlayCard(chieftain);
		// An Orc who is not [moria]: 2 to play + 2 roaming
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));

		scn.ShadowPlayCard(scrabbler2);
		// 1 to play + 2 roaming - 1 for the first Scrabbler, and nothing for itself
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, scrabbler2);

		scn.ShadowPlayCard(patrol);
		// Two Scrabblers in play: 6 to play + 2 roaming - 2.  The penalty is less; the minion is no less roaming.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.HasKeyword(patrol, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoriaOrcWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var scrabbler = scn.GetShadowCard("scrabbler1");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		// Site 4, the site number of the Patrol Troop: he pays no roaming penalty for the Scrabbler to make less.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(scrabbler);
		scn.SetShadowHand(patrol);

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(patrol));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(patrol));

		scn.ShadowPlayCard(patrol);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(patrol, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
