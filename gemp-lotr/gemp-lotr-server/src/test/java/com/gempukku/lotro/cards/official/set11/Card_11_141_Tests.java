package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_141_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("undisciplined", "11_141");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("sam", "1_311");         // Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound by rule but not the Ring-bearer; his abilities are used only on purpose
					put("bounder", "1_286");     // Bounder: Hobbit ally, home site 2 of this path, where he may be assigned; a skirmish ability used only on purpose

					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, vitality 2; +2 strength at an underground site, which no reading here takes
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion who is no Orc (a Half-troll), vitality 2; its assignment trigger wants more companions than minions
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UndisciplinedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Undisciplined
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 8
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Toil 3</b>. <helper>(For each [orc] character you exert when playing this, its twilight cost is -3.)</helper><br>Assign an [orc] minion to a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("undisciplined");

		assertEquals("Undisciplined", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(8, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToilTakesThreeOffItsCostForEachOrcCharacterExertedAsItIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undisciplined = scn.GetShadowCard("undisciplined");
		var aragorn = scn.GetFreepsCard("aragorn");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(undisciplined);

		scn.StartGame();
		assertInHand(undisciplined);
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		// 8 - 3 - 3 is the least it can cost, with both [orc] characters exerted; the [moria] Troop does not count.
		// Toil 2 would want 4, and toil 4 (or the Troop counted) would let 0 do.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(undisciplined));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(undisciplined));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(undisciplined);
		// The pool is 3 short of its 8: one exertion is owed, and the second is the Shadow player's to make or
		// not.  Toil 2 would owe two.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(halftroll);

		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		// 5 - (8 - 3)
		assertEquals(0, scn.GetTwilight());

		// Aragorn is the one companion it can take; the minion is the Shadow player's to choose.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		scn.ShadowChooseCard(denizen);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, denizen));
		assertInZone(Zone.DISCARD, undisciplined);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignsAnOrcMinionToACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undisciplined = scn.GetShadowCard("undisciplined");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(undisciplined);

		scn.StartGame();
		assertInHand(undisciplined);
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(undisciplined);
		// The pool pays the 8 in full: toil's exertions are not owed.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		assertTrue(scn.IsCharAssignedAgainst(sam, halftroll));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(bounder));
		assertFalse(scn.IsCharAssigned(denizen));
		assertFalse(scn.IsCharAssigned(troop));
		assertInZone(Zone.DISCARD, undisciplined);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undisciplined = scn.GetShadowCard("undisciplined");
		var aragorn = scn.GetFreepsCard("aragorn");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(undisciplined);

		scn.StartGame();
		assertInHand(undisciplined);
		scn.MoveMinionsToTable(denizen);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(undisciplined));
		assertInHand(undisciplined);
	}

	@Test
	public void WithNoOrcMinionTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undisciplined = scn.GetShadowCard("undisciplined");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(undisciplined);

		scn.StartGame();
		assertInHand(undisciplined);
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// CR 4.2 "effect": an event may be played though it will have no effect.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(undisciplined));
		scn.ShadowPlayCard(undisciplined);

		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(troop));
		assertInZone(Zone.DISCARD, undisciplined);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoCompanionButTheRingBearerTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var undisciplined = scn.GetShadowCard("undisciplined");
		var frodo = scn.GetRingBearer();
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(undisciplined);

		scn.StartGame();
		assertInHand(undisciplined);
		scn.MoveMinionsToTable(denizen);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(undisciplined));
		scn.ShadowPlayCard(undisciplined);
		// The pool pays the 8 in full: Denizen need not exert for toil.
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(denizen));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(denizen));
		assertInZone(Zone.DISCARD, undisciplined);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
