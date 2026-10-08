package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_03_079_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("malice", "53_79");
					put("runner", "1_178");       // Goblin Runner: [moria] Orc
					put("soldier", "1_271");      // Orc Soldier: [sauron] Orc
					put("scimitar", "1_180");     // Goblin Scimitar: [moria] possession, cost 0, bearer must be a [moria] Orc
					put("armory", "1_173");       // Goblin Armory: [moria] condition, cost 0

					put("bowman", "2_60");        // Goblin Bowman: [moria] Orc, Archer and nothing else
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, no text
					put("taskmaster", "2_90");    // Orc Taskmaster: [sauron] Orc, a Shadow ability used only on purpose
					put("forger", "3_56");        // Isengard Forger: [isengard] Orc, a Shadow ability used only on purpose
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, given another culture where a non-Orc is needed

					put("axe", "1_190");          // Moria Axe: [moria] possession, cost 1, bearer must be a [moria] Orc
					put("spear", "1_182");        // Goblin Spear: [moria] possession, cost 1, bearer must be a [moria] minion
					put("orcscimitar", "1_269");  // Orc Scimitar: [sauron] possession, cost 1, bearer must be a [sauron] Orc
					put("isenaxe", "3_55");       // Isengard Axe: [isengard] possession, cost 0, bearer must be an [isengard] Orc
					put("pinned", "1_192");       // Pinned Down: [moria] condition, cost 1, plays to the support area
					put("stone", "9_47");         // Ithil Stone: [sauron] artifact, cost 0, to play exert a [sauron] minion

					put("legolas", "1_50");       // Legolas, Greenleaf: Archer companion
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Archer ally
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: [elven] companion, not an archer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Malice
		 * Unique: false
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a [moria] Orc and a [sauron] Orc to play up to 2 [sauron] or [moria] items from your discard pile.  Wound a Free Peoples archer for each item played.
		 */

		var scn = GetScenario();

		var malice = scn.GetFreepsCard("malice");

		assertEquals("Malice", malice.getBlueprint().getTitle());
		assertNull(malice.getBlueprint().getSubtitle());
		assertFalse(malice.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, malice.getBlueprint().getSide());
		assertEquals(Culture.MORIA, malice.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, malice.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(malice, Timeword.MANEUVER));
		assertEquals(0, malice.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableUnlessASauronOrMoriaItemInTheDiscardPileCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var axe = scn.GetShadowCard("axe");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(malice);
		//A [moria] card that could be played, but is not an item
		scn.MoveCardsToDiscard(armory);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(0);
		scn.FreepsPass();

		//Playing from the discard pile is treated as a cost: with nothing to play, the event is withheld
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(malice));

		//The Moria Axe could go on the Runner, but costs 1 and the pool is empty
		scn.MoveCardsToDiscard(axe);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(malice));

		//The Scimitar can go on the Runner, and costs nothing
		scn.MoveCardsToDiscard(scimitar);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(malice));
	}

	@Test
	public void PlaysTwoMoriaOrSauronItemsFromDiscardAndWoundsAFreePeoplesArcherForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.MoveCardsToSupportArea(orophin);

		var malice = scn.GetShadowCard("malice");
		var bowman = scn.GetShadowCard("bowman");
		var troop = scn.GetShadowCard("troop");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var forger = scn.GetShadowCard("forger");
		var axe = scn.GetShadowCard("axe");
		var spear = scn.GetShadowCard("spear");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		var isenaxe = scn.GetShadowCard("isenaxe");
		var pinned = scn.GetShadowCard("pinned");
		var stone = scn.GetShadowCard("stone");
		scn.MoveCardsToHand(malice);
		scn.MoveCardsToDiscard(axe, spear, orcscimitar, stone, isenaxe, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(bowman, troop, taskmaster, forger);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(malice));
		scn.ShadowPlayCard(malice);

		// [moria] and [sauron] items, possessions and an artifact; not an [isengard] item that could go on the
		// Forger, nor a [moria] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, spear, orcscimitar, stone));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(isenaxe, pinned));
		scn.ShadowChooseCard(axe);
		// Moria Axe goes on a [moria] Orc
		assertTrue(scn.ShadowHasCardChoiceAvailable(bowman, troop));
		scn.ShadowChooseCard(bowman);

		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, orcscimitar, stone));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(isenaxe, pinned));
		scn.ShadowChooseCard(orcscimitar);

		assertAttachedTo(axe, bowman);
		assertAttachedTo(orcscimitar, taskmaster);
		// 5 - 1 for the Moria Axe - 1 for the Orc Scimitar
		assertEquals(3, scn.GetTwilight());
		// Up to 2: the third item stays where it was.
		assertInZone(Zone.DISCARD, spear, stone, isenaxe, pinned);

		// Two items played: two wounds, each on a Free Peoples archer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(arwen, frodo, bowman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));

		// "For each" may wound the same archer again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, orophin));
		scn.ShadowChooseCard(legolas);

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, malice);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OneItemPlayedGivesOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(orophin);

		var malice = scn.GetShadowCard("malice");
		var bowman = scn.GetShadowCard("bowman");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var axe = scn.GetShadowCard("axe");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		scn.MoveCardsToHand(malice);
		scn.MoveCardsToDiscard(axe, orcscimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(bowman, taskmaster);
		scn.SkipToPhase(Phase.MANEUVER);
		// Enough for one item costing 1, not for two.
		scn.SetTwilight(1);
		scn.FreepsPass();

		scn.ShadowPlayCard(malice);
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, orcscimitar));
		scn.ShadowChooseCard(orcscimitar);

		assertAttachedTo(orcscimitar, taskmaster);
		assertEquals(0, scn.GetTwilight());
		// The Moria Axe can no longer be paid for, so only one item is played.
		assertInZone(Zone.DISCARD, axe);

		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, orophin));
		scn.ShadowChooseCard(orophin);

		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayPlayFewerThanTwoItems() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var malice = scn.GetShadowCard("malice");
		var bowman = scn.GetShadowCard("bowman");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var axe = scn.GetShadowCard("axe");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		scn.MoveCardsToHand(malice);
		scn.MoveCardsToDiscard(axe, orcscimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(bowman, taskmaster);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(malice);

		// "Up to 2": neither play is forced.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, orcscimitar));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);

		assertAttachedTo(axe, bowman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(orcscimitar));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowPass();

		assertInZone(Zone.DISCARD, orcscimitar);
		assertEquals(4, scn.GetTwilight());

		// One item played: one wound, on the one Free Peoples archer.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var bowman = scn.GetShadowCard("bowman");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(malice);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();
		// A [moria] Orc, an Orc of another culture, and a [sauron] minion that is no Orc.
		scn.MoveMinionsToTable(bowman, forger, savage);
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, savage, null, Culture.SAURON));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(malice));

		scn.MoveMinionsToTable(taskmaster);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(malice));
	}

	@Test
	public void IsNotPlayableWithoutAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var malice = scn.GetShadowCard("malice");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var bowman = scn.GetShadowCard("bowman");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		scn.MoveCardsToHand(malice);
		scn.MoveCardsToDiscard(orcscimitar);

		scn.StartGame();
		// A [sauron] Orc, an Orc of another culture, and a [moria] minion that is no Orc.
		scn.MoveMinionsToTable(taskmaster, forger, savage);
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, savage, null, Culture.MORIA));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(malice));

		scn.MoveMinionsToTable(bowman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(malice));
	}
}
