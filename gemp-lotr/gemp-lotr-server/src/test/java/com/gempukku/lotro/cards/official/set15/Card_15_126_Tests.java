package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gamling", "15_126");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant, no hunter, no other text
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, home 4T; a fellowship heal used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit; a Man only beside 3 [rohan] companions or Eowyn
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("faramir", "18_48");    // Faramir, Captain of Ithilien: [gondor] Man companion, hunter 2; a maneuver heal used only on purpose
					put("elfguard", "17_7");    // Elven Guardian: [elven] Elf companion, hunter 1; texts that need a skirmish or the maneuver phase
					put("uruk", "15_171");      // Tracking Uruk: [uruk-hai] Uruk-hai, hunter 1 is its whole text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GamlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gamling, The Old
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper> To play, spot a [rohan] Man.
		 * 	While you can spot 3 hunters, Gamling is strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gamling");

		assertEquals("Gamling", card.getBlueprint().getTitle());
		assertEquals("The Old", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilARohanManCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(merry, boromir);
		scn.MoveCardsToHand(gamling, guard);

		scn.StartGame();

		// Merry, a [rohan] Hobbit who is no Man (one [rohan] companion), and Boromir, a Man of [gondor]: no [rohan] Man.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gamling));

		scn.FreepsPlayCard(guard);
		// Merry and the Household Guard are two [rohan] companions: Merry is still no Man, and the Guard is the one spotted.
		assertTrue(scn.FreepsPlayAvailable(gamling));
		scn.FreepsPlayCard(gamling);
		assertInZone(Zone.FREE_CHARACTERS, gamling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManAllyIsSpottedToPlayHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(gamling);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(gamling));
		scn.FreepsPlayCard(gamling);
		assertInZone(Zone.FREE_CHARACTERS, gamling);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithThreeHuntersHimselfAmongThemGamlingIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var faramir = scn.GetFreepsCard("faramir");
		var elfguard = scn.GetFreepsCard("elfguard");
		scn.MoveCompanionsToTable(gamling, guard, faramir);
		scn.MoveCardsToHand(elfguard);

		scn.StartGame();

		// Two hunters, Gamling and Faramir; the Household Guard is a companion and no hunter.
		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(7, scn.GetStrength(faramir));

		scn.FreepsPlayCard(elfguard);
		// Three hunters: 6 + 3. Faramir, another companion, is untouched.
		assertEquals(9, scn.GetStrength(gamling));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHunterMinionIsCountedAmongTheThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var faramir = scn.GetFreepsCard("faramir");
		var uruk = scn.GetShadowCard("uruk");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gamling, faramir);

		scn.StartGame();
		// Minions cheated onto the table in the fellowship phase, to read his standing text.
		scn.MoveMinionsToTable(runner);
		// Gamling and Faramir, beside a minion who is no hunter.
		assertEquals(6, scn.GetStrength(gamling));

		scn.MoveMinionsToTable(uruk);
		// The Tracking Uruk is the third hunter: 6 + 3.
		assertEquals(9, scn.GetStrength(gamling));
		assertEquals(7, scn.GetStrength(faramir));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
