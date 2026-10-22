package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loss", "10_35");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, strength 8; a maneuver ability used only on purpose
					// Stairs of Khazad-dum: [dwarven] condition, cost 0, no requirement: played only to take the hand from
					// four cards to three; its text reaches only a Dwarf, and none is here
					put("stairs", "1_24");
					// Dwarven Armor: cards to hold in hand and nothing else (no Dwarf to bear one).
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8; Damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8;
					// each companion is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SufferedMuchLossStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Suffered Much Loss
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each site on the adventure path is a <b>battleground</b>.<br>While you have initiative, each Uruk-hai is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("loss");

		assertEquals("Suffered Much Loss", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachSiteOnTheAdventurePathIsABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loss = scn.GetShadowCard("loss");

		scn.StartGame();
		// Site 1 is the Free Peoples player's own; site 2 is laid by the Shadow player as the fellowship moves.
		var site1 = scn.GetSite(1);
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));

		scn.MoveCardsToSupportArea(loss);
		assertInZone(Zone.SUPPORT, loss);
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));

		scn.FreepsPass();
		var site2 = scn.GetSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AControlledSiteIsOffThePathAndNoBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var loss = scn.GetShadowCard("loss");

		scn.StartGame();
		// Both markers on site 2: site 1 is behind every marker.
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));

		scn.MoveCardsToSupportArea(loss);
		assertInZone(Zone.SUPPORT, loss);
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileTheShadowHasInitiativeEachUrukHaiIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		var loss = scn.GetShadowCard("loss");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToSupportArea(loss);
		// Laid early, to be read before and after a Free Peoples play (Q-0228).
		scn.MoveMinionsToTable(savage, sentry, troop, saruman);
		scn.FreepsRefreshDecision();

		// Four cards in hand: the Free Peoples player has initiative, and nobody is strengthened.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(sentry));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(aragorn));

		// Four cards to three: the Shadow has initiative.
		scn.FreepsPlayCard(stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());

		// Uruk-hai of two cultures, +1 each; not an Orc, nor an [uruk-hai] minion who is no Uruk-hai, nor a companion.
		assertEquals(6, scn.GetStrength(savage));
		assertEquals(9, scn.GetStrength(sentry));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
