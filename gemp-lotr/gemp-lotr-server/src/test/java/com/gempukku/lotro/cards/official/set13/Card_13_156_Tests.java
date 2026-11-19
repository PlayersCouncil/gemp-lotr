package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_156_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "13_156");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, twilight 1; a skirmish ability used only on purpose

					// Followers, cheated onto their bearers or into the support area: their transfer texts answer
					// only a transfer to a companion
					put("worker", "15_135");    // Rohan Worker: [rohan] follower, no printed bonus
					put("army", "18_47");       // Elendil's Army: [gondor] follower, strength +2, no other text
					put("daddy", "13_144");     // Daddy Twofoot, Next-door Neighbor: [shire] follower, resistance +1
					put("radagast", "13_38");   // Radagast, Tender of Beasts: [gandalf] follower, resistance +2

					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
					put("swarms", "1_183");     // Goblin Swarms: [moria] support-area condition; answers only a [moria] Orc's win
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Sam, Bearer of Great Need
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Game Text: <b>Ring-bound</b>.<br>Sam is resistance +1 for each Hobbit you can spot.<br><b>Regroup:</b> Exert Sam and transfer a follower he is bearing to your support area to discard a minion from play.
		*/

		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");

		assertEquals("Sam", sam.getBlueprint().getTitle());
		assertEquals("Bearer of Great Need", sam.getBlueprint().getSubtitle());
		assertTrue(sam.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sam.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, sam.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, sam.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, sam.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertTrue(sam.getBlueprint().canStartWithRing());
		assertEquals(2, sam.getBlueprint().getTwilightCost());
		assertEquals(3, sam.getBlueprint().getStrength());
		assertEquals(4, sam.getBlueprint().getVitality());
		assertEquals(5, sam.getBlueprint().getResistance());
	}

	@Test
	public void SamIsResistancePlusOneForEachHobbitOfAnyCultureTypeOrSideHimselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var daddy = scn.GetFreepsCard("daddy");
		var bounder = scn.GetFreepsCard("bounder");
		var pippin = scn.GetFreepsCard("pippin");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(sam, smeagol);
		scn.MoveCardsToSupportArea(daddy);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, daddy);
		assertEquals(0, scn.GetBurdens());
		scn.MoveCardsToHand(bounder);
		scn.FreepsRefreshDecision();

		// Sam and Frodo; not Smeagol, a [shire] companion who is no Hobbit, nor the follower in the support area.
		assertEquals(7, scn.GetResistance(sam));    // 5 + 2
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(smeagol));

		// A Hobbit ally played.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(8, scn.GetResistance(sam));    // 5 + 3

		// A [gondor] Hobbit companion.
		scn.MoveCompanionsToTable(pippin);
		assertEquals(9, scn.GetResistance(sam));    // 5 + 4

		// The opponent's Hobbit, a minion.
		scn.FreepsPass();
		scn.MoveMinionsToTable(ted);
		assertEquals(10, scn.GetResistance(sam));   // 5 + 5

		// Only Sam is changed.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(6, scn.GetResistance(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsSamAndSendsAFollowerHeBearsToTheSupportAreaToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var worker = scn.GetFreepsCard("worker");
		var army = scn.GetFreepsCard("army");
		var daddy = scn.GetFreepsCard("daddy");
		var radagast = scn.GetFreepsCard("radagast");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var swarms = scn.GetShadowCard("swarms");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(sam, worker, army);
		scn.AttachCardsTo(merry, daddy);
		scn.MoveCardsToSupportArea(radagast);

		scn.StartGame();
		assertAttachedTo(worker, sam);
		assertAttachedTo(army, sam);
		assertAttachedTo(daddy, merry);
		assertInZone(Zone.SUPPORT, radagast);
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(swarms);
		scn.SkipToPhase(Phase.REGROUP);

		assertInPlay(troop, savage, swarms);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);

		// The followers Sam bears; not one Merry bears, nor one in the support area.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, army));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(daddy, radagast));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(worker);

		// Minions of two cultures and races; not the Shadow condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(swarms));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.SUPPORT, worker);
		assertAttachedTo(army, sam);
		assertAttachedTo(daddy, merry);
		assertInDiscard(troop);
		assertInPlay(savage, swarms);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayTheRegroupAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, worker);

		scn.StartGame();
		assertAttachedTo(worker, sam);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		// The one follower he bears goes unasked.
		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.SUPPORT, worker);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExhaustedSamCannotUseTheRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, worker);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(sam));
		assertAttachedTo(worker, sam);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));
		assertAttachedTo(worker, sam);
	}

	@Test
	public void WithNoFollowerOnSamTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var daddy = scn.GetFreepsCard("daddy");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(merry, daddy);
		scn.MoveCardsToSupportArea(radagast);

		scn.StartGame();
		assertAttachedTo(daddy, merry);
		assertInZone(Zone.SUPPORT, radagast);
		scn.SkipToPhase(Phase.REGROUP);

		// A follower borne by another companion, and one in the support area; Sam could exert.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sam));
		assertAttachedTo(daddy, merry);
	}
}
