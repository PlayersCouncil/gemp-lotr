package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rancor", "9_44");
					// Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgûl, strength 11, vitality 4, fierce; his
					// maneuver ability needs 6 companions or 5 burdens, never there
					put("enquea", "1_231");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, vitality 3, fierce; his text
					// needs 3 burdens, never there
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, no Nazgûl, vitality 2; stronger only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofRancorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Rancor
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: 1
		 * Game Text: Bearer must be a Nazgûl.
		 * 	<b>Maneuver:</b> Remove a burden to heal each Nazgûl.
		 * 	<b>Response:</b> If a player reconciles, return bearer to his owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rancor");

		assertEquals("Ring of Rancor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnANazgulOfAnyCultureAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rancor = scn.GetShadowCard("rancor");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(rancor);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, khamul, hound);
		assertInZone(Zone.HAND, rancor);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(rancor));
		scn.ShadowPlayCard(rancor);
		// A [wraith] Nazgûl and a [raider] one; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(rancor, khamul);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityRemovesABurdenToHealEachNazgulAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rancor = scn.GetShadowCard("rancor");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, khamul, hound);
		scn.AttachCardsTo(enquea, rancor);
		scn.AddBurdens(1);
		scn.AddWoundsToChar(enquea, 2);
		scn.AddWoundsToChar(khamul, 1);
		scn.AddWoundsToChar(hound, 1);
		assertAttachedTo(rancor, enquea);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rancor));
		scn.ShadowUseCardAction(rancor);

		// Each Nazgûl heals one wound, the bearer and the [raider] one; the [wraith] Orc does not.
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoNazgulWoundedTheBurdenIsRemovedForNothingAndWithNoBurdenTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rancor = scn.GetShadowCard("rancor");
		var enquea = scn.GetShadowCard("enquea");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, hound);
		scn.AttachCardsTo(enquea, rancor);
		scn.AddBurdens(1);
		scn.AddWoundsToChar(hound, 1);
		assertAttachedTo(rancor, enquea);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(hound));

		// No Nazgûl is wounded: a cost can be paid, so it is offered, and paid for nothing.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(rancor));
		scn.ShadowUseCardAction(rancor);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(hound));

		// No burden left to remove: not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rancor));
	}

	@Test
	public void WhenTheShadowPlayerReconcilesHeMayReturnTheBearerToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rancor = scn.GetShadowCard("rancor");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, khamul);
		scn.AttachCardsTo(enquea, rancor);
		// A card in his hand, so that his reconcile asks him.
		scn.MoveCardsToHand(hound);
		assertAttachedTo(rancor, enquea);
		assertInZone(Zone.HAND, hound);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// Every player passed: the Shadow player reconciles, and then may respond.
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rancor));
		scn.ShadowDeclineReconciliation();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rancor));
		scn.ShadowChooseOptionalTrigger(rancor);

		// The bearer goes to his owner's hand and the Ring with him from play; the other Nazgûl stays.
		assertInZone(Zone.HAND, enquea);
		assertInZone(Zone.DISCARD, rancor);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void WhenTheFreePeoplesPlayerReconcilesTheShadowPlayerMayReturnTheBearerToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rancor = scn.GetShadowCard("rancor");
		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, khamul);
		scn.AttachCardsTo(enquea, rancor);
		assertAttachedTo(rancor, enquea);

		scn.SkipToMovementDecision();
		// The skipper stops at the offer that follows the Shadow player's own reconcile: declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rancor));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertAttachedTo(rancor, enquea);

		// The Free Peoples player stays and reconciles: the Shadow player is offered it again.
		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rancor));
		scn.ShadowChooseOptionalTrigger(rancor);

		assertInZone(Zone.HAND, enquea);
		assertInZone(Zone.DISCARD, rancor);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void MovingAgainIsNoReconcileAndOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rancor = scn.GetShadowCard("rancor");
		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.AttachCardsTo(enquea, rancor);
		assertAttachedTo(rancor, enquea);

		scn.SkipToMovementDecision();
		// The skipper stops at the offer that follows the Shadow player's own reconcile: declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rancor));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// No reconcile of the Free Peoples player: the bearer stays in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rancor));
		assertAttachedTo(rancor, enquea);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
	}
}
