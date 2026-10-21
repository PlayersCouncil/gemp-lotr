package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_42");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl; his text is about characters bearing [wraith] conditions
					put("lemenya", "1_232");
					// Úlairë Otsëa, Lieutenant of Morgul: [wraith] Nazgûl; a Shadow ability only
					put("otsea", "1_235");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; fierce, his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgûl (an Orc); his text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Drawn to Its Power: [wraith] support-area condition, cost 2; its text answers a companion killed
					put("drawn", "1_211");
					// Orc Chieftain: a [sauron] minion; its text answers a weapon played on it
					put("chieftain", "1_266");
					// Bent on Discovery: [wraith] support-area condition; the Shadow player draws a card at the start of his Shadow phase
					put("bent", "1_206");

					// Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: abilities used only on purpose
					put("sam", "1_311");
					// Barliman Butterbur: an ally, with a Fellowship ability used only on purpose
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofAsperityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Asperity
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: 1
		 * Game Text: Bearer must be a Nazgûl.<br>When you play this artifact, you may wound an unwounded companion.<br><b>Response:</b> If a player reconciles, return bearer to his owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Asperity", card.getBlueprint().getTitle());
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
	public void BearerMustBeANazgulAndWithEveryCompanionWoundedNoWoundIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		// The Ring-bearer is the only companion, and he is wounded: there is no unwounded companion to wound.
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.MoveCardsToHand(ring);
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.SkipToPhase(Phase.SHADOW);

		assertTrue(scn.ShadowPlayAvailable(ring));
		scn.ShadowPlayCard(ring);

		// A [wraith] Nazgûl and a [raider] Nazgûl may bear it; a [wraith] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);

		assertAttachedTo(ring, khamul);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedTheShadowPlayerMayWoundAnUnwoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 1);
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(ring);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		scn.SkipToPhase(Phase.SHADOW);

		// Lemenya is the only Nazgûl, so the Ring goes on him unasked.
		scn.ShadowPlayCard(ring);
		assertAttachedTo(ring, lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowAcceptOptionalTrigger();

		// Two unwounded companions are offered; a wounded companion and an unwounded ally are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningWoundsNobodyAndAWraithCardPlayedAfterItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetShadowCard("ring");
		var lemenya = scn.GetShadowCard("lemenya");
		var drawn = scn.GetShadowCard("drawn");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(ring, drawn);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPlayCard(ring);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ring));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [wraith] card played with the Ring in play is no play of the Ring.
		scn.ShadowPlayCard(drawn);

		assertInZone(Zone.SUPPORT, drawn);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
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
		assertAttachedTo(ring, lemenya);
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
