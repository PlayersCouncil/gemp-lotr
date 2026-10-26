package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sting", "8_113");
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit who is neither Frodo nor Sam
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8

					// Shelob, Eater of Light: fierce, strength 8, vitality 8; her other texts act as she is
					// played and on minions stacked on [gollum] possessions.
					put("shelob", "8_25");
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; his texts are about unbound Hobbits
					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce, strength 8 and +1 for each companion
					put("seeker", "4_195");     // Uruk Seeker: fierce, strength 6, vitality 2; stronger only against a search card
					// Charging Uruk: fierce, strength 7; in a fierce skirmish +3 and hunter 1 (+1 against a non-hunter)
					put("charging", "15_156");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Sting, Bane of the Eight Legs
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Frodo or Sam.
		 * <b>Response:</b> If a fierce skirmish involving bearer is about to end, add a threat to discard a minion
		 * involved in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sting");

		assertEquals("Sting", card.getBlueprint().getTitle());
		assertEquals("Bane of the Eight Legs", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnFrodoOrSamAndNotOnAnotherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(sting);
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sting));
		scn.FreepsPlayCard(sting);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(sam);

		assertAttachedTo(sting, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheEndOfARegularSkirmishNothingIsOfferedAndAtTheEndOfTheFierceOneAThreatDiscardsTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		var shelob = scn.GetShadowCard("shelob");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Sam's 5 against Shelob's 8: he loses, is not overwhelmed, and takes a wound. The skirmish was
		// against a fierce minion but was no fierce skirmish: it has ended with no offer, though a
		// threat could have been added.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.InFierceRound());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Sam is made strong enough that Shelob and the Uruk Seeker together (8 + 6) do not overwhelm him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 3));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, shelob);
		scn.ShadowAssignToMinions(sam, seeker);
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, shelob, seeker);

		scn.FreepsAcceptOptionalTrigger();

		// Either minion of the skirmish; not Sam, who is involved in it too (Q-0266).
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(shelob, seeker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(shelob);
		assertInZone(Zone.DISCARD, shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);

		// Frodo and Sam leave room for a second threat, so the response is offered again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyBearersFierceSkirmishBringsTheOfferAndOnlyAMinionInItMayBeDiscardedOncePerThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);

		var pursuer = scn.GetShadowCard("pursuer");
		var rider = scn.GetShadowCard("rider");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		// Frodo is 3 + 1 for the Ring + 2 for Sting; with 2 more he is 8, and the 15 of the Pursuer (5)
		// and the Black Rider (8 + 2 companions) is not double that.
		scn.ApplyAdHocModifier(new StrengthModifier(null, frodo, null, 2));
		scn.MoveMinionsToTable(pursuer, rider, seeker);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, pursuer}, new PhysicalCardImpl[]{aragorn, seeker});
		scn.ShadowAssignToMinions(frodo, rider);

		// Aragorn's fierce skirmish first: he beats the Seeker, who lives. Frodo is not in it.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(seeker));
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(frodo);
		assertEquals(8, scn.GetStrength(frodo));
		scn.PassFierceSkirmishActions();
		// Frodo loses; The Ruling Ring's offer to take the wound as a burden is declined.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(seeker));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rider);

		assertInZone(Zone.DISCARD, rider);
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer, seeker);

		// The skirmish is still about to end and a second threat fits: the response is used again,
		// and the one minion left in the skirmish is discarded.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sting));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, pursuer);
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		// Two companions, two threats: no third can be added.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoRoomForAThreatTheResponseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		// Two companions, two threats.
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, shelob);
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABearerOverwhelmedInAFierceSkirmishIsGoneWithStingBeforeItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sting = scn.GetFreepsCard("sting");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sting);

		var charging = scn.GetShadowCard("charging");

		scn.StartGame();
		scn.MoveMinionsToTable(charging);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, charging);
		scn.FreepsResolveSkirmish(sam);
		// 7 + 3 in a fierce skirmish + 1 as a hunter against Sam: 11, more than double his 5.
		assertEquals(11, scn.GetStrength(charging));
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DEAD, sam);
		assertInZone(Zone.DISCARD, sting);
		// Frodo alone still leaves room for a threat, but Sting is no longer there to answer.
		assertEquals(1, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, charging);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
