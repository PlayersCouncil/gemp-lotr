package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "15_136");

					put("hguard", "5_83");      // Household Guard: a [rohan] Man companion, strength 6, vitality 3; Valiant only
					put("hguard2", "5_83");
					// Léowyn: a [rohan] Man ally and villager; a fellowship ability that needs initiative, used only on purpose.
					put("leowyn", "7_239");
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Éowyn) can be
					// spotted; his exert trigger answers a [rohan] Man exerting, which nothing here does.
					put("merry", "103_85");
					put("boromir", "0_65");     // Boromir, Steward's Heir: a [gondor] Man; keywords only

					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2; Hunter 6 is its whole text
					// Hunting Herdsman: [men] Man, strength 8, vitality 2, hunter 1; +3 for each follower, and there is none
					put("herdsman", "15_83");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, no hunter; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers sites: Léowyn's home is site 3 of that block, where she may be assigned.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "15_136");
					put("leowyn", "7_239");     // Léowyn: a [rohan] Man ally, home 3T
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2; Hunter 6 is its whole text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rohirrim Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [rohan] Man.<br>While bearer is skirmishing a hunter minion, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Rohirrim Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnlyOnARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var hguard = scn.GetFreepsCard("hguard");
		var leowyn = scn.GetFreepsCard("leowyn");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(axe);
		// Two [rohan] companions (the Guard and Merry): Merry is no Man.
		scn.MoveCompanionsToTable(hguard, merry, boromir);
		scn.MoveAlliesToTable(leowyn);

		scn.StartGame();
		assertInHand(axe);
		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);

		// A [rohan] Man companion and a [rohan] Man ally; not a [rohan] Hobbit, nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, leowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(leowyn);

		assertAttachedTo(axe, leowyn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnlyWhileSkirmishingAHunterMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var hguard = scn.GetFreepsCard("hguard");
		var hguard2 = scn.GetFreepsCard("hguard2");
		scn.MoveCompanionsToTable(hguard, hguard2);
		scn.AttachCardsTo(hguard, axe);

		var butcher = scn.GetShadowCard("butcher");
		var herdsman = scn.GetShadowCard("herdsman");

		scn.StartGame();
		assertAttachedTo(axe, hguard);
		scn.MoveMinionsToTable(butcher, herdsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hguard, butcher}, new PhysicalCardImpl[]{hguard2, herdsman});

		// Hunter minions of two cultures and races.  The other Guard's skirmish first: the bearer is assigned to the
		// Butcher, his skirmish not begun, and the other Guard skirmishes a hunter minion without the Axe.
		scn.FreepsResolveSkirmish(hguard2);
		assertTrue(scn.IsCharSkirmishingAgainst(hguard2, herdsman));
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hguard2, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		// 9 against 6: the other Guard loses and lives through one wound.
		scn.FreepsResolveSkirmish(hguard);
		assertTrue(scn.IsCharSkirmishingAgainst(hguard, butcher));
		assertEquals(1, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hguard2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1SkirmishingAHunterMinionOfAnotherCultureAndRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var hguard = scn.GetFreepsCard("hguard");
		scn.MoveCompanionsToTable(hguard);
		scn.AttachCardsTo(hguard, axe);

		var herdsman = scn.GetShadowCard("herdsman");

		scn.StartGame();
		assertAttachedTo(axe, hguard);
		scn.MoveMinionsToTable(herdsman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard, herdsman);

		// Not yet skirmishing: no damage bonus.
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		scn.FreepsResolveSkirmish(hguard);

		// The Herdsman, a [men] Man (the Butcher of the other test is an [uruk-hai] Uruk-hai).
		assertTrue(scn.IsCharSkirmishingAgainst(hguard, herdsman));
		assertEquals(1, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsNotDamagePlus1SkirmishingAMinionWhoIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var hguard = scn.GetFreepsCard("hguard");
		scn.MoveCompanionsToTable(hguard);
		scn.AttachCardsTo(hguard, axe);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(axe, hguard);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard, savage);
		scn.FreepsResolveSkirmish(hguard);

		assertTrue(scn.IsCharSkirmishingAgainst(hguard, savage));
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByAnAllySkirmishingAHunterMinionHeIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var axe = scn.GetFreepsCard("axe");
		var leowyn = scn.GetFreepsCard("leowyn");
		scn.MoveAlliesToTable(leowyn);
		scn.AttachCardsTo(leowyn, axe);

		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		assertAttachedTo(axe, leowyn);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(butcher);
		// The move takes the fellowship to site 3, Léowyn's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(leowyn, butcher);
		scn.FreepsResolveSkirmish(leowyn);

		assertTrue(scn.IsCharSkirmishingAgainst(leowyn, butcher));
		assertEquals(1, scn.GetKeywordCount(leowyn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
