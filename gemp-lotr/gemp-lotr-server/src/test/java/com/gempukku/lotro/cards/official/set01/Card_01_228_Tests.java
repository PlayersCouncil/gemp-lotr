package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_228_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("world", "1_228");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("world", "1_228");
					// Two Nazgûl of strength 9 and vitality 2 who are not fierce.  Lemenya's text needs a companion
					// bearing a [wraith] condition, Nertëa's only answers his being played.
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur
					// Morgul Hound: a [wraith] minion that is no Nazgûl (an Orc), strength 5, vitality 2, so it
					// could exert; stronger and fierce only at 2 wounds on the Ring-bearer.  It beats Frodo (3, and
					// 1 for the Ring) by one.
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetReachScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("world", "1_228");
					// Saruman's Reach: [isengard] Maneuver event, cost 0.  "Exert an Uruk-hai to make the opponent
					// choose to either exert 2 companions or make the Ring-bearer put on The One Ring until the
					// regroup phase."  With the Ring-bearer the only companion, the Ring goes on unasked.
					put("reach", "1_137");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else

					put("otsea", "1_235");      // Úlairë Otsëa: [wraith] Nazgûl, vitality 3; a Shadow ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, vitality 3; his texts need 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, vitality 2, so it could exert

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheTwilightWorldStatsAndKeywordsAreCorrect()throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Twilight World
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Response:</b> If the Ring-bearer puts on The One Ring, exert a Nazgûl to add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("world");

		assertEquals("The Twilight World", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseExertsANazgulToAddTwoBurdensWhenTheRingBearerPutsOnTheOneRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var world = scn.GetShadowCard("world");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		scn.MoveCardsToHand(world);
		assertInHand(world);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(lemenya, nertea, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// Frodo has lost and is about to take the wound: the Ring's own response.
		assertFalse(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));

		scn.FreepsChooseOptionalTrigger(ring);
		// He has put it on, and the wound has not yet been made a burden.
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(world));

		scn.ShadowChooseOptionalTrigger(world);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nertea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(lemenya);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertEquals(0, scn.GetWoundsOn(hound));
		// 1 before + 2 from the event + 1 for the wound the Ring turns into a burden
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(world);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnswersTheRingPutOnBySarumansReachByExertingANazgulOfEitherCultureToAddTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReachScenario();

		var world = scn.GetShadowCard("world");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		var otsea = scn.GetShadowCard("otsea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(world, reach);
		assertInHand(world);

		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(savage, otsea, khamul, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));

		// The Savage is the one Uruk-hai, and Frodo the one companion: the Ring goes on with nothing asked.
		scn.ShadowPlayCard(reach);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(world));

		scn.ShadowChooseOptionalTrigger(world);
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(world);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheFreePeoplesPlayerExertsTwoCompanionsInsteadOfPuttingOnTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReachScenario();

		var world = scn.GetShadowCard("world");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(world, reach);
		assertInHand(world);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, otsea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(otsea));

		scn.ShadowPlayCard(reach);
		scn.FreepsChoose("Exert 2 companions");
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.RBWearingOneRing());

		// No Ring put on: the game goes on to the Free Peoples player's maneuver actions, nothing offered.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(world));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInHand(world);
	}

	@Test
	public void IsNotOfferedWhileTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReachScenario();

		var world = scn.GetShadowCard("world");
		var reach = scn.GetShadowCard("reach");
		var savage = scn.GetShadowCard("savage");
		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(world, reach);
		assertInHand(world);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, otsea, hound);
		// Vitality 3 with 2 wounds: Otsëa cannot exert; the Hound beside him could.
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(reach);
		assertTrue(scn.RBWearingOneRing());

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(world));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInHand(world);
	}
}
