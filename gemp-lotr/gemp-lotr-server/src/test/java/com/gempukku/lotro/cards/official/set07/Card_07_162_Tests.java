package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_162_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "7_162");
					put("captain2", "7_162");
					put("soldier", "4_254");    // Southron Soldier: another [raider] Southron Man; ambush (2) and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, to hold threats
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, to hold threats
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion, to hold threats
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion, to hold threats
					// Cursed of Erech: a [gondor] Wraith companion, "To play, add a threat"; exerts himself twice as he is played
					put("cursed", "10_26");
					// Sauron's Hatred: a [sauron] condition, "Shadow: Remove a threat to play a [sauron] Orc without paying
					// any roaming penalty"
					put("hatred", "7_310");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, cost 2; a trigger on weapons played on him only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Captain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * While you can spot 4 threats, this minion is <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");

		assertEquals("Southron Captain", captain.getBlueprint().getTitle());
		assertNull(captain.getBlueprint().getSubtitle());
		assertFalse(captain.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, captain.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, captain.getBlueprint().getCulture());
		assertEquals(CardType.MINION, captain.getBlueprint().getCardType());
		assertEquals(Race.MAN, captain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(captain, Keyword.SOUTHRON));
		assertEquals(6, captain.getBlueprint().getTwilightCost());
		assertEquals(13, captain.getBlueprint().getStrength());
		assertEquals(3, captain.getBlueprint().getVitality());
		assertEquals(4, captain.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus2WhileFourOrMoreThreatsCanBeSpottedAndNotWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var captain2 = scn.GetShadowCard("captain2");
		var soldier = scn.GetShadowCard("soldier");
		// Five companions with Frodo, to hold five threats (CR 4.2 "threats").
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("legolas"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		// Laid in the fellowship phase only to read the standing text (Q-0228).
		scn.MoveMinionsToTable(captain, captain2, soldier);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(captain, Keyword.DAMAGE));

		// Three threats: one short.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(captain2, Keyword.DAMAGE));

		// Four threats: each copy is damage +2 on its own, not +4, and another [raider] Man gains nothing.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(2, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(captain2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));

		// Five threats: still +2.
		scn.AddThreats(1);
		assertEquals(5, scn.GetThreats());
		assertEquals(2, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(captain2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
	}

	@Test
	public void SwitchesOnAsCursedOfErechAddsTheFourthThreatAndOffAsSauronsHatredRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var cursed = scn.GetFreepsCard("cursed");
		var hatred = scn.GetShadowCard("hatred");
		var chieftain = scn.GetShadowCard("chieftain");
		// Four companions with Frodo, so that a fourth threat can be added.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("legolas"));
		scn.MoveCardsToHand(cursed, chieftain);
		scn.MoveCardsToSupportArea(hatred);
		assertInHand(cursed, chieftain);
		assertInZone(Zone.SUPPORT, hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.AddThreats(3);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(captain, Keyword.DAMAGE));

		// Cursed of Erech's cost is the fourth threat.
		scn.FreepsPlayCard(cursed);
		assertInZone(Zone.FREE_CHARACTERS, cursed);
		assertEquals(4, scn.GetThreats());
		assertEquals(2, scn.GetKeywordCount(captain, Keyword.DAMAGE));

		// In the Shadow phase, Sauron's Hatred removes a threat to play the Orc Chieftain.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(hatred));
		scn.ShadowUseCardAction(hatred);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
