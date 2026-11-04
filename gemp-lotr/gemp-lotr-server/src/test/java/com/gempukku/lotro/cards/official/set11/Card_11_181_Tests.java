package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("determined", "11_181");

					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, cost 2, strength 5; +3 while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard; companions resistance -1,
					                             // and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1, no other text
					put("endless", "13_161");    // Endless Assault: an [uruk-hai] condition; a skirmish ability used only on purpose
					put("commander", "15_86");   // Mumak Commander: [men] Man minion, vitality 3.
					                             // Maneuver: exert it twice to exert a companion twice (except the Ring-bearer)

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: a Dwarf, strength 6, vitality 3; Skirmish: exert Gimli to make him strength +2
					put("calaglin", "3_10");     // Calaglin, Elf of Lorien: an ally; Regroup: exert Calaglin to wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeterminedUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Determined Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: To play, spot an [uruk-hai] minion.<br>Each time a companion exerts, you may draw a card.
		*/

		var scn = GetScenario();

		var determined = scn.GetFreepsCard("determined");

		assertEquals("Determined Uruk", determined.getBlueprint().getTitle());
		assertNull(determined.getBlueprint().getSubtitle());
		assertFalse(determined.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, determined.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, determined.getBlueprint().getCulture());
		assertEquals(CardType.MINION, determined.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, determined.getBlueprint().getRace());
		assertEquals(2, determined.getBlueprint().getTwilightCost());
		assertEquals(7, determined.getBlueprint().getStrength());
		assertEquals(2, determined.getBlueprint().getVitality());
		assertEquals(5, determined.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiCultureMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var determined = scn.GetShadowCard("determined");
		var hounding = scn.GetShadowCard("hounding");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");

		scn.StartGame();
		scn.MoveCardsToHand(determined, hounding);
		// A [sauron] Uruk-hai on the table, and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(guard);
		scn.MoveCardsToSupportArea(endless);
		scn.SetTwilight(7);
		assertInHand(determined, hounding);
		assertInZone(Zone.SUPPORT, endless);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(determined));

		scn.ShadowPlayCard(hounding);
		assertTrue(scn.ShadowPlayAvailable(determined));

		scn.ShadowPlayCard(determined);
		assertInZone(Zone.SHADOW_CHARACTERS, determined);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhenTheOnlyUrukHaiCultureMinionIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var determined = scn.GetShadowCard("determined");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToHand(determined);
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(7);
		assertInHand(determined);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(determined));
		scn.ShadowPlayCard(determined);
		assertInZone(Zone.SHADOW_CHARACTERS, determined);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionExertedTwiceByAMinionDrawsTwoCardsAndTheMinionsOwnExertionsDrawNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var determined = scn.GetShadowCard("determined");
		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(determined, commander);
		scn.SkipToPhase(Phase.MANEUVER);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(commander));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPass();
		scn.ShadowUseCardAction(commander);

		// The Commander has exerted twice to pay, and nothing is offered for a minion's exertions.
		assertEquals(2, scn.GetWoundsOn(commander));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		scn.ShadowChooseCard(aragorn);

		// Two exertions of one companion by one effect: each is asked about as it happens, and draws one.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(determined));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(determined));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningDrawsNothingAndTheNextExertionIsAskedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var determined = scn.GetShadowCard("determined");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(determined);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, determined);
		scn.FreepsResolveSkirmish(gimli);

		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli exerts to pay for his own skirmish ability.
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(determined));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// Declining once is not declining for good.
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(determined));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyExertingDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var determined = scn.GetShadowCard("determined");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);

		scn.StartGame();
		scn.MoveMinionsToTable(determined);
		scn.SkipToPhase(Phase.REGROUP);

		int hand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(calaglin));
		assertEquals(0, scn.GetWoundsOn(determined));

		scn.FreepsUseCardAction(calaglin);
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertEquals(1, scn.GetWoundsOn(determined));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
