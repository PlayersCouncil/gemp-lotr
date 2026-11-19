package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throne", "17_39");

					// Saruman, Master of Foul Folk: [isengard] Wizard minion, cost 4, site 4.  A move trigger heals
					// [isengard] Orcs, of whom there are none; a Shadow ability used only on purpose; his first sentence
					// is about archery wounds and assignment.
					put("foulfolk", "5_56");
					// Saruman, Coldly Still: [men] Wizard minion, cost 4, site 4; strength +2 for [men] minions bearing
					// possessions, of which there are none; a skirmish ability used only on purpose
					put("coldly1", "17_48");
					put("coldly2", "17_48");
					// Saruman, Servant of Sauron: the [isengard] Saruman who is no minion, a follower, cost 1; Aid and
					// a skirmish-win trigger only
					put("follower", "17_38");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, strength 5, vitality 3; Damage +1

					// Aragorn, Ranger of the North: a ranger, strength 8, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					put("whatare", "1_119");    // What Are They?: Maneuver event, cost 1.  "Spot a ranger to discard a roaming minion."
					// Gandalf, Friend of the Shirefolk: a Wizard companion; strength +1 for each race in the fellowship
					put("gandalf", "1_72");
					// Radagast, The Brown: a [gandalf] Wizard ally (Hobbit set: the one kind of printed Wizard who is an
					// ally, beside Saruman, The White); his fellowship ability is used only on purpose
					put("radagast", "31_17");
					// Saruman, The White: the one Free Peoples Saruman in print, a [gandalf] Wizard ally (Hobbit set),
					// cost 4; Gandalf is defender +1 beside him, and his abilities are used only on purpose
					put("white", "32_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThroneofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Throne of Isengard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: Each time you play Saruman, spot a minion. That minion is <b>fierce</b> until the start of the regroup phase.<br>Each Wizard is strength +3 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("throne");

		assertEquals("Throne of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeYouPlaySarumanAMinionIsChosenAndMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var throne = scn.GetShadowCard("throne");
		var foulfolk = scn.GetShadowCard("foulfolk");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCardsToHand(foulfolk);
		assertInZone(Zone.SUPPORT, throne);
		assertInHand(foulfolk);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.ShadowPlayCard(foulfolk);

		// A minion must be chosen: Saruman himself and minions of two other cultures and races; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(foulfolk, troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(foulfolk, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheMinionMadeFierceIsFierceInTheFierceAssignmentPhaseAndNotAtTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var throne = scn.GetShadowCard("throne");
		var foulfolk = scn.GetShadowCard("foulfolk");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCardsToHand(foulfolk);
		assertInZone(Zone.SUPPORT, throne);
		assertInHand(foulfolk);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.ShadowPlayCard(foulfolk);
		scn.ShadowChooseCard(savage);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		// Aragorn's 8 beats the Savage's 5 in the skirmish, and again in the fierce skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The fierce assignment phase: still fierce.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		// The regroup phase has started: fierce is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void ASecondSarumanPlayedAtASecondShadowPhaseAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatare);
		assertInHand(whatare);

		var throne = scn.GetShadowCard("throne");
		var coldly1 = scn.GetShadowCard("coldly1");
		var coldly2 = scn.GetShadowCard("coldly2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCardsToHand(coldly1);
		assertInZone(Zone.SUPPORT, throne);
		assertInHand(coldly1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [men] Saruman: asked, and he is chosen himself.
		scn.ShadowPlayCard(coldly1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(coldly1, savage));
		scn.ShadowChooseCard(coldly1);
		assertTrue(scn.HasKeyword(coldly1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// What Are They? discards him, roaming at site 2 (his site is 4).
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(whatare);
		scn.FreepsChooseCard(coldly1);
		assertInDiscard(coldly1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// A second move, and a second Shadow phase, with a second copy in hand.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveCardsToHand(coldly2);
		assertInHand(coldly2);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.ShadowPlayCard(coldly2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(coldly2, savage));
		scn.ShadowChooseCard(savage);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(coldly2, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSarumanWhoIsAFollowerPlayedAsksForAMinionToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var throne = scn.GetShadowCard("throne");
		var follower = scn.GetShadowCard("follower");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCardsToHand(follower);
		assertInZone(Zone.SUPPORT, throne);
		assertInHand(follower);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.ShadowPlayCard(follower);
		assertInZone(Zone.SUPPORT, follower);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(follower, aragorn));
		scn.ShadowChooseCard(savage);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherIsengardMinionPlayedSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var throne = scn.GetShadowCard("throne");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCardsToHand(savage);
		assertInZone(Zone.SUPPORT, throne);
		assertInHand(savage);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayersSarumanPlayedSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var white = scn.GetFreepsCard("white");
		scn.MoveCardsToHand(white);
		assertInHand(white);

		var throne = scn.GetShadowCard("throne");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(throne);
		assertInZone(Zone.SUPPORT, throne);

		scn.StartGame();
		// A minion that will stand in play later in the turn, laid early, so that a wrong answer has one to make fierce.
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(white));
		scn.FreepsPlayCard(white);
		assertInZone(Zone.SUPPORT, white);
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWizardOfEitherSideIsStrengthPlus3AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast);

		var throne = scn.GetShadowCard("throne");
		var coldly1 = scn.GetShadowCard("coldly1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Minions that will stand in play later in the turn, laid early.
		scn.MoveMinionsToTable(coldly1, troop);

		// Gandalf 6 +1 for the Hobbit in the fellowship; Radagast and Saruman 8; the Troop 13.
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(radagast));
		assertEquals(8, scn.GetStrength(coldly1));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(radagast, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(coldly1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));

		scn.MoveCardsToSupportArea(throne);
		assertInZone(Zone.SUPPORT, throne);

		// A Wizard companion, ally and minion; not the Troop, who is no Wizard.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(11, scn.GetStrength(radagast));
		assertEquals(11, scn.GetStrength(coldly1));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(radagast, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(coldly1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
	}
}
