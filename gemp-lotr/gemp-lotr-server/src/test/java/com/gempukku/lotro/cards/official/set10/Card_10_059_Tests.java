package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gorbag", "10_59");

					// Cirith Ungol Warrior: another [wraith] Orc, strength 9.  Its Shadow ability is never used here.
					put("warrior", "10_56");
					// Ulaire Attea, The Easterling: a [wraith] minion who is no Orc (a Nazgul), strength 12.  His
					// "when you play" is never set off: he is cheated into play.
					put("attea", "2_82");
					// Goblin Patrol Troop: an Orc who is not [wraith] ([moria]), strength 13, no text.
					put("troop", "1_177");
					// Goblin Marksman: a [moria] Orc archer, strength 7, no other text.  His archery wound is how the
					// Ring-bearer is wounded by legal play.
					put("marksman", "1_176");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  A maneuver ability, never used here.
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: Ring-bound as Frodo is, and not the Ring-bearer.  Vitality 4.  His
					// abilities are never used here.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: vitality 4, so he can hold 3 wounds.  A fellowship ability, never used here.
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorbagStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gorbag, Covetous Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: For each wound on the Ring-bearer, each [wraith] Orc is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gorbag");

		assertEquals("Gorbag", card.getBlueprint().getTitle());
		assertEquals("Covetous Captain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachWraithOrcIsStrengthPlusOneForEachWoundOnTheRingBearerAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var gorbag = scn.GetShadowCard("gorbag");
		var warrior = scn.GetShadowCard("warrior");
		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, warrior, attea, troop);

		// Wounds on another companion, wounds on Sam (Ring-bound, but not the Ring-bearer) and burdens on the
		// Ring-bearer are not wounds on the Ring-bearer.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.AddBurdens(2);

		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertEquals(11, scn.GetStrength(gorbag));
		assertEquals(9, scn.GetStrength(warrior));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(13, scn.GetStrength(troop));

		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Gorbag is himself a [wraith] Orc.
		assertEquals(12, scn.GetStrength(gorbag));
		assertEquals(10, scn.GetStrength(warrior));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(13, scn.GetStrength(troop));

		scn.AddWoundsToChar(frodo, 2);
		// 3 wounds, all Frodo (vitality 4) can hold: 11 + 3 and 9 + 3.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(14, scn.GetStrength(gorbag));
		assertEquals(12, scn.GetStrength(warrior));
		// Neither the Nazgul nor the [moria] Orc gains anything.
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void AnArcheryWoundPlacedOnTheRingBearerMakesGorbagStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gorbag = scn.GetShadowCard("gorbag");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, marksman);
		scn.SkipToArcheryWounds();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(11, scn.GetStrength(gorbag));
		assertEquals(7, scn.GetStrength(marksman));

		// The minion archery total of 1 is the Marksman's; the Free Peoples player places it on Frodo.
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(12, scn.GetStrength(gorbag));
		// The Marksman is a [moria] Orc.
		assertEquals(7, scn.GetStrength(marksman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
