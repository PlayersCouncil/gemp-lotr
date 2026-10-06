package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("two", "103_16");
					put("two2", "103_16");
					put("noser", "5_28");       // Sméagol, Old Noser: [gollum], strength 3, vitality 4; a regroup ability only
					put("noser2", "5_28");      // a second copy of him
					put("slinker", "5_29");     // Sméagol, Slinker: [gollum], strength 3; a skirmish ability never used here
					put("poor", "6_45");        // Sméagol, Poor Creature: a skirmish ability only
					put("helps", "7_71");       // Sméagol, Always Helps: an assignment ability only
					put("hurried", "7_72");     // Sméagol, Hurried Guide: a skirmish ability only
					put("shire", "103_116");    // Smeagol, Once Like Us: the one Smeagol who is [shire], and costs 1; an assignment ability only
					put("arb", "9_30");         // Sméagol, Bearer of Great Secrets: the Smeagol who can bear the Ring; his trigger needs the fellowship to move
					put("arb2", "9_30");        // a second copy of him
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is not Smeagol
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("dont", "6_39");        // Don't Look at Them: a Free Peoples [gollum] condition in the support area
					put("follow", "5_23");      // Follow Sméagol: a Free Peoples condition Smeagol bears; a regroup ability only
					put("boat", "13_48");       // Fishing Boat: a Free Peoples possession Smeagol bears, vitality +1
					put("breath", "1_207");     // Black Breath: a Shadow condition, here on Smeagol; wounds may not be removed from bearer

					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: strength 5, vitality 4; a regroup ability only
					put("shelob", "10_23");     // Shelob, Her Ladyship: fierce; played with Gollum in play, keeps a companion from being assigned this turn
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSmeagolRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("two", "103_16");
					put("slinker", "5_29");     // Sméagol, Slinker
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SmeagolRB,     // Sméagol, Bearer of Great Secrets, as the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwoMindsofItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Two Minds of It
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship or Maneuver or Skirmish
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Restore Smeagol.  Then you may spot Smeagol (if he is not the Ring-bearer) and add a threat to replace him with another Smeagol from your discard pile (keep all tokens and borne cards).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("two");

		assertEquals("Two Minds of It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresSmeagolAndNoOtherHinderedCardAndThenAThreatBuysHisReplacementFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var sam = scn.GetFreepsCard("sam");
		var dont = scn.GetFreepsCard("dont");
		scn.MoveCompanionsToTable(noser, sam);
		scn.MoveCardsToSupportArea(dont);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);
		scn.HinderCard(noser, sam, dont);

		scn.StartGame();

		assertTrue(scn.IsHindered(noser));
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.HAND, two);
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.IsHindered(noser));
		// A hindered companion who is not Smeagol and a hindered [gollum] card of mine stay as they were.
		assertTrue(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(dont));
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertInZone(Zone.DISCARD, slinker);

		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, noser);
		assertFalse(scn.IsHindered(slinker));
		// The new Smeagol is not played: nothing more is added to the pool, and his "To play, add a burden" is not paid.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, two);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheManeuverPhaseSmeagolIsRestoredAndReplacedAndGollumTheMinionStaysHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var troop = scn.GetShadowCard("troop");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, gollum);
		// Gollum the minion is Smeagol's other self and no Smeagol.
		scn.HinderCard(noser, gollum);
		scn.SkipToPhase(Phase.MANEUVER);

		int twilight = scn.GetTwilight();
		assertTrue(scn.IsHindered(noser));
		assertTrue(scn.IsHindered(gollum));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertEquals(twilight + 2, scn.GetTwilight());
		assertFalse(scn.IsHindered(noser));
		assertTrue(scn.IsHindered(gollum));
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));

		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, noser);
		assertTrue(scn.IsHindered(gollum));
		assertInZone(Zone.DISCARD, two);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheReplacementLeavesSmeagolRestoredAndNothingElseChanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);
		scn.HinderCard(noser);

		scn.StartGame();

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));

		scn.FreepsChooseNo();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertFalse(scn.IsHindered(noser));
		assertInZone(Zone.DISCARD, slinker);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, two);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ReplacedInASkirmishTheNewSmeagolFightsItOnInHisPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(noser, runner);

		// Smeagol is not hindered: there is nothing to restore, and the offer follows all the same.
		assertFalse(scn.IsHindered(noser));
		assertTrue(scn.IsCharSkirmishingAgainst(noser, runner));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, noser);
		assertFalse(scn.IsCharSkirmishing(noser));
		assertTrue(scn.IsCharSkirmishingAgainst(slinker, runner));
		// The skirmish has not ended or begun again: the Shadow player is next to act in it.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();

		// Strength 3 against the Runner's 5: the Smeagol who came in takes the wound for losing.
		assertEquals(1, scn.GetWoundsOn(slinker));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ReplacedWhileAssignedToASkirmishNotYetBegunTheNewSmeagolIsAssignedInHisPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(noser, aragorn);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { noser, runner }
		);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));
		assertFalse(scn.IsCharSkirmishing(noser));
		assertTrue(scn.IsCharAssignedAgainst(noser, runner));
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();

		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, noser);
		assertFalse(scn.IsCharAssigned(noser));
		assertTrue(scn.IsCharAssignedAgainst(slinker, runner));
		assertFalse(scn.IsCharSkirmishing(slinker));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));

		// Aragorn's skirmish is finished, and the one left to fight is the new Smeagol's.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(slinker);

		assertTrue(scn.IsCharSkirmishingAgainst(slinker, runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheNewSmeagolKeepsTheWoundsAndTheCardsTheOldOneBore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var shire = scn.GetFreepsCard("shire");
		var slinker = scn.GetFreepsCard("slinker");
		var follow = scn.GetFreepsCard("follow");
		var boat = scn.GetFreepsCard("boat");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCompanionsToTable(shire);
		scn.AttachCardsTo(shire, follow, boat, breath);
		scn.AddWoundsToChar(shire, 2);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);
		// The Smeagol restored here is the [shire] one.
		scn.HinderCard(shire);

		scn.StartGame();

		assertTrue(scn.IsHindered(shire));
		assertEquals(2, scn.GetWoundsOn(shire));
		assertAttachedTo(follow, shire);
		assertAttachedTo(boat, shire);
		assertAttachedTo(breath, shire);

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(shire));
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, shire);
		assertEquals(2, scn.GetWoundsOn(slinker));
		// A condition and a possession of mine and a condition of the Shadow player's.
		assertAttachedTo(follow, slinker);
		assertAttachedTo(boat, slinker);
		assertAttachedTo(breath, slinker);
		assertInZone(Zone.ATTACHED, follow, boat, breath);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnEffectInForceOnTheOldSmeagolHoldsForTheNewOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(noser, aragorn);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(shelob);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		// Her Ladyship, played with Gollum in play: the companion spotted cannot be assigned until the end of the turn.
		scn.ShadowPlayCard(shelob);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(noser);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(noser));
		assertTrue(scn.FreepsCanAssign(aragorn));
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(two);
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();
		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, noser);

		// Aragorn's 8 beats Gollum's 5, and Shelob, who is fierce, brings a second assignment phase.
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.FreepsCanAssign(slinker));
		assertTrue(scn.FreepsCanAssign(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherSmeagolIsOneOfAnyCultureInMyOwnDiscardPileAndNotACopyOfTheOneInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var arb = scn.GetFreepsCard("arb");
		var arb2 = scn.GetFreepsCard("arb2");
		var slinker = scn.GetFreepsCard("slinker");
		var shire = scn.GetFreepsCard("shire");
		var poor = scn.GetFreepsCard("poor");
		var helps = scn.GetFreepsCard("helps");
		var noser = scn.GetFreepsCard("noser");
		var myGollum = scn.GetFreepsCard("gollum");
		var theirHurried = scn.GetShadowCard("hurried");
		// The Smeagol who can bear the Ring is in play as an ordinary companion: Frodo is the Ring-bearer.
		scn.MoveCompanionsToTable(arb);
		scn.MoveCardsToDiscard(slinker, shire, arb2, myGollum, theirHurried);
		scn.MoveCardsToHand(two, poor);
		scn.MoveCardsToDeadPile(helps);

		scn.StartGame();

		assertInZone(Zone.DECK, noser);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(two);
		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		// A [gollum] Smeagol and the [shire] one, both in my discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(slinker, shire));
		// Not a second copy of the Smeagol in play, nor Gollum the minion, in my discard pile; nor a Smeagol in my
		// hand, my draw deck or my dead pile, or in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arb2, myGollum, poor, noser, helps, theirHurried));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(shire);

		assertInZone(Zone.FREE_CHARACTERS, shire);
		assertInZone(Zone.DISCARD, arb);
		assertInZone(Zone.DISCARD, slinker);
		// The [shire] Smeagol costs 1 and adds a burden when played: he is not played. Only the event's 2 are in the pool.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyACopyOfTheSmeagolInPlayInTheDiscardPileNoReplacementIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var noser2 = scn.GetFreepsCard("noser2");
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(noser2);
		scn.MoveCardsToHand(two);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(two));
		scn.FreepsPlayCard(two);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertInZone(Zone.DISCARD, noser2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoSmeagolInTheDiscardPileNoReplacementIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var myGollum = scn.GetFreepsCard("gollum");
		scn.MoveCompanionsToTable(noser);
		// A Smeagol in hand, and in the discard pile only Gollum the minion.
		scn.MoveCardsToHand(two, slinker);
		scn.MoveCardsToDiscard(myGollum);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(two));
		scn.FreepsPlayCard(two);

		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertInZone(Zone.HAND, slinker);
		assertInZone(Zone.DISCARD, myGollum);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("Q-0066: with no other Smeagol in the discard pile the offer to add a threat is withheld; it has a cost that can be paid, and replacing is not playing")
	public void WithNoSmeagolInTheDiscardPileTheOfferIsStillMadeAndTheThreatBuysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var myGollum = scn.GetFreepsCard("gollum");
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToHand(two, slinker);
		scn.MoveCardsToDiscard(myGollum);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(two);

		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertInZone(Zone.HAND, slinker);
		assertInZone(Zone.DISCARD, myGollum);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SmeagolTheRingBearerIsRestoredAndCannotBeReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmeagolRingBearerScenario();

		var two = scn.GetFreepsCard("two");
		var slinker = scn.GetFreepsCard("slinker");
		var smeagol = scn.GetRingBearer();
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two);
		scn.HinderCard(smeagol);

		scn.StartGame();

		assertTrue(scn.IsHindered(smeagol));
		// One companion and no threat: a threat could be added.
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetThreatLimit());
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertFalse(scn.IsHindered(smeagol));
		assertFalse(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.DISCARD, slinker);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoSmeagolInPlayTheEventIsStillPlayedAndOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToDiscard(slinker, noser);
		scn.MoveCardsToHand(two);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, two);
		assertFalse(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.DISCARD, slinker, noser);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRoomForAThreatNoReplacementIsOfferedAndWithRoomForOneItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var two2 = scn.GetFreepsCard("two2");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);
		scn.MoveCardsToHand(two, two2);

		scn.StartGame();
		// Frodo and Smeagol: two companions, so two threats are as many as there can be.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());

		scn.FreepsPlayCard(two);

		assertFalse(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, noser);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.RemoveThreats(1);
		scn.FreepsPlayCard(two2);

		assertTrue(scn.FreepsDecisionAvailable("add a threat to replace Smeagol"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, slinker);
		assertInZone(Zone.DISCARD, noser);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableInTheArcheryAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(noser, aragorn);
		scn.MoveCardsToHand(two);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(two));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(two));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(two));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(two));

		// Aragorn's 8 loses to the Troop's 13 and the turn goes on to the regroup phase.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(two));
	}
}
