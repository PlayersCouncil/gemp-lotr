package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mumak", "5_78");

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
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; Valiant and no other text
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3; a fellowship ability, and a response to Frodo's death
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; Maneuver: exert him to let him skirmish
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarMumakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: War Mûmak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 4
		 * Game Text: Bearer must be a Southron.<br>Bearer is <b>fierce</b>.<br>Each time bearer overwhelms a character, you may add (5).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mumak");

		assertEquals("War Mûmak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
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
		// (3) for the Army's ability and (3) for the War Mumak.
		scn.SetTwilight(6);
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
	public void EachTimeBearerOverwhelmsACharacterYouMayAdd5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var nomad = scn.GetShadowCard("nomad");
		var sentry = scn.GetShadowCard("sentry");

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, guard, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad, sentry);
		scn.AttachCardsTo(nomad, mumak);
		assertAttachedTo(mumak, nomad);
		scn.SkipToAssignments();
		// Nomad 9 + 4 = 13 against Gimli's 6 (13 is at least twice 6); Sentry 9, who bears no Mumak, against Sam's 3.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, nomad}, new PhysicalCardImpl[]{sam, sentry});
		scn.SetTwilight(0);

		// The other Southron overwhelms Sam: nothing is offered.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mumak));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer overwhelms Gimli: offered, and taken.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mumak));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(5, scn.GetTwilight());

		// The War Mumak makes him fierce: he overwhelms the Household Guard (6) in the fierce round, is offered again, and declines.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, nomad);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mumak));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerOverwhelmingAnAllyIsOfferedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var nomad = scn.GetShadowCard("nomad");

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.AttachCardsTo(nomad, mumak);
		assertAttachedTo(mumak, nomad);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		// Nomad 9 + 4 = 13 against Thrarin's 4.
		scn.FreepsAssignToMinions(thrarin, nomad);
		scn.SetTwilight(0);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, thrarin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mumak));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		// The Nomad, fierce, goes on to the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhenBearerWinsWithoutOverwhelmingNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mumak = scn.GetShadowCard("mumak");
		var nomad = scn.GetShadowCard("nomad");

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.AttachCardsTo(nomad, mumak);
		assertAttachedTo(mumak, nomad);
		scn.SkipToAssignments();
		// Nomad 9 + 4 = 13 against Aragorn's 8: a win, short of the 16 that would overwhelm him.
		scn.FreepsAssignToMinions(aragorn, nomad);
		scn.SetTwilight(0);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(mumak));
		assertEquals(0, scn.GetTwilight());
		// The Nomad, fierce, goes on to the fierce round.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
