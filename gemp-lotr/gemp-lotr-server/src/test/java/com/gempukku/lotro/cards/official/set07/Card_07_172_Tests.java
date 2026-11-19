package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "7_172");
					put("troop2", "7_172");
					put("soldier", "4_254");    // Southron Soldier: another [raider] Southron Man; ambush (2) and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, to hold threats
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, to hold threats
					put("boromir", "1_97");     // Boromir, Son of Denethor: a companion, to hold threats
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion, to hold threats
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
	public void TroopofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Troop of Haradrim
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br>While you can spot 2 threats, this minion is an <b>archer</b>.<br>While you can spot 3 threats, this minion is <b>fierce</b>.<br>While you can spot 4 threats, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop");

		assertEquals("Troop of Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsAnArcherFrom2ThreatsFierceFrom3AndDamagePlus1From4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		// Laid in the fellowship phase only to read the standing text (Q-0228).
		scn.MoveMinionsToTable(troop, troop2, soldier);

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// One threat: one short of the first.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// Two threats: an archer, and no more.
		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// Three threats: fierce as well.
		scn.AddThreats(1);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// Four threats: damage +1 as well.  Each copy has its own, not +2, and another [raider] Southron Man gains
		// nothing.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(troop2, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(troop2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(soldier, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(soldier, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));

		// Five threats: still all three, at +1.
		scn.AddThreats(1);
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
	}

	@Test
	public void DamageBonusSwitchesOnAsCursedOfErechAddsTheFourthThreatAndOffAsSauronsHatredRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var cursed = scn.GetFreepsCard("cursed");
		var hatred = scn.GetShadowCard("hatred");
		var chieftain = scn.GetShadowCard("chieftain");
		// Four companions with Frodo, so that a fourth threat can be added for Cursed of Erech.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToHand(cursed, chieftain);
		scn.MoveCardsToSupportArea(hatred);
		assertInHand(cursed, chieftain);
		assertInZone(Zone.SUPPORT, hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddThreats(3);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetThreats());
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// Cursed of Erech's cost is the fourth threat.
		scn.FreepsPlayCard(cursed);
		assertInZone(Zone.FREE_CHARACTERS, cursed);
		assertEquals(4, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		// In the Shadow phase, Sauron's Hatred removes a threat to play the Orc Chieftain.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(hatred));
		scn.ShadowUseCardAction(hatred);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(troop, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
