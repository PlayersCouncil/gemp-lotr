package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attack", "18_101");
					put("guard", "5_83");       // Household Guard: [rohan] Man; Valiant is its whole text
					put("spear", "4_288");      // Rider's Spear: [rohan] possession (hand weapon), "Bearer must be a [rohan] Man"
					// Rider's Mount: [rohan] possession (mount); its start-of-skirmish exertion is never reached.
					put("mount", "4_287");
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession that lies in the support area and
					// nobody bears; its maneuver ability is never used.
					put("signal", "103_86");
					put("theoden", "4_292");    // Theoden, Son of Thengel: [rohan] Man; a regroup ability used only on purpose
					put("herugrim", "54_280");  // Herugrim: a [rohan] ARTIFACT (hand weapon), "Bearer must be Theoden"
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("armor", "1_92");       // Armor: a [gondor] possession, "Bearer must be a Man"
					// Cast from the Hall: a [rohan] condition in the support area; it answers only a [rohan] possession
					// played from hand.
					put("cast", "18_94");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("sting", "1_313");      // Sting: a Free Peoples possession, "Bearer must be Frodo"; never used
					// Black Breath: a [wraith] condition a companion bears; burdens and wounds may not be removed from
					// its bearer, and nothing here removes any.
					put("breath1", "1_207");
					put("breath2", "1_207");
					put("poison1", "8_28");     // Spider Poison: a Shadow possession that a companion bears
					put("poison2", "8_28");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text, vitality 3
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play trigger is never reached
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PreciseAttackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Precise Attack
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Discard a [rohan] possession from play to choose one: wound an unwounded minion; exert a minion; or
		 * 	discard a Shadow card the Ring-bearer is bearing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attack");

		assertEquals("Precise Attack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheCostIsARohanPossessionInPlayBorneOrNotAndNoArtifactConditionOrPossessionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var signal = scn.GetFreepsCard("signal");
		var theoden = scn.GetFreepsCard("theoden");
		var herugrim = scn.GetFreepsCard("herugrim");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var cast = scn.GetFreepsCard("cast");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(guard, theoden, boromir);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(theoden, herugrim);
		scn.AttachCardsTo(boromir, armor);
		scn.MoveCardsToSupportArea(signal, cast);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(armor, boromir);
		assertInZone(Zone.SUPPORT, signal, cast);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsPlayAvailable(attack));
		scn.FreepsPlayCard(attack);
		// The Mount (borne) and the Signal-fire (in the support area) are [rohan] possessions in play.  Herugrim is
		// a [rohan] artifact, the Armor a [gondor] possession, Cast from the Hall a [rohan] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mount, signal));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herugrim, armor, cast));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(signal);

		assertInDiscard(signal);
		assertAttachedTo(mount, guard);
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(armor, boromir);
		assertInZone(Zone.SUPPORT, cast);

		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsChoose("Wound an unwounded minion");
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(attack);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoRohanPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var theoden = scn.GetFreepsCard("theoden");
		var herugrim = scn.GetFreepsCard("herugrim");
		var cast = scn.GetFreepsCard("cast");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(guard, theoden);
		scn.AttachCardsTo(theoden, herugrim);
		scn.MoveCardsToSupportArea(cast);
		// A [rohan] possession in hand is not in play.
		scn.MoveCardsToHand(attack, mount);
		assertAttachedTo(herugrim, theoden);
		assertInZone(Zone.SUPPORT, cast);
		assertInHand(mount);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(attack));
	}

	@Test
	public void WoundBranchWoundsAnUnwoundedMinionOfAnyKindAndOnlyAnUnwoundedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage1, savage2);
		scn.AddWoundsToChar(savage2, 1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		// The Mount is the one [rohan] possession in play, and goes with nothing asked.
		scn.FreepsPlayCard(attack);
		assertInDiscard(mount);

		// Nothing Shadow is on the Ring-bearer: that branch cannot be carried out, and is not offered.
		assertTrue(scn.FreepsChoiceAvailable("Wound an unwounded minion"));
		assertTrue(scn.FreepsChoiceAvailable("Exert a minion"));
		assertFalse(scn.FreepsChoiceAvailable("Discard a Shadow card"));
		scn.FreepsChoose("Wound an unwounded minion");

		// An unwounded [moria] Orc and an unwounded [isengard] Uruk-hai; not the wounded minion, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2, guard, frodo));
		scn.FreepsChooseCard(runner);

		// A wound, not an exertion: the Runner, with vitality 1, is killed.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ExertBranchExertsAMinionWoundedOrNotThatCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, savage1, savage2);
		scn.AddWoundsToChar(savage1, 1);
		scn.AddWoundsToChar(savage2, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsPlayCard(attack);
		assertInDiscard(mount);
		scn.FreepsChoose("Exert a minion");

		// A wounded Uruk-hai and an unwounded Orc may exert; the exhausted Savage (2 wounds of 3) and the Runner
		// (vitality 1) may not, nor may a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2, runner, guard));
		scn.FreepsChooseCard(savage1);

		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void DiscardBranchDiscardsAShadowCardOnTheRingBearerAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var sam = scn.GetFreepsCard("sam");
		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();
		var breath1 = scn.GetShadowCard("breath1");
		var breath2 = scn.GetShadowCard("breath2");
		var poison = scn.GetShadowCard("poison1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, sam);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);
		assertAttachedTo(sting, frodo);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(frodo, breath1, poison);
		scn.AttachCardsTo(sam, breath2);
		assertAttachedTo(breath1, frodo);
		assertAttachedTo(poison, frodo);
		assertAttachedTo(breath2, sam);
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsPlayCard(attack);
		assertInDiscard(mount);
		scn.FreepsChoose("Discard a Shadow card");

		// A Shadow condition and a Shadow possession on Frodo; not the Shadow card Sam bears, nor Frodo's own Sting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(breath1, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(breath2, sting));
		scn.FreepsChooseCard(poison);

		assertInDiscard(poison);
		assertAttachedTo(breath1, frodo);
		assertAttachedTo(breath2, sam);
		assertAttachedTo(sting, frodo);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOnlyAWoundedMinionAndNothingOnTheRingBearerTheExertionIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsPlayCard(attack);
		assertInDiscard(mount);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOnlyAnUnwoundedMinionWhoCannotExertTheWoundIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		// The Runner is unwounded and, with vitality 1, exhausted.
		assertEquals(0, scn.GetWoundsOn(runner));
		scn.FreepsPlayCard(attack);
		assertInDiscard(mount);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(runner);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOnlyAnExhaustedWoundedMinionTheShadowCardOnTheRingBearerIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var frodo = scn.GetRingBearer();
		var breath = scn.GetShadowCard("breath1");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 2);
		scn.AttachCardsTo(frodo, breath);
		assertAttachedTo(breath, frodo);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsPlayCard(attack);
		assertInDiscard(mount);

		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(breath);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNothingForAnyBranchItIsPlayedAndThePossessionDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(attack);
		assertAttachedTo(mount, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		// The one minion is wounded and exhausted, and the Ring-bearer bears nothing Shadow.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(attack));
		scn.FreepsPlayCard(attack);

		assertInDiscard(mount, attack);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AHinderedShadowCardTheRingBearerBearsMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var card = scn.GetFreepsCard("attack");
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, spear);
		scn.MoveCardsToHand(card);
		assertAttachedTo(spear, guard);

		var hindered = scn.GetShadowCard("poison1");
		var unhindered = scn.GetShadowCard("poison2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(frodo, hindered, unhindered);
		assertAttachedTo(hindered, frodo);
		assertAttachedTo(unhindered, frodo);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098:
		// "a Shadow card" may be a hindered one).
		scn.HinderCard(hindered);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.IsHindered(hindered));
		assertTrue(scn.FreepsPlayAvailable(card));

		// The Spear is the one [rohan] possession in play.
		scn.FreepsPlayCard(card);
		assertInZone(Zone.DISCARD, spear);
		scn.FreepsChoose("Discard a Shadow card");

		assertTrue(scn.FreepsHasCardChoiceAvailable(hindered, unhindered));
		scn.FreepsChooseCard(hindered);

		assertInZone(Zone.DISCARD, hindered);
		assertAttachedTo(unhindered, frodo);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
