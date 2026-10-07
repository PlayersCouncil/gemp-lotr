package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Whose support area a card is in.
 *
 * Comprehensive Rules 4.2 has "support area" ("a row of cards behind your fellowship") and says nothing of a card
 * that another card in it bears.  Ketura, 2026-10-07 (Q-0101), on the texts that say "in / from <a player's>
 * support area": "The borne cards are physically placed under the cards which bear them, and so by transitive
 * property I would expect that if their bearer is 'in the support area' then they must, too."  "If my opponent's
 * Black Breath is on my Elrond, it's in my support area, because that's where Elrond is."  "Stacked cards are
 * definitely not seen."
 *
 * So a card is in the support area of the player in whose support area the card at the bottom of its chain of
 * bearers lies, whoever owns it; a card stacked on a card there is in nobody's.  The definitions say it with
 * support(you), support(opponent), support(free people) and the plain `support` (anybody's); mechanics/
 * SupportAreaTests has what a "support card" is.
 */
public class WhoseSupportAreaTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Bilbo, Well-spoken Gentlehobbit: an ally. "Fellowship: Exert Bilbo and discard a tale to discard
					// a Shadow condition from an opponent's support area."
					put("bilbo", "2_96");
					put("redbook1", "2_113");   // Red Book of Westmarch: a tale in the support area; answers a tale being played
					put("redbook2", "2_113");
					put("promise", "2_112");    // A Promise: Free Peoples condition, support area; needs Sam to do anything

					// Great Peril of Fire: a Shadow condition in the support area. "Maneuver: Discard an engine to
					// discard a condition from the Free Peoples support area."
					put("peril", "7_280");
					put("towers", "7_281");     // Great Siege-towers: an engine, and a Shadow condition in the support area
					put("pinned1", "1_192");    // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					put("pinned2", "1_192");
					put("armory", "1_157");     // Uruk-hai Armory: Shadow condition, support area; idle without an Uruk-hai
					put("scaffolding", "6_73"); // Scaffolding: Shadow possession, support area; its text is about [isengard] Orcs
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary", "4_212");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AShadowConditionOnAnAllyIsInTheFreePeoplesPlayersSupportAreaAndNotInItsOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var redbook = scn.GetFreepsCard("redbook1");
		var promise = scn.GetFreepsCard("promise");
		var peril = scn.GetShadowCard("peril");
		var towers = scn.GetShadowCard("towers");
		var pinned = scn.GetShadowCard("pinned1");
		var armory = scn.GetShadowCard("armory");
		var weary = scn.GetShadowCard("weary");
		var runner = scn.GetShadowCard("runner");

		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(redbook, promise, peril, towers, pinned, armory);
		// The Shadow player's card, on the Free Peoples player's ally
		scn.AttachCardsTo(bilbo, weary);

		scn.StartGame();

		// "A Shadow condition from an opponent's support area", asked of the Free Peoples player: the conditions
		// lying in the Shadow player's support area.  The Weary on Bilbo is a Shadow condition and the
		// opponent's card, and is not there.
		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInDiscard(redbook);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned, armory, peril, towers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weary));
		scn.FreepsChooseCard(armory);
		assertInDiscard(armory);
		assertAttachedTo(weary, bilbo);

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// "A condition from the Free Peoples support area", asked of the Shadow player: the Free Peoples
		// player's condition lying there, and the Shadow player's own Weary on Bilbo.  Not the conditions in
		// the Shadow player's support area.
		scn.ShadowUseCardAction(peril);
		assertInDiscard(towers);
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, peril));
		scn.ShadowChooseCard(weary);
		assertInDiscard(weary);
		assertInZone(Zone.SUPPORT, promise, pinned, peril, bilbo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACardStackedOnACardInASupportAreaIsInNobodysSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var redbook1 = scn.GetFreepsCard("redbook1");
		var redbook2 = scn.GetFreepsCard("redbook2");
		var promise = scn.GetFreepsCard("promise");
		var peril = scn.GetShadowCard("peril");
		var towers = scn.GetShadowCard("towers");
		var pinned1 = scn.GetShadowCard("pinned1");
		var pinned2 = scn.GetShadowCard("pinned2");
		var armory = scn.GetShadowCard("armory");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var runner = scn.GetShadowCard("runner");

		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(redbook1, promise, peril, towers, pinned1, armory, scaffolding);
		// A Shadow condition stacked on a card in the Shadow player's support area, and a Free Peoples
		// condition stacked on a card in the Free Peoples player's
		scn.StackCardsOn(scaffolding, pinned2);
		scn.StackCardsOn(bilbo, redbook2);

		scn.StartGame();

		scn.FreepsUseCardAction(bilbo);
		assertInDiscard(redbook1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pinned1, armory));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pinned2, scaffolding));
		scn.FreepsChooseCard(pinned1);
		assertInDiscard(pinned1);
		assertInZone(Zone.STACKED, pinned2);

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Bilbo's own condition is the only one in the Free Peoples support area, so it is taken without a
		// question; the tale stacked on Bilbo is not discarded
		scn.ShadowUseCardAction(peril);
		assertInDiscard(towers);
		assertInDiscard(promise);
		assertInZone(Zone.STACKED, redbook2);
		assertInZone(Zone.STACKED, pinned2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
