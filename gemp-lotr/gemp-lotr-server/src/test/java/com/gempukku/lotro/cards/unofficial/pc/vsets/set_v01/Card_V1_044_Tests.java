package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fell", "101_44");

					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc that is not unique, strength 7; a skirmish ability that is never used
					put("warden", "1_259");     // Morgul Warden: a unique [sauron] Orc, strength 9, and +1 for each other [sauron] Orc
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai; damage +1 is its whole text
					put("summons", "1_242");    // The Dark Lord's Summons: [sauron] condition, support area; a Shadow-phase ability
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition borne by a companion, who cannot heal at a sanctuary

					put("luthien", "1_100");    // The Choice of Luthien: [gondor] condition, support area; a maneuver ability that is never used
					put("tale", "1_317");       // There and Back Again: [shire] condition borne by a Hobbit companion; a maneuver ability that is never used
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, strength +2 and no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FelltoItsPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Fell to Its Power
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Discard a [Sauron] Orc to add a burden. The Free Peoples player may discard a Free Peoples condition to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fell");

		assertEquals("Fell to Its Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsASauronOrcToAddABurdenAndNobodyIsAskedWithNoFreePeoplesConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fell = scn.GetShadowCard("fell");
		var soldier = scn.GetShadowCard("soldier");
		var warden = scn.GetShadowCard("warden");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");
		var evil = scn.GetShadowCard("evil");
		var sword = scn.GetFreepsCard("sword");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCardsToHand(fell);
		// What the Free Peoples player has that is nearly a Free Peoples condition in play: a possession, a
		// Shadow condition on his companion, and a condition in his hand.
		scn.AttachCardsTo(frodo, sword, evil);
		scn.MoveCardsToHand(luthien);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, warden, runner, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetBurdens());
		// 1 companion + 2 for the shadow number of site 2
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.HAND, fell);
		assertTrue(scn.ShadowPlayAvailable(fell));

		scn.ShadowPlayCard(fell);

		// A [sauron] Orc that is not unique and one that is; not the [moria] Orc, nor the [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, warden));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, sentry));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowChooseCard(warden);

		assertInDiscard(warden);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, runner, sentry);
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(fell);
		// The burden is added with nothing asked of the Free Peoples player.
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(sword, frodo);
		assertAttachedTo(evil, frodo);
		assertInZone(Zone.HAND, luthien);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSauronOrcInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fell = scn.GetShadowCard("fell");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var sentry = scn.GetShadowCard("sentry");
		// The [sauron] Orc is in hand, and "discard" is from play.
		scn.MoveCardsToHand(fell, soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// The pool would pay for it, and an Orc and a [sauron] minion are in play.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, sentry);
		assertInZone(Zone.HAND, fell, soldier);
		assertFalse(scn.ShadowPlayAvailable(fell));
	}

	@Test
	public void IsPlayedInTheRegroupPhaseAndNotInTheAssignmentPhaseNorASkirmishBeforeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var fell = scn.GetShadowCard("fell");
		var warden = scn.GetShadowCard("warden");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(warden);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// 2 companions + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(fell));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsAssignToMinions(aragorn, warden);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fell));

		// The Warden's 9 beats Aragorn's 8, and the regroup phase follows.
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, warden);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fell));
	}

	@Test
	public void FreePeoplesPlayerMayDiscardAFreePeoplesConditionToPreventTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fell = scn.GetShadowCard("fell");
		var soldier = scn.GetShadowCard("soldier");
		var summons = scn.GetShadowCard("summons");
		var evil = scn.GetShadowCard("evil");
		var luthien = scn.GetFreepsCard("luthien");
		var tale = scn.GetFreepsCard("tale");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(fell);
		scn.MoveCardsToSupportArea(summons, luthien);
		scn.AttachCardsTo(frodo, tale, sword, evil);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		scn.ShadowPlayCard(fell);

		// The Orc is discarded before anything is asked.
		assertInDiscard(soldier);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard a Free Peoples condition to prevent adding a burden"));
		scn.FreepsChooseYes();

		// A [gondor] condition in the support area and a [shire] one borne by a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(luthien, tale));
		// A Shadow condition on a companion, a Shadow condition in the Shadow support area, and a Free
		// Peoples possession.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(evil, summons, sword));
		scn.FreepsChooseCard(tale);

		assertInDiscard(tale);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SUPPORT, luthien, summons);
		assertAttachedTo(evil, frodo);
		assertAttachedTo(sword, frodo);
		assertEquals(0, scn.GetBurdens());
		// What was paid stays paid: the Orc, the event and its (1).
		assertInDiscard(soldier);
		assertInDiscard(fell);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningToPreventLeavesTheConditionsInPlayAndTheBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fell = scn.GetShadowCard("fell");
		var soldier = scn.GetShadowCard("soldier");
		var luthien = scn.GetFreepsCard("luthien");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCardsToHand(fell);
		scn.MoveCardsToSupportArea(luthien);
		scn.AttachCardsTo(frodo, tale);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetBurdens());

		scn.ShadowPlayCard(fell);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard a Free Peoples condition to prevent adding a burden"));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, luthien);
		assertAttachedTo(tale, frodo);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInDiscard(soldier);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
