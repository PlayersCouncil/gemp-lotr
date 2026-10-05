package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("driven", "102_1");
					put("driven2", "102_1");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, cost 2, site 3, no text
					put("saruman", "4_33");     // Saruman, Rabble-rouser: the one [dunland] minion who is not a Man; cost 4, site 4
					put("footman", "11_82");    // Footman of Dunland: a Man minion who is [men], not [dunland]; makes the site plains

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability that is never used
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability that is never used
					put("merry", "1_303");      // Merry, From O'er the Brandywine: stronger with a weapon, and bears none
					put("pippin", "1_306");     // Pippin, Friend to Frodo: cost 1, nothing to spot, protects [shire] tales
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability that is never used
					put("gaffer", "1_291");     // The Gaffer: an ally; a fellowship ability that is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrivenIntoTheHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Driven into the Hills
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [Dunland] minion.
		 * For each companion you can spot over 4, the Free Peoples player must have an additional card in hand
		 * to have initiative.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven into the Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilADunlandMinionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var driven2 = scn.GetShadowCard("driven2");
		var brigand = scn.GetShadowCard("brigand");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(driven, brigand);
		scn.MoveCardsToSupportArea(driven2);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for the site
		assertEquals(10, scn.GetTwilight());
		// In play are a Man minion of another culture and a [dunland] card that is no minion (the other copy
		// of this condition); the [dunland] minion is still in hand.
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertInZone(Zone.SUPPORT, driven2);
		assertInHand(brigand);
		assertFalse(scn.ShadowPlayAvailable(driven));

		scn.ShadowPlayCard(brigand);
		// 2 to play + 2 roaming: site number 3, played at site 2
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(driven));

		scn.ShadowPlayCard(driven);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, driven);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ADunlandMinionWhoIsNotAManIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(driven, saruman);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for the site
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(driven));

		// Saruman, Rabble-rouser is a unique [dunland] Wizard.
		scn.ShadowPlayCard(saruman);
		// 4 to play + 2 roaming: site number 4, played at site 2
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(driven));

		scn.ShadowPlayCard(driven);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, driven);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FourCompanionsAndAnAllyLeaveInitiativeAtFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		scn.MoveCardsToSupportArea(driven);
		// With Frodo, 4 companions. The Gaffer is a fifth Free Peoples character and no companion.
		scn.MoveCompanionsToTable("aragorn", "boromir", "merry");
		scn.MoveAlliesToTable("gaffer");
		scn.MoveCardsToFreepsHand("brigand", "footman", "saruman");

		scn.StartGame();

		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		scn.MoveCardsToFreepsHand("driven2");
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCompanionOverFourAsksForOneMoreCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(driven);
		scn.MoveCompanionsToTable("aragorn", "boromir", "merry");
		scn.MoveCardsToFreepsHand("pippin", "brigand", "footman", "saruman", "driven2");

		scn.StartGame();

		// With Frodo, 4 companions.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		// Pippin is the fifth companion, and playing him leaves 4 cards in hand: enough for initiative by the
		// rule, and one short of the 4 + 1 now asked for.
		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		scn.MoveCardsToFreepsHand("gaffer");
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// A sixth companion: 4 + 2.
		scn.MoveCompanionsToTable(sam);
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		scn.MoveCardsToFreepsHand("driven");
		assertEquals(6, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondCopyAsksForAnotherCardForEachCompanionOverFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetShadowCard("driven");
		var driven2 = scn.GetShadowCard("driven2");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(driven);
		scn.MoveCardsToHand(driven2);
		// With Frodo, 5 companions.
		scn.MoveCompanionsToTable("aragorn", "boromir", "merry", "pippin");
		scn.MoveCardsToFreepsHand("brigand", "footman", "saruman", "driven", "driven2");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// One copy in play: 4 + 1.
		assertInZone(Zone.SUPPORT, driven);
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowPlayAvailable(driven2));

		// Two copies: each asks for a card of its own for the fifth companion, 4 + 1 + 1.
		scn.ShadowPlayCard(driven2);
		assertInZone(Zone.SUPPORT, driven, driven2);
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		scn.MoveCardsToFreepsHand("sam");
		assertEquals(6, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
