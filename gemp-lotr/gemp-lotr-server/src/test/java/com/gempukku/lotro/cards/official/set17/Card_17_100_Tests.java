package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("caves", "17_100");
					put("caves2", "17_100");

					// [rohan] cards to spot, and one that is not
					put("guard", "5_83");        // Household Guard: [rohan] companion, not unique; valiant and no other text
					put("rider", "4_286");       // Rider of Rohan: [rohan] companion, not unique; his ability needs a mount
					put("theoden", "4_292");     // Théoden, Son of Thengel: unique [rohan] companion, cost 2; a regroup ability only
					put("guma", "4_277");        // Guma, Plains Farmer: a [rohan] Man who is an ally; a fellowship ability only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4

					put("sam", "1_311");         // Sam, Son of Hamfast: a [shire] Hobbit beside Frodo; strength 3, vitality 4
					put("sword", "1_299");       // Hobbit Sword: strength +2 and no text; keeps Frodo from being overwhelmed by a 9
					put("visitor", "7_126");     // Unexpected Visitor: on a [gondor] companion; each minion gains "Assignment: Assign this minion to bearer"

					// Minions with keywords in their game text
					put("sauron", "9_48");       // Sauron, The Lord of the Rings: damage +2, enduring, fierce
					put("captain", "4_225");     // Easterling Captain: [raider] Man, Easterling and fierce; strength 11, site 4
					put("blackrider", "12_161"); // Black Rider: [wraith] Nazgûl, fierce; strength 8 +1 for each companion
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, damage +1 and no other text
					put("scout", "1_270");       // Orc Scouting Band: [sauron] Orc, tracker; a skirmish ability used only on purpose
					put("axeman1", "4_224");     // Easterling Axeman: Easterling; strength +3 and fierce while 2 burdens can be spotted
					put("axeman2", "4_224");
					put("nomad1", "7_132");      // Desert Nomad: [raider] Man, Southron, strength 9, vitality 2; damage +1 while 2 threats can be spotted
					put("nomad2", "7_132");

					put("spearman", "7_201");    // Morgul Spearman: while a Nazgûl can be spotted, the Free Peoples player must exert a companion to assign it
					put("lemenya", "1_232");     // Úlairë Lemenya: the Nazgûl to spot; strength 9, no keyword; her text is about [wraith] conditions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo is strength 3 and takes his wounds
				VirtualTableScenario.GreatRing
		);
	}

	// Gimli, Bearer of Grudges is the Ring-bearer.  Frodo, Frenzied Fighter is the one Frodo who can stand beside
	// another Ring-bearer.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("caves", "17_100");
					put("frodo", "13_149");      // Frodo, Frenzied Fighter: [shire] Hobbit, Ring-bound, strength 3, vitality 4; a skirmish ability used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North
					put("captain", "4_225");     // Easterling Captain: [raider] Man, Easterling and fierce
					put("blackrider", "12_161"); // Black Rider: [wraith] Nazgûl, fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void IntotheCavesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Into the Caves
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 3 [rohan] companions.
		 * <b>Assignment:</b> Assign a minion to Frodo to make that minion lose all game text keywords of your choice
		 * and unable to gain game text keywords until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("caves");

		assertEquals("Into the Caves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAThirdRohanCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caves = scn.GetFreepsCard("caves");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var theoden = scn.GetFreepsCard("theoden");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, rider, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(caves, theoden);

		scn.StartGame();

		// Two [rohan] companions, a [rohan] ally, and two companions who are not [rohan].
		assertInZone(Zone.HAND, caves);
		assertFalse(scn.FreepsPlayAvailable(caves));

		scn.FreepsPlayCard(theoden);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(caves));

		scn.FreepsPlayCard(caves);
		// 2 for Théoden + 1 for the condition
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, caves);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IntotheCavesAssignsFrodoToMinionToRemoveKeywords() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var sauron = scn.GetShadowCard("sauron");
		scn.MoveCardsToSupportArea(caves);

		scn.StartGame();
		scn.MoveMinionsToTable(sauron);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sauron));
		assertTrue(scn.FreepsActionAvailable(caves));

		// The one minion goes to Frodo with nothing asked.
		scn.FreepsUseCardAction(caves);
		assertTrue(scn.IsCharAssignedAgainst(frodo, sauron));

		// The whole damage bonus goes with the one word.
		assertTrue(scn.FreepsDecisionAvailable("keyword"));
		scn.FreepsChooseOption("DAMAGE");
		assertEquals(0, scn.GetKeywordCount(sauron, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		scn.FreepsChooseYes();
		scn.FreepsChooseOption("ENDURING");
		assertFalse(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		scn.FreepsChooseNo();

		// "Of your choice": the keyword that was not named stays.
		assertFalse(scn.HasKeyword(sauron, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(sauron, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(sauron, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AssignsAMinionOfThePlayersChoiceToFrodoAndItLosesTheKeywordHeNames() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var caves2 = scn.GetFreepsCard("caves2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		var captain = scn.GetShadowCard("captain");
		var blackrider = scn.GetShadowCard("blackrider");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(aragorn, visitor);
		scn.MoveCardsToSupportArea(caves, caves2);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, blackrider, savage, scout);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsActionAvailable(caves));
		assertTrue(scn.FreepsActionAvailable(caves2));
		scn.FreepsPass();

		// The Orc Scouting Band goes to Aragorn by the ability Unexpected Visitor gives it.
		scn.ShadowUseCardAction(scout);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, scout));

		assertTrue(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));
		// A site-4 minion at site 2
		assertTrue(scn.HasKeyword(captain, Keyword.ROAMING));
		assertFalse(scn.IsCharAssigned(frodo));
		scn.FreepsUseCardAction(caves);

		// Minions of three cultures and three races, exactly one to be chosen; the minion that is already assigned
		// is not among them, nor are the companions beside Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(captain, blackrider, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(captain);

		// To Frodo, not to the other Hobbit beside him.
		assertTrue(scn.IsCharAssignedAgainst(frodo, captain));
		assertFalse(scn.IsCharAssigned(sam));

		// Both keywords of its game text can be named.  Easterling is, and fierce is not.
		assertTrue(scn.FreepsDecisionAvailable("keyword"));
		assertTrue(scn.FreepsChoiceAvailable("EASTERLING"));
		assertTrue(scn.FreepsChoiceAvailable("FIERCE"));
		scn.FreepsChooseOption("EASTERLING");
		scn.FreepsChooseNo();

		assertFalse(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));
		// Roaming is no keyword of its game text, and its strength is untouched.
		assertTrue(scn.HasKeyword(captain, Keyword.ROAMING));
		assertEquals(11, scn.GetStrength(captain));
		// "That minion": the others keep theirs.
		assertTrue(scn.HasKeyword(blackrider, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(scout, Keyword.TRACKER));

		// With a minion on Frodo no second minion can be assigned to him: neither copy is offered again, though
		// two minions are unassigned.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(blackrider));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.FreepsActionAvailable(caves));
		assertFalse(scn.FreepsActionAvailable(caves2));
	}

	@Test
	public void BesideAnotherRingBearerTheMinionIsAssignedToFrodoAndNotToTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var gimli = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var frodo = scn.GetFreepsCard("frodo");
		var captain = scn.GetShadowCard("captain");
		var blackrider = scn.GetShadowCard("blackrider");
		scn.MoveCompanionsToTable(frodo);
		scn.MoveCardsToSupportArea(caves);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, blackrider);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(caves));
		assertTrue(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));
		scn.FreepsUseCardAction(caves);
		assertTrue(scn.FreepsHasCardChoiceAvailable(captain, blackrider));
		scn.FreepsChooseCard(captain);

		// The card names Frodo: the minion goes to him, and not to Gimli, who bears the Ring.
		assertTrue(scn.IsCharAssignedAgainst(frodo, captain));
		assertFalse(scn.IsCharAssigned(gimli));

		scn.FreepsChooseOption("EASTERLING");
		scn.FreepsChooseNo();

		assertFalse(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(blackrider, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileNoFrodoIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var gimli = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var aragorn = scn.GetFreepsCard("aragorn");
		var captain = scn.GetShadowCard("captain");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(caves);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// A Ring-bearer, another companion and a minion, all unassigned; nobody is named Frodo.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(captain));
		assertFalse(scn.FreepsActionAvailable(caves));
	}

	@Test
	public void TheKeywordNamedIsGoneAndCannotBeGainedBackUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var nomad1 = scn.GetShadowCard("nomad1");
		var nomad2 = scn.GetShadowCard("nomad2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(caves);
		// Frodo is strength 5, so that a Nomad at 9 beats him without overwhelming him
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.AddThreats(2);
		scn.MoveMinionsToTable(nomad1, nomad2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// With 2 threats each Desert Nomad is damage +1.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.HasKeyword(nomad1, Keyword.SOUTHRON));
		assertEquals(1, scn.GetKeywordCount(nomad1, Keyword.DAMAGE));

		scn.FreepsUseCardAction(caves);
		scn.FreepsChooseCard(nomad1);
		scn.FreepsChooseOption("DAMAGE");
		scn.FreepsChooseNo();

		// The damage bonus is gone, though its own text goes on giving it; the keyword that was not named
		// stays.  The other Nomad is as it was.
		assertEquals(0, scn.GetKeywordCount(nomad1, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(nomad1, Keyword.SOUTHRON));
		assertEquals(1, scn.GetKeywordCount(nomad2, Keyword.DAMAGE));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nomad2);

		// Aragorn's skirmish, 8 against 9: 1 for losing + 1 for the other Nomad's damage +1.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Frodo's, 5 against 9, after it: the Nomad that was named still has no damage bonus, and he takes
		// the one wound.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(nomad1, Keyword.DAMAGE));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The regroup phase: the threats are still 2, and it is damage +1 again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(nomad1, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(nomad1, Keyword.SOUTHRON));
	}

	@Test
	@Ignore("Q-0282: 'unable to gain game text keywords' is built for the keywords the player named alone; a keyword he did not name is gained")
	public void AMinionCannotGainAKeywordThatWasNotNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCardsToSupportArea(caves);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// No burden: an Easterling Axeman has the one keyword, and is strength 6.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.HasKeyword(axeman1, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));

		scn.FreepsUseCardAction(caves);
		scn.FreepsChooseCard(axeman1);
		scn.FreepsChooseOption("EASTERLING");
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(frodo, axeman1));
		assertFalse(scn.HasKeyword(axeman1, Keyword.EASTERLING));

		// Two burdens come after the loss: each Axeman's own text would now make it strength +3 and fierce.
		scn.AddBurdens(2);
		assertEquals(6 + 3, scn.GetStrength(axeman2));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));
		// The one that was named has the strength, which is no keyword, and cannot gain fierce.
		assertEquals(6 + 3, scn.GetStrength(axeman1));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AKeywordNamedInAnEarlierUseIsNotLostByTheMinionOfALaterUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var sword = scn.GetFreepsCard("sword");
		var axeman1 = scn.GetShadowCard("axeman1");
		var captain = scn.GetShadowCard("captain");
		scn.MoveCardsToSupportArea(caves);
		// Frodo is strength 5, so that an Axeman at 6 beats him without overwhelming him
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, captain);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The first use: the Axeman goes to Frodo and loses Easterling, its one keyword.
		scn.FreepsUseCardAction(caves);
		scn.FreepsChooseCard(axeman1);
		scn.FreepsChooseOption("EASTERLING");
		scn.FreepsChooseNo();
		assertFalse(scn.HasKeyword(axeman1, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));

		// Nobody else is assigned.  Frodo, 5 against 6, takes a wound, and the Captain brings the fierce
		// assignment phase about.
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The second use: the Captain, the one fierce minion, goes to Frodo, and fierce alone is named.
		assertTrue(scn.FreepsActionAvailable(caves));
		scn.FreepsUseCardAction(caves);
		assertTrue(scn.IsCharAssignedAgainst(frodo, captain));
		scn.FreepsChooseOption("FIERCE");
		scn.FreepsChooseNo();

		assertFalse(scn.HasKeyword(captain, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AMorgulSpearmanNamedIsPaidForWithAnExertionOnceItIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(caves);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(caves));
		scn.FreepsUseCardAction(caves);
		assertTrue(scn.FreepsHasCardChoiceAvailable(spearman, lemenya));
		scn.FreepsChooseCard(spearman);

		// "The Free Peoples player must exert a companion to assign this minion": asked now that it is the minion
		// named, of either companion.
		assertFalse(scn.IsCharAssigned(spearman));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spearman));
		assertFalse(scn.IsCharAssigned(lemenya));

		// The assignment paid for, the card goes on to its keywords.
		assertTrue(scn.FreepsDecisionAvailable("keyword"));
		scn.FreepsChooseOption("DAMAGE");
		scn.FreepsChooseNo();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNobodyAbleToExertAMorgulSpearmanIsNotHisToNameAndTheOtherMinionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var caves = scn.GetFreepsCard("caves");
		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(caves);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya);
		// The Ring-bearer, the one companion, is exhausted
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(caves));
		scn.FreepsUseCardAction(caves);

		// Nobody could exert for the Spearman: the Nazgûl is the one minion that can be named, and is assigned
		// without a choice.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, lemenya));
		assertFalse(scn.IsCharAssigned(spearman));

		assertTrue(scn.FreepsDecisionAvailable("keyword"));
		scn.FreepsChooseOption("DAMAGE");
		scn.FreepsChooseNo();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
