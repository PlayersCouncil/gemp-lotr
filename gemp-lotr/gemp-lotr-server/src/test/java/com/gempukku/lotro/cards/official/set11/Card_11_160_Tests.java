package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("war", "11_160");
					put("war2", "11_160");

					put("hguard", "5_83");      // Household Guard: a [rohan] Man companion, resistance 6, valiant and nothing else
					put("soldier", "17_101");   // Soldier of Rohan: a [rohan] Man companion, resistance 5; strength +2 only while
					                            //   the Ring-bearer is assigned, and he never is here
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man companion, resistance 6, strength 5; his skirmish
					                            //   ability needs him mounted
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, no Man beside one other
					                            //   [rohan] companion; offers to exert to heal another [rohan] Man who exerts
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarNowCallsUsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: War Now Calls Us
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a [rohan] companion to make him or her strength +3 (and <b>damage +1</b> if he or she has resistance 5 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("war");

		assertEquals("War Now Calls Us", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheRohanCompanionChosenForStrengthPlus3AndDamagePlus1OnlyIfHisResistanceIs5OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var war2 = scn.GetFreepsCard("war2");
		var hguard = scn.GetFreepsCard("hguard");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(hguard, soldier, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(war, war2);

		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");

		assertInHand(war, war2);
		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(savage, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hguard, savage}, new PhysicalCardImpl[]{soldier, explorer});
		scn.FreepsResolveSkirmish(hguard);

		// Resistance 6 less 1 burden is exactly 5; the Soldier's 5 less 1 is 4.
		assertEquals(5, scn.GetResistance(hguard));
		assertEquals(4, scn.GetResistance(soldier));
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(6, scn.GetStrength(hguard));
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));

		assertTrue(scn.FreepsPlayAvailable(war));
		scn.FreepsPlayCard(war);
		assertEquals(1, scn.FreepsGetChoiceMin());
		// In this skirmish and in another; not a companion of another culture, nor a [rohan] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, guma));
		scn.FreepsChooseCard(soldier);

		// The Soldier, at resistance 4, is strength +3 and no more, though the Guard beside him has 5.
		assertInDiscard(war);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(6, scn.GetStrength(hguard));
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(war2);
		scn.FreepsChooseCard(hguard);

		// The Guard, at resistance exactly 5, is strength +3 and damage +1 as well.
		assertInDiscard(war2);
		assertEquals(1, scn.GetWoundsOn(hguard));
		assertEquals(9, scn.GetStrength(hguard));
		assertEquals(1, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanHobbitMayBeChosenAndTheBonusLastsOnlyTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var rider = scn.GetFreepsCard("rider");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, merry);
		scn.MoveCardsToHand(war);

		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");

		assertInHand(war);
		scn.StartGame();
		scn.MoveMinionsToTable(savage, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, savage}, new PhysicalCardImpl[]{merry, explorer});
		scn.FreepsResolveSkirmish(rider);

		assertEquals(6, scn.GetResistance(rider));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		scn.FreepsPlayCard(war);
		// Merry is a [rohan] companion who is no Man here (one other [rohan] companion and no Eowyn).
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, merry));
		scn.FreepsChooseCard(rider);
		// Merry offers to exert himself to heal the Rider, who has exerted: declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(war);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(8, scn.GetStrength(rider));
		assertEquals(1, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 8 against 5: the Rider wins, and his damage +1 adds a wound to the Savage's.
		assertEquals(2, scn.GetWoundsOn(savage));

		// Gone in the next skirmish, a skirmish phase of its own.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(0, scn.GetKeywordCount(rider, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithItsOnlyRohanCompanionExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");
		var hguard = scn.GetFreepsCard("hguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(hguard, aragorn);
		scn.MoveCardsToHand(war);

		var savage = scn.GetShadowCard("savage");

		assertInHand(war);
		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.  Aragorn, of another culture, could exert.
		scn.AddWoundsToChar(hguard, 2);
		assertEquals(2, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(war));
	}
}
