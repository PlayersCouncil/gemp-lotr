package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_042_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Footman is
	// played in the Shadow phase at Goblin Town (site 4), his own site number, so he is not roaming; Goblin Town's
	// only text is a Free Peoples maneuver action.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("footman", "30_42");
					put("footman2", "30_42");
					// Azog's Mace: [moria] hand weapon, cost 1, "Bearer must be an Orc"; fierce on Azog and a
					// Shadow ability, neither reached
					put("mace", "32_29");
					// Savage Warg: [gundabad] mount, cost 3, "Bearer must be an Orc"; bearer fierce, and a text on
					// the bearer winning a skirmish (no skirmish is fought)
					put("warg", "31_25");
					// Ring of Thrór: [sauron] artifact, cost 0, "Bearer must be Sauron"; an artifact, no possession
					put("ringofthror", "32_44");
					// Sauron, The Necromancer: [sauron] Maia minion who may bear the Ring of Thrór; +1 and fierce
					// to [gundabad] Orcs (none in play) and an assignment ability
					put("sauron", "32_45");
					// Goblin Runner: [moria] Orc, 5/1; another of the Shadow player's minions, cheated in so its
					// text on being played does not fire
					put("runner", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinFootmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Goblin Footman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may play a possession from your discard pile on your minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("footman");

		assertEquals("Goblin Footman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingFootmanMayPlayAPossessionFromTheDiscardPileOnAnyOfYourMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var mace = scn.GetShadowCard("mace");
		var warg = scn.GetShadowCard("warg");
		var ringofthror = scn.GetShadowCard("ringofthror");
		var sauron = scn.GetShadowCard("sauron");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveCardsToDiscard(mace, warg, ringofthror);
		scn.MoveMinionsToTable(runner, sauron);
		scn.MoveCardsToHand(footman);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInDiscard(mace, warg, ringofthror);
		assertInHand(footman);

		scn.ShadowPlayCard(footman);
		assertInPlay(footman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(footman));
		scn.ShadowAcceptOptionalTrigger();

		// The two possessions, of two cultures; not the artifact, which Sauron could bear.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mace, warg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ringofthror));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(warg);

		// Any minion of his may take it, not only the Footman.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);

		assertAttachedTo(warg, runner);
		assertTrue(scn.GetAttachedCards(footman).isEmpty());
		assertInDiscard(mace, ringofthror);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesThePossessionsInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var mace = scn.GetShadowCard("mace");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		scn.MoveCardsToDiscard(mace, warg);
		scn.MoveCardsToHand(footman);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInDiscard(mace, warg);

		scn.ShadowPlayCard(footman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(footman));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(mace, warg);
		assertTrue(scn.GetAttachedCards(footman).isEmpty());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondFootmanPlayedOffersOnlyItsOwnPlayAndNotTheFirstFootmans() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var footman2 = scn.GetShadowCard("footman2");
		var mace = scn.GetShadowCard("mace");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		scn.MoveMinionsToTable(footman);
		scn.MoveCardsToDiscard(mace);
		scn.MoveCardsToHand(footman2);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInPlay(footman);
		assertInDiscard(mace);

		scn.ShadowPlayCard(footman2);

		// Another minion of the Footman's own kind played: the Footman in play answers nothing, the one played
		// offers its own.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(footman2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(footman));
		scn.ShadowDeclineOptionalTrigger();

		assertInDiscard(mace);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoPossessionInTheDiscardPileCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var mace = scn.GetShadowCard("mace");
		var ringofthror = scn.GetShadowCard("ringofthror");
		var sauron = scn.GetShadowCard("sauron");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		scn.MoveCardsToDiscard(mace, ringofthror);
		scn.MoveMinionsToTable(sauron);
		scn.MoveCardsToHand(footman);
		// The Footman's 3 and nothing over: the Mace's 1 cannot be paid, and the Ring of Thrór, which costs
		// nothing and has Sauron to bear it, is no possession.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInDiscard(mace, ringofthror);

		scn.ShadowPlayCard(footman);

		assertInPlay(footman);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(mace, ringofthror);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionInTheOtherPlayersDiscardPileIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");
		var freepsWarg = scn.GetFreepsCard("warg");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();

		// The Free Peoples player's own copy of the Savage Warg, in his own discard pile; the Shadow player's
		// discard pile holds no possession.
		scn.MoveCardsToDiscard(freepsWarg);
		scn.MoveCardsToHand(footman);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInDiscard(freepsWarg);

		scn.ShadowPlayCard(footman);

		assertInPlay(footman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(freepsWarg);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
