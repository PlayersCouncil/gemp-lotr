package com.gempukku.lotro.cards.unofficial.pc.errata.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_114_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("demoralized", "61_114");

					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc minion, cost 1, site 4
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll minion
					put("slayer", "3_93");       // Morgul Slayer: [sauron] Orc minion.
					                             // Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("commander", "15_86");   // Mumak Commander: [men] Man minion, vitality 3.
					                             // Maneuver: exert it twice to exert a companion twice (except the Ring-bearer)
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] condition in the support area

					put("sam", "1_311");         // Sam, Son of Hamfast. Fellowship: exert Sam to remove a burden
					put("aragorn", "1_89");      // Aragorn, Ranger of the North
					put("boromir", "1_97");      // Boromir, Son of Denethor
					put("gimli", "1_13");        // Gimli, Son of Gloin
					put("legolas", "1_50");      // Legolas, Greenleaf: cost 2
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: ally. Fellowship: exert Elrond to draw a card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DemoralizedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Demoralized
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [orc] minion.
		 * Each time a companion exerts, you may add (1) (or (2) if you can spot 6 companions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("demoralized");

		assertEquals("Demoralized", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnOrcCultureMinionIsSpottedAndThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var demoralized = scn.GetShadowCard("demoralized");
		var goblin = scn.GetShadowCard("goblin");
		var slayer = scn.GetShadowCard("slayer");
		var beckoning = scn.GetShadowCard("beckoning");

		scn.MoveCardsToHand(demoralized, goblin);
		// An [orc] card that is no minion.
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		// A minion that is an Orc, of the [sauron] culture.
		scn.MoveMinionsToTable(slayer);
		scn.SetTwilight(7);
		scn.SkipToPhase(Phase.SHADOW);

		// 7 set + 1 companion + 2 for the site.
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(demoralized));

		scn.ShadowPlayCard(goblin);
		// 1 to play + 2 roaming.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(demoralized));

		scn.ShadowPlayCard(demoralized);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, demoralized);
		assertNull(demoralized.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyOrcCultureMinionIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var demoralized = scn.GetShadowCard("demoralized");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.MoveCardsToHand(demoralized);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		scn.SetTwilight(7);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(demoralized));

		scn.ShadowPlayCard(demoralized);
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, demoralized);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeACompanionExertsOneTwilightIsAddedWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var demoralized = scn.GetShadowCard("demoralized");

		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(demoralized);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sam));

		// Sam exerts to pay for his own ability; with no burden to remove it does nothing else.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionExertedTwiceByAMinionAddsOneForEachExertionAndTheMinionsOwnExertionsAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var demoralized = scn.GetShadowCard("demoralized");
		var commander = scn.GetShadowCard("commander");

		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(demoralized);

		scn.StartGame();
		scn.MoveMinionsToTable(commander);
		scn.SkipToPhase(Phase.MANEUVER);

		// 3 companions + 2 for the site.
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();
		scn.ShadowUseCardAction(commander);

		// The Commander has exerted twice to pay, and the pool has not moved.
		assertEquals(2, scn.GetWoundsOn(commander));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn));
		scn.ShadowChooseCard(aragorn);

		// Two exertions of one companion by one effect: 1 for each.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAllyExertingAMinionExertingAndACompanionTakingAWoundAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var demoralized = scn.GetShadowCard("demoralized");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(demoralized);

		scn.StartGame();

		// The condition is at work on this board: Sam's exertion adds 1.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetTwilight());

		// An ally exerts.
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.FreepsPass();
		// The minion exerts, and Sam, the one companion who is not the Ring-bearer, takes a wound that is no exertion.
		scn.ShadowUseCardAction(slayer);

		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(pool, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AddsOneWithFiveCompanionsAndAnAllyAndTwoOnceASixthCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var demoralized = scn.GetShadowCard("demoralized");

		// Frodo and four more: 5 companions.  The ally is a sixth character.
		scn.MoveCompanionsToTable(sam, aragorn, boromir, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(legolas);
		scn.MoveCardsToSupportArea(demoralized);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetTwilight());

		// Legolas costs 2 and is the sixth companion.
		scn.FreepsPlayCard(legolas);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		// 3 + 2.
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
