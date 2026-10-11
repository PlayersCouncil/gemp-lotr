package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_262_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "1_262");
					put("runner", "1_178");     // Goblin Runner: a second minion, strength 5; its text acts only as it is played
					// Sam, Proper Poet: a Hobbit companion, vitality 4; "Maneuver: Exert Sam twice to make him
					// defender +1 until the regroup phase."
					put("sam", "2_114");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The fellowship fights at Ettenmoors, site 2 of the Fellowship path.
	protected VirtualTableScenario GetHobbitsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "1_262");
					put("assassin2", "1_262");
					put("assassin3", "1_262");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: the second Hobbit companion; his text needs a weapon
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion; a fellowship ability used only on purpose
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit.  "To play, spot a [gondor] companion",
					// so he is cheated in; his skirmish ability is never used.
					put("trollslayer", "103_31");
					// Smeagol, Once Like Us: a [shire] companion with no race.  "To play, add a burden", so he is
					// cheated in; his own assignment ability needs a fellowship of [shire] companions alone.
					put("smeagol", "103_116");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("guest", "1_297");      // Hobbit Party Guest: a Hobbit ally whose home is site 1, away from home at site 2
					put("bounder", "1_286");    // Bounder: a Hobbit ally whose home is site 2, at home there; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRoamingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Assassin: cost 2, site number 6
					put("assassin1", "1_262");
					put("assassin2", "1_262");
					put("assassin3", "1_262");
					// Cirith Ungol Guard: a [sauron] minion who is no Orc (an Uruk-hai), cost 3, site number 6;
					// Damage +1 is its whole text
					put("guard1", "10_81");
					put("guard2", "10_81");
					put("guard3", "10_81");
					// Uruk Savage: an [isengard] minion, cost 2, site number 5; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.<br>The roaming penalty for each [sauron] minion you play is -1.<br><b>Assignment:</b> Spot 2 Hobbit companions to make the Free Peoples player assign a Hobbit to skirmish this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("assassin");

		assertEquals("Orc Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OrcAssassinDoesNotInterruptSkirmishesAfterUsingAssignmentAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		// Frodo and Sam
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		assertTrue(scn.CanBeAssignedViaAction(sam));
		scn.FreepsChooseCard(sam);
		assertTrue(scn.IsCharAssignedAgainst(sam, assassin));

		// The Free Peoples player's own assigning goes on with those who are left.
		scn.PassAssignmentActions();
		assertFalse(scn.FreepsCanAssign(sam));
		assertFalse(scn.FreepsCanAssign(assassin));
		assertTrue(scn.FreepsCanAssign(runner));
		assertTrue(scn.FreepsCanAssign(frodo));
		scn.FreepsAssignToMinions(frodo, runner);

		// No minion is left over, and the skirmish the ability made is fought like any other.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertTrue(scn.IsCharSkirmishing(assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OrcAssassinDoesNotPreventDefenderBonusFromBeingUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, runner);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		assertTrue(scn.CanBeAssignedViaAction(sam));
		scn.FreepsChooseCard(sam);
		assertTrue(scn.IsCharAssignedAgainst(sam, assassin));

		scn.PassAssignmentActions();

		// Sam has the Assassin already, and with defender +1 may be given the Runner as well.
		assertTrue(scn.HasKeyword(sam, Keyword.DEFENDER));
		assertTrue(scn.FreepsCanAssign(sam));
		scn.FreepsAssignToMinions(sam, runner);
		assertTrue(scn.IsCharAssignedAgainst(sam, assassin));
		assertTrue(scn.IsCharAssignedAgainst(sam, runner));

		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(assassin));
		assertTrue(scn.IsCharSkirmishing(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AHobbitAllyAwayFromHomeIsNotOfferedForTheAssassinAndOneAtHomeIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var assassin = scn.GetShadowCard("assassin");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guest = scn.GetFreepsCard("guest");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(guest, bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.IsCharAssigned(assassin));
		assertTrue(scn.ShadowActionAvailable(assassin));

		scn.ShadowUseCardAction(assassin);
		// "A Hobbit" names no card type, so it is every Hobbit who can skirmish here: the two companions and
		// the ally whose home this site is.  The ally away from home cannot skirmish, and an assignment action
		// cannot reach him; Aragorn is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guest, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(bounder);
		assertTrue(scn.IsCharAssignedAgainst(bounder, assassin));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(guest));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheNextSauronMinionPaysOneLessForRoamingAndTheAssassinItselfPaysThePenaltyInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var assassin1 = scn.GetShadowCard("assassin1");
		var assassin2 = scn.GetShadowCard("assassin2");
		var assassin3 = scn.GetShadowCard("assassin3");
		var guard1 = scn.GetShadowCard("guard1");
		var guard2 = scn.GetShadowCard("guard2");
		var guard3 = scn.GetShadowCard("guard3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(assassin1, assassin2, assassin3, guard1, guard2, guard3, savage);

		scn.StartGame();
		scn.SetTwilight(27);
		scn.FreepsPass();

		// Site 2: every minion here is roaming, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 27 set + 1 companion + 2 for Ettenmoors
		assertEquals(30, scn.GetTwilight());

		scn.ShadowPlayCard(assassin1);
		// 2 to play + 2 roaming: its own text is not in force while it is being played
		assertEquals(26, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, assassin1);

		scn.ShadowPlayCard(guard1);
		// 3 to play + 2 roaming - 1 for the Assassin.  The penalty is less; the minion is no less roaming.
		assertEquals(22, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard1, Keyword.ROAMING));

		scn.ShadowPlayCard(savage);
		// An [isengard] minion: 2 to play + 2 roaming
		assertEquals(18, scn.GetTwilight());

		scn.ShadowPlayCard(assassin2);
		// 2 to play + 2 roaming - 1 for the first Assassin, and nothing for itself
		assertEquals(15, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, assassin2);

		scn.ShadowPlayCard(guard2);
		// Two Assassins in play: 3 to play + 2 roaming - 2
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard2, Keyword.ROAMING));

		scn.ShadowPlayCard(assassin3);
		// 2 to play + 2 roaming - 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(guard3);
		// Three Assassins in play take the penalty of 2 to nothing and no lower: 3 to play, not 3 - 1
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard3, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithExactlyTwoHobbitCompanionsEachAssassinMayHaveAHobbitAssignedToItselfAndOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var frodo = scn.GetRingBearer();
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		// Two Hobbit companions and no more: Frodo, and a Pippin who is a [gondor] card.
		scn.MoveCompanionsToTable(trollslayer, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(assassin));
		assertTrue(scn.ShadowActionAvailable(assassin2));

		scn.ShadowUseCardAction(assassin);
		// The choice is the Free Peoples player's, and is not one he may decline.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.FreepsGetChoiceMin());
		// A Hobbit of whatever culture or card type; Smeagol is a [shire] companion and no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, trollslayer, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn));

		scn.FreepsChooseCard(trollslayer);
		// It is the Assassin whose ability was used that he faces, and not the one beside it.
		assertTrue(scn.IsCharAssignedAgainst(trollslayer, assassin));
		assertFalse(scn.IsCharAssigned(assassin2));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(bounder));

		// An assigned minion cannot be assigned again, so the first Assassin's ability would do nothing: it is
		// not offered, though two Hobbit companions can still be spotted and two Hobbits are unassigned.  The
		// second Assassin's still is.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));
		assertTrue(scn.ShadowActionAvailable(assassin2));

		scn.ShadowUseCardAction(assassin2);
		// Pippin has a minion already.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trollslayer, smeagol, aragorn));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin2));
		assertTrue(scn.IsCharAssignedAgainst(trollslayer, assassin));
		assertFalse(scn.IsCharAssigned(bounder));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneHobbitCompanionBesideAHobbitAllyAndAShireCompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var assassin = scn.GetShadowCard("assassin");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo is the one Hobbit companion.  The Bounder is a Hobbit and no companion, Smeagol a [shire]
		// companion and no Hobbit.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// Hobbits who could be assigned to it are there: only the two to spot are not.
		assertTrue(scn.CanBeAssignedViaAction(frodo));
		assertTrue(scn.CanBeAssignedViaAction(bounder));
		assertFalse(scn.IsCharAssigned(assassin));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(assassin));
	}

	@Test
	public void WithEveryHobbitAlreadyAssignedAnUnassignedAssassinDoesNotOfferItsAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitsScenario();

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var assassin3 = scn.GetShadowCard("assassin3");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2, assassin3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(assassin3));
		scn.ShadowUseCardAction(assassin);
		scn.FreepsChooseCard(sam);
		scn.FreepsPass();
		// Frodo is the one Hobbit left, and is assigned without a question.
		scn.ShadowUseCardAction(assassin2);
		assertTrue(scn.IsCharAssignedAgainst(sam, assassin));
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin2));

		// Two Hobbit companions can be spotted and the third Assassin is unassigned, but there is no Hobbit
		// left for the Free Peoples player to assign to it.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(assassin3));
		assertTrue(scn.CanBeAssignedViaAction(aragorn));
		assertFalse(scn.ShadowActionAvailable(assassin3));
	}

	@Test
	public void ASauronMinionWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var assassin1 = scn.GetShadowCard("assassin1");
		var guard1 = scn.GetShadowCard("guard1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();

		// Site 6, the site number of the Cirith Ungol Guard: he pays no roaming penalty for the Assassin to
		// make less.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(assassin1);
		scn.SetShadowHand(guard1);

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(guard1));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(guard1));

		scn.ShadowPlayCard(guard1);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(guard1, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
