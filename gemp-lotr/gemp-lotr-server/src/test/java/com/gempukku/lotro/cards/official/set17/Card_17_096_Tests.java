package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "17_96");

					put("sam", "1_311");         // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; strength 3, vitality 4
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
					put("sword", "1_299");       // Hobbit Sword: strength +2 and no text; keeps Frodo from being overwhelmed by a 9

					// Minions with keywords in their game text
					put("captain", "4_225");     // Easterling Captain: [raider] Man, Easterling and fierce; strength 11, site 4
					put("rider", "12_161");      // Black Rider: [wraith] Nazgûl, fierce; strength 8 +1 for each companion
					put("savage1", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and no other text; strength 5, vitality 3
					put("savage2", "1_151");
					put("scout", "1_270");       // Orc Scouting Band: [sauron] Orc, tracker; a skirmish ability used only on purpose
					put("axeman1", "4_224");     // Easterling Axeman: Easterling; strength +3 and fierce while 2 burdens can be spotted
					put("axeman2", "4_224");
					put("cry1", "1_128");        // Lurtz's Battle Cry: [isengard] Skirmish event, cost 1; an Uruk-hai is strength -1 and damage +1
					put("cry2", "1_128");

					put("party1", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and no other text
					put("party2", "1_158");
					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1
					put("skirmisher", "18_90");  // Orkish Skirmisher: strength 9, vitality 3; when it wins it may exert twice to wound any companion

					put("spearman", "7_201");    // Morgul Spearman: while a Nazgûl can be spotted, the Free Peoples player must exert a companion to assign it
					put("lemenya", "1_232");     // Úlairë Lemenya: the Nazgûl to spot; strength 9, no keyword; her text is about [wraith] conditions
					put("visitor", "7_126");     // Unexpected Visitor: on a [gondor] companion; each minion gains "Assignment: Assign this minion to bearer"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo is strength 3 and takes his wounds
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Éowyn, Northwoman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While the Ring-bearer is assigned to a skirmish, Éowyn cannot take wounds while skirmishing.
		 * 	<b>Assignment:</b> Exert Éowyn and assign a minion to the Ring-bearer to make that minion lose
		 * 	all game text keywords and unable to gain game text keywords until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Northwoman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileTheRingBearerIsAssignedSheTakesNoWoundForASkirmishSheLosesAndAnotherCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, party1}, new PhysicalCardImpl[]{aragorn, party2},
				new PhysicalCardImpl[]{frodo, runner});

		// Her skirmish is fought while Frodo's still waits.  6 against 9, damage +1: she loses and takes neither wound.
		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(9, scn.GetStrength(party1));
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(party1));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn, 8 against 9, loses his with Frodo still waiting, and takes both.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(frodo));
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void OnceTheRingBearersSkirmishIsOverSheTakesTheWoundsOfHerOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var party1 = scn.GetShadowCard("party1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner}, new PhysicalCardImpl[]{eowyn, party1});

		// Frodo's skirmish first: 3 against 5.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// He is assigned to no skirmish now: 1 for losing + 1 for damage +1
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerBeingAssignedDoesNotSpareHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var sam = scn.GetFreepsCard("sam");
		var party1 = scn.GetShadowCard("party1");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{eowyn, party1});

		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedAndNotYetSkirmishingSheTakesAWoundAndThenNoneInHerOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		var runner = scn.GetShadowCard("runner");
		var skirmisher = scn.GetShadowCard("skirmisher");
		scn.MoveCompanionsToTable(eowyn, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, runner, skirmisher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, skirmisher}, new PhysicalCardImpl[]{eowyn, party1},
				new PhysicalCardImpl[]{frodo, runner});

		// Aragorn's skirmish: 8 against 9.  The Skirmisher wins and may exert twice to wound any companion.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(skirmisher));

		// Frodo is assigned to a skirmish and so is Éowyn, but she is not skirmishing: she may be chosen, and is wounded.
		assertTrue(scn.IsCharAssigned(frodo));
		assertTrue(scn.IsCharAssigned(eowyn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(eowyn, frodo, aragorn));
		scn.ShadowChooseCard(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));

		// In her own skirmish, with Frodo still waiting, she takes none of the 2 for losing it.
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.IsCharAssigned(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignmentAbilityExertsEowynAndAssignsAMinionToTheRingBearerWhichLosesTheKeywordsOfItsGameText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var sam = scn.GetFreepsCard("sam");
		var captain = scn.GetShadowCard("captain");
		var rider = scn.GetShadowCard("rider");
		var savage = scn.GetShadowCard("savage1");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(eowyn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, rider, savage, scout);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));
		// A site-4 minion at site 2
		assertTrue(scn.HasKeyword(captain, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsUseCardAction(eowyn);

		// Minions of four cultures and four races, exactly one to be chosen.
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(captain, rider, savage, scout));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(captain);

		// To the Ring-bearer, not to the other Ring-bound companion.
		assertTrue(scn.IsCharAssignedAgainst(frodo, captain));
		assertFalse(scn.IsCharAssigned(sam));
		// Both keywords of its game text are gone, the unloaded one and the loaded one.  Roaming is no keyword of
		// its game text, and its strength is untouched.
		assertFalse(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(captain, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(captain, Keyword.ROAMING));
		assertEquals(11, scn.GetStrength(captain));
		// "That minion": the others keep theirs.
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(scout, Keyword.TRACKER));

		// With a minion on the Ring-bearer no second minion can be assigned to him: the ability is not offered
		// again, though Éowyn could exert once more and three minions are unassigned.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetVitality(eowyn));
		assertFalse(scn.IsCharAssigned(rider));
		assertFalse(scn.FreepsActionAvailable(eowyn));
	}

	@Test
	public void IsNotOfferedOnceEowynIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(eowyn, 1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// One exertion left: offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(eowyn));

		// None left, with the Ring-bearer and the minion both still unassigned.
		scn.AddWoundsToChar(eowyn, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetVitality(eowyn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(savage));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eowyn));
	}

	@Test
	public void TheMinionCannotGainAKeywordFromAnEventPlayedInItsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var cry1 = scn.GetShadowCard("cry1");
		var cry2 = scn.GetShadowCard("cry2");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(cry1, cry2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(eowyn);
		scn.FreepsChooseCard(savage1);
		assertEquals(0, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage2, Keyword.DAMAGE));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(eowyn, savage2);

		// The event on the other Savage, in Éowyn's skirmish: strength -1 and damage +1.
		scn.FreepsResolveSkirmish(eowyn);
		scn.FreepsPass();
		scn.ShadowPlayCard(cry1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage1, savage2));
		scn.ShadowChooseCard(savage2);
		assertEquals(5 - 1, scn.GetStrength(savage2));
		assertEquals(1 + 1, scn.GetKeywordCount(savage2, Keyword.DAMAGE));
		// 6 against 4
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage2));

		// The same event on the Savage the ability named, in Frodo's skirmish: strength -1 and no damage bonus.
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();
		scn.ShadowPlayCard(cry2);
		scn.ShadowChooseCard(savage1);
		assertEquals(5 - 1, scn.GetStrength(savage1));
		assertEquals(0, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(savage1, Keyword.DAMAGE));

		// 3 against 4: Frodo loses, and takes the one wound of a minion with no damage bonus.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheMinionIsWithoutTheKeywordsOfItsTextAndThoseItsTextWouldGiveItUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		// Frodo is strength 5, so that an Axeman at 9 beats him without overwhelming him
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With 2 burdens each Easterling Axeman is strength +3 and fierce.
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(axeman1, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertEquals(6 + 3, scn.GetStrength(axeman1));

		scn.FreepsUseCardAction(eowyn);
		scn.FreepsChooseCard(axeman1);

		// It loses the keyword printed in its text and cannot have the one its text would give it; the strength
		// its text gives it is no keyword.  The other Axeman is as he was.
		assertFalse(scn.HasKeyword(axeman1, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertEquals(6 + 3, scn.GetStrength(axeman1));
		assertTrue(scn.HasKeyword(axeman2, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, axeman2);

		// Aragorn's skirmish, 8 against 9, and then Frodo's, 5 against 9: the Axeman is still neither.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(frodo);
		assertFalse(scn.HasKeyword(axeman1, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The other Axeman brings the fierce assignment phase about; the one she named is not fierce in it and is
		// not the Free Peoples player's to assign.
		assertTrue(scn.InFierceRound());
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(axeman2));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(axeman1));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The regroup phase: the burdens are still 2, and it is an Easterling and fierce again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(axeman1, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(axeman1, Keyword.FIERCE));
	}

	@Test
	public void AMorgulSpearmanSheNamesIsPaidForWithAnExertionOnceItIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(eowyn, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(spearman, lemenya));
		scn.FreepsChooseCard(spearman);

		// "The Free Peoples player must exert a companion to assign this minion": asked now that it is the minion
		// named, of any companion, Éowyn among them.
		assertFalse(scn.IsCharAssigned(spearman));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, eowyn, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spearman));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNobodyLeftToExertAMorgulSpearmanIsNotHersToNameAndTheOtherMinionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya);
		// Éowyn has the one exertion her own ability takes; the Ring-bearer is exhausted
		scn.AddWoundsToChar(eowyn, 1);
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsUseCardAction(eowyn);

		// With her exertion spent nobody could exert for the Spearman: the Nazgûl is the one minion she can name,
		// and is assigned without a choice.
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));
		assertFalse(scn.IsCharAssigned(spearman));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyMinionLeftForTheRingBearerIsAMorgulSpearmanNobodyCouldExertFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya);
		// Éowyn has the one exertion her own ability takes; Frodo and Aragorn are exhausted
		scn.AddWoundsToChar(eowyn, 1);
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// While the Nazgûl can still be assigned to the Ring-bearer the ability is offered: she costs nothing.
		assertTrue(scn.FreepsActionAvailable(eowyn));
		scn.FreepsPass();

		// The Nazgûl goes to Aragorn by the ability Unexpected Visitor gives her.
		assertTrue(scn.ShadowActionAvailable(lemenya));
		scn.ShadowUseCardAction(lemenya);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, lemenya));

		// The Spearman is the one minion left for the Ring-bearer.  "Exert Éowyn and assign a minion": once she
		// has exerted, no companion could exert for the Spearman, so the whole cost cannot be paid.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetVitality(eowyn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(spearman));
		assertFalse(scn.FreepsActionAvailable(eowyn));
	}
}
