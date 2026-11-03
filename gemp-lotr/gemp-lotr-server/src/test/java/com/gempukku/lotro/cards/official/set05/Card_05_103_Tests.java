package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "5_103");
					put("captain2", "5_103");     // a second copy: not unique, and its limit is its own
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, cost 2; his text answers only a weapon played on him
					put("fear", "1_278");         // Strength Born of Fear: [sauron] support-area condition; a skirmish ability used only on purpose
					put("evil", "1_283");         // You Bring Great Evil: [sauron] condition borne by a companion; no heal at a sanctuary
					put("armory", "1_173");       // Goblin Armory: [moria] support-area condition; answers only [moria] weapons and wounds
					put("scimitar", "1_269");     // Orc Scimitar: [sauron] possession, a hand weapon on a [sauron] Orc; a skirmish ability
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion to bear You Bring Great Evil
					// Strength Born of Fear again: the cards of the Shadow draw deck
					put("deck1", "1_278");
					put("deck2", "1_278");
					put("deck3", "1_278");
					put("deck4", "1_278");
					put("deck5", "1_278");
					put("deck6", "1_278");
					put("deck7", "1_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Captain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: When you play this minion, you may spot a [sauron] condition to draw a card for each site you control (limit 3).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("captain");

		assertEquals("Orc Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedSpottingASauronConditionDrawsACardForEachSiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(fear);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetShadowHand(captain);
		scn.SetShadowDrawDeck(scn.GetShadowCard("deck1"), scn.GetShadowCard("deck2"), scn.GetShadowCard("deck3"));
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(captain);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(captain));
		scn.ShadowAcceptOptionalTrigger();

		// Two sites: two cards.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(fear);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetShadowHand(captain);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();

		scn.ShadowPlayCard(captain);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(captain));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronConditionBorneByACompanionIsSpottedAndOnlyYourOwnSitesAreCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var evil = scn.GetShadowCard("evil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AttachCardsTo(aragorn, evil);
		// The Free Peoples player took site 1 on a turn of his opponent's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetShadowHand(captain);
		scn.SetShadowDrawDeck(scn.GetShadowCard("deck1"), scn.GetShadowCard("deck2"), scn.GetShadowCard("deck3"));
		scn.SetTwilight(10);
		assertAttachedTo(evil, aragorn);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		scn.FreepsPass();

		scn.ShadowPlayCard(captain);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(captain));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithAConditionOfAnotherCultureAndASauronPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var chieftain = scn.GetShadowCard("chieftain");
		var scimitar = scn.GetShadowCard("scimitar");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(armory);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetShadowHand(captain);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, armory);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();
		scn.MoveMinionsToTable(chieftain);
		scn.AttachCardsTo(chieftain, scimitar);
		scn.ShadowRefreshDecision();

		assertAttachedTo(scimitar, chieftain);
		scn.ShadowPlayCard(captain);

		assertInZone(Zone.SHADOW_CHARACTERS, captain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(captain));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenYouControlNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(fear);
		// Only the other player controls a site.
		scn.FreepsTakeControlOfSite();
		scn.SetShadowHand(captain);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		scn.FreepsPass();

		scn.ShadowPlayCard(captain);

		assertInZone(Zone.SHADOW_CHARACTERS, captain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(captain));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DrawsNoMoreThanThreeAndASecondCopyDrawsItsOwnThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var captain2 = scn.GetShadowCard("captain2");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(fear);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetShadowHand(captain, captain2);
		scn.SetShadowDrawDeck(scn.GetShadowCard("deck1"), scn.GetShadowCard("deck2"), scn.GetShadowCard("deck3"),
				scn.GetShadowCard("deck4"), scn.GetShadowCard("deck5"), scn.GetShadowCard("deck6"), scn.GetShadowCard("deck7"));
		scn.SetTwilight(20);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(4, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowPlayCard(captain);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(captain));
		scn.ShadowAcceptOptionalTrigger();

		// Four sites, limit 3: the other Captain and 3 cards.
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDrawDeck().size());

		scn.ShadowPlayCard(captain2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(captain2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDrawDeck().size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherSauronOrcPlayedSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var chieftain = scn.GetShadowCard("chieftain");
		var fear = scn.GetShadowCard("fear");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(fear);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.SetShadowHand(chieftain);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();
		scn.MoveMinionsToTable(captain);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, captain);
		scn.ShadowPlayCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(captain));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
