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

public class Card_15_151_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "15_151");
					// Robin Smallburrow, Shirriff Cock-Robin: a [shire] follower, cost 2; Aid (2), and a transfer
					// trigger only on a transfer
					put("robin", "18_111");
					// Hobbiton Brewer, Maker of Fine Ales: a [shire] follower of another title, cost 2; to play,
					// spot a Hobbit
					put("brewer", "15_146");
					// Rohan Worker: a [rohan] follower, cost 1; its texts answer a transfer and a won skirmish
					put("worker", "15_135");
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, cost 1, no text
					// Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; a fellowship ability
					// used only on purpose
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3; +2 only with a weapon
					put("merry", "1_303");
					// Hunting Herdsman: [men] Man, strength 8, vitality 2, hunter 1; +3 for each follower spotted,
					// and no follower is in play where he skirmishes
					put("herdsman", "15_83");
					// White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2, hunter 6, no other text
					put("butcher", "17_122");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1: no hunter
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3, vitality 4
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Pippin, The Short One
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 9
		 * Game Text: Each [shire] follower is twilight cost -1.
		 * 	<b>Skirmish:</b> If Pippin is skirmishing a hunter minion, exert the Ring-bearer to make Pippin strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("The Short One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(9, card.getBlueprint().getResistance());
	}

	@Test
	public void EachShireFollowerIsTwilightCostMinus1AndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var robin = scn.GetFreepsCard("robin");
		var brewer = scn.GetFreepsCard("brewer");
		var worker = scn.GetFreepsCard("worker");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(robin, brewer, worker, sword);

		scn.StartGame();

		// Without Pippin, each costs what is printed on it.
		assertEquals(2, scn.GetTwilightCost(robin));
		assertEquals(2, scn.GetTwilightCost(brewer));
		assertEquals(1, scn.GetTwilightCost(worker));
		assertEquals(1, scn.GetTwilightCost(sword));

		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();

		// The two [shire] followers are 1 less; a [rohan] follower and a [shire] possession are not.
		assertEquals(1, scn.GetTwilightCost(robin));
		assertEquals(1, scn.GetTwilightCost(brewer));
		assertEquals(1, scn.GetTwilightCost(worker));
		assertEquals(1, scn.GetTwilightCost(sword));

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(robin));
		scn.FreepsPlayCard(robin);
		assertInZone(Zone.SUPPORT, robin);
		// 2 printed - 1
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsTheRingBearerToMakePippinStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetShadowCard("herdsman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin, sam, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, herdsman}, new PhysicalCardImpl[]{merry, savage});
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		// The Ring-bearer exerts, and not Sam, the other Ring-bound companion.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 5 against 8 + 1 hunter: Pippin loses and takes a wound, but is not overwhelmed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pippin));
		scn.FreepsResolveSkirmish(merry);

		// In the next skirmish the +2 is gone.
		assertTrue(scn.IsCharSkirmishing(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedOnlyInPippinsOwnSkirmishAgainstAHunterMinionOfAnyCultureAndRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var herdsman = scn.GetShadowCard("herdsman");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCompanionsToTable(pippin, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(herdsman, butcher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, butcher}, new PhysicalCardImpl[]{merry, herdsman});
		scn.FreepsResolveSkirmish(merry);

		// Merry skirmishes a hunter minion, and Pippin is only assigned to one: the ability is not offered.
		assertTrue(scn.IsCharSkirmishing(merry));
		assertTrue(scn.IsCharAssignedAgainst(pippin, butcher));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(pippin);

		// Against the Butcher, an [uruk-hai] Uruk-hai, as against the Herdsman, a [men] Man.
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, butcher));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));
		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedAgainstAMinionWhoIsNoHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(pippin, savage);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage));
		// The Ring-bearer could exert.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithTheRingBearerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(herdsman);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(pippin, herdsman);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, herdsman));
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
