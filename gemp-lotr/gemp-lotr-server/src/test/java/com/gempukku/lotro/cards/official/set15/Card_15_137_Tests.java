package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("doorwarden", "15_137");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is his whole text
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside 3 [rohan]
					// companions or Éowyn); his abilities are used only on purpose
					put("merry", "103_85");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is his whole text
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession on a Man; changes no number
					put("helm", "5_89");        // Rohirrim Helm: a [rohan] possession on a [rohan] Man; changes no number; its skirmish ability is used only on purpose
					// Spider Poison: a [gollum] (Shadow) possession, cheated onto a companion; changes no number, and
					// its bearer exerts only when assigned to a skirmish
					put("poison", "8_28");
					// Narsil, Blade of the Faithful: a [gondor] artifact on a [gondor] Man, vitality +1; its strength
					// text is for Elendil or Isildur only
					put("narsil", "9_34");
					put("wounded", "4_215");    // Wounded: an [isengard] condition on a Free Peoples Man, cheated on; changes no number
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimDoorwardenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rohirrim Doorwarden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Follower
		 * Game Text: <b>Aid</b> – (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper> To play, spot a [rohan] Man.
		 * 	Bearer is strength +1 for each possession he or she bears.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("doorwarden");

		assertEquals("Rohirrim Doorwarden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, scn.GetAidTwilightCost(card));
	}

	@Test
	public void CannotBePlayedUntilARohanManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorwarden = scn.GetFreepsCard("doorwarden");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveCardsToHand(doorwarden, guard);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(doorwarden));

		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsPlayAvailable(doorwarden));

		scn.FreepsPlayCard(doorwarden);
		assertInZone(Zone.SUPPORT, doorwarden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingARohanManWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorwarden = scn.GetFreepsCard("doorwarden");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(doorwarden);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(doorwarden));
		scn.FreepsPlayCard(doorwarden);

		assertInZone(Zone.SUPPORT, doorwarden);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakenByItsAidToACompanionHeIsStrengthPlusOneForEachPossessionHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorwarden = scn.GetFreepsCard("doorwarden");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var helm = scn.GetFreepsCard("helm");
		var narsil = scn.GetFreepsCard("narsil");
		var poison = scn.GetShadowCard("poison");
		var wounded = scn.GetShadowCard("wounded");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveCardsToSupportArea(doorwarden);
		// The Knight bears a Free Peoples possession, a Shadow possession, an artifact and a condition.
		scn.AttachCardsTo(knight, chain, narsil, poison, wounded);
		// The Guard bears a possession, and will not bear the follower.
		scn.AttachCardsTo(guard, helm);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		// In the support area the follower has no bearer, and nobody is changed.
		assertInZone(Zone.SUPPORT, doorwarden);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(guard));
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(doorwarden, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// The Aid: (2) added.
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, guard, frodo));
		scn.FreepsChooseCard(knight);

		assertAttachedTo(doorwarden, knight);
		// 6 + 1 for Heavy Chain + 1 for Spider Poison; nothing for Narsil, an artifact, or Wounded, a condition.
		assertEquals(8, scn.GetStrength(knight));
		// The Guard's possession does him no good: he does not bear the follower.
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlusOneMoreForEachPossessionPlayedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var doorwarden = scn.GetFreepsCard("doorwarden");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, doorwarden);
		scn.MoveCardsToHand(chain, helm);

		scn.StartGame();

		// No possession borne: his printed 6.
		assertAttachedTo(doorwarden, guard);
		assertEquals(6, scn.GetStrength(guard));

		// The Guard is the only Man, so the chain goes to him without a choice.
		scn.FreepsPlayCard(chain);
		assertAttachedTo(chain, guard);
		assertEquals(7, scn.GetStrength(guard));

		scn.FreepsPlayCard(helm);
		assertAttachedTo(helm, guard);
		assertEquals(8, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
