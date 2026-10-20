package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a Ring-bound Hobbit who does not bear the Ring; strength 3, vitality 4.
					// His fellowship ability and his response to Frodo's death are never used here.
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					// Hobbit Intuition: [shire] Skirmish event, cost 1.  Away from sites 1 to 4, "make a Hobbit strength +3."
					put("intuition", "1_296");

					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Regroup: Exert Gollum twice to wound the
					// Ring-bearer."  His other abilities exert the Ring-bearer or remove a burden to add twilight.
					put("gollum", "9_28");
					// Little Snuffler: [gollum] Shadow event, cost 2.  In region 1, "spot Gollum to wound a companion."
					put("snuffler", "13_52");
					// Gate Picket: [sauron] Orc, strength 11, vitality 3.  "Maneuver: Exert this minion twice to make
					// the Free Peoples player name 3 cultures. Wound each companion and ally not of a named culture."
					put("picket", "6_101");
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1; Archer and nothing else
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2.  "Skirmish: Exert this minion to wound a
					// character he is skirmishing."
					put("soldier", "1_271");
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 and nothing else
					// Ulaire Enquea, Ringwraith in Twilight: strength 11, vitality 4.  "Each time Ulaire Enquea wins a
					// skirmish, you may exert him to wound the Ring-bearer once (or twice if you can spot 5 burdens)."
					put("enquea", "2_83");
					// Enduring Evil: [sauron] Skirmish event, cost 0.  "Spot X burdens to make a character skirmishing
					// a [sauron] Orc strength -X."
					put("evil", "1_246");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Isildur, Bearer of Heirlooms bears the Ring: a Man, strength 7, vitality 3, resistance 6.  "While Isildur is the
	// Ring-bearer, at the start of each skirmish involving him, add 2 burdens or exert 3 companions."
	protected VirtualTableScenario GetIsildurScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Southron Fanatic: [raider] Man, strength 8, vitality 2.  "Each time a Man is assigned to
					// skirmish this minion, wound that Man": a wound in an assignment phase.
					put("fanatic", "10_49");
				}},
				VirtualTableScenario.FellowshipSites,
				"9_33",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RulingRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The One Ring, The Ruling Ring
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Strength: +1
		 * Game Text: <b>Response:</b> If bearer is about to take a wound in a skirmish, he wears The One Ring until the regroup phase.
		 * While wearing The One Ring, each time the Ring-bearer is about to take a wound during a skirmish, add a burden instead.
		 */

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Ruling Ring", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void RulingRingRespondsOnlyDuringSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var gollum = scn.GetShadowCard("gollum");
		var picket = scn.GetShadowCard("picket");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var snuffler = scn.GetShadowCard("snuffler");
		scn.MoveCardsToHand(snuffler);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, picket, marksman, soldier);
		// Little Snuffler costs 2.
		scn.SetTwilight(2);

		// Frodo, Son of Drogo is vitality 4: this Ring adds nothing to his vitality.
		assertEquals(4, scn.GetVitality(frodo));

		// The Shadow phase: Little Snuffler's wound is in no skirmish.  Nothing is offered, and it is a wound.
		scn.FreepsPass();
		scn.ShadowPlayCard(snuffler);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The maneuver phase: the Free Peoples player names no culture, so Gate Picket wounds Frodo.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowUseCardAction(picket);
		scn.FreepsDeclineChoosing();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The archery phase: the Marksman's wound goes to the only companion.
		scn.SkipToArcheryWounds();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// The Soldier's own wound comes in a skirmish, and now the response is offered.
		scn.FreepsPass();
		scn.ShadowUseCardAction(soldier);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The wound for losing, 4 against 7, is a burden with nothing asked.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void RulingRingConvertsWoundsOnlyDuringSkirmishPhaseWhenRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsildurScenario();

		var isildur = scn.GetRingBearer();
		var ring = scn.GetRing();
		var fanatic = scn.GetShadowCard("fanatic");

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic);
		// Fierce only so that there is a second assignment phase, with the Ring on.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, fanatic, null, Keyword.FIERCE));
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());

		// The Fanatic wounds the Man assigned to it: a wound in the assignment phase, the Ring not worn.
		scn.FreepsAssignToMinions(isildur, fanatic);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Isildur's own text, with no 3 companions to exert: 2 burdens as his skirmish starts.
		scn.FreepsResolveSkirmish(isildur);
		assertEquals(2, scn.GetBurdens());

		// 8 against 8, and a tie is the Shadow's: he puts the Ring on, and the wound for losing is a burden.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(isildur));

		// The fierce assignment phase.  The Ring is on, and the Fanatic's wound is in no skirmish: it is a
		// wound, and no burden is added.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.RBWearingOneRing());
		scn.FreepsAssignToMinions(isildur, fanatic);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetWoundsOn(isildur));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void PutOnInASkirmishTheRingStaysOnThroughTheFierceSkirmishAndComesOffAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var soldier = scn.GetShadowCard("soldier");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, gollum);
		// Fierce only so that the Soldier fights Frodo a second time.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, soldier, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());

		// 4 against 7: the response is offered at the wound for losing, and that wound is a burden.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fierce skirmish follows one already resolved, and the Ring is still on.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());

		// Worn: the Soldier's own wound is a second burden, and the response is not offered again ...
		scn.FreepsPass();
		scn.ShadowUseCardAction(soldier);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// ... and the wound for losing again is a third.
		scn.PassFierceSkirmishActions();
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());

		// A wound in the regroup phase is in no skirmish: Gollum's is not answered, and is a wound.
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum, "Wound the Ring-bearer");
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionWhoDoesNotBearTheRingTakesHisSkirmishWoundsWhetherItIsWornOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		// Fierce only so that Sam fights a second time, after the Ring is on.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, runner1, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner1}, new PhysicalCardImpl[]{frodo, runner2});

		// Sam, 3, loses to a Runner, 5.  He bears no Ring: nothing is offered, and the wound is a wound.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Frodo, 4, loses to the other: the response is offered, and he puts the Ring on.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fierce skirmish, the Ring on Frodo's finger: Sam's wound is still a wound, and no burden is added.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, runner1);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.RBWearingOneRing());
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseLeavesTheWoundAWoundAndTheNextWoundAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		// 4 against 5, and damage +1: two wounds for one loss.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());

		// The second wound asks again, and answered it is a burden.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Comprehensive Rules 4.2, Section Three, THE ONE RING, THE RULING RING: "You may use the special ability of this
	// card in response to a wound placed by The Witch-king, Lord of the Nazgul or Ulaire Enquea, Ringwraith in
	// Twilight."
	@Test
	public void AWoundFromEnqueaAfterHeWinsASkirmishMayBeAnsweredByPuttingTheRingOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 against 11: Aragorn loses, and Enquea may exert to wound the Ring-bearer, who fought nobody.
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(enquea));

		// The skirmish is resolved and its phase is not yet over: the wound is one taken in a skirmish.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Comprehensive Rules 4.2, "modifier": "Frodo (strength 3) bears The One Ring, The Ruling Ring (strength +1). His
	// strength is 4. During a skirmish, Enduring Evil is played ..., spotting 6 burdens on Frodo. Frodo's strength is
	// 3 + 1 - 6 = -2. This is reset to zero. Hobbit Intuition is played ... He has strength 3 (Frodo) +1 (Ring) - 6
	// (Enduring Evil) + 3 (Intuition) = 1."
	@Test
	public void AStrengthBroughtBelowZeroOnFrodoWithTheRulingRingCountsFromItsTrueSum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var intuition = scn.GetFreepsCard("intuition");
		var soldier = scn.GetShadowCard("soldier");
		var evil = scn.GetShadowCard("evil");

		scn.StartGame();
		// Hobbit Intuition makes a Hobbit stronger only away from sites 1 to 4: the skirmish is fought at site 5.
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(soldier);
		scn.MoveCardsToHand(intuition, evil);
		scn.AddBurdens(6);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, soldier);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetBurdens());

		scn.FreepsPass();
		scn.ShadowPlayCard(evil);
		scn.ShadowChoose("6");
		// 3 + 1 - 6 = -2, which counts as zero
		assertEquals(0, scn.GetStrength(frodo));

		scn.FreepsPlayCard(intuition);
		// 3 + 1 - 6 + 3
		assertEquals(1, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
