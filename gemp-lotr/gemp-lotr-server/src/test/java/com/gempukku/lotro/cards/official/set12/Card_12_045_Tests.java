package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "12_45");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, unbound, strength 8; his maneuver ability is not used
					put("gimli", "1_13");       // Gimli, Son of Glóin: an unbound [dwarven] Dwarf, strength 6; his skirmish ability is not used
					put("pippin", "103_31");    // Pippin, Trollslayer: an unbound [gondor] Hobbit, strength 3 (no Man); his skirmish ability is not used
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound by rule and not the Ring-bearer, strength 3; his abilities are not used
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally, strength 6; his maneuver ability is not used
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: an unbound [rohan] Man, strength 6; a response to a wound, never set off here
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConfrontingtheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Confronting the Eye
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fellowship:</b> Spot a [gondor] Man and add (2) to make each unbound companion strength +1 until the regroup phase (limit +1).
		*/

		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");

		assertEquals("Confronting the Eye", eye.getBlueprint().getTitle());
		assertNull(eye.getBlueprint().getSubtitle());
		assertFalse(eye.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eye.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, eye.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, eye.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(eye, Keyword.SUPPORT_AREA));
		assertEquals(1, eye.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipAbilityAdds2ToMakeEachUnboundCompanionStrengthPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToSupportArea(eye);
		scn.MoveCompanionsToTable(aragorn, gimli, pippin, sam);
		scn.MoveAlliesToTable(merchant);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));    // 3 printed + 1 for The Ruling Ring
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsActionAvailable(eye));

		scn.FreepsUseCardAction(eye);
		// The unbound companions, a [gondor] Man, a [dwarven] Dwarf and a [gondor] Hobbit; not Sam (Ring-bound),
		// not Frodo (the Ring-bearer), not the Merchant (an ally).
		assertEquals(2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner},
				new PhysicalCardImpl[]{aragorn, runner2});

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// In the second skirmish, after the first is over, the bonus is still there.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(pippin));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(pippin));
	}

	@Test
	public void IsNotOfferedUntilAGondorManCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCardsToSupportArea(eye);
		scn.MoveCompanionsToTable(pippin, eowyn);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		// A [gondor] Hobbit and a [rohan] Man, and no [gondor] Man.
		assertInHand(aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eye));

		scn.FreepsPlayCard(aragorn);
		int pool = scn.GetTwilight();
		// Aragorn, a [gondor] Man companion, is the only one there is: the ability is offered.
		assertTrue(scn.FreepsActionAvailable(eye));

		scn.FreepsUseCardAction(eye);
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(pippin));
		assertEquals(7, scn.GetStrength(eowyn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorManWhoIsAnAllyIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToSupportArea(eye);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(eye));

		scn.FreepsUseCardAction(eye);
		assertEquals(2, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionPlayedAfterTheUseIsNotStrengthenedNorByASecondUseAndTheLimitRenewsInALaterFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToSupportArea(eye);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();

		assertInHand(gimli);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsUseCardAction(eye);
		assertEquals(2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));

		// Played after the use, an unbound companion is not one of those it made stronger.
		scn.FreepsPlayCard(gimli);
		int pool2 = scn.GetTwilight();
		assertEquals(6, scn.GetStrength(gimli));

		// The card's +1 is given for this phase: a second use adds (2) and makes nobody stronger, Gimli included.
		assertTrue(scn.FreepsActionAvailable(eye));
		scn.FreepsUseCardAction(eye);
		assertEquals(pool2 + 2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship phase of the third turn, the player's next: the bonus of the first is gone, and the
		// ability gives +1 again.
		scn.SkipToSite(2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		int pool3 = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(eye));
		assertEquals("false", scn.FreepsActionNoEffect(eye));
		scn.FreepsUseCardAction(eye);
		assertEquals(pool3 + 2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityAtItsLimitIsStillOfferedAndPaidForAndMakesNobodyStronger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var eye = scn.GetFreepsCard("eye");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(eye);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals("false", scn.FreepsActionNoEffect(eye));
		scn.FreepsUseCardAction(eye);
		assertEquals(2, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));

		// Limit +1: (2) can still be added, so the ability is still offered, flagged as doing nothing
		// (Ketura on Q-0041).
		assertTrue(scn.FreepsActionAvailable(eye));
		assertEquals("limit", scn.FreepsActionNoEffect(eye));

		scn.FreepsUseCardAction(eye);
		assertEquals(4, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
