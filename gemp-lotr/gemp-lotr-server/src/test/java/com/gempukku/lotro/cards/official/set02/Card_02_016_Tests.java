package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_16");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukHaiScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("race", "2_16");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf to spot
					// Uruk Savage: an Uruk-hai of strength 5; Damage +1 is its whole text
					put("savage", "1_151");
					// Cave Troll of Moria: a Troll of strength 15 with damage +1 (and fierce); its other texts
					// are about playing it
					put("troll", "1_165");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ABlendedRaceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: A Blended Race
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, spot an Elf. Plays to your support area.<br>Each Uruk-hai is strength -2 and loses all <b>damage</b> bonuses. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("A Blended Race", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUrukHaiLosesItsDamageBonusAndAMinionOfAnotherRaceKeepsIts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukHaiScenario();

		var race = scn.GetFreepsCard("race");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(race);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troll);

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troll));
		assertTrue(scn.FreepsPlayAvailable(race));

		scn.FreepsPlayCard(race);
		assertInZone(Zone.SUPPORT, race);
		assertEquals(1, scn.GetTwilight());
		// The Uruk-hai: no damage bonus, and strength 5 - 2
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(savage));
		// The Troll is as it was
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troll));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
