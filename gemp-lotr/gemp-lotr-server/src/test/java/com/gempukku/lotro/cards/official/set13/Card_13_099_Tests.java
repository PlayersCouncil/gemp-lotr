package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stragglers1", "13_99");
					put("stragglers2", "13_99");
					// Howdah: [men] support-area condition, cost 0, that gives itself a [men] token for each [men]
					// archer spotted as it is played (none here); its start-of-archery trigger is never reached
					put("howdah1", "13_94");
					put("howdah2", "13_94");
					// Minions with no text that reaches a test
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7; makes the current site plains
					put("throng", "11_102");    // Throng of Harad: [men] Man, strength 11; fierce only against resistance 5 or less
					put("legion", "11_88");     // Legion of Harad: [men] Man, strength 14; Toil 2 is its whole text
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities only used on purpose
					put("ted", "18_75");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, no text
					// Free Peoples cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; his maneuver ability is never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a second companion to skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StragglersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Stragglers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, add a [men] token here.
		 * 	<b>Skirmish:</b> Discard this from play or remove a token from here to make a [men] minion strength +2
		 * 	for each [men] minion not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stragglers1");

		assertEquals("Stragglers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedAddsAMenTokenHereAndOnNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stragglers1 = scn.GetShadowCard("stragglers1");
		var stragglers2 = scn.GetShadowCard("stragglers2");
		var howdah1 = scn.GetShadowCard("howdah1");
		var howdah2 = scn.GetShadowCard("howdah2");
		scn.MoveCardsToHand(stragglers1, stragglers2, howdah2);
		scn.MoveCardsToSupportArea(howdah1);

		scn.StartGame();
		scn.AddTokensToCard(howdah1, 1);
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertInZone(Zone.HAND, stragglers1, stragglers2, howdah2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(stragglers1);
		assertInZone(Zone.SUPPORT, stragglers1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		// "Here": the [men] condition beside it gets none.
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second copy played later puts its token on itself, not on the first.
		scn.ShadowPlayCard(stragglers2);
		assertInZone(Zone.SUPPORT, stragglers2);
		assertEquals(1, scn.GetCultureTokensOn(stragglers2));
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [men] condition played later adds no token to either.
		scn.ShadowPlayCard(howdah2);
		assertInZone(Zone.SUPPORT, howdah2);
		assertEquals(0, scn.GetCultureTokensOn(howdah2));
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		assertEquals(1, scn.GetCultureTokensOn(stragglers2));
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesAMenMinionStrongerByTwoForEachMenMinionNotAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var stragglers1 = scn.GetShadowCard("stragglers1");
		var stragglers2 = scn.GetShadowCard("stragglers2");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");
		var legion = scn.GetShadowCard("legion");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(stragglers1, stragglers2);

		scn.StartGame();
		scn.AddTokensToCard(stragglers1, 1);
		scn.AddTokensToCard(stragglers2, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		assertEquals(1, scn.GetCultureTokensOn(stragglers2));
		assertInZone(Zone.SUPPORT, stragglers1, stragglers2);
		scn.MoveMinionsToTable(footman, throng, legion, ted, brigand);
		scn.SkipToAssignments();
		// The Footman skirmishes Aragorn now, Throng of Harad skirmishes Gandalf later; Legion of Harad (a Man), Ted
		// Sandyman (a Hobbit) and the Brigand ([dunland]) are assigned to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gandalf, throng}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(3, scn.GetStrength(ted));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(stragglers1));
		scn.ShadowUseCardAction(stragglers1);
		// Both costs can be paid, so the player is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Discard this from play"));
		assertTrue(scn.ShadowChoiceAvailable("Remove a token from here"));
		scn.ShadowChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(stragglers1));
		assertInZone(Zone.SUPPORT, stragglers1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers2));
		// [men] minions of two races, in the skirmish, waiting for theirs and in none; not the [dunland] Man, nor a
		// [men] card that is no minion.
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, throng, legion, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, stragglers2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(footman);
		// 7 + 2 for each of Legion of Harad and Ted Sandyman; the Brigand is no [men] minion, and Throng of Harad
		// and the Footman are assigned.
		assertEquals(11, scn.GetStrength(footman));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(14, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		// No token here now (the one on the other copy is not "here"): the discard is carried out unasked.
		scn.ShadowUseCardAction(stragglers1);
		assertInDiscard(stragglers1);
		assertInZone(Zone.SUPPORT, stragglers2);
		assertEquals(1, scn.GetCultureTokensOn(stragglers2));
		// The text names no skirmish: Ted Sandyman, in none, may be chosen, and is counted himself.
		scn.ShadowChooseCard(ted);
		assertEquals(7, scn.GetStrength(ted));
		assertEquals(11, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		// A skirmish ability lasts for the skirmish phase it is used in; this is the next one.
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMenMinionLeftUnassignedTheBonusIsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var stragglers1 = scn.GetShadowCard("stragglers1");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(stragglers1);

		scn.StartGame();
		scn.AddTokensToCard(stragglers1, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		scn.MoveMinionsToTable(footman, throng, brigand);
		scn.SkipToAssignments();
		// Both [men] minions are assigned (the Footman in the skirmish, Throng of Harad waiting); the Brigand, who is
		// no [men] minion, is assigned to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gandalf, throng}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(11, scn.GetStrength(throng));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowUseCardAction(stragglers1);
		scn.ShadowChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(stragglers1));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, throng));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		scn.ShadowChooseCard(throng);
		// +2 for each of none.
		assertEquals(11, scn.GetStrength(throng));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMenMinionInPlayTheSkirmishAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var stragglers1 = scn.GetShadowCard("stragglers1");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(stragglers1);

		scn.StartGame();
		scn.AddTokensToCard(stragglers1, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, brigand);
		assertEquals(8, scn.GetStrength(brigand));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// An ability with a cost is offered whatever its effect can do: the token is paid, and nobody is stronger.
		assertTrue(scn.ShadowActionAvailable(stragglers1));
		scn.ShadowUseCardAction(stragglers1);
		scn.ShadowChooseOption("Remove a token from here");
		assertEquals(0, scn.GetCultureTokensOn(stragglers1));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var stragglers1 = scn.GetShadowCard("stragglers1");
		var footman = scn.GetShadowCard("footman");
		var legion = scn.GetShadowCard("legion");
		scn.MoveCardsToSupportArea(stragglers1);

		scn.StartGame();
		scn.AddTokensToCard(stragglers1, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers1));
		scn.MoveMinionsToTable(footman, legion);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(stragglers1));
	}
}
