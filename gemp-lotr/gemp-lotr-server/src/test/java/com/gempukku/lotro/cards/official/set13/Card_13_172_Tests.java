package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("outrider1", "13_172");
					put("outrider2", "13_172");
					put("warrior", "4_165");    // Orthanc Warrior: another minion, damage +1 printed

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a companion, strength 7 beside Frodo
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; his ability is never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally; his ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukOutriderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Outrider
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 15
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: This minion is <b>damage +1</b> for each character in the dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("outrider1");

		assertEquals("Uruk Outrider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus1OnceACompanionItKillsIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var outrider1 = scn.GetShadowCard("outrider1");
		var outrider2 = scn.GetShadowCard("outrider2");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(outrider1, outrider2, warrior);
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));

		// Nobody in the dead pile: no bonus.
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetKeywordCount(outrider1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(outrider2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, outrider1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);
		// Gandalf (6 + 1 for a Hobbit) loses to 15 and dies.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, gandalf);
		assertEquals(1, scn.GetFreepsDeadCount());
		// Each copy counts him once; the Orthanc Warrior keeps only its own +1.
		assertEquals(1, scn.GetKeywordCount(outrider1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(outrider2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CountsACompanionAndAnAllyInTheFreePeoplesDeadPileButNotTheShadowPlayersOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToDeadPile(gandalf, thrarin, shadowAragorn);

		var outrider1 = scn.GetShadowCard("outrider1");
		var outrider2 = scn.GetShadowCard("outrider2");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(outrider1, outrider2, warrior);

		// Gandalf and Thrarin lie in the Free Peoples player's dead pile; the Shadow player's Aragorn in his own.
		assertInZone(Zone.DEAD, gandalf, thrarin, shadowAragorn);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertEquals(2, scn.GetKeywordCount(outrider1, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(outrider2, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
	}
}
