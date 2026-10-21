package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "10_60");

					// Gorbag, Covetous Captain: a [wraith] Orc, strength 11.  "For each wound on the Ring-bearer, each
					// [wraith] Orc is strength +1": no companion is wounded here.
					put("gorbag", "10_59");
					// Gorbag, Lieutenant of Cirith Ungol: the other Gorbag who can bear the Sword ([wraith] Orc).  Strength
					// +2 and damage +1 while a Nazgul can be spotted: none is laid with him.  Never in play beside 10_59.
					put("gorbag7", "7_180");
					// Cirith Ungol Warrior: a [wraith] Orc who is no Gorbag, strength 9.  Its Shadow ability (spot a
					// possession to be fierce) is never used.
					put("warrior", "10_56");
					// Ulaire Attea, The Easterling: a [wraith] minion who is no Orc.  Cheated into play, so his "when
					// you play" is never set off.
					put("attea", "2_82");
					// Goblin Patrol Troop: an Orc who is not [wraith] ([moria]), no text.
					put("troop", "1_177");
					// Morgul Axe: a [wraith] possession, hand weapon, cost 1, "Bearer must a [wraith] Orc".  Damage +1
					// while 2 wounds can be spotted on the Ring-bearer, never so here.  No trigger on its play.
					put("axe", "7_186");

					// Aragorn, Ranger of the North, bearing Ranger's Sword (a Free Peoples possession).
					put("aragorn", "1_89");
					put("rangersword", "1_112");
					// Old Toby: a Free Peoples possession in a support area, borne by nobody.  Its draw on its own play
					// is not set off by the cheat.
					put("toby", "1_305");
					// Gimli, Son of Gloin, bearing Ring of Accretion: a Free Peoples ARTIFACT, no possession.  Its
					// maneuver ability is never used.
					put("gimli", "1_13");
					put("ring", "9_6");
					// Lord of Moria: a Free Peoples condition in a support area, no possession.
					put("lord", "1_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorbagsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Gorbag's Sword
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [wraith] Orc.
		 * 	When you play this possession, you may discard a possession. Its owner may place it on top of his or her draw deck instead.
		 * 	If bearer is Gorbag, he is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Gorbag's Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnlyOnAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var gorbag = scn.GetShadowCard("gorbag");
		var warrior = scn.GetShadowCard("warrior");
		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, warrior, attea, troop);
		scn.SetTwilight(5);
		assertInHand(sword);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);

		assertTrue(scn.ShadowHasCardChoiceAvailable(gorbag, warrior));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attea, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warrior);

		assertAttachedTo(sword, warrior);
		// The Sword's own "when you play" is offered (it can discard itself) and declined.
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(sword, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedItMayDiscardAnyPossessionAndAFreePeoplesOwnerMayPutItOnTopOfHisDrawDeckInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		var toby = scn.GetFreepsCard("toby");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.AttachCardsTo(gimli, ring);
		scn.MoveCardsToSupportArea(toby, lord);

		var sword = scn.GetShadowCard("sword");
		var gorbag = scn.GetShadowCard("gorbag");
		var warrior = scn.GetShadowCard("warrior");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, warrior);
		scn.AttachCardsTo(warrior, axe);
		scn.SetTwilight(5);
		assertInHand(sword);
		assertAttachedTo(rangersword, aragorn);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(axe, warrior);
		assertInZone(Zone.SUPPORT, toby, lord);
		scn.FreepsPass();

		// The Warrior already bears a hand weapon: the Sword goes on Gorbag unasked.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, gorbag);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sword));
		scn.ShadowAcceptOptionalTrigger();

		// A possession of either side, borne or in a support area; the Sword itself is a possession too.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rangersword, toby, axe, sword));
		// An artifact and a condition are no possessions.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, lord));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rangersword);

		// Ranger's Sword is the Free Peoples player's: he is the one asked.
		assertTrue(scn.FreepsChoiceAvailable("top of deck"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChooseOption("top of deck");

		assertEquals(rangersword, scn.GetFreepsTopOfDeck());
		assertNotAttachedTo(rangersword, aragorn);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertAttachedTo(axe, warrior);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(sword, gorbag);
		assertInZone(Zone.SUPPORT, toby, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesOwnerWhoDoesNotPutItOnHisDeckHasItDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		var sword = scn.GetShadowCard("sword");
		var gorbag = scn.GetShadowCard("gorbag");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag);
		scn.SetTwilight(5);
		assertInHand(sword);
		assertAttachedTo(rangersword, aragorn);
		scn.FreepsPass();

		// Gorbag is the only bearer: the Sword goes on him unasked.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, gorbag);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(rangersword);

		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseOption("Discard");

		assertInDiscard(rangersword);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertNotEquals(rangersword, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayersOwnPossessionIsHisToPutOnTopOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var gorbag = scn.GetShadowCard("gorbag");
		var warrior = scn.GetShadowCard("warrior");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, warrior);
		scn.AttachCardsTo(warrior, axe);
		scn.SetTwilight(5);
		assertInHand(sword);
		assertAttachedTo(axe, warrior);
		scn.FreepsPass();

		// The Warrior already bears a hand weapon: the Sword goes on Gorbag unasked.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, gorbag);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(axe);

		// The Axe is the Shadow player's own: he is asked, and the Free Peoples player is not.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowChoiceAvailable("top of deck"));
		scn.ShadowChooseOption("top of deck");

		assertEquals(axe, scn.GetShadowTopOfDeck());
		assertNotAttachedTo(axe, warrior);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferDiscardsNothingAndAnotherPossessionPlayedAfterItMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		var sword = scn.GetShadowCard("sword");
		var gorbag = scn.GetShadowCard("gorbag");
		var warrior = scn.GetShadowCard("warrior");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(sword, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, warrior);
		scn.SetTwilight(5);
		assertInHand(sword, axe);
		assertAttachedTo(rangersword, aragorn);
		scn.FreepsPass();

		scn.ShadowPlayCard(sword);
		scn.ShadowChooseCard(gorbag);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sword));
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(rangersword, aragorn);
		assertAttachedTo(sword, gorbag);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [wraith] possession played on a [wraith] Orc, with Ranger's Sword still there to discard.  Gorbag
		// already bears a hand weapon, so the Axe goes on the Warrior unasked.
		scn.ShadowPlayCard(axe);
		assertAttachedTo(axe, warrior);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertAttachedTo(rangersword, aragorn);
	}

	@Test
	public void GorbagBearingItIsFierceAndAnotherBearerIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var gorbag = scn.GetShadowCard("gorbag");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag, warrior);
		scn.AttachCardsTo(warrior, sword);

		// Borne by the Warrior: neither the bearer, who is no Gorbag, nor Gorbag, who does not bear it, is fierce.
		assertAttachedTo(sword, warrior);
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(gorbag, Keyword.FIERCE));

		scn.AttachCardsTo(gorbag, sword);

		assertAttachedTo(sword, gorbag);
		assertTrue(scn.HasKeyword(gorbag, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
	}

	@Test
	public void TheOtherGorbagBearingItIsFierceToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var gorbag7 = scn.GetShadowCard("gorbag7");

		scn.StartGame();
		scn.MoveMinionsToTable(gorbag7);

		assertFalse(scn.HasKeyword(gorbag7, Keyword.FIERCE));
		scn.AttachCardsTo(gorbag7, sword);

		assertAttachedTo(sword, gorbag7);
		assertTrue(scn.HasKeyword(gorbag7, Keyword.FIERCE));
	}
}
