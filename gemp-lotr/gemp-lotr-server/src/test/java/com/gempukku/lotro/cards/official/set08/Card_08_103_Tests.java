package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grond", "8_103");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, cost 2, site 6; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, cost 2, site 6, damage +1
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, damage +1: a minion of another culture
					put("banner", "1_263");     // Orc Banner: [sauron] condition, cost 1, support area; acts only when a skirmish is lost
					put("banner2", "1_263");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, cost 4; a maneuver ability used only on purpose
					put("shield", "1_107");     // Great Shield: [gondor] possession, cost 1, borne by a Man
					put("lorienelf", "1_53");   // Lorien Elf: companion, cost 1, no text but a requirement to play
					put("guard", "1_7");        // Dwarf Guard: companion, cost 1, no text but a requirement to play
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession, cost 1, support area
					put("songs", "1_61");       // Songs of the Blessed Realm: [elven] condition, cost 1, support area
					put("mirror", "1_55");      // The Mirror of Galadriel: [elven] possession, cost 2, support area; its ability needs Galadriel
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, cost 4
					put("treebeard", "4_103");  // Treebeard, Earthborn: ally, cost 4; acts only when an unbound Hobbit is discarded
					put("guardian", "12_50");   // Guardian: [gondor] condition, cost 4, support area; a skirmish ability that needs a [gondor] companion
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, cost 1; a maneuver ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, cost 0, borne by a Dwarf
					put("armor2", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Grond, Hammer of the Underworld
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Engine</b>.<br><b>Shadow:</b> Play a [sauron] minion to add a [sauron] token here.<br><b>Regroup:</b> Remove X [sauron] tokens here to discard a Free Peoples card (except a companion) with a twilight cost of X. Discard a [sauron] minion or this possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grond");

		assertEquals("Grond", card.getBlueprint().getTitle());
		assertEquals("Hammer of the Underworld", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.ENGINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityPlaysASauronMinionFromHandToAddATokenToGrond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		var banner2 = scn.GetShadowCard("banner2");
		scn.MoveCardsToSupportArea(grond, banner2);
		scn.MoveCardsToHand(soldier, sentry, savage, banner);

		scn.StartGame();
		// Enough in the pool to pay for any of the minions
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(0, scn.GetCultureTokensOn(grond));
		assertEquals(0, scn.GetCultureTokensOn(banner2));

		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, banner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(1, scn.GetCultureTokensOn(grond));
		assertEquals(0, scn.GetCultureTokensOn(banner2));
		assertInZone(Zone.HAND, sentry, savage, banner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NeitherAbilityIsOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(soldier);

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(savage);
		scn.AddTokensToCard(grond, 2);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(grond));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		// A [sauron] minion in hand with the twilight to pay for it, and tokens on Grond with a Free Peoples card
		// costing 1 to discard: neither ability is offered outside its own phase.
		assertInZone(Zone.HAND, soldier);
		assertTrue(scn.GetTwilight() >= 4);
		assertEquals(2, scn.GetCultureTokensOn(grond));
		assertInZone(Zone.SUPPORT, toby);
		assertFalse(scn.ShadowActionAvailable(grond));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoSauronMinionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(savage, banner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(savage));
		assertTrue(scn.ShadowPlayAvailable(banner));
		assertFalse(scn.ShadowActionAvailable(grond));
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForTheSauronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Orc Soldier: 2 to play + 2 roaming (site 6 at site 2), one short
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(grond));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grond));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesXTokensToDiscardAFreePeoplesCardCostingXAndThenASauronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(grond, banner);

		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var toby = scn.GetFreepsCard("toby");
		var songs = scn.GetFreepsCard("songs");
		var mirror = scn.GetFreepsCard("mirror");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, lorienelf);
		scn.AttachCardsTo(aragorn, shield);
		scn.MoveCardsToSupportArea(toby, songs, mirror);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, savage);
		scn.AddTokensToCard(grond, 2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(2, scn.GetCultureTokensOn(grond));
		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		// X is any number from none to every token on Grond
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		// Each Free Peoples card costing 1 that is not a companion, wherever it lies
		assertTrue(scn.ShadowHasCardChoiceAvailable(toby, songs, shield));
		// A companion costing 1, a Shadow card costing 1, and Free Peoples cards costing 2 and 4
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lorienelf, banner, mirror, elrond));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(toby);

		assertEquals(1, scn.GetCultureTokensOn(grond));
		assertInZone(Zone.DISCARD, toby);
		// Then a [sauron] minion, or Grond itself; not an [isengard] minion, nor a [sauron] card that is no minion
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry, grond));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, banner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertInZone(Zone.DISCARD, soldier, toby);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry, savage);
		assertInZone(Zone.SUPPORT, songs, mirror, elrond, banner, grond);
		assertAttachedTo(shield, aragorn);
		assertEquals(1, scn.GetCultureTokensOn(grond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtXOfZeroACardCostingZeroIsDiscardedAndGrondMayGoInPlaceOfAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(grond);

		var thrarin = scn.GetFreepsCard("thrarin");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(thrarin, armor);
		scn.AttachCardsTo(guard, armor2);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AddTokensToCard(grond, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("0");
		// Both suits of Dwarven Armor cost 0; Old Toby costs 1
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, armor2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby));
		scn.ShadowChooseCard(armor);

		assertInZone(Zone.DISCARD, armor);
		assertEquals(1, scn.GetCultureTokensOn(grond));
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, grond));
		scn.ShadowChooseCard(grond);

		assertInZone(Zone.DISCARD, armor, grond);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertAttachedTo(armor2, guard);
		assertInZone(Zone.SUPPORT, toby, thrarin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSauronMinionInPlayGrondIsDiscardedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(grond, banner);

		var toby = scn.GetFreepsCard("toby");
		var songs = scn.GetFreepsCard("songs");
		scn.MoveCardsToSupportArea(toby, songs);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddTokensToCard(grond, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("1");
		assertTrue(scn.ShadowHasCardChoiceAvailable(toby, songs));
		scn.ShadowChooseCard(toby);

		// Only an [isengard] minion and a [sauron] condition beside it: Grond is the one card that can go.
		assertInZone(Zone.DISCARD, toby, grond);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, banner, songs);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsOfferedWithNoFreePeoplesCardToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(grond);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AddTokensToCard(grond, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// The only Free Peoples cards are companions; the cost can still be paid
		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("1");

		assertEquals(0, scn.GetCultureTokensOn(grond));
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, grond));
		scn.ShadowChooseCard(soldier);

		assertInZone(Zone.DISCARD, soldier);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.SUPPORT, grond);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHinderedElrondHasNoTwilightCostAndCannotBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToSupportArea(grond);

		var elrond = scn.GetFreepsCard("elrond");
		var treebeard = scn.GetFreepsCard("treebeard");
		var guard = scn.GetFreepsCard("guard");
		var guardian = scn.GetFreepsCard("guardian");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveAlliesToTable(elrond, treebeard, thrarin);
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, armor);
		scn.AttachCardsTo(thrarin, armor2);
		scn.MoveCardsToSupportArea(guardian);

		scn.StartGame();
		scn.HinderCard(elrond);
		scn.MoveMinionsToTable(soldier, sentry);
		scn.AddTokensToCard(grond, 4);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(elrond));
		scn.ShadowUseCardAction(grond);
		scn.ShadowChoose("4");
		// Treebeard and Guardian cost 4; Elrond, hindered, costs nothing at all; the armor costs 0 and Thrarin 1
		assertTrue(scn.ShadowHasCardChoiceAvailable(treebeard, guardian));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond, armor, armor2, thrarin));
		scn.ShadowChooseCard(treebeard);
		scn.ShadowChooseCard(soldier);

		assertInZone(Zone.DISCARD, treebeard, soldier);
		assertEquals(0, scn.GetCultureTokensOn(grond));
		assertInZone(Zone.SUPPORT, elrond, guardian, grond);

		// Nor is he a card with a twilight cost of 0
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(grond));
		scn.ShadowUseCardAction(grond);
		// With no token left, X can only be 0 and is not asked
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, armor2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(elrond));
		scn.ShadowChooseCard(armor);
		scn.ShadowChooseCard(sentry);

		assertInZone(Zone.DISCARD, armor, sentry);
		assertInZone(Zone.SUPPORT, elrond, grond);
		assertTrue(scn.IsHindered(elrond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
