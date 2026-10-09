package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_021_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shot", "102_21");

					// Battering Ram: a machine; its skirmish ability needs 5 companions.
					put("ram", "5_44");
					// Scaling Ladder: a machine.  "Skirmish: Spot an [isengard] token here to heal an
					// Uruk-hai", which is only ever used on purpose.
					put("ladder", "5_57");
					// No Business of Ours: an [isengard] condition that is no machine; it keeps the Free
					// Peoples player from looking at the Shadow hand.
					put("business", "2_44");

					// Uruk Savage: an [isengard] Uruk-hai costing 2; Damage +1 is its whole text.
					put("savage", "1_151");
					// Murderous Uruk: an Uruk-hai of the [uruk-hai] culture costing 3; Damage +1 and muster,
					// which is a matter for the regroup phase.
					put("murderous", "11_195");
					// Uruk-hai Raiding Party: an [isengard] Uruk-hai costing 4; Damage +1 is its whole text.
					put("party", "1_158");
					// Isengard Worker: an [isengard] Orc costing 2, with a regroup ability.
					put("worker", "3_62");

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: an unbound companion with a maneuver ability
					put("guard", "5_83");        // Household Guard: an unbound companion; "Valiant" is its whole text
					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					// Bounder: an ally whose home is site 2, where these tests are; a skirmish ability.
					put("bounder", "1_286");
					// Grimir, Dwarven Elder: an ally whose home is site 3; a fellowship ability.
					put("grimir", "1_17");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	/**
	 * The Towers path with Streets of Edoras as its site 3: "Each [rohan] ally may not participate in archery fire
	 * or skirmishes at Streets of Edoras."
	 */
	protected VirtualTableScenario GetStreetsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shot", "102_21");
					put("ram", "5_44");          // Battering Ram: a machine; its skirmish ability needs 5 companions
					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai costing 2; Damage +1 is its whole text
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: an unbound companion with a maneuver ability
					put("aldor", "4_262");       // Aldor: a [rohan] ally whose homes are sites 3 and 6 of this path; an archery ability that needs a villager
					put("grimir", "1_17");       // Grimir, Dwarven Elder: an ally whose home is site 3 of the Fellowship path, so at home nowhere on this one
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");      // Eastemnet Gullies: a fellowship ability used only on purpose
					put("site2", "4_331");      // Eastfold: a Shadow ability that needs 3 [isengard] trackers
					put("site3", "4_340");      // Streets of Edoras
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreachingShotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Breaching Shot
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Remove X tokens from machines to assign an Uruk-hai costing X or less to an ally or
		 * unbound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shot");

		assertEquals("Breaching Shot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesThreeTokensFromMachinesToAssignAnUrukHaiCostingThreeOrLessToAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, aragorn, guard);
		scn.MoveAlliesToTable(bounder);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var ladder = scn.GetShadowCard("ladder");
		var business = scn.GetShadowCard("business");
		var savage = scn.GetShadowCard("savage");
		var murderous = scn.GetShadowCard("murderous");
		var party = scn.GetShadowCard("party");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram, ladder, business);
		scn.AddTokensToCard(ram, 3);
		scn.AddTokensToCard(ladder, 2);
		// Tokens on a card that is no machine.
		scn.AddTokensToCard(business, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, murderous, party, worker);

		// An assignment event: with everything in place it is not on offer in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shot));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(shot));
		// At site 2 every one of these Uruk-hai (site number 5) is roaming; what it costs is the number on
		// the card all the same.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, savage.getBlueprint().getTwilightCost());
		assertEquals(3, murderous.getBlueprint().getTwilightCost());
		assertEquals(4, party.getBlueprint().getTwilightCost());
		assertEquals(2, worker.getBlueprint().getTwilightCost());
		var twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shot);
		// X is at least 1 and at most the 3 + 2 tokens on machines; the 2 on the other card are not counted.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		// The three tokens are taken off one at a time, from whichever machines their owner likes.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ram, ladder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(business));
		scn.ShadowChooseCard(ram);
		scn.ShadowChooseCard(ladder);
		scn.ShadowChooseCard(ram);
		assertEquals(3 - 2, scn.GetCultureTokensOn(ram));
		assertEquals(2 - 1, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(business));

		// The game asks whom to assign to first.  An ally or an unbound companion: not the Ring-bearer, and
		// not Sam, who is Ring-bound.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// An Uruk-hai costing 3 or less, of whatever culture: not the one costing 4, and not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, murderous));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(party, worker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(murderous);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, murderous));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(party));
		assertFalse(scn.IsCharAssigned(worker));
		assertFalse(scn.IsCharAssigned(guard));
		assertFalse(scn.IsCharAssigned(bounder));
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, shot);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void RemovingMoreTokensThanAnyUrukHaiCostsOffersThemAllAndAnAllyAtHomeMayBeTheOneAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var savage = scn.GetShadowCard("savage");
		var murderous = scn.GetShadowCard("murderous");
		var party = scn.GetShadowCard("party");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 5);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, murderous, party, worker);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(shot));

		scn.ShadowPlayCard(shot);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		// Five, though no Uruk-hai here costs more than 4.
		scn.ShadowChoose("5");
		assertEquals(0, scn.GetCultureTokensOn(ram));

		// The fellowship is at site 2, which is the Bounder's home.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, bounder));
		scn.ShadowChooseCard(bounder);

		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, murderous, party));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));
		scn.ShadowChooseCard(party);

		assertTrue(scn.IsCharAssignedAgainst(bounder, party));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(murderous));
		assertInZone(Zone.DISCARD, shot);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyAwayFromHomeMayBeTheOneAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var savage = scn.GetShadowCard("savage");
		var murderous = scn.GetShadowCard("murderous");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, murderous);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Grimir's home is site 3 and the fellowship is at site 2: left to the rules he fights nobody.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsCanAssign(grimir));
		scn.FreepsPass();

		scn.ShadowPlayCard(shot);
		scn.ShadowChoose("3");
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, grimir));
		scn.ShadowChooseCard(grimir);

		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, murderous));
		scn.ShadowChooseCard(savage);

		assertTrue(scn.IsCharAssignedAgainst(grimir, savage));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(murderous));
		assertInZone(Zone.DISCARD, shot);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The assignment stands, and he fights: his skirmish is there to be resolved like any other.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, murderous);
		scn.FreepsResolveSkirmish(grimir);
		assertTrue(scn.IsCharSkirmishing(grimir));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoTokenOnAnyMachine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var business = scn.GetShadowCard("business");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram, business);
		// A machine with no tokens, and tokens on a card that is no machine.
		scn.AddTokensToCard(business, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(3, scn.GetCultureTokensOn(business));
		// 2 companions + 2 for the site: the event's own cost is not what is missing.
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.HAND, shot);
		assertFalse(scn.ShadowPlayAvailable(shot));
	}

	@Test
	public void RemovingFewerTokensThanAnyUrukHaiCostsPaysForTheEventAndAssignsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var murderous = scn.GetShadowCard("murderous");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(murderous, party);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(shot));
		// Three tokens reach the cheaper Uruk-hai, so the event is not marked as one that will do nothing,
		// though the player may still remove too few.
		assertEquals("false", scn.ShadowActionNoEffect(shot));
		var twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shot);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		// Two, where the cheaper Uruk-hai costs 3.
		scn.ShadowChoose("2");

		assertEquals(3 - 2, scn.GetCultureTokensOn(ram));
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, shot);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(murderous));
		assertFalse(scn.IsCharAssigned(party));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsOfferedWithTokensToRemoveThoughNoUrukHaiCostsThatLittle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var murderous = scn.GetShadowCard("murderous");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram);
		// Two tokens, where the cheaper Uruk-hai costs 3.
		scn.AddTokensToCard(ram, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(murderous, party);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(shot));
		// Offered, and marked as an action whose effect will do nothing.
		assertEquals("effect", scn.ShadowActionNoEffect(shot));
		var twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shot);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, shot);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(murderous));
		assertFalse(scn.IsCharAssigned(party));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsOfferedWithTokensToRemoveThoughNoUrukHaiIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 2);

		scn.StartGame();
		// The only minion is an Orc costing 2: cheap enough for the two tokens, and no Uruk-hai.
		scn.MoveMinionsToTable(worker);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(2, worker.getBlueprint().getTwilightCost());
		// There are tokens to remove, and that is all the event asks for.
		assertTrue(scn.ShadowPlayAvailable(shot));
		// Offered, and marked as an action whose effect will do nothing.
		assertEquals("effect", scn.ShadowActionNoEffect(shot));
		var twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shot);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// The tokens are gone and the event is paid for and spent; there was no Uruk-hai to assign.
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, shot);
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(worker));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyACardBarsFromSkirmishesIsNotOfferedThoughAnAllyAwayFromHomeIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStreetsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(aldor, grimir);

		var shot = scn.GetShadowCard("shot");
		var ram = scn.GetShadowCard("ram");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ram);
		scn.AddTokensToCard(ram, 3);
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		// Left to the rules neither ally fights here: the site bars Aldor at his home, and Grimir is away from his.
		assertFalse(scn.FreepsCanAssign(aldor));
		assertFalse(scn.FreepsCanAssign(grimir));
		scn.FreepsPass();

		scn.ShadowPlayCard(shot);
		scn.ShadowChoose("3");
		assertEquals(0, scn.GetCultureTokensOn(ram));
		// "An ally" reaches the one away from home (Q-0042) and not the one a card says may not skirmish (Q-0150).
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, grimir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aldor));
		scn.ShadowChooseCard(grimir);

		// The Savage is the only Uruk-hai, and is taken without asking.
		assertTrue(scn.IsCharAssignedAgainst(grimir, savage));
		assertFalse(scn.IsCharAssigned(aldor));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertInZone(Zone.DISCARD, shot);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
