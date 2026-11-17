package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nomad", "7_132");
					put("nomad2", "7_132");
					put("soldier", "4_254");    // Southron Soldier: another [raider] Southron Man; ambush (2) and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, to hold threats
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, to hold threats
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
	public void DesertNomadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Desert Nomad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * While you can spot 2 threats, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var nomad = scn.GetFreepsCard("nomad");

		assertEquals("Desert Nomad", nomad.getBlueprint().getTitle());
		assertNull(nomad.getBlueprint().getSubtitle());
		assertFalse(nomad.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nomad.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, nomad.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nomad.getBlueprint().getCardType());
		assertEquals(Race.MAN, nomad.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(nomad, Keyword.SOUTHRON));
		assertEquals(4, nomad.getBlueprint().getTwilightCost());
		assertEquals(9, nomad.getBlueprint().getStrength());
		assertEquals(2, nomad.getBlueprint().getVitality());
		assertEquals(4, nomad.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus1WhileTwoOrMoreThreatsCanBeSpottedAndNotWithOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nomad = scn.GetShadowCard("nomad");
		var nomad2 = scn.GetShadowCard("nomad2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"));

		scn.StartGame();
		// Laid in the fellowship phase only to read the standing text (Q-0228).
		scn.MoveMinionsToTable(nomad, nomad2, soldier);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(nomad, Keyword.DAMAGE));

		// One threat: one short.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(nomad, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nomad2, Keyword.DAMAGE));

		// Two threats: each copy is damage +1 on its own, not +2, and another [raider] Man gains nothing.
		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(nomad, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(nomad2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));

		// Three threats: still +1.
		scn.AddThreats(1);
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(nomad, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(nomad2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
	}

	@Test
	public void SwitchesOnAsCursedOfErechAddsTheSecondThreatAndOffAsSauronsHatredRemovesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nomad = scn.GetShadowCard("nomad");
		var cursed = scn.GetFreepsCard("cursed");
		var hatred = scn.GetShadowCard("hatred");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(cursed, chieftain);
		scn.MoveCardsToSupportArea(hatred);
		assertInHand(cursed, chieftain);
		assertInZone(Zone.SUPPORT, hatred);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.AddThreats(1);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(nomad, Keyword.DAMAGE));

		// Cursed of Erech's cost is the second threat.
		scn.FreepsPlayCard(cursed);
		assertInZone(Zone.FREE_CHARACTERS, cursed);
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(nomad, Keyword.DAMAGE));

		// In the Shadow phase, Sauron's Hatred removes a threat to play the Orc Chieftain.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(hatred));
		scn.ShadowUseCardAction(hatred);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(nomad, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
