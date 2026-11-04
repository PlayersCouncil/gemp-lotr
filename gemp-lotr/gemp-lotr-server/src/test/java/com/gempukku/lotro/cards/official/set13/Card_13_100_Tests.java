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

public class Card_13_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunlending1", "13_100");
					put("dunlending2", "13_100");
					// Minions with no text that reaches a test
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7, vitality 3; makes the current site plains
					put("throng", "11_102");    // Throng of Harad: [men] Man, vitality 3; fierce only against resistance 5 or less
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities only used on purpose
					put("ted", "18_75");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Fleet of Corsair Ships: a [men] POSSESSION in the support area; its abilities are never used
					put("fleet", "18_66");
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
	public void ViciousDunlendingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Vicious Dunlending
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> If this minion is not assigned to a skirmish, you may exert him to make another
		 * 	[men] minion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dunlending1");

		assertEquals("Vicious Dunlending", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsHimToMakeAnotherMenMinionDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var dunlending1 = scn.GetShadowCard("dunlending1");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToSupportArea(fleet);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fleet);
		scn.MoveMinionsToTable(dunlending1, dunlending2, footman, throng, ted, brigand);
		scn.SkipToAssignments();
		// The Footman skirmishes Aragorn now, Throng of Harad skirmishes Gandalf later; both Dunlendings, Ted
		// Sandyman and the Brigand are assigned to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gandalf, throng}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(dunlending1));
		assertEquals(0, scn.GetWoundsOn(dunlending2));
		assertEquals(0, scn.GetKeywordCount(footman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dunlending1));
		scn.ShadowUseCardAction(dunlending1);
		// He exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(dunlending1));
		assertEquals(0, scn.GetWoundsOn(dunlending2));
		// [men] minions of two races, in the skirmish, waiting for theirs and in none, the other copy among them;
		// not this one, nor the [dunland] Man, nor a [men] card that is no minion.
		assertEquals(4, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, throng, dunlending2, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dunlending1, brigand, fleet));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(footman);
		assertEquals(1, scn.GetKeywordCount(footman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(ted, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(dunlending2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		// A skirmish ability lasts for the skirmish phase it is used in; this is the next one.
		assertEquals(0, scn.GetKeywordCount(footman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionAlreadyDamagePlus1IsOfferedAgainAndBecomesDamagePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dunlending1 = scn.GetShadowCard("dunlending1");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending1, dunlending2, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowUseCardAction(dunlending2);
		scn.ShadowChooseCard(footman);
		assertEquals(1, scn.GetKeywordCount(footman, Keyword.DAMAGE));
		scn.FreepsPass();

		// The Footman holds damage +1 already: the other copy's ability is still offered, and he is still offered.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dunlending1));
		scn.ShadowUseCardAction(dunlending1);
		assertEquals(1, scn.GetWoundsOn(dunlending1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, dunlending2));
		scn.ShadowChooseCard(footman);
		assertEquals(2, scn.GetKeywordCount(footman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherMenMinionTheSkirmishAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dunlending1 = scn.GetShadowCard("dunlending1");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// He is the only [men] minion; the Brigand is a [dunland] Man.
		scn.MoveMinionsToTable(dunlending1, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, brigand);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(dunlending1));
		// An ability with a cost is offered whatever its effect can do: he exerts, and nobody gains damage.
		assertTrue(scn.ShadowActionAvailable(dunlending1));
		scn.ShadowUseCardAction(dunlending1);
		assertEquals(1, scn.GetWoundsOn(dunlending1));
		assertEquals(0, scn.GetKeywordCount(dunlending1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileHeIsAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var dunlending1 = scn.GetShadowCard("dunlending1");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending1, dunlending2, footman);
		scn.SkipToAssignments();
		// The first Dunlending is assigned to Gandalf, his skirmish still to come; the second to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, footman},
				new PhysicalCardImpl[]{gandalf, dunlending1}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dunlending1));
		assertTrue(scn.ShadowActionAvailable(dunlending2));

		// In his own skirmish as well.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dunlending1));
		assertTrue(scn.ShadowActionAvailable(dunlending2));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var dunlending1 = scn.GetShadowCard("dunlending1");
		var dunlending2 = scn.GetShadowCard("dunlending2");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending1, dunlending2, footman);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(dunlending1, 1);
		assertEquals(1, scn.GetWoundsOn(dunlending1));
		assertEquals(0, scn.GetWoundsOn(dunlending2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dunlending1));
		assertTrue(scn.ShadowActionAvailable(dunlending2));
	}
}
