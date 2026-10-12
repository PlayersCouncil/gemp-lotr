package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orcbane", "2_109");
					put("sting", "1_313");        // Sting: bearer must be Frodo; a fellowship or regroup ability
					put("samsting", "8_113");     // Sting, Bane of the Eight Legs: bearer must be Frodo or Sam; a response in a fierce skirmish
					put("glamdring", "1_75");     // Glamdring: bearer must be Gandalf; a fellowship or regroup ability
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: vitality 4
					put("sam", "1_311");          // Sam, Son of Hamfast: vitality 4; a fellowship ability and a response
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("sword", "1_112");        // Ranger's Sword: a hand weapon on Aragorn
					put("hobbitsword", "1_299");  // Hobbit Sword: a [shire] hand weapon with no text
					put("staff", "2_22");         // Gandalf's Staff: a [gandalf] artifact on Gandalf; a skirmish ability

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability
					put("worker", "3_62");        // Isengard Worker: [isengard] Orc, vitality 3; a regroup ability
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2
					put("raiders", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3
					put("lemenya", "1_232");      // Ulaire Lemenya: Nazgul, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcbaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Orc-bane
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot Sting or Glamdring and exert its bearer X times to wound X Orcs or X Uruk-hai.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orcbane");

		assertEquals("Orc-bane", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsStingsBearerOnceToWoundOneOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var sword = scn.GetFreepsCard("sword");
		var orcbane = scn.GetFreepsCard("orcbane");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier, savage, lemenya);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(orcbane));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPlayCard(orcbane);
		// Sting's bearer is the only one there is.  X is at least 1 and at most what Frodo can exert (vitality 4),
		// though only two Orcs and one Uruk-hai are on the table.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// One Orc or one Uruk-hai: either can be done.
		assertTrue(scn.FreepsChoiceAvailable("Orcs"));
		assertTrue(scn.FreepsChoiceAvailable("Uruk-hai"));
		scn.FreepsChoose("Wound X Orcs");

		// Orcs of two cultures; not the Uruk-hai, not the Nazgul.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, lemenya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, orcbane);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ExertsGlamdringsBearerTwiceToWoundTwoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var orcbane = scn.GetFreepsCard("orcbane");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var raiders = scn.GetShadowCard("raiders");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, raiders, worker, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(orcbane);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Two Orcs and three Uruk-hai: either can be done.
		assertTrue(scn.FreepsChoiceAvailable("Orcs"));
		assertTrue(scn.FreepsChoiceAvailable("Uruk-hai"));
		scn.FreepsChoose("Wound X Uruk-hai");

		// Uruk-hai of two cultures; not the Orc of the Uruk-hai's own culture, nor the other Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, guard, raiders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(worker, troop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage, guard);

		// One wound each: the Guard, of vitality 2, is still there.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, orcbane);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneOrcAndXOfTwoTheTwoUrukHaiAreWoundedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var orcbane = scn.GetFreepsCard("orcbane");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, guard);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(orcbane);
		scn.FreepsChoose("2");

		// Two Orcs cannot be wounded and two Uruk-hai can: that is what is done, with nothing asked.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, orcbane);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithBothWeaponsInPlayThePlayerChoosesWhichBearerExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var glamdring = scn.GetFreepsCard("glamdring");
		var sword = scn.GetFreepsCard("sword");
		var orcbane = scn.GetFreepsCard("orcbane");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 1);
		scn.MoveMinionsToTable(troop, soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(orcbane);
		// Sting's bearer or Glamdring's; not Aragorn, who bears another hand weapon.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		// Vitality 4 with 1 wound: Gandalf can exert twice.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// There is no Uruk-hai and there are just two Orcs: nothing more is asked.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void StingBorneBySamExertsSamAndNotFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var samsting = scn.GetFreepsCard("samsting");
		var orcbane = scn.GetFreepsCard("orcbane");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, samsting);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(orcbane));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPlayCard(orcbane);
		// Sam's vitality is 4.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutStingOrGlamdring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var staff = scn.GetFreepsCard("staff");
		var orcbane = scn.GetFreepsCard("orcbane");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		// A [shire] hand weapon on Frodo and a [gandalf] artifact on Gandalf: neither is Sting or Glamdring.
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(orcbane));
		assertInZone(Zone.HAND, orcbane);
	}

	@Test
	public void CannotBePlayedWhenStingsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var sting = scn.GetFreepsCard("sting");
		var orcbane = scn.GetFreepsCard("orcbane");
		var troop = scn.GetShadowCard("troop");
		// Gandalf bears nothing and could exert.
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		// Vitality 4 with 3 wounds: Frodo cannot exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(orcbane));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}

	@Test
	public void WithNoOrcOrUrukHaiItIsPlayedAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		var orcbane = scn.GetFreepsCard("orcbane");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(orcbane);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);

		// An event may be played though it will do nothing.
		assertTrue(scn.FreepsPlayAvailable(orcbane));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(orcbane);
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChoose("1");

		// The cost is paid and the Nazgul is not wounded.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.DISCARD, orcbane);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
