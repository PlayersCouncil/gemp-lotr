package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_117_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "103_117");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit. "Response: If Frodo dies, make Sam the Ring-bearer"
					// Bilbo, Melancholy Hobbit: a Ring-bound Hobbit who is never the Ring-bearer; an offer at the start of
					// a skirmish involving him and nothing else
					put("bilbo", "12_119");
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit; a skirmish ability never used here
					put("smeagol", "103_116");  // Smeagol, Once Like Us: Ring-bound and [shire], with no race; an assignment ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability never used here
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally; a maneuver ability never used here
					put("sword", "1_299");      // Hobbit Sword: a hand weapon; bearer must be a Hobbit, cost 1, no text
					put("march1", "103_22");    // Ethereal March: while you can spot 2 [gondor] Wraiths, the threat limit is +2
					put("march2", "103_22");
					put("wraith1", "103_32");   // Tormented Revenant: a [gondor] Wraith companion; stronger only against minions skirmishing it
					put("wraith2", "103_32");

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
					put("marksman", "1_176");   // Goblin Marksman: Archer and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Sting, Sharp Elven-blade
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Resistance: 1
		 * Game Text: Bearer must be a Ring-bound Hobbit.
		 * 	Skirmish: If you cannot spot more than 4 companions, add a threat to make bearer
		 * 	strength +1 for each threat you can spot (limit +6).
		 * 	Response: If The One Ring is transferred, play this on the new Ring-bearer.
		 * 	You may use this ability from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Sharp Elven-blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedForOneOnARingBoundHobbitWhoGainsAStrengthAndAResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry, smeagol);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.FreepsPlayAvailable(sting));

		scn.FreepsPlayCard(sting);
		// Frodo and Sam are Ring-bound Hobbits. Merry is a Hobbit and unbound; Smeagol is Ring-bound, and
		// [shire], and no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, smeagol));

		scn.FreepsChooseCard(sam);
		assertAttachedTo(sting, sam);
		assertEquals(1, scn.GetTwilight());
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(6, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsAThreatAndMakesBearerStrongerByOneForEachThreatThenThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Frodo, Sam, Aragorn and Gimli: exactly 4 companions, with an ally beside them.
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(0, scn.GetThreats());
		// 3 + 1 for Sting
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(sting));
		assertEquals("false", scn.FreepsActionNoEffect(sting));

		scn.FreepsUseCardAction(sting);
		// The threat just added is counted.
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetStrength(sam));

		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		assertEquals(2, scn.GetThreats());
		// 4 + 1 + 2
		assertEquals(7, scn.GetStrength(sam));

		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		assertEquals(3, scn.GetThreats());
		// 4 + 1 + 2 + 3: +6 in all
		assertEquals(10, scn.GetStrength(sam));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OnceSixHasBeenGivenInASkirmishAFurtherUseAddsItsThreatAndNoStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertEquals(0, scn.GetThreats());
		assertEquals(4, scn.GetStrength(sam));

		scn.FreepsUseCardAction(sting);
		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		scn.ShadowPass();
		// +1, +2 and +3: the limit of +6 is reached, and with 3 threats on 4 companions a fourth can be added.
		assertEquals(3, scn.GetThreats());
		assertEquals(10, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(sting));
		assertEquals("limit", scn.FreepsActionNoEffect(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(4, scn.GetThreats());
		assertEquals(10, scn.GetStrength(sam));

		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(sting));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AUseThatWouldPassSixInASkirmishGivesOnlyWhatIsLeftOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);
		scn.AddThreats(1);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertEquals(1, scn.GetThreats());
		assertEquals(4, scn.GetStrength(sam));

		scn.FreepsUseCardAction(sting);
		// 2 threats: +2
		assertEquals(6, scn.GetStrength(sam));
		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		// 3 threats: +3, which makes +5
		assertEquals(9, scn.GetStrength(sam));
		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		// 4 threats would be +4; only +1 is left of the +6
		assertEquals(4, scn.GetThreats());
		assertEquals(10, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASingleUseGivesNoMoreThanSixWhateverTheThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var wraith1 = scn.GetFreepsCard("wraith1");
		var wraith2 = scn.GetFreepsCard("wraith2");
		var march1 = scn.GetFreepsCard("march1");
		var march2 = scn.GetFreepsCard("march2");
		scn.MoveCompanionsToTable(wraith1, wraith2);
		scn.MoveCardsToSupportArea(march1, march2);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);
		scn.AddThreats(6);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(wraith1, runner);

		// 3 companions, + 2 for each Ethereal March
		assertEquals(7, scn.GetThreatLimit());
		assertEquals(6, scn.GetThreats());
		// 3, + 1 for The One Ring, + 1 for Sting
		assertEquals(5, scn.GetStrength(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		// 7 threats, and +6
		assertEquals(7, scn.GetThreats());
		assertEquals(11, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileAFifthCompanionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Frodo, Sam, Aragorn, Gimli and Merry
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsActionAvailable(sting));

		// A hindered companion cannot be spotted: 4 companions, a hindered fifth and an ally.
		scn.HinderCard(merry);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sting));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);
		// 2 companions and 2 threats
		scn.AddThreats(2);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertFalse(scn.FreepsActionAvailable(sting));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(sting));
		assertEquals(4, scn.GetStrength(sam));

		scn.FreepsUseCardAction(sting);
		assertEquals(2, scn.GetThreats());
		assertEquals(6, scn.GetStrength(sam));

		scn.ShadowPass();
		assertFalse(scn.FreepsActionAvailable(sting));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BonusLastsForTheSkirmishItIsGivenInAndTheLimitIsNewInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{sam, runner});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish: Sting is used for Sam, who is waiting for his own.
		assertEquals(4, scn.GetStrength(sam));
		scn.FreepsUseCardAction(sting);
		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		scn.ShadowPass();
		scn.FreepsUseCardAction(sting);
		// +1, +2 and +3: the whole +6
		assertEquals(3, scn.GetThreats());
		assertEquals(10, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn 8 against 5: the Savage takes a wound and lives.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);

		// Sam's own skirmish: the bonus has gone, the threats have not.
		assertTrue(scn.IsCharSkirmishing(sam));
		assertEquals(3, scn.GetThreats());
		assertEquals(4, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		// 4 threats: +4, though +6 was given in the last skirmish
		assertEquals(4, scn.GetThreats());
		assertEquals(8, scn.GetStrength(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WhenFrodoDiesBearingItTheResponsePlaysItFromTheDiscardPileOnSamTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		// Bilbo is a Ring-bound Hobbit Sting could be played on, and not the one its response names.
		scn.MoveCompanionsToTable(sam, bilbo);
		scn.AttachCardsTo(frodo, sting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		// Vitality 4: three wounds leave Frodo one from death.
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		assertAttachedTo(sting, frodo);
		// 3 companions + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());

		// The Marksman's one archery wound
		scn.FreepsChooseCard(frodo);

		assertInZone(Zone.DEAD, frodo);
		// Sting went to the discard pile with its bearer.
		assertInZone(Zone.DISCARD, sting);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));

		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertInZone(Zone.DISCARD, sting);
		// 3, + 1 for The One Ring
		assertEquals(4, scn.GetStrength(sam));
		int resistance = scn.GetResistance(sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));

		scn.FreepsChooseOptionalTrigger(sting);

		// On Sam without a choice: Bilbo was not a candidate.
		assertAttachedTo(sting, sam);
		assertNotAttachedTo(sting, bilbo);
		// 5 + 1 to play Sting
		assertEquals(6, scn.GetTwilight());
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(resistance + 1, scn.GetResistance(sam));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void WhenFrodoDiesWithStingOnBilboTheResponseMovesItToSamTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, bilbo, merry);
		scn.AttachCardsTo(bilbo, sting);

		scn.StartGame();

		// With Frodo dead, Sam the Ring-bearer and Bilbo bearing Sting, no card is a Ring-bound Hobbit besides, so
		// Merry is made one: a Hobbit Sting could go to, and not the one its response names.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, merry, null, Keyword.RING_BOUND));
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		assertTrue(scn.HasKeyword(merry, Keyword.RING_BOUND));
		// 4 companions + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());

		// The Marksman's one archery wound
		scn.FreepsChooseCard(frodo);

		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));

		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertAttachedTo(sting, bilbo);
		// 3 + 1 for Sting
		assertEquals(4, scn.GetStrength(bilbo));
		// 3 + 1 for The One Ring
		assertEquals(4, scn.GetStrength(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));

		scn.FreepsChooseOptionalTrigger(sting);

		// On Sam without a choice: Merry was not a candidate.
		assertAttachedTo(sting, sam);
		assertNotAttachedTo(sting, bilbo);
		assertNotAttachedTo(sting, merry);
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(5, scn.GetStrength(sam));
		// A card moved from one bearer to another by an effect is transferred, and costs nothing.
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void OncePlayedOnTheNewRingBearerTheResponseIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(sting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsChooseOptionalTrigger(sting);

		// It is on Sam and there is nothing more for it to do.
		assertAttachedTo(sting, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseFromTheDiscardPileMayBeDeclinedAndIsNotOfferedToTheOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sting = scn.GetFreepsCard("sting");
		var theirSting = scn.GetShadowCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(sting, theirSting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		// 2 companions + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(ring, sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));

		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, sting);
		assertInZone(Zone.DISCARD, theirSting);
		assertNotAttachedTo(sting, sam);
		assertNotAttachedTo(theirSting, sam);
		assertEquals(4, scn.GetTwilight());
		assertEquals(4, scn.GetStrength(sam));
		// The Shadow player's copy, in his own discard pile, made him no offer on the way.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseIsNotOfferedWhileStingIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		// The Ring has been transferred, and the game has gone on without a word from Sting.
		assertEquals(sam, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.HAND, sting);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseIsNotOfferedWhileStingIsAlreadyOnTheNewRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		assertEquals(sam, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertAttachedTo(sting, sam);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseFromTheDiscardPileIsNotOfferedWhenTheNewRingBearerAlreadyBearsAHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToDiscard(sting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		// Sam has the Ring and a Hobbit Sword: a second hand weapon cannot be played on him.
		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(sword, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.DISCARD, sting);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheResponseFromPlayIsNotOfferedWhenTheNewRingBearerAlreadyBearsAHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, bilbo);
		scn.AttachCardsTo(sam, sword);
		scn.AttachCardsTo(bilbo, sting);

		scn.StartGame();

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveMinionsToTable(marksman);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToArcheryWounds();

		scn.FreepsChooseCard(frodo);
		assertInZone(Zone.DEAD, frodo);
		scn.FreepsChooseOptionalTrigger(sam);

		// Sam has the Ring and a Hobbit Sword: Sting cannot move to him from Bilbo.
		assertEquals(sam, scn.GetRingBearer());
		assertAttachedTo(sword, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TransferringAnotherItemIsNotTheOneRingBeingTransferred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, bilbo, merry);
		scn.AttachCardsTo(bilbo, sword);
		scn.MoveCardsToDiscard(sting);

		scn.StartGame();

		assertAttachedTo(sword, bilbo);
		assertTrue(scn.FreepsTransferAvailable(sword));

		scn.FreepsTransferCard(sword);
		// Frodo, Sam and Merry could each bear the sword
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, merry));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(sword, sam);
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertInZone(Zone.DISCARD, sting);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The same for a Sting in play. It is on Bilbo now, and the sword goes on from Sam to Merry: Frodo, the
		// Ring-bearer, bears no hand weapon, so Sting could move to him if its response were offered.
		scn.AttachCardsTo(bilbo, sting);
		scn.FreepsRefreshDecision();
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.FreepsTransferAvailable(sword));

		scn.FreepsTransferCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(merry);

		assertAttachedTo(sword, merry);
		assertEquals(frodo, scn.GetRingBearer());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
