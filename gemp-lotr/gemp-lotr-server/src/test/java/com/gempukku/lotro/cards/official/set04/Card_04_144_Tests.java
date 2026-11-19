package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burning", "4_144");
					// Uruk Savage: an [isengard] Uruk-hai of site 5, cost 2; Damage +1 is its whole text.
					put("savage", "1_151");
					// Cirith Ungol Guard: a [sauron] Uruk-hai of site 6, cost 3; Damage +1 is its whole text.
					put("guard", "10_81");
					// Isengard Forger: an [isengard] Orc of site 4, cost 2; a Shadow ability never used.
					put("forger", "3_56");
					// Lord of Moria: a [dwarven] condition in the support area; each Dwarf is damage +1.
					put("lord", "1_21");
					// There and Back Again: a [shire] tale condition borne by a Hobbit companion; a maneuver
					// ability never used.
					put("tale", "1_317");
					// Hobbit Sword: a Free Peoples possession that a Hobbit bears, with no text.
					put("sword", "1_299");
					// Sauron's Defenses: a [sauron] condition in the Shadow support area; it does something only
					// with 3 companions in the dead pile.
					put("defenses", "1_274");
					// The Weight of a Legacy: a [sauron] condition borne by Aragorn; strength -1.
					put("weight", "1_282");
					// Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: a Hobbit companion; a fellowship ability never used.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BurningofWestfoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Burning of Westfold
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Shadow:</b> If you control a site, play an Uruk-hai to discard a Free Peoples condition.
		*/

		var scn = GetScenario();

		var burning = scn.GetFreepsCard("burning");

		assertEquals("Burning of Westfold", burning.getBlueprint().getTitle());
		assertNull(burning.getBlueprint().getSubtitle());
		assertFalse(burning.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, burning.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, burning.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, burning.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(burning, Keyword.SUPPORT_AREA));
		assertEquals(0, burning.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileControllingASiteShadowAbilityPlaysAnUrukHaiToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burning = scn.GetShadowCard("burning");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		var defenses = scn.GetShadowCard("defenses");
		var weight = scn.GetShadowCard("weight");
		var lord = scn.GetFreepsCard("lord");
		var tale = scn.GetFreepsCard("tale");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is behind both markers: the Shadow player takes it.
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(burning, defenses, lord);
		scn.AttachCardsTo(sam, tale, sword);
		scn.AttachCardsTo(aragorn, weight);
		scn.MoveCardsToHand(savage, guard, forger);
		assertInZone(Zone.SUPPORT, burning, defenses, lord);
		assertAttachedTo(tale, sam);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInZone(Zone.HAND, savage, guard, forger);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowActionAvailable(burning));
		scn.ShadowUseCardAction(burning);

		// Uruk-hai of two cultures in hand; not an [isengard] Orc, though it could be paid for.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		scn.ShadowChooseCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.HAND, guard, forger);

		// Free Peoples conditions of two cultures, in the support area and borne; not a Free Peoples possession,
		// nor a Shadow condition in the support area or borne by a companion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, tale));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, defenses, weight));
		scn.ShadowChooseCard(lord);

		assertInZone(Zone.DISCARD, lord);
		assertAttachedTo(tale, sam);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInZone(Zone.SUPPORT, defenses, burning);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burning = scn.GetShadowCard("burning");
		var savage = scn.GetShadowCard("savage");
		var lord = scn.GetFreepsCard("lord");

		scn.StartGame();
		scn.SkipToSite(2);
		// Player 1 took site 1 on player 2's turn: a site is controlled, but not by the condition's owner.
		scn.FreepsTakeControlOfSite();
		scn.MoveCardsToSupportArea(burning, lord);
		scn.MoveCardsToHand(savage);
		assertInZone(Zone.SUPPORT, burning, lord);
		assertInZone(Zone.HAND, savage);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(burning));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileNoUrukHaiInHandCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burning = scn.GetShadowCard("burning");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		var lord = scn.GetFreepsCard("lord");
		// The Savage, whom the pool could pay for, is kept out of the hands the turns ahead deal.
		scn.MoveCardsToDiscard(savage);
		assertInZone(Zone.DISCARD, savage);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(burning, lord);
		scn.MoveCardsToHand(guard, forger);
		assertInZone(Zone.SUPPORT, burning, lord);
		assertInZone(Zone.HAND, guard, forger);
		scn.FreepsPass();
		// At site 3 the Guard costs 3 + 2 roaming = 5, the Forger 2 + 2 roaming = 4.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(forger));
		assertFalse(scn.ShadowActionAvailable(burning));

		// One more twilight and the Guard can be played: the ability is offered.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(burning));
	}

	@Test
	public void WithNoFreePeoplesConditionInPlayTheAbilityPlaysTheUrukHaiForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burning = scn.GetShadowCard("burning");
		var savage = scn.GetShadowCard("savage");
		var defenses = scn.GetShadowCard("defenses");
		var weight = scn.GetShadowCard("weight");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(aragorn, sam);
		// The other Uruk-hai, and the Orc, are kept out of the hands the turns ahead deal.
		scn.MoveCardsToDiscard(guard, forger);
		assertInZone(Zone.DISCARD, guard, forger);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(burning, defenses);
		scn.AttachCardsTo(sam, sword);
		scn.AttachCardsTo(aragorn, weight);
		scn.MoveCardsToHand(savage);
		assertInZone(Zone.SUPPORT, burning, defenses);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInZone(Zone.HAND, savage);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(burning));
		scn.ShadowUseCardAction(burning);

		// The one Uruk-hai is played unasked, and there is nothing to discard.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertAttachedTo(sword, sam);
		assertAttachedTo(weight, aragorn);
		assertInZone(Zone.SUPPORT, defenses, burning);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
