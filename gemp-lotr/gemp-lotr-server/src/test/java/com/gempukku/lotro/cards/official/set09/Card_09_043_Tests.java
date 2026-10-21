package com.gempukku.lotro.cards.official.set09;

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

public class Card_09_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_43");
					// The Witch-king, Lord of Angmar: [wraith] Nazgûl, strength 14, vitality 4; fierce, and +2 for each other Nazgûl
					put("witchking", "1_237");
					// The Witch-king, Empowered by His Master: [wraith] Nazgûl of the same title, enduring and printed damage +1; the bearer who already holds the keyword
					put("witchkingE", "103_80");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl of another title; his text is about characters bearing [wraith] conditions
					put("lemenya", "1_232");
					// Úlairë Otsëa, Lieutenant of Morgul: [wraith] Nazgûl; a Shadow ability only
					put("otsea", "1_235");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; fierce, his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgûl (an Orc); his text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Drawn to Its Power: a [wraith] card that is no minion (a support-area condition); its text answers a companion killed
					put("drawn", "1_211");
					// Orc Chieftain: a [sauron] minion; its text answers a weapon played on it
					put("chieftain", "1_266");
					// Bent on Discovery: [wraith] support-area condition; the Shadow player draws a card at the start of his Shadow phase
					put("bent", "1_206");

					// Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: abilities used only on purpose
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofIreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Ire
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: 1
		 * Game Text: Bearer must be The Witch-king.<br><b>Skirmish:</b> Exert bearer twice to make a [wraith] minion <b>damage +1</b>.<br><b>Response:</b> If a player reconciles, return bearer to his owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Ire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeTheWitchKing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.MoveCardsToHand(ring);
		scn.SkipToPhase(Phase.SHADOW);

		// A [wraith] Nazgûl of another title and a [wraith] Orc: nobody may bear it.
		assertInZone(Zone.HAND, ring);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(ring));

		scn.MoveMinionsToTable(witchking);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(ring));
		// The Witch-king is the one card that may bear it, so it goes on him unasked.
		scn.ShadowPlayCard(ring);

		assertAttachedTo(ring, witchking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsTheBearerTwiceToMakeAWraithMinionDamagePlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var drawn = scn.GetShadowCard("drawn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		// 8 + 4 = 12: the Witch-king's 19 (14, +2 for each of two other Nazgûl, +1 for the Ring) beats him without
		// being double his strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, lemenya, hound, khamul);
		scn.AttachCardsTo(witchking, ring);
		scn.MoveCardsToSupportArea(drawn);
		assertAttachedTo(ring, witchking);
		assertInZone(Zone.SUPPORT, drawn);
		scn.SkipToAssignments();
		// Lemenya waits in a skirmish of his own; the Hound and Khamûl are left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, witchking}, new PhysicalCardImpl[]{sam, lemenya});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(ring));

		scn.ShadowUseCardAction(ring);

		// The bearer exerts twice; Lemenya, another Nazgûl who could exert, does not.
		assertEquals(2, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		// [wraith] minions in this skirmish, in another and in none are offered; a [raider] Nazgûl and a [wraith]
		// card that is no minion are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, drawn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(lemenya);

		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 19 against 12: Aragorn loses and takes one wound, the Witch-king having no damage bonus.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The bonus lasted the skirmish it was made in: in Lemenya's own skirmish it is gone.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityOffersAMinionWhoIsAlreadyDamagePlusOneAndMakesHimMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchkingE");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, hound);
		scn.AttachCardsTo(witchking, ring);
		assertAttachedTo(ring, witchking);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The bearer is printed damage +1 and the Hound has none.
		assertEquals(1, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowActionAvailable(ring));

		scn.ShadowUseCardAction(ring);

		assertEquals(2, scn.GetWoundsOn(witchking));
		// A minion who already has the keyword is still a candidate, as one who has not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, hound));

		scn.ShadowChooseCard(witchking);

		// Damage bonuses add up.
		assertEquals(2, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheWitchKingCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchking");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, hound);
		scn.AttachCardsTo(witchking, ring);
		// Vitality 4 with 2 wounds: he can exert once, but not twice.
		scn.AddWoundsToChar(witchking, 2);
		assertAttachedTo(ring, witchking);
		assertEquals(2, scn.GetWoundsOn(witchking));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(2, scn.GetVitality(witchking));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ring));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
	}

	@Test
	public void AtHisOwnReconcileTheShadowPlayerMayReturnTheBearerToHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchking");
		var otsea = scn.GetShadowCard("otsea");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Two Nazgûl that came through the skirmishes, the Witch-king bearing the Ring.
		scn.MoveMinionsToTable(witchking, otsea);
		scn.AttachCardsTo(witchking, ring);
		scn.SetShadowHand(chieftain);

		assertAttachedTo(ring, witchking);
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		scn.ShadowDeclineReconciliation();

		// The hand is made up to 8 before the response is offered.
		assertEquals(8, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, witchking, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.HAND, witchking);
		assertEquals(9, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, ring);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void DeclinedAtHisOwnReconcileItIsOfferedAgainWhenTheFreePeoplesPlayerReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchking");
		var otsea = scn.GetShadowCard("otsea");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(witchking, otsea);
		scn.AttachCardsTo(witchking, ring);
		scn.SetShadowHand(chieftain);
		scn.SetFreepsHand(aragorn);

		assertAttachedTo(ring, witchking);

		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, witchking, otsea);
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();

		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();

		assertEquals(8, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, witchking, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		// The Witch-king is back in hand; Otsëa is discarded with the other minions as the turn ends.
		assertInZone(Zone.HAND, witchking);
		assertInZone(Zone.DISCARD, ring, otsea);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ACardDrawnOutsideAReconcileIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var witchking = scn.GetShadowCard("witchking");
		var bent = scn.GetShadowCard("bent");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, ring);
		scn.MoveCardsToSupportArea(bent);
		assertAttachedTo(ring, witchking);
		int hand = scn.GetShadowHandCount();

		scn.FreepsPass();

		// Bent on Discovery has drawn the Shadow player a card at the start of his Shadow phase.
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertAttachedTo(ring, witchking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
