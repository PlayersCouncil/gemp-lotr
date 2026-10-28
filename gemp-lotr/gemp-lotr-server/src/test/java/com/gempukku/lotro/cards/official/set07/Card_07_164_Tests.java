package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("conqueror", "7_164");
					put("conqueror2", "7_164");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 or 5 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a companion of cost 1; a bonus while bearing a weapon only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of cost 2; a skirmish ability only
					put("bounder", "1_286");    // Bounder: an ally of cost 1; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronConquerorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Conqueror
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.
		 * While you can spot 6 twilight tokens, other [raider] Men are <b>damage +1</b>.
		 * While you can spot 9 twilight tokens, other [raider] Men are <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var conqueror = scn.GetFreepsCard("conqueror");

		assertEquals("Southron Conqueror", conqueror.getBlueprint().getTitle());
		assertNull(conqueror.getBlueprint().getSubtitle());
		assertFalse(conqueror.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, conqueror.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, conqueror.getBlueprint().getCulture());
		assertEquals(CardType.MINION, conqueror.getBlueprint().getCardType());
		assertEquals(Race.MAN, conqueror.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(conqueror, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(conqueror, Keyword.ARCHER));
		assertEquals(5, conqueror.getBlueprint().getTwilightCost());
		assertEquals(10, conqueror.getBlueprint().getStrength());
		assertEquals(3, conqueror.getBlueprint().getVitality());
		assertEquals(4, conqueror.getBlueprint().getSiteNumber());
	}

	@Test
	public void OtherRaiderMenAreDamagePlus1At6TwilightTokensAndPlus2At9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var conqueror = scn.GetShadowCard("conqueror");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Laid in the fellowship phase only to read the standing text (Q-0228).  Two [raider] Men of different
		// kinds, a Southron and an Easterling; a [raider] minion who is no Man, a Man who is not [raider], and the
		// Free Peoples player's own Man.
		scn.MoveMinionsToTable(conqueror, explorer, axeman, khamul, brigand);

		// Five twilight tokens: one short.
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(axeman, Keyword.DAMAGE));

		// Six: each other [raider] Man is damage +1, and nobody else.
		scn.SetTwilight(6);
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(axeman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(conqueror, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// Eight: one short of the second sentence, still +1.
		scn.SetTwilight(8);
		assertEquals(8, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(axeman, Keyword.DAMAGE));

		// Nine: both sentences, +2, and still nobody else.
		scn.SetTwilight(9);
		assertEquals(9, scn.GetTwilight());
		assertEquals(2, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(axeman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(conqueror, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void EachCopyStrengthensTheOtherCopyButNotItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var conqueror = scn.GetShadowCard("conqueror");
		var conqueror2 = scn.GetShadowCard("conqueror2");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(conqueror, conqueror2, explorer);

		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(conqueror, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(conqueror2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));

		// Six: each copy is +1 from the other, and the Explorer +1 from each.
		scn.SetTwilight(6);
		assertEquals(1, scn.GetKeywordCount(conqueror, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(conqueror2, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(explorer, Keyword.DAMAGE));

		// Nine: +2 from the other copy, +4 from both.
		scn.SetTwilight(9);
		assertEquals(2, scn.GetKeywordCount(conqueror, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(conqueror2, Keyword.DAMAGE));
		assertEquals(4, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
	}

	@Test
	public void SwitchesOnAsFreePeoplesPlaysAddTheSixthAndTheNinthToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(merry, gimli, bounder);
		assertInHand(merry, gimli, bounder);

		var conqueror = scn.GetShadowCard("conqueror");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(conqueror, explorer);
		scn.SetTwilight(5);
		scn.FreepsRefreshDecision();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));

		scn.FreepsPlayCard(merry);
		assertEquals(6, scn.GetTwilight()); // 5 + 1
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.DAMAGE));

		scn.FreepsPlayCard(gimli);
		assertEquals(8, scn.GetTwilight()); // 6 + 2
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.DAMAGE));

		scn.FreepsPlayCard(bounder);
		assertEquals(9, scn.GetTwilight()); // 8 + 1
		assertEquals(2, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(conqueror, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
