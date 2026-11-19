package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_183_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("precious", "15_183");

					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9, vitality 2; her text lowers a
					// character who bears a [wraith] condition, and none does
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: the [raider] Nazgûl, strength 12, vitality 3; his texts need 3
					// and 5 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, no Nazgûl, strength 5, vitality 2; +4 and fierce only at 2 wounds on
					// the Ring-bearer
					put("hound", "7_192");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					// Defend It and Hope: [gondor] Maneuver event, cost 0; "Exert a [gondor] Man to wound a minion once (or
					// twice if that minion is an Uruk-hai)": a Free Peoples wound on a minion
					put("defend1", "4_115");
					put("defend2", "4_115");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a Maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheyFeelThePreciousStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: They Feel The Precious
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If a Nazgûl is about to take a wound, remove a threat to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("precious");

		assertEquals("They Feel The Precious", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseRemovesAThreatToPreventAWoundToANazgulAndIsNotOfferedForAMinionThatIsNoNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend1, defend2);

		var precious = scn.GetShadowCard("precious");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(precious);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, precious);
		// Two companions, so two threats is the limit.
		scn.AddThreats(2);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.SkipToPhase(Phase.MANEUVER);

		// A wound on the Hound, a [wraith] minion who is no Nazgûl: nothing is offered, and it takes the wound.
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, khamul, hound));
		scn.FreepsChooseCard(hound);
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A wound on Khamul, the [raider] Nazgûl: the response is offered, a threat removed, and the wound prevented.
		scn.ShadowPass();
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(precious));
		scn.ShadowChooseOptionalTrigger(precious);

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, precious);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OfNazgulLosingOneSkirmishTheShadowPlayerChoosesWhoseWoundToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var precious = scn.GetShadowCard("precious");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(precious);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, precious);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		// Aragorn at 27 beats the three minions together (9 + 12 + 5 = 26) without doubling them: each loser takes one
		// wound, and all three wounds come at once.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 19));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowAssignToMinions(aragorn, khamul, hound);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(precious));
		scn.ShadowChooseOptionalTrigger(precious);
		// Both Nazgûl, of either culture; not the Hound, who is losing with them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(khamul);

		// The wounds still to come, Lemenya's among them, may be answered again; declined.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(precious));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));
		// Khamul is fierce: the fierce assignment phase follows.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveTheResponseIsNotOfferedAndTheNazgulIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend1);

		var precious = scn.GetShadowCard("precious");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToSupportArea(precious);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, precious);
		scn.MoveMinionsToTable(lemenya, khamul);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(lemenya);

		// No threat to pay with: Lemenya takes the wound, and the Shadow player is at his maneuver actions.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
