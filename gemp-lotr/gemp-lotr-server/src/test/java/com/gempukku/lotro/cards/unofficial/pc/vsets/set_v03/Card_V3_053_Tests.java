package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_053_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("honor", "103_53");
					put("explorer", "4_250");     // Southron Explorer: Southron, strength 5, vitality 3; a regroup ability never used here
					put("nomad", "7_132");        // Desert Nomad: Southron, strength 9, vitality 2; damage +1 only while there are 2 threats
					put("easterling", "7_141");   // Easterling Attacker: [raider] Man, Easterling and no Southron, vitality 2; a skirmish ability never used here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text at all
					// Bladetusk Rearguard: [raider] artifact in the support area. Maneuver: remove (5) to make it a fierce
					// mounted Southron minion until the end of the turn, strength 5, vitality 5, no race
					put("rearguard", "103_43");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: unbound [dwarven] Dwarf
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("bounder", "1_286");      // Bounder: ally whose home is site 2, where these tests are fought
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HonoroftheDesertWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Honor of the Desert Warrior
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Exert a Southron and hinder all other minions to assign that Southron to an unbound companion.
		 * 	The Free Peoples player may make that Southron strength +3, <b>ambush (4)</b>, and <b>fierce</b> until
		 * 	the regroup phase to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("honor");

		assertEquals("Honor of the Desert Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASouthronAndHindersEveryOtherMinionToAssignItToAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var explorer = scn.GetShadowCard("explorer");
		var nomad = scn.GetShadowCard("nomad");
		var easterling = scn.GetShadowCard("easterling");
		var troop = scn.GetShadowCard("troop");
		var rearguard = scn.GetShadowCard("rearguard");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(honor);
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, nomad, easterling, troop);
		// Every card printed a Southron is a [raider] Man. The Southron who is no Man is a Bladetusk Rearguard that
		// has made itself a Southron minion for the turn by its own Maneuver ability.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(14);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		// An Assignment event: the Shadow player cannot play it in the archery phase before.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(honor));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// 14 set in the maneuver phase - 5 for the Rearguard's ability
		assertEquals(9, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertFalse(scn.IsHindered(nomad));
		assertFalse(scn.IsHindered(easterling));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(rearguard));
		assertTrue(scn.ShadowPlayAvailable(honor));

		scn.ShadowPlayCard(honor);

		// 9 - 2 to play
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		// A Southron: two Southron Men and the Southron minion that is no Man; not the Easterling, not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, nomad, rearguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(easterling, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(explorer);

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertFalse(scn.IsHindered(explorer));
		// All other minions, Southron or not, [raider] or not, Man or not.
		assertTrue(scn.IsHindered(nomad));
		assertTrue(scn.IsHindered(easterling));
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.IsHindered(rearguard));
		assertEquals(0, scn.GetWoundsOn(nomad));

		// An unbound companion: not the Ring-bearer, not Ring-bound Sam, and not an ally,
		// though this ally is at home and could be assigned.
		assertTrue(scn.FreepsCanAssign(bounder));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(gimli);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to make"));
		assertFalse(scn.IsCharAssigned(explorer));

		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(gimli, explorer));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(nomad));
		// Declined, so the Southron is as it was.
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(explorer, Keyword.FIERCE));
		assertInDiscard(honor);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayPreventTheAssignmentByMakingTheSouthronStrongerWithAmbushFourAndFierceUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var nomad = scn.GetShadowCard("nomad");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(honor);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad, easterling);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// 3 set + 2 for Frodo and Aragorn + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(nomad));
		assertFalse(scn.IsHindered(easterling));

		// The Nomad is the only Southron and Aragorn the only unbound companion, so neither is asked for.
		scn.ShadowPlayCard(honor);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to make"));
		assertEquals(9, scn.GetStrength(nomad));
		assertEquals(0, scn.GetKeywordCount(nomad, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(nomad, Keyword.FIERCE));

		scn.FreepsChooseYes();

		// Strength 9 + 3
		assertEquals(12, scn.GetStrength(nomad));
		assertEquals(4, scn.GetKeywordCount(nomad, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(nomad, Keyword.FIERCE));
		// The assignment is prevented, and what was paid for it stays paid.
		assertFalse(scn.IsCharAssigned(nomad));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(1, scn.GetWoundsOn(nomad));
		assertTrue(scn.IsHindered(easterling));
		// 7 - 2 to play
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(honor);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Assigned to by the Free Peoples player, the Nomad's ambush (4) is its owner's to use.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nomad);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// 5 + 4 for ambush (4)
		assertEquals(9, scn.GetTwilight());

		// The Nomad (12) beats Aragorn (8).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Fierce: a second assignment and a second skirmish, in which everything given still holds.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nomad);
		scn.ShadowAcceptOptionalTrigger();
		// 9 + 4 for ambush (4) again
		assertEquals(13, scn.GetTwilight());
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, nomad));
		assertEquals(12, scn.GetStrength(nomad));
		assertEquals(4, scn.GetKeywordCount(nomad, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(nomad, Keyword.FIERCE));

		scn.PassSkirmishActions();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, nomad);
		assertEquals(9, scn.GetStrength(nomad));
		assertEquals(0, scn.GetKeywordCount(nomad, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(nomad, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySouthronIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var explorer = scn.GetShadowCard("explorer");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(honor);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, easterling);
		// The Southron has 1 vitality left and cannot exert; the Easterling could, and is no Southron.
		scn.AddWoundsToChar(explorer, 2);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// 3 set + 2 for Frodo and Aragorn + 2 for the shadow number of site 2: more than the 2 it costs
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(easterling));
		assertInZone(Zone.HAND, honor);
		assertFalse(scn.ShadowPlayAvailable(honor));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNoOtherMinionALoneSouthronStillPaysAndIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(honor);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// "All other minions" is nobody here, and the cost can still be paid.
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowPlayAvailable(honor));

		scn.ShadowPlayCard(honor);

		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertFalse(scn.IsHindered(explorer));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to make"));

		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, explorer));
		assertInDiscard(honor);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionItCanStillBePlayedAndItsCostsArePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honor = scn.GetShadowCard("honor");
		var explorer = scn.GetShadowCard("explorer");
		var easterling = scn.GetShadowCard("easterling");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(honor);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, easterling);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// 3 set + 2 for Frodo and Sam + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertFalse(scn.IsHindered(easterling));
		// Everything before "to" can be paid; the assignment after it is an effect that will find nobody.
		assertTrue(scn.ShadowPlayAvailable(honor));

		scn.ShadowPlayCard(honor);

		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.IsHindered(easterling));
		// The Free Peoples player's "may ... to prevent this" has a cost he can pay, so he is asked,
		// though there is no assignment for it to prevent.
		assertTrue(scn.FreepsDecisionAvailable("Would you like to make"));

		scn.FreepsChooseNo();

		assertInDiscard(honor);
		assertFalse(scn.IsCharAssigned(explorer));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
