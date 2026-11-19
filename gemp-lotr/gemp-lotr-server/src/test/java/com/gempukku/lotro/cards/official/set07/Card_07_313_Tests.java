package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_313_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("art", "7_313");
					put("art2", "7_313");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc of another title, cost 2; a skirmish ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is an Uruk-hai; Damage +1 is its whole text
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; stronger and fierce only at 2 wounds on the Ring-bearer

					// [sauron] conditions to play: Sauron's Hatred (support area, cost 0; to play, spot a [sauron] Orc;
					// its abilities are used only on purpose) and You Bring Great Evil (cost 1, plays on a companion who
					// is no Elf; to play, spot a [sauron] Orc; it only stops healing at a sanctuary).
					put("hatred", "7_310");
					put("hatred2", "7_310");
					put("evil", "1_283");
					// A Shadow Fell Over Them: a [wraith] support-area condition, cost 1, with no requirement to play;
					// its abilities are used only on purpose.
					put("fell", "15_182");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose

					// Five cards for the Free Peoples hand, so that he would have initiative: four Goblin Patrol Troops
					// (no text; four copies, a deck's most of one title) and a Dwarf Guard, inert in hand.
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
					put("troop5", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SomeSecretArtofFlameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Some Secret Art of Flame
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [sauron] Orc.
		 * <b>Regroup:</b> Play a [sauron] condition to place a [sauron] token on this card.
		 * While there are 3 tokens on this card, the Shadow has initiative, regardless of the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");

		assertEquals("Some Secret Art of Flame", art.getBlueprint().getTitle());
		assertNull(art.getBlueprint().getSubtitle());
		assertFalse(art.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, art.getBlueprint().getSide());
		assertEquals(Culture.SAURON, art.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, art.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(art, Keyword.SUPPORT_AREA));
		assertEquals(1, art.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaWithASauronOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetShadowCard("art");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(art);
		assertInHand(art);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(art));
		scn.ShadowPlayCard(art);
		assertInZone(Zone.SUPPORT, art);
		assertEquals(0, scn.GetCultureTokensOn(art));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetShadowCard("art");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(art, soldier);
		assertInHand(art, soldier);

		scn.StartGame();
		// A [wraith] Orc and a [sauron] Uruk-hai: an Orc of another culture and a [sauron] minion who is no Orc.
		scn.MoveMinionsToTable(hound, sentry);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(art));

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.ShadowPlayAvailable(art));

		scn.ShadowPlayCard(art);
		assertInZone(Zone.SUPPORT, art);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionPlaysASauronConditionToPlaceATokenOnThatCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var art = scn.GetShadowCard("art");
		var art2 = scn.GetShadowCard("art2");
		var chieftain = scn.GetShadowCard("chieftain");
		var hatred = scn.GetShadowCard("hatred");
		var evil = scn.GetShadowCard("evil");
		var soldier = scn.GetShadowCard("soldier");
		var fell = scn.GetShadowCard("fell");
		scn.MoveCardsToSupportArea(art, art2);
		scn.MoveCardsToHand(hatred, evil, soldier, fell);
		assertEquals(0, scn.GetCultureTokensOn(art));
		assertEquals(0, scn.GetCultureTokensOn(art2));
		assertInHand(hatred, evil, soldier, fell);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		// Enough to pay for any card in the hand.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(art));
		scn.ShadowUseCardAction(art);
		// A [sauron] condition of either kind; not a [sauron] minion, nor a condition of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hatred, evil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, fell));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hatred);

		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(evil, soldier, fell);
		// The token goes on the copy used, and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(art));
		assertEquals(0, scn.GetCultureTokensOn(art2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoSauronConditionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var art = scn.GetShadowCard("art");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var fell = scn.GetShadowCard("fell");
		scn.MoveCardsToSupportArea(art);
		scn.MoveCardsToHand(soldier, fell);
		assertInZone(Zone.SUPPORT, art);
		assertInHand(soldier, fell);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Only a [sauron] minion and a [wraith] condition in hand.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(art));
		assertInHand(soldier, fell);
		assertEquals(0, scn.GetCultureTokensOn(art));
	}

	@Test
	public void RegroupActionIsNotOfferedUntilTheSauronConditionInHandCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var art = scn.GetShadowCard("art");
		var chieftain = scn.GetShadowCard("chieftain");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToSupportArea(art);
		scn.MoveCardsToHand(evil);
		assertInZone(Zone.SUPPORT, art);
		assertInHand(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// You Bring Great Evil costs 1: with nothing in the pool it cannot be played.
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(art));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(art));
		assertInHand(evil);
		assertEquals(0, scn.GetCultureTokensOn(art));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheThirdTokenPlacedGivesTheShadowInitiativeWhateverTheFreePeoplesHandAndAFourthKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"));

		var art = scn.GetShadowCard("art");
		var chieftain = scn.GetShadowCard("chieftain");
		var hatred = scn.GetShadowCard("hatred");
		var hatred2 = scn.GetShadowCard("hatred2");
		scn.MoveCardsToSupportArea(art);
		scn.MoveCardsToHand(hatred, hatred2);
		assertInHand(hatred, hatred2);

		scn.StartGame();
		scn.AddTokensToCard(art, 2);
		scn.MoveMinionsToTable(chieftain);
		assertEquals(2, scn.GetCultureTokensOn(art));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Two tokens: the five cards in hand give the Free Peoples player initiative.
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());

		scn.ShadowUseCardAction(art);
		scn.ShadowChooseCard(hatred);
		assertInZone(Zone.SUPPORT, hatred);
		assertEquals(3, scn.GetCultureTokensOn(art));
		assertEquals(5, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());

		// A fourth token: still at least three, and still the Shadow's initiative.
		scn.FreepsPass();
		scn.ShadowUseCardAction(art);
		assertInZone(Zone.SUPPORT, hatred2);
		assertEquals(4, scn.GetCultureTokensOn(art));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoTokensOnEachOfTwoCopiesDoNotGiveTheShadowInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCardsToHand(scn.GetFreepsCard("troop1"), scn.GetFreepsCard("troop2"), scn.GetFreepsCard("troop3"),
				scn.GetFreepsCard("troop4"), scn.GetFreepsCard("troop5"));

		var art = scn.GetShadowCard("art");
		var art2 = scn.GetShadowCard("art2");
		scn.MoveCardsToSupportArea(art, art2);

		scn.StartGame();
		scn.AddTokensToCard(art, 2);
		scn.AddTokensToCard(art2, 2);
		scn.FreepsRefreshDecision();

		// Four [sauron] tokens in all, but three on neither card.
		assertEquals(2, scn.GetCultureTokensOn(art));
		assertEquals(2, scn.GetCultureTokensOn(art2));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		scn.AddTokensToCard(art2, 1);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetCultureTokensOn(art2));
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
