package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "102_77");
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: "Fellowship: Exert Gandalf to play a companion who has the Gandalf signet", at -2
					put("shirefolk", "1_72");     // Gandalf, Friend of the Shirefolk: another Gandalf, cost 4; his text is a strength bonus of his own
					put("mithrandir", "6_30");    // Gandalf, Mithrandir: strength 10; "Each time the fellowship moves, wound Gandalf."
					put("radagast", "9_26");      // Radagast, The Brown: a [gandalf] Wizard companion who is not Gandalf
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: Gandalf signet; "Maneuver: Exert Aragorn to make him defender +1"

					// Glamdring (The Hobbit): an artifact that is a hand weapon, bearer must be Gandalf.  Its trigger
					// answers a skirmish its bearer wins.
					put("glamdring", "30_29");
					put("brand", "6_31");         // Glamdring, Lightning Brand: possession, hand weapon, bearer must be Gandalf, cost 2
					put("pipe", "1_74");          // Gandalf's Pipe: a possession that is no weapon
					put("sword", "1_112");        // Ranger's Sword: hand weapon, bearer must be Aragorn

					put("sleep1", "1_84");        // Sleep, Caradhras: [gandalf] event, cost 3, "Fellowship: Exert Gandalf to discard every condition."
					put("sleep2", "1_84");
					put("friends", "2_98");       // Dear Friends: [shire] event, cost 0, "Fellowship: Exert a companion to place a Free Peoples card from your discard pile beneath your draw deck"
					put("betrayal", "3_29");      // Betrayal of Isengard: [gandalf] condition, "Maneuver: Exert Gandalf to wound Saruman twice."

					put("eye", "1_281");          // Under the Watching Eye: Shadow condition, "Each time the fellowship moves, the Free Peoples player must exert a companion."
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Gandalf's Staff, Old Man's Crutch
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: +1
		 * Game Text: Bearer must be Gandalf.
		 * 	Discard any weapon he bears.
		 * 	Response: If Gandalf is exerted by a Free Peoples card, heal him (limit once per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertEquals("Old Man's Crutch", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForTwoOnAGandalfAndOnNoOneElseAndRaisesHisVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var shirefolk = scn.GetFreepsCard("shirefolk");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		// A [gandalf] Wizard with the Gandalf signet who is not Gandalf, and a Man with that signet.
		scn.MoveCompanionsToTable(radagast, aragorn);
		scn.MoveCardsToHand(staff, shirefolk);

		scn.StartGame();

		// With Radagast, Aragorn and Frodo on the table and no Gandalf, there is nobody to bear it.
		assertEquals(0, scn.GetTwilight());
		assertInHand(staff);
		assertFalse(scn.FreepsPlayAvailable(staff));

		scn.FreepsPlayCard(shirefolk);
		assertInZone(Zone.FREE_CHARACTERS, shirefolk);
		// 4 for Gandalf
		assertEquals(4, scn.GetTwilight());
		assertEquals(4, scn.GetVitality(shirefolk));
		assertEquals(4, scn.GetVitality(radagast));
		assertTrue(scn.FreepsPlayAvailable(staff));

		// Gandalf is the only one who may bear it, so nobody is asked.
		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, shirefolk);
		assertEquals(5, scn.GetVitality(shirefolk));
		assertEquals(4, scn.GetVitality(radagast));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsTheWeaponGandalfBearsWhenPlayedAndAnyWeaponPlayedOnHimAfterwards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glamdring = scn.GetFreepsCard("glamdring");
		var brand = scn.GetFreepsCard("brand");
		var pipe = scn.GetFreepsCard("pipe");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		// Gandalf bears the one weapon he may, a hand weapon that is an artifact, and his Pipe.
		scn.AttachCardsTo(gandalf, glamdring, pipe);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(staff, brand);

		var eye = scn.GetShadowCard("eye");
		scn.MoveCardsToSupportArea(eye);

		scn.StartGame();

		// Without the Staff he bears his weapon.
		assertAttachedTo(glamdring, gandalf);
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(sword, aragorn);

		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, gandalf);
		assertInDiscard(glamdring);
		// His Pipe is no weapon, and Aragorn's sword is not his.
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A weapon can still be played on him, this one a possession: it is paid for, and discarded as it
		// arrives.
		assertTrue(scn.FreepsPlayAvailable(brand));
		scn.FreepsPlayCard(brand);
		// 2 for the Staff + 2 for Glamdring
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(brand);
		assertAttachedTo(staff, gandalf);
		assertAttachedTo(pipe, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Aragorn's sword is still on the table and is no business of the Staff's. So when the fellowship
		// moves, Under the Watching Eye's exertion is the only thing that has to happen: the Free Peoples
		// player is asked whom to exert at once, with no second required action to put in order beside it.
		scn.FreepsPass();
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseHealsGandalfWhenHisOwnAbilityExertsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, radagast);
		scn.AttachCardsTo(gandalf, staff);
		scn.AddWoundsToChar(radagast, 1);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsUseCardAction(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// "Him" is Gandalf: the other wounded Wizard is not healed.
		assertEquals(1, scn.GetWoundsOn(radagast));

		// Gandalf's ability goes on to its effect: Aragorn, the one companion in hand with his signet, for 4 - 2.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsLimitedToOnceInATurnWhateverThePhaseAndIsOfferedAgainNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep1 = scn.GetFreepsCard("sleep1");
		var sleep2 = scn.GetFreepsCard("sleep2");
		var betrayal = scn.GetFreepsCard("betrayal");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(sleep1, sleep2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(sleep1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Sleep, Caradhras discards every condition, so Betrayal of Isengard goes down after it.
		scn.MoveCardsToSupportArea(betrayal);
		scn.SkipToPhase(Phase.MANEUVER);

		// Another phase of the same turn: the limit is for the turn.
		assertTrue(scn.FreepsActionAvailable(betrayal));
		scn.FreepsUseCardAction(betrayal);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Staff's owner's next turn.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(sleep2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseMayBeDeclinedWhichLeavesGandalfExertedAndTheLimitUnspent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var friends = scn.GetFreepsCard("friends");
		var sleep1 = scn.GetFreepsCard("sleep1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(friends, sleep1);

		scn.StartGame();

		// Dear Friends is a [shire] card, and exerts whichever companion its player picks.
		scn.FreepsPlayCard(friends);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, frodo));
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The response was not used, so the next exertion this turn offers it again.
		scn.FreepsPlayCard(sleep1);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAShadowCardExertsGandalfNorWhenAnotherCompanionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var betrayal = scn.GetFreepsCard("betrayal");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(betrayal);

		var eye = scn.GetShadowCard("eye");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(eye);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// The fellowship moves, and Under the Watching Eye has the Free Peoples player exert a companion.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn));
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Aragorn exerts for his own ability: a Free Peoples card exerting someone who is not Gandalf.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// Neither spent the limit: exerted now by Betrayal of Isengard, Gandalf is offered the heal.
		scn.FreepsUseCardAction(betrayal);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(staff));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFreePeoplesCardWoundsGandalfNorWhenASiteExertsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var mithrandir = scn.GetFreepsCard("mithrandir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(mithrandir);
		scn.AttachCardsTo(mithrandir, staff);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// The fellowship moves, and Mithrandir's own text wounds him: a wound by a Free Peoples card, and
		// no exertion.
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(mithrandir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(mithrandir, runner);

		// Site 2, Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2."
		// A site is neither a Free Peoples card nor a Shadow card.
		var ettenmoors = scn.GetCurrentSite();
		assertEquals(10, scn.GetStrength(mithrandir));
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsUseCardAction(ettenmoors);
		assertTrue(scn.FreepsHasCardChoiceAvailable(mithrandir, frodo));
		scn.FreepsChooseCard(mithrandir);
		assertEquals(2, scn.GetWoundsOn(mithrandir));
		assertEquals(12, scn.GetStrength(mithrandir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
