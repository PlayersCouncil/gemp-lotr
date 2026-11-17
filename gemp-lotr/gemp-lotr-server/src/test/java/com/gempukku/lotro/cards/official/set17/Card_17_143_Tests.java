package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "17_143");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl; his text is about characters bearing [wraith] conditions
					put("lemenya", "1_232");
					// Úlairë Otsëa, Lieutenant of Morgul: [wraith] Nazgûl; a Shadow ability only
					put("otsea", "1_235");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; fierce, his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgûl (an Orc); his text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// All Veils Removed: [wraith] Skirmish event
					put("veils", "1_204");
					// Drawn to Its Power: [wraith] support-area condition, cost 2
					put("drawn", "1_211");
					// Orc Chieftain: a [sauron] minion
					put("chieftain", "1_266");
					// Threshold of Shadow: a [wraith] Skirmish event, laid only in the Free Peoples player's discard pile
					put("threshold", "1_227");
					// Bent on Discovery: [wraith] support-area condition; the Shadow player draws a card at the start of his Shadow phase
					put("bent", "1_206");

					// Companions to raise the threat limit
					put("aragorn", "1_89");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Ring of Terror
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Game Text: Bearer must be a Nazgûl. Bearer is <b>damage +1</b>.<br>When you play this artifact, you may remove two threats to take a [wraith] card into hand from your discard pile.<br><b>Response:</b> If a player reconciles, return bearer to his owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Terror", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeANazgulAndIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.MoveCardsToHand(ring);
		scn.SkipToPhase(Phase.SHADOW);

		// No threats: the Ring's own "when you play" cost cannot be paid, and nothing is asked of it.
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, ring);
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(ring));

		scn.ShadowPlayCard(ring);

		// A [wraith] Nazgûl and a [raider] Nazgûl may bear it; a [wraith] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);

		assertAttachedTo(ring, khamul);
		assertEquals(1, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedTheShadowPlayerMayRemoveTwoThreatsToTakeAWraithCardFromHisDiscardPileIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var veils = scn.GetShadowCard("veils");
		var drawn = scn.GetShadowCard("drawn");
		var chieftain = scn.GetShadowCard("chieftain");
		var threshold = scn.GetFreepsCard("threshold");
		scn.MoveCompanionsToTable("aragorn", "sam");
		scn.MoveCardsToDiscard(veils, drawn, chieftain, threshold);

		scn.StartGame();
		scn.AddThreats(3);
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(ring);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.DISCARD, veils, drawn, chieftain, threshold);

		scn.ShadowPlayCard(ring);

		assertAttachedTo(ring, lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		// A [wraith] event and a [wraith] condition are offered; a [sauron] minion is not, nor the Free Peoples
		// player's [wraith] event in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veils, drawn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, threshold));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCardBPFromSelection(drawn);

		assertInZone(Zone.HAND, drawn);
		assertInZone(Zone.DISCARD, veils, chieftain, threshold);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheThreatsAndTheDiscardPileAndAWraithCardPlayedAfterItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var veils = scn.GetShadowCard("veils");
		var drawn = scn.GetShadowCard("drawn");
		scn.MoveCompanionsToTable("sam");
		scn.MoveCardsToDiscard(veils);

		scn.StartGame();
		scn.AddThreats(2);
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(ring, drawn);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, veils);

		scn.ShadowPlayCard(ring);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, veils);
		assertInZone(Zone.HAND, drawn);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [wraith] card played with the Ring in play is no play of the Ring.
		scn.ShadowPlayCard(drawn);

		assertInZone(Zone.SUPPORT, drawn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, veils);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var veils = scn.GetShadowCard("veils");
		scn.MoveCompanionsToTable("sam");
		scn.MoveCardsToDiscard(veils);

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(ring);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, veils);

		scn.ShadowPlayCard(ring);

		assertAttachedTo(ring, lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, veils);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtHisOwnReconcileTheShadowPlayerMayReturnTheBearerToHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Two Nazgûl that came through the skirmishes, one bearing the Ring.
		scn.MoveMinionsToTable(lemenya, otsea);
		scn.AttachCardsTo(lemenya, ring);
		scn.SetShadowHand(chieftain);

		assertAttachedTo(ring, lemenya);
		assertEquals(1, scn.GetShadowHandCount());

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		scn.ShadowDeclineReconciliation();

		// The hand is made up to 8 before the response is offered.
		assertEquals(8, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.HAND, lemenya);
		assertEquals(9, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, ring);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void DeclinedAtHisOwnReconcileItIsOfferedAgainWhenTheFreePeoplesPlayerReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(lemenya, otsea);
		scn.AttachCardsTo(lemenya, ring);
		scn.SetShadowHand(chieftain);
		scn.SetFreepsHand(aragorn);

		assertAttachedTo(ring, lemenya);

		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, otsea);
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();

		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();

		assertEquals(8, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		// Lemenya is back in hand; Otsëa is discarded with the other minions as the turn ends.
		assertInZone(Zone.HAND, lemenya);
		assertInZone(Zone.DISCARD, ring, otsea);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ACardDrawnOutsideAReconcileIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var bent = scn.GetShadowCard("bent");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, ring);
		scn.MoveCardsToSupportArea(bent);
		int hand = scn.GetShadowHandCount();

		scn.FreepsPass();

		// Bent on Discovery has drawn the Shadow player a card at the start of his Shadow phase.
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertAttachedTo(ring, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
