package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "13_92");

					// Footman of Dunland: a [men] Man, strength 7, vitality 3.  Its text makes the current site a plains,
					// which nothing here reads.
					put("footman", "11_82");
					// Saruman, Coldly Still: a [men] minion who is no Man (Wizard), strength 8, vitality 4.  His +2 is
					// for [men] minions bearing a possession, and none does; his skirmish ability is never used.
					put("saruman", "17_48");
					// Axeman of Harad: a [men] Man, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");
					// Goaded to War: a [men] condition, no minion.  Its trigger answers a move to a battleground or
					// plains, and nobody moves.
					put("goaded", "12_67");
					// Morgul Slayer: [sauron] Orc.  Regroup: exert this minion to wound a companion (except the
					// Ring-bearer).
					put("slayer", "3_93");
					// Easterling Axeman: [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens.
					// Two of them are the [raider] Men Fierce in Despair spots.
					put("easterling1", "4_224");
					put("easterling2", "4_224");
					// Fierce in Despair: [raider] Regroup event, cost 7.  Spot 2 [raider] Men to discard a companion
					// (except the Ring-bearer).
					put("despair", "7_148");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Dwarf Guard: a [dwarven] Dwarf, strength 4, vitality 2, no text but a requirement to play.
					put("guard", "1_7");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2.  His maneuver ability lets him take
					// part in skirmishes until the regroup phase.
					put("thrarin", "1_27");
					// Defend It and Hope: [gondor] Maneuver event.  Exert a [gondor] Man to wound a minion once (twice
					// if an Uruk-hai).
					put("defend", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Gríma, Footman of Saruman
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>Each time a Free Peoples character is killed, you may spot another [men] minion to exert each companion.
		*/

		var scn = GetScenario();

		var grima = scn.GetFreepsCard("grima");

		assertEquals("Gríma", grima.getBlueprint().getTitle());
		assertEquals("Footman of Saruman", grima.getBlueprint().getSubtitle());
		assertTrue(grima.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, grima.getBlueprint().getSide());
		assertEquals(Culture.MEN, grima.getBlueprint().getCulture());
		assertEquals(CardType.MINION, grima.getBlueprint().getCardType());
		assertEquals(Race.MAN, grima.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(grima, Keyword.LURKER));
		assertEquals(2, grima.getBlueprint().getTwilightCost());
		assertEquals(4, grima.getBlueprint().getStrength());
		assertEquals(3, grima.getBlueprint().getVitality());
		assertEquals(3, grima.getBlueprint().getSiteNumber());
	}

	@Test
	public void ACompanionKilledLetsGrimaSpotAMenManToExertEachCompanionAndNoAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.MoveAlliesToTable(thrarin);
		// Vitality 3 with 2 wounds: Gimli is exhausted, and an exertion cannot be placed on him.
		scn.AddWoundsToChar(gimli, 2);
		// Vitality 2 with 1 wound: the Guard dies of the wound for losing.
		scn.AddWoundsToChar(guard, 1);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));

		var grima = scn.GetShadowCard("grima");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// The Footman's 7 beats the Guard's 4: the wound kills him.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();

		// Each companion exerts: Frodo and Aragorn.  Exhausted Gimli takes nothing (a wound would kill him); the
		// ally and the minions are no companions.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherMenMinionToSpotAKillOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));

		var grima = scn.GetShadowCard("grima");
		var brigand = scn.GetShadowCard("brigand");
		var goaded = scn.GetShadowCard("goaded");

		scn.StartGame();
		// Beside Gríma himself: a [men] card that is no minion, and a minion of another culture.
		scn.MoveCardsToSupportArea(goaded);
		scn.MoveMinionsToTable(grima, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// The Brigand's 8 beats the Guard's 4: the wound kills him.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledIsOfferedAndDeclinedAndACompanionKilledLaterIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveAlliesToTable(thrarin);

		var grima = scn.GetShadowCard("grima");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, saruman, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thrarin, axeman},
				new PhysicalCardImpl[]{guard, saruman});
		scn.ShadowDeclineAssignments();

		// The Axeman's 11 overwhelms Thrarin's 4: an ally killed.  Saruman, a [men] Wizard, is the minion spotted.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Saruman's 8 overwhelms the Guard's 4: a companion killed, and the offer comes again.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionDiscardedOffersNothingAndOneKilledInTheSamePhaseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));

		var grima = scn.GetShadowCard("grima");
		var footman = scn.GetShadowCard("footman");
		var slayer = scn.GetShadowCard("slayer");
		var easterling1 = scn.GetShadowCard("easterling1");
		var easterling2 = scn.GetShadowCard("easterling2");
		var despair = scn.GetShadowCard("despair");
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, footman, slayer, easterling1, easterling2);
		// No skirmish is fought: the minions stand unassigned into the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// Gimli is discarded from play, not killed.
		scn.ShadowPlayCard(despair);
		scn.ShadowChooseCard(gimli);
		assertInDiscard(gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The Slayer's wound kills the Guard in the same phase: offered.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionKilledOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var grima = scn.GetShadowCard("grima");
		var footman = scn.GetShadowCard("footman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, footman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn exerts to wound the Brigand, of vitality 1: a minion killed.
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(brigand);
		assertInDiscard(brigand);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithEveryCompanionLeftExhaustedAKillOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		// Frodo (vitality 4) and Aragorn (vitality 4) with 3 wounds each: neither can exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));

		var grima = scn.GetShadowCard("grima");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(grima, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(grima));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
