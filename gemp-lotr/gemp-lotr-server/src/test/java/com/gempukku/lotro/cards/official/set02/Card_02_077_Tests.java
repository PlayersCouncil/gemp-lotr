package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "2_77");

					// Úlairë Nelya, Ringwraith in Twilight: a twilight Nazgûl, vitality 3; her text needs 2 burdens
					// or 2 wounds on the Ring-bearer, and there are none here
					put("nelya", "2_84");
					// Úlairë Attëa, The Easterling: a twilight Nazgûl of another title; his text answers his own play
					put("attea", "2_82");
					// Úlairë Enquëa, Ringwraith in Twilight: a twilight Nazgûl left out of the skirmish; his text
					// answers his own win
					put("enquea", "2_83");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl who is not twilight, vitality 2; his text
					// reaches only a companion or ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Wisp of Pale Sheen: a twilight [sauron] Wraith who is no Nazgûl, vitality 2; a Regroup ability only
					put("wisp", "6_108");

					// Aragorn, Ranger of the North: a [gondor] Man to exert for Defend It and Hope
					put("aragorn", "1_89");
					// Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once (twice an Uruk-hai)
					put("defend", "4_115");
					// Gimli, Son of Glóin: damage +1, so a minion he beats takes two wounds
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisTerribleServantsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: His Terrible Servants
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Response:</b> If a twilight Nazgûl is about to take a wound, remove (1) to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("His Terrible Servants", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesOneTwilightToPreventAWoundToATwilightNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);
		scn.MoveCardsToSupportArea(servants);
		assertInZone(Zone.SUPPORT, servants);
		assertInHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn, the only [gondor] Man, exerts; Nelya, the only minion, is about to be wounded.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInZone(Zone.SUPPORT, servants);
		assertInDiscard(defend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTwilightToRemoveItIsNotOfferedAndTheWoundLands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);
		scn.MoveCardsToSupportArea(servants);
		assertInZone(Zone.SUPPORT, servants);
		assertInHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.FreepsPlayCard(defend);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(servants));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertInDiscard(defend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnlyTheTwilightNazgulAmongTheLosersOfASkirmishAreSavedEachForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var wisp = scn.GetShadowCard("wisp");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(servants);
		assertInZone(Zone.SUPPORT, servants);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, attea, lemenya, wisp, enquea);
		// 8 + 32 = 40 against 10 + 12 + 9 + 8 = 39: Aragorn wins, and is not doubled.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 32));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		// Enquëa is left out of every skirmish.
		scn.ShadowAssignToMinions(aragorn, attea, lemenya, wisp);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(wisp));
		assertEquals(0, scn.GetWoundsOn(enquea));
		scn.PassSkirmishActions();

		// The four losers are about to be wounded together.  The twilight Nazgûl among them may be saved; the
		// Nazgûl who is not twilight, the twilight Wraith who is no Nazgûl and the twilight Nazgûl taking no wound
		// may not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, wisp, enquea));
		scn.ShadowChooseCard(nelya);

		// A response may be used again in the same situation: Attëa's wound is still to come.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());

		// Nothing is left that it may prevent, though (1) is still in the pool.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(servants));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(wisp));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsOnlyTheOneWoundItAnswersAndIsOfferedAgainForTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var nelya = scn.GetShadowCard("nelya");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(servants);
		assertInZone(Zone.SUPPORT, servants);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		// 6 + 5 = 11 against 10: Gimli wins, and his damage +1 makes two wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.PassSkirmishActions();

		// The first wound is prevented for (1)...
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nelya));

		// ...and the second is a wound of its own, answered again; declined, it lands.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(servants));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
