package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * 93_8: Each Wraith is strength +1 for each wound on a character in its skirmish.
 *
 * Wraith is a race (Man, Elf, Dwarf, Wraith, etc.), not the [Wraith] culture.
 * Both Free Peoples Wraiths (dead men of Dunharrow) and Shadow Wraiths (Barrow-wights, etc.) qualify.
 * Not owner-gated — applies to all Wraiths.
 *
 * Test cards:
 * - Aragorn (1_89): Gondor companion, strength 8, vitality 4
 * - Oathbreaker (8_41): FP Gondor Wraith companion, strength 6, vitality 3, Enduring (+2 str per own wound)
 * - Barrow-wight Stalker (16_1): Shadow Wraith minion, strength 11, vitality 4, Enduring (+2 str per own wound)
 */
public class Card_93_008_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Aragorn (1_89): Gondor companion, strength 8, vitality 4
		put("aragorn", "1_89");
		// Oathbreaker (8_41): FP Gondor Wraith companion, strength 6, vitality 3, Enduring
		put("oathbreaker", "8_41");
		// Barrow-wight Stalker (16_1): Shadow Wraith minion, strength 11, vitality 4, Enduring
		put("stalker", "16_1");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_8", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ShadowWraithGainsBonusFromWoundsInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);
		// 2 wounds on Aragorn; stalker is unwounded
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// In skirmish: 2 wounds on Aragorn, 0 on Stalker = 2 wounds total in skirmish
		// Stalker: base 11 + 2 (93_8 bonus) = 13
		// No Enduring bonus since Stalker has no wounds
		assertEquals(13, scn.GetStrength(stalker));
	}

	@Test
	public void FPWraithAlsoGetsBonusInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(oathbreaker);
		scn.MoveMinionsToTable(stalker);
		// 1 wound on Stalker; Oathbreaker is unwounded
		scn.AddWoundsToChar(stalker, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(oathbreaker, stalker);

		// In skirmish: 1 wound on Stalker, 0 on Oathbreaker = 1 wound total
		// Oathbreaker: base 6 + 1 (93_8 bonus) = 7
		// No Enduring bonus since Oathbreaker has no wounds
		assertEquals(7, scn.GetStrength(oathbreaker));

		// Stalker also gets bonus: base 11 + 1 (93_8 for wound in skirmish)
		// Plus Enduring: +2 for own 1 wound = +2
		// Total: 11 + 1 + 2 = 14
		assertEquals(14, scn.GetStrength(stalker));
	}

	@Test
	public void EnduringSelfWoundsStackWithSkirmishBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);
		// 2 wounds on Stalker, 1 wound on Aragorn
		scn.AddWoundsToChar(stalker, 2);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// In skirmish: 2 wounds on Stalker + 1 wound on Aragorn = 3 wounds total
		// Stalker: base 11 + 3 (93_8 bonus) + 4 (Enduring: 2 per own wound x 2) = 18
		assertEquals(18, scn.GetStrength(stalker));
	}

	@Test
	public void NoBonusWithNoWoundsInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// No wounds on anyone = no bonus
		assertEquals(11, scn.GetStrength(stalker));
	}

	@Test
	public void NonWraithDoesNotGetBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stalker = scn.GetShadowCard("stalker");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(stalker);
		scn.AddWoundsToChar(stalker, 1);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, stalker);

		// Aragorn is a Man, not a Wraith — should not get the bonus
		// Aragorn: base 8, no bonus from 93_8
		assertEquals(8, scn.GetStrength(aragorn));
	}
}
