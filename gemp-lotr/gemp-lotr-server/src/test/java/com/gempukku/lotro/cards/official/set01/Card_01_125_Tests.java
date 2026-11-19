package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("greed", "1_125");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2; damage +1 only
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai minion; damage +1 only
					put("worker", "3_62");      // Isengard Worker: an [isengard] Orc minion; a Regroup ability used only on purpose
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard); each companion resistance -1

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a Maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an Archery ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Greed
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an Uruk-hai. Plays to your support area.<br><b>Maneuver:</b> Spot 6 companions and remove (2) to wound a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("greed");

		assertEquals("Greed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var greed = scn.GetShadowCard("greed");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(greed, savage);

		scn.StartGame();
		assertInHand(greed);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// An [isengard] minion who is an Orc, and an [uruk-hai] minion who is a Wizard: neither is an Uruk-hai.
		scn.MoveMinionsToTable(worker, saruman);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(greed);
		assertFalse(scn.ShadowPlayAvailable(greed));

		scn.ShadowPlayCard(savage);
		assertInPlay(savage);
		assertTrue(scn.ShadowPlayAvailable(greed));

		scn.ShadowPlayCard(greed);
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedBesideAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var greed = scn.GetShadowCard("greed");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(greed);

		scn.StartGame();
		assertInHand(greed);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// A [sauron] Uruk-hai.
		scn.MoveMinionsToTable(guard);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(greed);
		assertTrue(scn.ShadowPlayAvailable(greed));

		scn.ShadowPlayCard(greed);
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSixCompanionsItRemovesTwoToWoundACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var greed = scn.GetShadowCard("greed");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo and five more: exactly 6 companions, beside an ally.
		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(greed);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, greed);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 2 for Ettenmoors + 6 companions.
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(greed));

		scn.ShadowUseCardAction(greed);
		assertEquals(6, scn.GetTwilight());
		// Companions of every race on the table; not the Ring-bearer, not the ally, not the minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir, legolas, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, bounder, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.SUPPORT, greed);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithFiveCompanionsBesideAnAllyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var greed = scn.GetShadowCard("greed");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo and four more: 5 companions, and the ally a sixth Free Peoples character who is no companion.
		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(greed);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 2 for Ettenmoors + 5 companions: the cost can be paid.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(greed));
	}

	@Test
	public void WithSixCompanionsAndOneTwilightTokenItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var greed = scn.GetShadowCard("greed");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, boromir, legolas, gimli, sam);
		scn.MoveCardsToSupportArea(greed);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(greed));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(greed));
	}
}
