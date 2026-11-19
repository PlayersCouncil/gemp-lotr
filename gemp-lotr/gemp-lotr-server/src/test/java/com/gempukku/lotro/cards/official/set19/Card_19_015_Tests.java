package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blood", "19_15");
					put("blood2", "19_15");

					// Boromir, Destined Guide: a [gondor] Man with the Fellowship keyword.  His twilight discount and
					// his skirmish trigger reach no reading here.
					put("boromir", "19_13");
					// Merry, Resolute Friend: a [shire] Hobbit with the Fellowship keyword; a skirmish ability never used.
					put("merry", "19_29");
					// Aragorn, Ranger of the North: a [gondor] companion without the Fellowship keyword; a maneuver
					// ability never used.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthinMyBloodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Strength in My Blood
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Strength: +1
		 * Game Text: Bearer must be a fellowship companion.<br>While bearer is a [gondor] companion, he or she is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blood");

		assertEquals("Strength in My Blood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAFellowshipCompanionAndMakesAGondorBearerDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var blood = scn.GetFreepsCard("blood");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, merry, aragorn);
		scn.MoveCardsToHand(blood);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(blood));
		scn.FreepsPlayCard(blood);

		// The two companions with the Fellowship keyword, a [gondor] Man and a [shire] Hobbit.  Not Aragorn, a
		// companion without it, and not Frodo.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(boromir);

		assertAttachedTo(blood, boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		// Aragorn is a [gondor] companion too, and does not bear it.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAShireCompanionItGivesNoDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blood = scn.GetFreepsCard("blood");
		var blood2 = scn.GetFreepsCard("blood2");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(boromir, merry);
		scn.MoveCardsToHand(blood, blood2);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		scn.FreepsPlayCard(blood);
		scn.FreepsChooseCard(merry);
		assertAttachedTo(blood, merry);
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));

		// The same card on the [gondor] Fellowship companion beside him gives its bonus.
		scn.FreepsPlayCard(blood2);
		scn.FreepsChooseCard(boromir);
		assertAttachedTo(blood2, boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(merry, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
