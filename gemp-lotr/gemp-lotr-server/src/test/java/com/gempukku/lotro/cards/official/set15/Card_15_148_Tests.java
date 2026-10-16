package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_148");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetVitalityScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flower", "15_148");
					// Hobbits of vitality 4, as the Ring-bearer is.  Sam, Son of Hamfast: his fellowship ability
					// is not used.  Merry, Friend to Sam: a skirmish ability only.
					put("sam", "1_311");
					put("merry", "1_302");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a Man of vitality 4
					put("sword", "1_299");          // Hobbit Sword: a hand weapon, strength +2, on a Hobbit; no other text
					put("ranger", "1_112");         // Ranger's Sword: a hand weapon on Aragorn, strength +2 and damage +1
					// Ted Sandyman: a [men] minion who is a Hobbit, vitality 4; maneuver and regroup abilities only
					put("ted", "18_75");
					// Long Battle Bow: a ranged weapon on a [men] minion, strength +1, and bearer is an archer.
					// Its when-played text is not used: it is cheated on.
					put("bow", "11_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LittleGoldenFlowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Little Golden Flower
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each Hobbit bearing a hand weapon is vitality +1.<br>Each Hobbit bearing a follower is resistance +1.<br>Each unwounded Hobbit is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Little Golden Flower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHobbitBearingAHandWeaponIsVitalityPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetVitalityScenario();

		var flower = scn.GetFreepsCard("flower");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(sam, merry, aragorn);
		scn.MoveCardsToSupportArea(flower);
		scn.AttachCardsTo(aragorn, ranger);
		scn.MoveCardsToHand(sword);

		var ted = scn.GetShadowCard("ted");
		var bow = scn.GetShadowCard("bow");

		scn.StartGame();
		// A Hobbit who bears a weapon that is no hand weapon: the only one there is sits on the other side.
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, bow);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(merry));
		assertEquals(4, scn.GetVitality(frodo));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(4, scn.GetVitality(ted));

		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry));
		scn.FreepsChooseCard(sam);
		assertAttachedTo(sword, sam);

		// The Hobbit with the hand weapon and nobody else: not the Hobbits without one, not the Man with
		// one, not the Hobbit with a bow.
		assertEquals(5, scn.GetVitality(sam));
		assertEquals(4, scn.GetVitality(merry));
		assertEquals(4, scn.GetVitality(frodo));
		assertEquals(4, scn.GetVitality(aragorn));
		assertEquals(4, scn.GetVitality(ted));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The extra point is real: with three wounds on him, Sam takes a fourth from Ted's bow and lives.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(2, scn.GetVitality(sam));
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(sam);
		assertEquals(4, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetVitality(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
