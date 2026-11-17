package com.gempukku.lotro.cards.official.set16;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_16_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dread", "16_5");
					put("faces", "16_4");       // Dead Faces: [wraith] Wraith, cost 2; its trigger answers the Free Peoples player's assignment and costs (2)
					put("corpses", "16_2");     // Candle Corpses: [wraith] Wraith of another title; its trigger answers the Free Peoples player's assignment only
					// Barrow-wight Stalker: [wraith] Wraith; "Shadow: Remove a burden to play a [wraith] Wraith. That minion's
					// twilight cost is -2"; its bar on wounds outside skirmishes reaches no test here
					put("stalker", "16_1");
					put("lights", "6_99");      // Corpse Lights: a Wraith who is not [wraith] ([sauron]); a Skirmish ability never used
					// Morgul Brute: a [wraith] minion who is no Wraith (an Orc); "Skirmish: Remove a burden to make this minion
					// strength +3"; cheated in, so its play trigger never fires
					put("brute", "7_188");
					// Dark Swooping Shadows: [wraith] Skirmish event, cost 1, "Heal an enduring [wraith] minion to add a burden
					// or 2 threats": a heal of a minion, paid as a cost
					put("swooping", "10_58");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a second companion; a Skirmish ability never used
					put("sturdy", "1_315");     // Stout and Sturdy: [shire] Maneuver or Skirmish event, "Heal a Hobbit"
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, vitality 2; a Skirmish ability never used
					// Dead Ones: a [sauron] Wraith (a Wraith who is not [wraith]); "To play this minion, remove a burden or
					// spot a twilight minion": a burden removed by a Wraith's own text, with no twilight minion on the table
					put("ones", "6_100");
					// King of the Dead, Dead Keeper: [gondor] Wraith companion; "Assignment: Exert King of the Dead and restore
					// a Wraith to heal that Wraith"
					put("king", "103_27");
					// Dead Man of Dunharrow: [gondor] Wraith companion; cheated in, so its play trigger never fires
					put("deadman", "10_27");
					// Faramir, Captain of Ithilien: [gondor] Man companion; "Maneuver: Exert Faramir to heal another companion
					// with resistance 5 or more": a heal by a card that is no Wraith, of a Wraith (the Dead Man, resistance 6)
					put("faramir", "18_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiritofDreadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 16
		 * Name: Spirit of Dread
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Enduring</b>. While you can spot another [wraith] Wraith, wounds cannot be healed and burdens can not be removed (except by Wraiths).<br><b>Skirmish:</b> Exert Spirit of Dread to make another [wraith] Wraith strength +1.
		*/

		var scn = GetScenario();

		var dread = scn.GetFreepsCard("dread");

		assertEquals("Spirit of Dread", dread.getBlueprint().getTitle());
		assertNull(dread.getBlueprint().getSubtitle());
		assertTrue(dread.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, dread.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, dread.getBlueprint().getCulture());
		assertEquals(CardType.MINION, dread.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, dread.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(dread, Keyword.ENDURING));
		assertEquals(7, dread.getBlueprint().getTwilightCost());
		assertEquals(14, dread.getBlueprint().getStrength());
		assertEquals(3, dread.getBlueprint().getVitality());
		assertEquals(2, dread.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAnotherWraithWraithNeitherACompanionAnAllyNorAMinionCanBeHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sturdy = scn.GetFreepsCard("sturdy");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sturdy);

		var dread = scn.GetShadowCard("dread");
		var faces = scn.GetShadowCard("faces");
		var swooping = scn.GetShadowCard("swooping");
		scn.MoveCardsToHand(swooping);

		scn.StartGame();
		scn.MoveMinionsToTable(dread, faces);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(dread, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(dread));
		assertInHand(sturdy, swooping);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(sturdy));
		scn.FreepsPlayCard(sturdy);
		// The companion and the ally, the two Hobbits it could heal, both keep their wound.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertInDiscard(sturdy);

		scn.SkipToAssignments();
		// (1) pays the event and not the Dead Faces' trigger on its assignment, which removes (2).
		scn.SetTwilight(1);
		scn.FreepsAssignToMinions(aragorn, faces);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Its heal is the event's cost, and no minion can pay it.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(swooping));
		assertEquals(1, scn.GetWoundsOn(dread));
	}

	@Test
	public void WithNoOtherWraithWraithWoundsAreHealedAndBurdensRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sturdy = scn.GetFreepsCard("sturdy");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sturdy);

		var dread = scn.GetShadowCard("dread");
		var lights = scn.GetShadowCard("lights");
		var brute = scn.GetShadowCard("brute");
		var swooping = scn.GetShadowCard("swooping");
		scn.MoveCardsToHand(swooping);

		scn.StartGame();
		// Corpse Lights is a Wraith of another culture, Morgul Brute a [wraith] minion who is no Wraith, and Spirit of
		// Dread does not spot itself.
		scn.MoveMinionsToTable(dread, lights, brute);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(dread, 1);
		scn.AddBurdens(2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(dread));
		assertEquals(2, scn.GetBurdens());
		assertInHand(sturdy, swooping);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsPlayAvailable(sturdy));
		scn.FreepsPlayCard(sturdy);
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.SetTwilight(5);
		scn.FreepsAssignToMinions(aragorn, brute);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(swooping));
		scn.ShadowPlayCard(swooping);
		assertEquals(0, scn.GetWoundsOn(dread));
		scn.ShadowChoose("Add a burden");
		assertEquals(3, scn.GetBurdens());

		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(brute));
		assertTrue(scn.ShadowActionAvailable(brute));
		scn.ShadowUseCardAction(brute);
		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(brute));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnotherWraithWraithOnlyAWraithCanRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dread = scn.GetShadowCard("dread");
		var stalker = scn.GetShadowCard("stalker");
		var brute = scn.GetShadowCard("brute");
		var faces = scn.GetShadowCard("faces");
		var ones = scn.GetShadowCard("ones");
		scn.MoveCardsToHand(faces, ones);

		scn.StartGame();
		scn.MoveMinionsToTable(dread, stalker, brute);
		scn.AddBurdens(3);
		scn.SetTwilight(5);
		assertEquals(3, scn.GetBurdens());
		assertInHand(faces, ones);

		scn.FreepsPass();
		// The Barrow-wight Stalker is a Wraith: its removal of a burden is allowed.
		assertTrue(scn.ShadowActionAvailable(stalker));
		scn.ShadowUseCardAction(stalker);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, faces);

		// Dead Ones is a Wraith of another culture: the burden its own play removes is allowed too.
		assertTrue(scn.ShadowPlayAvailable(ones));
		scn.ShadowPlayCard(ones);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, ones);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brute);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Morgul Brute is [wraith] but no Wraith: its cost cannot be paid.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(brute));
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void WithAnotherWraithWraithAWraithsOwnTextMayStillHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(king, deadman);

		var dread = scn.GetShadowCard("dread");
		var faces = scn.GetShadowCard("faces");

		scn.StartGame();
		scn.MoveMinionsToTable(dread, faces);
		scn.AddWoundsToChar(deadman, 1);
		scn.HinderCard(deadman);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(king));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(king));
		scn.FreepsUseCardAction(king);
		assertEquals(1, scn.GetWoundsOn(king));
		// King of the Dead is a Wraith, and the heal is his own text's.
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithAnotherWraithWraithAHealByACardThatIsNoWraithIsStillRefused() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(faramir, deadman);

		var dread = scn.GetShadowCard("dread");
		var faces = scn.GetShadowCard("faces");

		scn.StartGame();
		scn.MoveMinionsToTable(dread, faces);
		scn.AddWoundsToChar(deadman, 1);
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.SkipToPhase(Phase.MANEUVER);
		// The exertion is a cost and is paid; the heal is Faramir's, who is no Wraith, so the Dead Man (a Wraith
		// himself) is not healed: the exception goes by who heals, not by who is healed.
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsSpiritOfDreadToMakeAnotherWraithWraithStrongerForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var dread = scn.GetShadowCard("dread");
		var faces = scn.GetShadowCard("faces");
		var corpses = scn.GetShadowCard("corpses");
		var lights = scn.GetShadowCard("lights");
		var brute = scn.GetShadowCard("brute");

		scn.StartGame();
		scn.MoveMinionsToTable(dread, faces, corpses, lights, brute);

		scn.SkipToAssignments();
		// With no twilight the Dead Faces' trigger on its assignment cannot be paid.
		scn.SetTwilight(0);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, faces}, new PhysicalCardImpl[]{gimli, lights});
		// Spirit of Dread, the Candle Corpses and the Brute are left unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(dread));
		assertEquals(8, scn.GetStrength(corpses));

		assertTrue(scn.ShadowActionAvailable(dread));
		scn.ShadowUseCardAction(dread);
		assertEquals(1, scn.GetWoundsOn(dread));
		// Another [wraith] Wraith, in this skirmish or in none; not Spirit of Dread himself, not the [sauron] Wraith,
		// not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(faces, corpses));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dread, lights, brute));
		scn.ShadowChooseCard(corpses);
		assertEquals(9, scn.GetStrength(corpses));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(corpses));
		scn.PassCurrentPhaseActions();

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(corpses));
	}

	@Test
	public void SkirmishAbilityWithNoOtherWraithWraithIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dread = scn.GetShadowCard("dread");
		var lights = scn.GetShadowCard("lights");
		var brute = scn.GetShadowCard("brute");

		scn.StartGame();
		// Corpse Lights is a Wraith of another culture and Morgul Brute a [wraith] minion who is no Wraith: nothing
		// here is "another [wraith] Wraith" to choose.
		scn.MoveMinionsToTable(dread, lights, brute);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brute);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(dread));
		int lightsStrength = scn.GetStrength(lights);
		int bruteStrength = scn.GetStrength(brute);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dread));

		scn.ShadowUseCardAction(dread);
		assertEquals(1, scn.GetWoundsOn(dread));
		assertEquals(lightsStrength, scn.GetStrength(lights));
		assertEquals(bruteStrength, scn.GetStrength(brute));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedSpiritOfDreadCannotUseHisSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dread = scn.GetShadowCard("dread");
		var faces = scn.GetShadowCard("faces");

		scn.StartGame();
		scn.MoveMinionsToTable(dread, faces);
		scn.AddWoundsToChar(dread, 2);
		assertEquals(2, scn.GetWoundsOn(dread));

		scn.SkipToAssignments();
		scn.SetTwilight(0);
		scn.FreepsAssignToMinions(aragorn, faces);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: exhausted.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dread));
	}
}
