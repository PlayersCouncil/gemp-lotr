package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_073_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mumak", "5_73");

					put("nomad", "7_132");      // Desert Nomad: [raider] Southron Man, strength 9, vitality 2; damage +1 only at 2 threats
					put("scout", "4_252");      // Southron Scout: [raider] Southron Man, Ambush (2), nothing else
					put("sentry", "4_253");     // Southron Sentry: [raider] Southron Man, strength 9; a maneuver ability only
					put("attacker", "7_141");   // Easterling Attacker: a [raider] Man who is no Southron; a skirmish ability only
					put("savage", "15_92");     // Savage Southron: a [men] Man, a Southron by title only; a maneuver ability only
					// Army of Haradrim: Skirmish: Remove (3) to play a [raider] mount from your discard pile
					put("army", "5_70");
					// Bladetusk Rearguard: [raider] artifact that its maneuver ability makes a fierce Southron minion who is no Man
					put("rearguard", "103_43");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MumakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Mûmak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 3
		 * Game Text: Bearer must be a Southron.<br>Bearer is <b>fierce</b>.<br>Each time bearer wins a skirmish, you may add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mumak");

		assertEquals("Mûmak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnASouthronOnlyAndMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var nomad = scn.GetShadowCard("nomad");
		var scout = scn.GetShadowCard("scout");
		var attacker = scn.GetShadowCard("attacker");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(mumak);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad, scout, attacker, savage);
		scn.SetTwilight(10);
		assertInHand(mumak);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(nomad, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(mumak));

		scn.ShadowPlayCard(mumak);
		// Two Southrons may bear it; a [raider] Man who is no Southron and a Southron by title only may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nomad, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attacker, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nomad);

		assertAttachedTo(mumak, nomad);
		assertTrue(scn.HasKeyword(nomad, Keyword.FIERCE));
		// The other Southron is not made fierce.
		assertFalse(scn.HasKeyword(scout, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromTheDiscardPileInASkirmishItMayGoOnASouthronWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var army = scn.GetShadowCard("army");
		var rearguard = scn.GetShadowCard("rearguard");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToSupportArea(rearguard);
		scn.MoveCardsToDiscard(mumak);
		assertInZone(Zone.SUPPORT, rearguard);
		assertInDiscard(mumak);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(army, attacker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		// The Rearguard is a Southron minion who is no Man from here to the end of the turn.
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		// (3) for the Army's ability and (2) for the Mumak.
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		// A Southron, a Man or not; not the Easterling.
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, rearguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attacker, aragorn));
		scn.ShadowChooseCard(rearguard);

		assertAttachedTo(mumak, rearguard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishYouMayAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var nomad = scn.GetShadowCard("nomad");
		var sentry = scn.GetShadowCard("sentry");

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad, sentry);
		scn.AttachCardsTo(nomad, mumak);
		assertAttachedTo(mumak, nomad);
		scn.SkipToAssignments();
		// Nomad 9 + 3 = 12 against Aragorn's 8; Sentry 9, who bears no Mumak, against Gimli's 6.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nomad}, new PhysicalCardImpl[]{gimli, sentry});
		scn.SetTwilight(0);

		// The other Southron wins: nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mumak));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer wins: offered, and taken.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mumak));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());

		// The Mumak makes him fierce: he wins again in the fierce round, and is offered again, and declines.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nomad);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mumak));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBearerLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var nomad = scn.GetShadowCard("nomad");

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.AttachCardsTo(nomad, mumak);
		assertAttachedTo(mumak, nomad);
		// Aragorn 8 + 5 = 13 against the Nomad's 12: he wins, and the Nomad lives through his one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nomad);
		scn.SetTwilight(0);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(nomad));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(nomad));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mumak));
		assertEquals(0, scn.GetTwilight());
		// The Nomad, fierce, goes on to the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
