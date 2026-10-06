package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_062_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("save1", "102_62");
					put("save2", "102_62");

					// Search cards.  None of them does anything on these boards.
					put("few", "1_260");        // The Number Must Be Few: [sauron], support area; needs 7 companions
					put("few2", "1_260");       // a second copy, for the discard pile
					put("journey", "1_253");    // Journey Into Danger: [sauron], support area; needs 5 companions
					put("nine", "1_219");       // The Nine Servants of Sauron: [wraith], support area; answers a Nazgul being played
					put("domain", "1_175");     // Goblin Domain: [moria], support area; answers a companion played at site 4 or later
					put("devilry", "1_202");    // What Is This New Devilry?: [moria], support area; needs site 5 or later
					put("rest", "4_171");       // Rest While You Can: [isengard], borne by a companion; answers its bearer being killed
					// Sauron's Defenses: a [sauron] condition that is not a search card.  Needs 3 dead companions.
					put("defenses", "1_274");

					put("scout", "2_89");       // Orc Scout: a [sauron] tracker, strength 6, vitality 2; a response to events that spot or exert a ranger
					put("pursuer", "5_108");    // Orc Pursuer: a [sauron] tracker, strength 6, vitality 2; lowers site numbers, and a trigger on being played
					put("raider", "53_74");     // Uruk Raider: an [isengard] tracker, strength 6, vitality 2; its text is about allies
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc and no tracker, strength 7, vitality 2; a skirmish ability
					put("mouth", "12_118");     // The Mouth of Sauron, Lieutenant of Barad-dur: a [sauron] Man and no tracker, strength 9, vitality 3; an assignment ability

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoOnesGoingtoSaveYouNowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: No One's Going to Save You Now
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Search.
		 * 	Make a [Sauron] tracker strength +1 for each search card you can spot (limit +5).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("save1");

		assertEquals("No One's Going to Save You Now", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnlyInASkirmishAndAddsNothingWithNoSearchCardInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var save1 = scn.GetShadowCard("save1");
		var save2 = scn.GetShadowCard("save2");
		scn.MoveCardsToHand(save1, save2);
		// Near a search card in play, and none of them one: a condition that is no search card, a search card
		// in the discard pile, the copy of the event still in hand, and the event itself.
		scn.MoveCardsToSupportArea(scn.GetShadowCard("defenses"));
		scn.MoveCardsToDiscard(scn.GetShadowCard("few2"));

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);

		// Not in the assignment phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(save1));
		scn.ShadowPass();

		scn.FreepsAssignAndResolve(aragorn, scout);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(save1));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());

		// An event is offered whatever it will do.  It is paid for and discarded, and with nothing to count
		// the Scout gains nothing.
		scn.ShadowPlayCard(save1);
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(save1);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 beats the Scout's 6, and the Scout lives to see the regroup phase.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Not in the regroup phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(save2));
	}

	@Test
	public void MakesASauronTrackerStrengthPlus1ForEachSearchCardInPlayUntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var save1 = scn.GetShadowCard("save1");
		var save2 = scn.GetShadowCard("save2");
		scn.MoveCardsToHand(save1, save2);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		// Three search cards in play: a [sauron] one and a [wraith] one in the support area, and an [isengard]
		// one on Aragorn.  Not counted: Sauron's Defenses, and the second Number Must Be Few in the discard pile.
		scn.MoveCardsToSupportArea(scn.GetShadowCard("few"), scn.GetShadowCard("nine"), scn.GetShadowCard("defenses"));
		scn.AttachCardsTo(aragorn, scn.GetShadowCard("rest"));
		scn.MoveCardsToDiscard(scn.GetShadowCard("few2"));

		var scout = scn.GetShadowCard("scout");
		var pursuer = scn.GetShadowCard("pursuer");
		var raider = scn.GetShadowCard("raider");
		var soldier = scn.GetShadowCard("soldier");
		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, pursuer, raider, soldier, mouth);
		// Every [sauron] tracker there is is an Orc, and the text does not ask for one: a [sauron] Man is
		// made a tracker.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, mouth, null, Keyword.TRACKER));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, scout},
				new PhysicalCardImpl[]{aragorn, pursuer});
		scn.ShadowDeclineAssignments();

		// Gimli's skirmish with the Orc Scout comes first.
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(save1));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(6, scn.GetStrength(pursuer));
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowPlayCard(save1);
		// Every [sauron] tracker: the one in this skirmish, the one waiting for the next, and the Man who is
		// in neither and is no Orc.  Not the [isengard] tracker, and not the [sauron] Orc who is no tracker.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, pursuer, mouth));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raider, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(scout);

		// 6 + 3 search cards.  The copy still in hand is not one of them.
		assertEquals(9, scn.GetStrength(scout));
		assertEquals(6, scn.GetStrength(pursuer));
		assertEquals(6, scn.GetStrength(raider));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(4, scn.GetTwilight());
		assertInDiscard(save1);
		assertEquals(1, scn.GetShadowHandCount());

		// The second copy goes on the Orc Pursuer, who is not in this skirmish.
		scn.FreepsPass();
		scn.ShadowPlayCard(save2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, pursuer));
		scn.ShadowChooseCard(pursuer);
		assertEquals(9, scn.GetStrength(pursuer));
		assertEquals(9, scn.GetStrength(scout));
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(save2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli's 6 loses to the Scout's 9.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(scout));

		// In the next skirmish both bonuses are gone, and Aragorn's 8 beats the Pursuer's 6.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		assertEquals(6, scn.GetStrength(pursuer));
		assertEquals(6, scn.GetStrength(scout));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyPlayedAddsNoMoreThan5HoweverManySearchCardsThereAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var save1 = scn.GetShadowCard("save1");
		var save2 = scn.GetShadowCard("save2");
		scn.MoveCardsToHand(save1, save2);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		// Six search cards in play: five in the support area and one on Gimli.
		scn.MoveCardsToSupportArea(scn.GetShadowCard("few"), scn.GetShadowCard("journey"), scn.GetShadowCard("nine"),
				scn.GetShadowCard("domain"), scn.GetShadowCard("devilry"));
		scn.AttachCardsTo(gimli, scn.GetShadowCard("rest"));

		var scout = scn.GetShadowCard("scout");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, scout);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(scout));

		scn.ShadowPlayCard(save1);
		scn.ShadowChooseCard(scout);
		// 6 + 5, not + 6.
		assertEquals(11, scn.GetStrength(scout));

		// The limit is each copy's own: a second copy adds its 5 as well.
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(save2));
		scn.ShadowPlayCard(save2);
		scn.ShadowChooseCard(scout);
		assertEquals(16, scn.GetStrength(scout));
		assertEquals(6, scn.GetStrength(pursuer));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
