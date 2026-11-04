package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander", "13_115");
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion; its start-of-assignment trigger reaches no test here
					put("spear", "12_96");       // Orc Spear: [orc] possession borne by an [orc] minion
					put("blow", "15_115");       // Pummeling Blow: [orc] condition that plays on the fellowship's current site
					put("sbof", "1_278");        // Strength Born of Fear: [sauron] support-area condition, a skirmish ability only
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; to play, exert an [orc] minion
					put("beckoning2", "12_83");
					put("grond", "18_82");       // Grond, Forged With Black Steel: [orc] support-area possession; a regroup-move trigger only
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1; its trigger needs an underground site

					put("aragorn", "1_89");      // [gondor] Men, to bear the borne conditions and possessions
					put("boromir", "1_97");
					put("alliance", "1_49");     // The Last Alliance of Elves and Men: [elven] condition on a [gondor] Man; +1 per Elf, none here
					put("alliance2", "1_49");
					put("armor", "1_92");        // Armor: Free Peoples possession on a Man
					put("armor2", "1_92");
					put("promise", "2_112");     // A Promise: [shire] support-area condition; nothing without Sam
					put("promise12", "12_128");  // A Promise (set 12): another card of the same title
					put("promise3", "2_112");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcRaidCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Orc Raid Commander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] card in your support area.<br>When you play this, spot a Free Peoples condition to discard from play each other card that has the same card title as that condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander");

		assertEquals("Orc Raid Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcConditionIsInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var halftroll = scn.GetShadowCard("halftroll");
		var spear = scn.GetShadowCard("spear");
		var blow = scn.GetShadowCard("blow");
		var sbof = scn.GetShadowCard("sbof");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(sbof);
		scn.MoveCardsToHand(commander, beckoning);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sbof);
		assertInHand(commander, beckoning);
		scn.MoveMinionsToTable(halftroll);
		scn.AttachCardsTo(halftroll, spear);
		assertInZone(Zone.ATTACHED, spear);
		scn.FreepsPass();
		// Shadow phase at Ettenmoors: the Commander is site 4, so roaming.
		scn.SetTwilight(10);
		scn.AttachCardsTo(scn.GetCurrentSite(), blow);
		scn.ShadowRefreshDecision();

		// [orc] cards in play but in no support area (a minion, the possession he bears, a condition on the site),
		// and a [sauron] condition in the support area.
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertInZone(Zone.ATTACHED, spear, blow);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(commander));

		assertEquals(0, scn.GetWoundsOn(halftroll));
		scn.ShadowPlayCard(beckoning);
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.SUPPORT, beckoning);

		assertTrue(scn.ShadowPlayAvailable(commander));
		scn.ShadowPlayCard(commander);
		assertInZone(Zone.SHADOW_CHARACTERS, commander);
		// No Free Peoples condition to spot: nothing more is asked.
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithAnOrcPossessionInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var grond = scn.GetShadowCard("grond");
		scn.MoveCardsToSupportArea(grond);
		scn.MoveCardsToHand(commander);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grond);
		assertInHand(commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(commander));
		scn.ShadowPlayCard(commander);
		assertInZone(Zone.SHADOW_CHARACTERS, commander);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedDiscardsEveryOtherCardTitledAsTheSpottedSupportAreaCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var goblin = scn.GetShadowCard("goblin");
		var beckoning = scn.GetShadowCard("beckoning");
		var beckoning2 = scn.GetShadowCard("beckoning2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var alliance = scn.GetFreepsCard("alliance");
		var alliance2 = scn.GetFreepsCard("alliance2");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var promise = scn.GetFreepsCard("promise");
		var promise12 = scn.GetFreepsCard("promise12");
		var promise3 = scn.GetFreepsCard("promise3");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, alliance, armor);
		scn.AttachCardsTo(boromir, alliance2, armor2);
		scn.MoveCardsToSupportArea(promise, promise12, beckoning, beckoning2);
		scn.MoveCardsToHand(commander, goblin);

		scn.StartGame();
		scn.MoveCardsToHand(promise3);
		assertInHand(promise3, commander, goblin);
		assertInZone(Zone.ATTACHED, alliance, alliance2, armor, armor2);
		assertInZone(Zone.SUPPORT, promise, promise12, beckoning, beckoning2);
		scn.SetTwilight(12);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(commander));
		scn.ShadowPlayCard(commander);

		assertEquals(1, scn.ShadowGetChoiceMin());
		// Free Peoples conditions of two cultures, in the support area and borne.
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, promise12, alliance, alliance2));
		// A Free Peoples possession and a Shadow condition, each with a second copy in play.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor, armor2, beckoning, beckoning2));
		scn.ShadowChooseCard(promise);

		// The other card of the title goes though it is another card (set 12's A Promise); the one spotted stays,
		// and so does the copy in hand ("from play").
		assertInZone(Zone.DISCARD, promise12);
		assertInZone(Zone.SUPPORT, promise, beckoning, beckoning2);
		assertInHand(promise3);
		assertInZone(Zone.ATTACHED, alliance, alliance2, armor, armor2);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] minion played afterwards sets nothing off: the Alliances stay.
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertInZone(Zone.ATTACHED, alliance, alliance2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedDiscardsTheOtherCopyOfASpottedBorneCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander = scn.GetShadowCard("commander");
		var beckoning = scn.GetShadowCard("beckoning");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var alliance = scn.GetFreepsCard("alliance");
		var alliance2 = scn.GetFreepsCard("alliance2");
		var promise = scn.GetFreepsCard("promise");
		var promise12 = scn.GetFreepsCard("promise12");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, alliance);
		scn.AttachCardsTo(boromir, alliance2);
		scn.MoveCardsToSupportArea(promise, promise12, beckoning);
		scn.MoveCardsToHand(commander);

		scn.StartGame();
		assertInHand(commander);
		assertInZone(Zone.ATTACHED, alliance, alliance2);
		assertInZone(Zone.SUPPORT, promise, promise12, beckoning);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(commander);
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, promise12, alliance, alliance2));
		scn.ShadowChooseCard(alliance);

		assertInZone(Zone.DISCARD, alliance2);
		assertInZone(Zone.ATTACHED, alliance);
		assertEquals(aragorn, alliance.getAttachedTo());
		assertInZone(Zone.SUPPORT, promise, promise12);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
