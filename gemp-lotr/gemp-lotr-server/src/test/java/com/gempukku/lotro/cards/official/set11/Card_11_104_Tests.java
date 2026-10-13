package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_104_Tests
{

	// The Fellowship path: site 2, Ettenmoors, is a plains and no battleground, and is the Bounder's home.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("death", "11_104");
					put("death2", "11_104");
					// The Mouth of Sauron, Messenger of Mordor: [men] Man, strength 9, vitality 3.  His maneuver
					// ability plays a [men] condition or possession from the draw deck, and is never used.
					put("mouth", "12_73");
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7, vitality 3; the current site gains plains
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit and no Man; strength 3,
					// vitality 4.  His abilities need a site the Shadow player controls, or are used on purpose.
					put("ted", "18_75");
					// Easterling Axeman: a Man who is [raider] and not [men]; strength 6, vitality 2.  Nothing
					// more below 2 burdens.
					put("axeman", "4_224");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability never used here
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, strength 2, vitality 2, home site 2; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with a site 2 that is a battleground.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("death", "11_104");
					put("death2", "11_104");
					put("mouth", "12_73");      // The Mouth of Sauron, Messenger of Mordor: [men] Man, strength 9, vitality 3; a maneuver ability never used
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7, vitality 3; the current site gains plains
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; nothing more below 2 burdens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhistlingDeathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Whistling Death
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Wound a character skirmishing a [men] minion.
		 * If the fellowship is at a battleground site, you may remove (3) to wound that character again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("death");

		assertEquals("Whistling Death", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhistlingDeathWoundsThenRemoves3MoreToWoundAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(death);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(death));

		// The Ruling Ring answers each wound of a skirmish; it is left off.
		scn.ShadowPlayCard(death);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetTwilight());

		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		scn.ShadowChooseYes();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetTwilight());
		// "That character": the companion who is not in the skirmish is untouched.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhistlingDeathSecondWoundIsNotOfferedAtNonBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(death);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(death));

		scn.ShadowPlayCard(death);
		scn.FreepsDeclineOptionalTrigger(); // The Ruling Ring
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetTwilight());

		// 7 in the pool, and nothing is asked: the event is over.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhistlingDeathSecondWoundIsNotOfferedWhenLessThan3TwilightAvailable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var frodo = scn.GetRingBearer();

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");
		scn.MoveCardsToHand(death);

		scn.StartGame();
		scn.MoveMinionsToTable(mouth);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowPlayAvailable(death));

		scn.ShadowPlayCard(death);
		scn.FreepsDeclineOptionalTrigger(); // The Ruling Ring
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The event's own 3 leave 2, one short of the price.
		assertEquals(2, scn.GetTwilight());

		assertFalse(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SecondWoundIsNotOfferedWhileOnlyTheShadowPlayersOwnFellowshipIsAtTheBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var frodo = scn.GetRingBearer();

		var death = scn.GetShadowCard("death");
		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		// Each player has moved once, to Uruk Camp, and it is the first player's turn again.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(death);
		scn.MoveMinionsToTable(mouth);
		// The first player's fellowship moves on to site 3, a sanctuary and no battleground.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, mouth);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// "The fellowship" is the one that moved this turn.  The Shadow player's own is still at the battleground.
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(death));

		scn.ShadowPlayCard(death);
		scn.FreepsDeclineOptionalTrigger(); // The Ruling Ring
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetTwilight());

		// 7 in the pool, and nothing is asked: the event is over.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningToRemoveThreeLeavesOneWoundAndTheEventCannotBePlayedInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var death = scn.GetShadowCard("death");
		var death2 = scn.GetShadowCard("death2");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(death, death2);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(9);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPlayCard(death);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetTwilight());

		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		scn.ShadowChooseNo();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetTwilight());
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 beats 7.  In the regroup phase the second copy stays in hand, with 6 to pay its 3.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertInZone(Zone.HAND, death2);
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(death2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AtABattlegroundWithNobodyWoundedTheThreeMayStillBeRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var death = scn.GetShadowCard("death");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(death);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(9);
		scn.FreepsPass();

		// Aragorn fights a [raider] Man: nobody is wounded.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.ShadowPlayCard(death);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetTwilight());

		// The second wound has a price, so it is offered though there is nobody to wound again.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove (3) to wound"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsNobodyInASkirmishAgainstAManOfAnotherCultureAndWoundsTheCharacterSkirmishingAMenMinionWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var death = scn.GetShadowCard("death");
		var death2 = scn.GetShadowCard("death2");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(death, death2);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman}, new PhysicalCardImpl[]{gimli, ted});

		// Aragorn fights a [raider] Man, and Gimli's skirmish with the [men] minion has not begun: the event is
		// played and paid for, and wounds nobody.
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(6);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(death));
		scn.ShadowPlayCard(death);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's 8 beats 6.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Ted Sandyman is a Hobbit, and a [men] minion: Gimli, who fights him, is wounded.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPlayCard(death2);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(death2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAnAllyWhoIsSkirmishingAMenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var death = scn.GetShadowCard("death");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(death);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		// Site 2 is the Bounder's home, so he may be assigned there.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, footman);
		scn.FreepsResolveSkirmish(bounder);
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.ShadowPlayCard(death);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(death);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
