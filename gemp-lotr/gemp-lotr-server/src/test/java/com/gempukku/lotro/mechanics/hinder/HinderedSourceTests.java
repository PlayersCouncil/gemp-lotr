package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A hindered card is still bound by "cannot" in what its text goes on to do.
 *
 * Comprehensive Rules 5.0, "hinder": "If a card is hindered while in the process of evaluating a special ability
 * or trigger on that card, the effects of that text continue to be executed until fulfilled."  So a card that
 * hinders itself as a cost, or is hindered in answer to its ability, is the source of an effect while hindered.
 * The same entry: "If the Ring-bearer is hindered, burdens may be spotted and counted, but they still cannot be
 * added or removed", and "The side/alignment of a hindered card continues to apply."  Ketura on Q-0070: "can't >
 * can; tokens cannot be added > continue to execute."
 *
 * The game used to ask what the source of an effect may not do in the same way it asks what applies to a card,
 * and nothing applies to a hindered card: so a hindered source escaped every such "cannot" (F-0151).
 */
public class HinderedSourceTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cover of Darkness, Omen of Dread: "Response: If a threat is added, hinder an Orc and this
					// condition to add a burden."  Its other response answers a burden with a threat.
					put("dread", "103_94");
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; a skirmish ability that is only ever used on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: an Uruk-hai, strength 8; damage +1 is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: a third companion, so that a threat can be added
					// Stout Resistance: a skirmish event.  "Add a threat to make a [gondor] Man strength +3."
					put("stout", "7_121");

					// Never: a [gollum] condition.  "Regroup: Discard Smeagol to remove 2 threats."
					put("never", "7_65");
					// Smeagol, Always Helps: his text is an assignment ability that is never used here.
					put("smeagol", "7_71");
					// Rallying Call: a [raider] condition.  "Threats cannot be removed by Free Peoples cards."
					put("call", "10_47");
					// Pass of the Spider: "Each time a special ability on a Free Peoples condition is used, you
					// may discard a minion stacked here to hinder that condition and heal Shelob."
					put("pass", "103_12");
					put("runner", "1_178");     // Goblin Runner: the minion stacked there
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardThatHindersItselfAsACostCannotAddABurdenWhileTheRingBearerIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dread = scn.GetShadowCard("dread");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stout = scn.GetFreepsCard("stout");

		scn.MoveCardsToSupportArea(dread);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(stout);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, sentry);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.HinderCard(frodo);

		assertTrue(scn.IsHindered(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.IsHindered(dread));

		// A threat is added, and the condition answers it: the Orc and the condition itself are hindered
		// to pay for the burden.
		scn.FreepsPlayCard(stout);
		assertEquals(1, scn.GetThreats());
		scn.ShadowChooseOptionalTrigger(dread);

		// The condition is hindered by the time its burden would be added, and the burden still cannot be.
		assertTrue(scn.IsHindered(soldier));
		assertTrue(scn.IsHindered(dread));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACardHinderedInAnswerToItsAbilityCannotRemoveThreatsThatItsSideCannotRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var never = scn.GetFreepsCard("never");
		var smeagol = scn.GetFreepsCard("smeagol");
		var call = scn.GetShadowCard("call");
		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(never);
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(call, pass);
		scn.StackCardsOn(pass, runner);

		scn.StartGame();
		scn.AddThreats(2);
		// No minion is in play, so the game goes from the move straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(never));

		// The cost is paid, and Pass of the Spider answers the ability by hindering the condition.
		scn.FreepsUseCardAction(never);
		assertInZone(Zone.DISCARD, smeagol);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));
		scn.ShadowAcceptOptionalTrigger();

		// The ability is carried out all the same, by a card that is hindered and still a Free Peoples card:
		// Rallying Call lets it remove nothing.
		assertTrue(scn.IsHindered(never));
		assertInZone(Zone.DISCARD, runner);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
