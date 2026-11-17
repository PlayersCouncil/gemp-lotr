package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "13_81");
					// Saruman, Keeper of Isengard: [isengard] Wizard minion who may not be assigned to a skirmish; his
					// response is about Uruk-hai, and none is laid.
					put("saruman", "3_68");
					// Saruman, Rabble-rouser: [dunland] Wizard minion who may not be assigned to a skirmish; his move
					// trigger counts [dunland] Men, and none is laid.
					put("rabble", "4_33");
					// Saruman, Servant of Sauron: [isengard] follower, a Saruman who is no minion; his Aid is declined
					// on the way.
					put("follower", "17_38");
					put("runner", "1_178");       // Goblin Runner: a minion who is no Wizard; "may add (2)" as it is played
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: the Free Peoples player's Wizard
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no Wizard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StaffofSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Staff of Saruman, Fallen Istar's Stave
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be a Wizard.<br><b>Regroup:</b> If Saruman is the only minion you can spot, discard this artifact from play to return him to your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Staff of Saruman", card.getBlueprint().getTitle());
		assertEquals("Fallen Istar's Stave", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnAWizardOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		var staff = scn.GetShadowCard("staff");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToHand(staff);
		assertInHand(staff);
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman, runner);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(staff);
		assertTrue(scn.ShadowPlayAvailable(staff));
		scn.ShadowPlayCard(staff);

		// "A Wizard" names no side: his own Saruman and the Free Peoples player's Gandalf.  Neither the minion nor
		// the companion who is no Wizard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gandalf);

		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItFromGandalfToReturnSarumanToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var staff = scn.GetShadowCard("staff");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.FreepsPass();
		// Saruman may not be assigned to a skirmish: he stands into the regroup phase, the only minion there is.
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowActionAvailable(staff));

		scn.ShadowUseCardAction(staff);

		// The artifact is discarded, and Saruman, not its bearer, goes to the Shadow player's hand.
		assertInDiscard(staff);
		assertInHand(saruman);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASarumanOfAnotherCultureBearingItIsReturnedToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetShadowCard("staff");
		var rabble = scn.GetShadowCard("rabble");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(rabble);
		scn.AttachCardsTo(rabble, staff);
		assertAttachedTo(staff, rabble);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowActionAvailable(staff));

		scn.ShadowUseCardAction(staff);

		assertInDiscard(staff);
		assertInHand(rabble);
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileAnotherMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetShadowCard("staff");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);

		scn.SkipToPhase(Phase.REGROUP);
		// A minion that lived through the skirmishes, beside Saruman.
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));
		assertAttachedTo(staff, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman, runner);
	}

	@Test
	public void ASarumanWhoIsNoMinionDoesNotMakeItUsable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var staff = scn.GetShadowCard("staff");
		var follower = scn.GetShadowCard("follower");

		scn.StartGame();
		scn.FreepsPass();
		// The follower in the Shadow player's support area, the Staff on Gandalf, and no minion at all.
		scn.MoveCardsToSupportArea(follower);
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, follower);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, follower);
	}
}
