package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_54");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCultureScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "12_54");
					// Sam, Son of Hamfast: a [shire] companion beside the Ring-bearer, strength 3; his fellowship
					// ability is not used
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit who is an ally, strength 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Saruman, Of Many Colours
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>When you play Saruman, name a culture. Each companion of the named culture is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Of Many Colours", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NamingACultureMakesEachCompanionOfThatCultureStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCultureScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10, and for the move 3 companions and the 2 of Ettenmoors
		assertEquals(15, scn.GetTwilight());
		// 3, and 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));

		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertTrue(scn.ShadowDecisionAvailable("Choose a culture"));
		assertEquals(4, scn.GetStrength(frodo));

		// Each [shire] companion is one less; the [gondor] companion and the [shire] ally are as they were.
		scn.ShadowChooseOption("Shire");
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bounder));
		// 15 - 4, and 2 for roaming: his site number is 4 and the fellowship is at site 2
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
