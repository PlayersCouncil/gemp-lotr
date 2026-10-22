package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "8_83");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, vitality 2; her text reaches only
					                            // a character bearing a [wraith] condition
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl (his texts want 3 and 5 burdens)
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, vitality 2 (a play trigger, never set off: cheated in)
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl (its text wants 2 wounds on the Ring-bearer)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: in the Shadow hand, a Free Peoples card to discard
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai minion whose whole text is Damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					// Defend It and Hope: [gondor] Maneuver event, cost 0; "Exert a [gondor] Man to wound a minion once (or
					// twice if that minion is an Uruk-hai)": a Free Peoples wound on a minion
					put("defend1", "4_115");
					put("defend2", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WingedMountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Winged Mount
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Game Text: To play, discard 2 cards from hand. Bearer must be a Nazgûl.<br><b>Response:</b> If bearer is about to take a wound, remove a threat to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mount");

		assertEquals("Winged Mount", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnANazgulOfAnyCultureByDiscardingTwoCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mount = scn.GetShadowCard("mount");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		// Three cards beside the mount, of three cultures and both sides: two are to be chosen.
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(mount, guard, troop, shadowAragorn);
		assertInHand(mount, guard, troop, shadowAragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, nertea, hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(mount));

		scn.ShadowPlayCard(mount);
		// A [wraith] Nazgûl, a second one, and a [raider] one; not the [wraith] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul, nertea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, frodo, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		// Every other card in hand may pay; the mount itself is not in hand to be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, troop, shadowAragorn));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(guard, shadowAragorn);

		assertInDiscard(guard, shadowAragorn);
		assertInHand(troop);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertAttachedTo(mount, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneOtherCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetShadowCard("mount");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(mount, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		// The mount and one card: the mount cannot pay for itself, so one card short.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowPlayAvailable(mount));

		// A second card beside the first, and it can be played.
		scn.MoveCardsToHand(guard);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(mount));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseRemovesAThreatToPreventAWoundToBearerAndIsNotOfferedForAnotherNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend1, defend2);

		var mount = scn.GetShadowCard("mount");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");

		scn.StartGame();
		// Two companions, so two threats is the limit.
		scn.AddThreats(2);
		scn.MoveMinionsToTable(lemenya, nertea);
		scn.AttachCardsTo(lemenya, mount);
		assertEquals(2, scn.GetThreats());
		assertAttachedTo(mount, lemenya);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		scn.SkipToPhase(Phase.MANEUVER);

		// A wound on another Nazgûl, who does not bear the mount: nothing is offered, and he takes it.
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, nertea));
		scn.FreepsChooseCard(nertea);
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A wound on the bearer: the response is offered, a threat removed, and the wound prevented.
		scn.ShadowPass();
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mount));
		scn.ShadowChooseOptionalTrigger(mount);

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertAttachedTo(mount, lemenya);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveTheResponseIsNotOfferedAndBearerIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend1);

		var mount = scn.GetShadowCard("mount");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nertea);
		scn.AttachCardsTo(lemenya, mount);
		assertAttachedTo(mount, lemenya);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(lemenya);

		// No threat to pay with: the bearer takes the wound, and the Shadow player is at his maneuver actions.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
