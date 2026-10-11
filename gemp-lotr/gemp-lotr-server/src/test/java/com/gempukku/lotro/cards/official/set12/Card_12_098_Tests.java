package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tormentor", "12_98");
					put("tormentor2", "12_98");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a second companion; his text is about being overwhelmed
					put("eowyn", "17_96");      // Éowyn, Northwoman: "Assignment: Exert Éowyn and assign a minion to the Ring-bearer"
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path
					// Unexpected Visitor: on a [gondor] companion, gives each minion "Assignment: Assign this minion to bearer"
					put("visitor", "7_126");
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples card for the hand
					put("runner", "1_178");     // Goblin Runner: a Shadow card for the hand, and a [moria] minion
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					// The End Comes: "Assignment: Spot 5 [moria] minions to make the Free Peoples player assign the
					// Ring-bearer to a skirmish"
					put("endcomes", "1_169");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Courteous Halfling is the Ring-bearer here: "While you can spot 3 unbound companions, Shadow cards
	// may not discard cards from your hand or from the top of your draw deck."
	protected VirtualTableScenario GetCourteousHalflingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tormentor", "12_98");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion
					put("boromir", "1_96");     // Boromir, Lord of Gondor: an unbound companion
					put("gimli", "1_13");       // Gimli, Son of Gloin: the third unbound companion
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples card for the hand
					put("runner", "1_178");     // Goblin Runner: a Shadow card for the hand
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcTormentorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Tormentor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: Each time the Free Peoples player assigns this minion to skirmish an unwounded companion, he or she must add a burden or discard a card from hand.
		*/

		var scn = GetScenario();

		var tormentor = scn.GetFreepsCard("tormentor");

		assertEquals("Orc Tormentor", tormentor.getBlueprint().getTitle());
		assertNull(tormentor.getBlueprint().getSubtitle());
		assertFalse(tormentor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, tormentor.getBlueprint().getSide());
		assertEquals(Culture.ORC, tormentor.getBlueprint().getCulture());
		assertEquals(CardType.MINION, tormentor.getBlueprint().getCardType());
		assertEquals(Race.ORC, tormentor.getBlueprint().getRace());
		assertEquals(2, tormentor.getBlueprint().getTwilightCost());
		assertEquals(6, tormentor.getBlueprint().getStrength());
		assertEquals(1, tormentor.getBlueprint().getVitality());
		assertEquals(4, tormentor.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForTwoAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tormentor = scn.GetShadowCard("tormentor");
		scn.MoveCardsToHand(tormentor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, tormentor);
		assertTrue(scn.ShadowPlayAvailable(tormentor));

		scn.ShadowPlayCard(tormentor);

		// 2 to play + 2 roaming: its site number is 4 and the fellowship is at site 2
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, tormentor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayerToAnUnwoundedCompanionItHasHimChooseAndABurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var tormentor = scn.GetShadowCard("tormentor");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToFreepsHand("armor", "runner");
		scn.MoveMinionsToTable(tormentor, troop);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		// The Troop goes to an unwounded companion in the same answer: only the Tormentor's assignment asks anything.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tormentor}, new PhysicalCardImpl[]{boromir, troop});

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a card from hand"));

		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.IsCharAssignedAgainst(aragorn, tormentor));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ChoosingToDiscardHasHimPickOneCardOfHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn);

		var tormentor = scn.GetShadowCard("tormentor");

		scn.StartGame();
		scn.MoveCardsToHand(armor, runner);
		scn.MoveMinionsToTable(tormentor);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsAssignToMinions(aragorn, tormentor);
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChoose("Discard a card from hand");

		// "A card": a Free Peoples card and a Shadow card of his hand are both his to pick, and one must go.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.HAND, armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoCardInHandTheBurdenIsAddedWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tormentor = scn.GetShadowCard("tormentor");

		scn.StartGame();
		scn.MoveMinionsToTable(tormentor);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(aragorn, tormentor);

		// A choice is of what can be done in full: with nothing to discard, the burden is all there is.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToAWoundedCompanionOrToAnUnwoundedAllyItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var tormentor = scn.GetShadowCard("tormentor");
		var tormentor2 = scn.GetShadowCard("tormentor2");

		scn.StartGame();
		scn.MoveCardsToFreepsHand("armor", "runner");
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(tormentor, tormentor2);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tormentor}, new PhysicalCardImpl[]{bounder, tormentor2});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, tormentor));
		assertTrue(scn.IsCharAssignedAgainst(bounder, tormentor2));
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToAnUnwoundedCompanionItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tormentor = scn.GetShadowCard("tormentor");

		scn.StartGame();
		scn.MoveCardsToFreepsHand("armor", "runner");
		scn.MoveMinionsToTable(tormentor);
		scn.SkipToAssignments();

		// The Free Peoples player leaves it unassigned, and the Shadow player assigns it himself.
		scn.FreepsDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowAssignToMinions(aragorn, tormentor);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, tormentor));
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayersOwnAssignmentActionItAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		var tormentor = scn.GetShadowCard("tormentor");

		scn.StartGame();
		scn.MoveCardsToFreepsHand("armor", "runner");
		scn.MoveMinionsToTable(tormentor);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Unexpected Visitor gives the minion an assignment action, and the one who uses it is the Shadow player.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(tormentor));
		scn.ShadowUseCardAction(tormentor);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, tormentor));
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignedByTheFreePeoplesPlayersOwnCardToTheUnwoundedRingBearerItHasHimChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(eowyn);

		var tormentor = scn.GetShadowCard("tormentor");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToHand(armor);
		scn.MoveMinionsToTable(tormentor, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(eowyn));

		// Éowyn exerts and the Free Peoples player assigns a minion of his choice to the Ring-bearer.
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(tormentor, troop));
		scn.FreepsChooseCard(tormentor);

		assertTrue(scn.IsCharAssignedAgainst(frodo, tormentor));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Discard a card from hand");

		// The one card of his hand goes with no further question.
		assertInZone(Zone.DISCARD, armor);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void MadeByAShadowCardToAssignTheRingBearerTheFreePeoplesPlayerWhoPicksItMustChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var tormentor = scn.GetShadowCard("tormentor");
		var troop = scn.GetShadowCard("troop");
		var endcomes = scn.GetShadowCard("endcomes");

		scn.StartGame();
		scn.MoveCardsToFreepsHand("armor", "runner");
		scn.MoveCardsToHand(endcomes);
		// Five [moria] minions for The End Comes to spot: the Troop and four Runners.
		scn.MoveMinionsToTable(tormentor, troop, scn.GetShadowCard("runner"), scn.GetShadowCard("runner2"),
				scn.GetShadowCard("runner3"), scn.GetShadowCard("runner4"));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(endcomes));
		scn.ShadowPlayCard(endcomes);

		// The event is the Shadow player's, but the one who assigns is the Free Peoples player: he picks the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tormentor, troop));
		scn.FreepsChooseCard(tormentor);

		assertTrue(scn.IsCharAssignedAgainst(frodo, tormentor));
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwoCopiesAssignedToUnwoundedCompanionsEachHaveHimChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var tormentor = scn.GetShadowCard("tormentor");
		var tormentor2 = scn.GetShadowCard("tormentor2");

		scn.StartGame();
		scn.MoveCardsToHand(armor, runner);
		scn.MoveMinionsToTable(tormentor, tormentor2);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tormentor}, new PhysicalCardImpl[]{boromir, tormentor2});

		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Add a burden");
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		// The second copy's assignment asks again.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("Discard a card from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, runner));
		scn.FreepsChooseCard(armor);

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, armor);
		assertInZone(Zone.HAND, runner);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void BesideFrodoCourteousHalflingWithThreeUnboundCompanionsTheDiscardIsStillOneOfHisOptions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourteousHalflingScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var tormentor = scn.GetShadowCard("tormentor");

		scn.StartGame();
		scn.MoveCardsToHand(armor, runner);
		scn.MoveMinionsToTable(tormentor);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsAssignToMinions(aragorn, tormentor);

		// Frodo's text stops a Shadow card that makes him discard, but this one gives him a choice (CR 4.2, the ruling
		// on Frodo, Courteous Halfling), and the discard is his to take.
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a card from hand"));
		scn.FreepsChoose("Discard a card from hand");
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, runner));
		scn.FreepsChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.HAND, armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
