package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attack", "4_137");
					put("attack2", "4_137");      // a second copy: the card is not unique and says "this card"

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5, vitality 3; damage +1 is its whole text
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, site 6; damage +1 is its whole text
					put("forger", "3_56");        // Isengard Forger: [isengard] Orc, cost 2, site 4; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AttackonHelmsDeepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Attack on Helm's Deep
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Shadow:</b> Play an Uruk-hai to place an [isengard] token on this card.<br><b>Regroup:</b> Remove 3 [isengard] tokens from this card and discard an Uruk-hai to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attack");

		assertEquals("Attack on Helm's Deep", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityPlaysAnUrukHaiFromHandToPlaceATokenOnThisCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var attack2 = scn.GetShadowCard("attack2");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(attack, attack2);
		scn.MoveCardsToHand(savage, guard, forger);
		assertInHand(savage, guard, forger);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(attack));
		assertEquals(0, scn.GetCultureTokensOn(attack2));
		assertTrue(scn.ShadowActionAvailable(attack));
		scn.ShadowUseCardAction(attack);

		// An Uruk-hai of either culture; not the [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertEquals(1, scn.GetCultureTokensOn(attack));
		assertEquals(0, scn.GetCultureTokensOn(attack2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Again, with the one Uruk-hai left in hand.
		assertTrue(scn.ShadowActionAvailable(attack));
		scn.ShadowUseCardAction(attack);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(2, scn.GetCultureTokensOn(attack));
		assertEquals(0, scn.GetCultureTokensOn(attack2));

		// The Orc can be played with what is left (2 + 2 for roaming), but he is no Uruk-hai.
		assertTrue(scn.ShadowPlayAvailable(forger));
		assertFalse(scn.ShadowActionAvailable(attack));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheUrukHaiInHandCannotBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(attack);
		scn.MoveCardsToHand(guard, forger);
		assertInHand(guard, forger);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 for Frodo + 2 for Ettenmoors: the Guard wants 3 + 2 for roaming, the Orc 2 + 2.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(forger));
		assertFalse(scn.ShadowActionAvailable(attack));

		// One twilight more, and the Guard can be played: the ability is offered.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(attack));
		assertEquals(0, scn.GetCultureTokensOn(attack));
	}

	@Test
	public void RegroupAbilityRemovesThreeTokensFromThisCardAndDiscardsAnUrukHaiToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var attack2 = scn.GetShadowCard("attack2");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(attack, attack2);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.AddTokensToCard(attack, 3);
		scn.AddTokensToCard(attack2, 2);
		scn.MoveMinionsToTable(savage, guard, forger);
		assertEquals(3, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetCultureTokensOn(attack2));

		// The fellowship moves to site 3; both markers have passed site 1.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		// This copy has 3 tokens; the other copy has 2, and the 3 are not made up from both.
		assertTrue(scn.ShadowActionAvailable(attack));
		assertFalse(scn.ShadowActionAvailable(attack2));

		scn.ShadowUseCardAction(attack);
		// An Uruk-hai of either culture; not the [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		assertInDiscard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, guard, forger);
		assertEquals(0, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetCultureTokensOn(attack2));
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoUrukHaiToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(attack);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.AddTokensToCard(attack, 3);
		scn.MoveMinionsToTable(forger);
		assertEquals(3, scn.GetCultureTokensOn(attack));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Three tokens here, and a site that could be taken; the only minion is an Orc.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertFalse(scn.ShadowActionAvailable(attack));
		assertEquals(3, scn.GetCultureTokensOn(attack));
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void RegroupAbilityWithNoSiteThatCanBeControlledIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(attack);
		scn.AddTokensToCard(attack, 3);

		scn.StartGame();
		var site1 = scn.GetSite(1);
		assertEquals(3, scn.GetCultureTokensOn(attack));
		scn.MoveMinionsToTable(savage);

		// The first turn: the Shadow player's marker is still on site 1, so no site can be controlled.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(attack));
		scn.ShadowUseCardAction(attack);

		// Paid in full (the one Uruk-hai is chosen unasked), and nothing is taken.
		assertEquals(0, scn.GetCultureTokensOn(attack));
		assertInDiscard(savage);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
