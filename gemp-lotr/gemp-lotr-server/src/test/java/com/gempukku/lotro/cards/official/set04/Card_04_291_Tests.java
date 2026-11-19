package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_291_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_291");

					put("hguard", "5_83");      // Household Guard: a [rohan] Man companion, strength 6, vitality 3; Valiant only
					put("hguard2", "5_83");
					// Léowyn: a [rohan] Man ally and villager; a fellowship ability that needs initiative, used only on purpose.
					put("leowyn", "7_239");
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions (or Éowyn) can be
					// spotted; his exert trigger answers a [rohan] Man exerting, which nothing here does.
					put("merry", "103_85");
					put("boromir", "0_65");     // Boromir, Steward's Heir: a [gondor] Man; keywords only

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2; Damage +1 only
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8;
					// each companion is resistance -1, and a skirmish ability used only on purpose.
					put("saruman", "17_116");
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
					put("sword", "4_291");
					put("leowyn", "7_239");     // Léowyn: a [rohan] Man ally, home 3T
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, vitality 2; Damage +1 only
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Sword of Rohan
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [rohan] Man.<br>While bearer is skirmishing an Uruk-hai, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Sword of Rohan", card.getBlueprint().getTitle());
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

		var sword = scn.GetFreepsCard("sword");
		var hguard = scn.GetFreepsCard("hguard");
		var leowyn = scn.GetFreepsCard("leowyn");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(sword);
		// Two [rohan] companions (the Guard and Merry): Merry is no Man.
		scn.MoveCompanionsToTable(hguard, merry, boromir);
		scn.MoveAlliesToTable(leowyn);

		scn.StartGame();
		assertInHand(sword);
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		// A [rohan] Man companion and a [rohan] Man ally; not a [rohan] Hobbit, nor a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, leowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(leowyn);

		assertAttachedTo(sword, leowyn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnlyWhileSkirmishingAnUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var hguard = scn.GetFreepsCard("hguard");
		var hguard2 = scn.GetFreepsCard("hguard2");
		scn.MoveCompanionsToTable(hguard, hguard2);
		scn.AttachCardsTo(hguard, sword);

		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		assertAttachedTo(sword, hguard);
		scn.MoveMinionsToTable(savage, sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hguard, savage}, new PhysicalCardImpl[]{hguard2, sentry});

		// The other Guard's skirmish first: the bearer is assigned to the Savage, his skirmish not begun, and the
		// other Guard skirmishes an Uruk-hai without the Sword.
		scn.FreepsResolveSkirmish(hguard2);
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hguard2, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(hguard);
		assertEquals(1, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hguard2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsNotDamagePlus1SkirmishingAMinionWhoIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var hguard = scn.GetFreepsCard("hguard");
		scn.MoveCompanionsToTable(hguard);
		scn.AttachCardsTo(hguard, sword);

		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		assertAttachedTo(sword, hguard);
		scn.MoveMinionsToTable(saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard, saruman);
		scn.FreepsResolveSkirmish(hguard);

		assertTrue(scn.IsCharSkirmishingAgainst(hguard, saruman));
		assertEquals(0, scn.GetKeywordCount(hguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByAnAllySkirmishingAnUrukHaiHeIsDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var sword = scn.GetFreepsCard("sword");
		var leowyn = scn.GetFreepsCard("leowyn");
		scn.MoveAlliesToTable(leowyn);
		scn.AttachCardsTo(leowyn, sword);

		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		assertAttachedTo(sword, leowyn);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(sentry);
		// The move takes the fellowship to site 3, Léowyn's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(leowyn, sentry);
		scn.FreepsResolveSkirmish(leowyn);

		assertTrue(scn.IsCharSkirmishingAgainst(leowyn, sentry));
		assertEquals(1, scn.GetKeywordCount(leowyn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
