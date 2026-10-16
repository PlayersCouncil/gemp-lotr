package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_074_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("raider", "53_74");
					put("raider2", "53_74");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 is its whole text
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support condition, archery total -1 only
					put("runner", "1_178");     // Goblin Runner: [moria] minion, so not an [isengard] card

					put("rosie", "1_309");      // Rosie Cotton: [shire] ally, vitality 2, "Fellowship: Exert Rosie Cotton to heal Sam."
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] ally, vitality 4, "Fellowship: Exert Elrond to draw a card."
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, "Fellowship: Exert Sam to remove a burden."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRaiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Uruk Raider
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: Tracker.
		 * 	While an ally is in the dead pile, this minion is strength +3 and <b>damage +1</b>.
		 * 	Each time an ally exerts, you may spot another [isengard] card and exert this minion to wound that ally.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("raider");

		assertEquals("Uruk Raider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertFalse(scn.HasKeyword(card, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void UrukRaiderIsStrengthPlus3AndDamagePlus1OnlyWhileAnAllyIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var elrond = scn.GetFreepsCard("elrond");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(rosie);

		var raider = scn.GetShadowCard("raider");
		var raider2 = scn.GetShadowCard("raider2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(raider, raider2, savage);
		// A companion in the dead pile and an ally in the discard pile: neither is an ally in the dead pile.
		scn.MoveCardsToDeadPile(sam);
		scn.MoveCardsToDiscard(elrond);

		assertInZone(Zone.DEAD, sam);
		assertInZone(Zone.DISCARD, elrond);
		assertEquals(6, scn.GetStrength(raider));
		assertEquals(0, scn.GetKeywordCount(raider, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(raider2));

		scn.MoveCardsToDeadPile(rosie);

		assertInZone(Zone.DEAD, rosie);
		assertEquals(9, scn.GetStrength(raider));
		assertEquals(1, scn.GetKeywordCount(raider, Keyword.DAMAGE));
		// Each copy gets its own bonus once: two copies do not give one of them +6.
		assertEquals(9, scn.GetStrength(raider2));
		assertEquals(1, scn.GetKeywordCount(raider2, Keyword.DAMAGE));
		// Another Uruk-hai is not "this minion".
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyInTheShadowPlayersOwnDeadPileDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The Shadow player's own ally, killed on one of his own turns.
		var shadowRosie = scn.GetShadowCard("rosie");
		var raider = scn.GetShadowCard("raider");

		scn.StartGame();
		scn.MoveMinionsToTable(raider);

		assertEquals(6, scn.GetStrength(raider));

		scn.MoveCardsToDeadPile(shadowRosie);

		assertInZone(Zone.DEAD, shadowRosie);
		assertEquals(6, scn.GetStrength(raider));
		assertEquals(0, scn.GetKeywordCount(raider, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAnAllyExertsShadowMayExertRaiderToWoundThatAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var rosie = scn.GetFreepsCard("rosie");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(elrond, rosie);
		scn.MoveCompanionsToTable(sam);

		var raider = scn.GetShadowCard("raider");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.MoveMinionsToTable(raider);
		scn.MoveCardsToSupportArea(armory);
		scn.FreepsRefreshDecision();
		scn.AddBurdens(1);

		// A companion exerting does not set it off.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(raider));

		// Declined: nothing more happens.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(raider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Each time: the same ally exerting again is answered again.
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(raider));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(raider));
		assertEquals(3, scn.GetWoundsOn(elrond));
		// Only the ally that exerted is wounded.
		assertEquals(0, scn.GetWoundsOn(rosie));
		assertEquals(1, scn.GetWoundsOn(sam));
		// The wound is not an exertion, so nothing further is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WoundingAnAllyToDeathSwitchesOnTheDeadPileBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(rosie, elrond);

		var raider = scn.GetShadowCard("raider");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.MoveMinionsToTable(raider);
		scn.MoveCardsToSupportArea(armory);
		scn.FreepsRefreshDecision();

		assertEquals(6, scn.GetStrength(raider));
		assertEquals(0, scn.GetKeywordCount(raider, Keyword.DAMAGE));

		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(raider));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(raider));
		// Vitality 2: the exertion and the wound kill her.
		assertInZone(Zone.DEAD, rosie);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(9, scn.GetStrength(raider));
		assertEquals(1, scn.GetKeywordCount(raider, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAnotherIsengardCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		var raider = scn.GetShadowCard("raider");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// The Raider itself is the only [isengard] card; the other minion is [moria].
		scn.MoveMinionsToTable(raider, runner);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertInZone(Zone.SUPPORT, rosie);
		assertEquals(0, scn.GetWoundsOn(raider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenRaiderCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCardsToSupportArea(rosie);

		var raider = scn.GetShadowCard("raider");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.MoveMinionsToTable(raider);
		scn.MoveCardsToSupportArea(armory);
		scn.FreepsRefreshDecision();
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(raider, 1);

		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertInZone(Zone.SUPPORT, rosie);
		assertEquals(1, scn.GetWoundsOn(raider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoRaidersEachOfferAndOnlyTheChosenOneExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(elrond);

		var raider = scn.GetShadowCard("raider");
		var raider2 = scn.GetShadowCard("raider2");

		scn.StartGame();
		// No other [isengard] card: each Raider spots the other.
		scn.MoveMinionsToTable(raider, raider2);
		scn.FreepsRefreshDecision();

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(raider));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(raider2));

		scn.ShadowChooseOptionalTrigger(raider2);
		assertEquals(1, scn.GetWoundsOn(raider2));
		assertEquals(0, scn.GetWoundsOn(raider));
		assertEquals(2, scn.GetWoundsOn(elrond));

		// The other copy's own offer is still there.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(raider));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(raider));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
