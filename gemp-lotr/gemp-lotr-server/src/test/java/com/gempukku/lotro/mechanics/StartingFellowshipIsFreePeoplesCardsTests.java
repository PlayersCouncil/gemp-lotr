package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A starting fellowship is Free Peoples cards, whatever card types a player may include in it.
 *
 * Comprehensive Rules 4.2, "starting fellowship": "You may then play other companions (not allies, possessions,
 * artifacts, or conditions) from your draw deck, as long as the total twilight cost of these companions is 4 or
 * less."  A card that lets a player include other card types takes away that parenthesis and nothing more: the
 * card types it names have no side word of their own, and the thing they are included in is a fellowship.
 * Ketura, 2026-10-09, on Q-0189: "ha, yes, A. No playing minions during the starting fellowship :)"
 *
 * The one card that lets other card types in is Race Text 94_3 (Race to Mount Doom): "You may include items,
 * conditions, allies, and followers in your starting fellowship."  Before F-0343 it offered its owner every
 * possession, artifact, condition, ally and follower in his draw deck that he could play, of either side: a
 * Shadow condition of cost 0 that plays to the support area went down beside his Free Peoples cards and was in
 * play when the game began, and a Shadow card with a cost was offered as soon as the running total of the
 * fellowship (which the game keeps in the twilight pool) was enough to pay for it.
 *
 * No card in the game is a Shadow ally, so the allies are read from the Free Peoples side only.
 *
 * What is played into a starting fellowship, the total of 4 and the limit on cards of cost 0 are that card's
 * tests (Card_94_003_Tests); these read only what is offered.
 */
public class StartingFellowshipIsFreePeoplesCardsTests
{
	// Race to Mount Doom, with player 1 holding Race Text 94_3.
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion, cost 2
					put("sword", "1_299");      // Hobbit Sword: Free Peoples possession, cost 1, on a Hobbit (the Ring-bearer)
					put("coat", "2_105");       // Mithril-coat: Free Peoples artifact, cost 2, on Frodo
					put("trolls", "3_114");     // Three Monstrous Trolls: Free Peoples condition, cost 0, support area
					put("proudfoot", "1_301");  // Master Proudfoot: ally, cost 1
					put("twofoot", "13_144");   // Daddy Twofoot: Free Peoples follower, cost 1

					// Shadow cards of the same card types, each played with nothing asked of the board
					put("armory", "1_173");     // Goblin Armory: [moria] condition, cost 0, support area
					put("swarms", "1_183");     // Goblin Swarms: [moria] condition, cost 1, support area
					put("poison", "8_28");      // Spider Poison: [gollum] possession, cost 1, support area
					put("library", "9_39");     // Library of Orthanc: [isengard] artifact, cost 2, support area
					put("saruman", "17_38");    // Saruman, Servant of Sauron: [isengard] follower, cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_3", null
		);
	}

	// The same table with player 2 holding Race Text 94_3.
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: companion, cost 2
					put("trolls", "3_114");     // Three Monstrous Trolls: Free Peoples condition, cost 0, support area
					put("armory", "1_173");     // Goblin Armory: [moria] condition, cost 0, support area
					put("swarms", "1_183");     // Goblin Swarms: [moria] condition, cost 1, support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_3"
		);
	}

	@Test
	public void AShadowCardOfCostZeroIsNotOfferedBesideTheFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var trolls = scn.GetFreepsCard("trolls");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var twofoot = scn.GetFreepsCard("twofoot");
		var armory = scn.GetFreepsCard("armory");

		// Nothing has gone down yet: the running total is 0
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());

		// A companion and a Free Peoples card of each card type let in
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, sword, coat, trolls, proudfoot, twofoot));
		// Goblin Armory is a condition of cost 0 as Three Monstrous Trolls is, and a Shadow card
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory));
	}

	@Test
	public void NoShadowCardIsOfferedWhenTheRunningTotalWouldPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var coat = scn.GetFreepsCard("coat");
		var trolls = scn.GetFreepsCard("trolls");
		var proudfoot = scn.GetFreepsCard("proudfoot");
		var twofoot = scn.GetFreepsCard("twofoot");
		var armory = scn.GetFreepsCard("armory");
		var swarms = scn.GetFreepsCard("swarms");
		var poison = scn.GetFreepsCard("poison");
		var library = scn.GetFreepsCard("library");
		var saruman = scn.GetFreepsCard("saruman");

		scn.FreepsChooseCard(gimli);

		// The game keeps the fellowship's running total in the twilight pool: 2 of the 4, which is twilight
		// enough for any of the Shadow cards and leaves room in the 4 for each of them
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(2, scn.GetTwilight());

		// A condition of cost 0 and one of cost 1, a possession, an artifact and a follower
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armory, swarms, poison, library, saruman));
		// The Free Peoples cards of those card types are still offered, the artifact of cost 2 among them
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, coat, trolls, proudfoot, twofoot));
	}

	@Test
	public void TheSecondPlayerIsOfferedNoShadowCardEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var gimli = scn.GetShadowCard("gimli");
		var trolls = scn.GetShadowCard("trolls");
		var armory = scn.GetShadowCard("armory");
		var swarms = scn.GetShadowCard("swarms");

		// Player 1 has no such card and takes nobody
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsDeclineChoosing();

		// A card's side is the side printed on it, whichever seat its owner sits in
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, trolls));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, swarms));

		scn.ShadowChooseCard(gimli);

		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(trolls));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, swarms));
	}
}
