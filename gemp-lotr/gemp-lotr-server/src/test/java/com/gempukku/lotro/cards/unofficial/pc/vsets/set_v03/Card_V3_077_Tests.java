package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "103_77");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: Nazgul, cost 4, vitality 2, site 3; his text is about companions bearing [wraith] conditions
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: Nazgul, cost 5, vitality 3, site 2; nothing else below 2 burdens
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, vitality 3, site 4, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, vitality 1; adds (2) when played
					put("squealer", "8_77");    // Morgul Squealer: "Shadow: Discard this minion to play a [wraith] minion from your discard pile."

					put("steed", "1_208");      // Black Steed: possession, mount, cost 1, bearer must be a Nazgul
					put("sword", "1_218");      // Nazgul Sword: possession, hand weapon, cost 1, bearer must be a Nazgul
					put("blade", "12_168");     // Nazgul Blade: possession, hand weapon, cost 1, bearer must be a Nazgul
					put("dolguldur", "6_85");   // Sword of Dol Guldur: possession, bearer must be a Nazgul; only ever left in the draw deck
					put("spear", "1_182");      // Goblin Spear: possession, hand weapon, cost 1, bearer must be a [moria] minion
					put("axe", "1_190");        // Moria Axe: possession, hand weapon, cost 1, bearer must be a [moria] Orc

					put("rancor", "9_44");      // Ring of Rancor: artifact, cost 0, bearer must be a Nazgul
					put("savagery", "17_142");  // Ring of Savagery: artifact, cost 0, bearer must be a Nazgul
					put("ire", "9_43");         // Ring of Ire: artifact, bearer must be The Witch-king
					put("crown", "101_39");     // Pale Crown: artifact that plays to the support area

					put("destroyed", "1_220");  // Not Easily Destroyed: [wraith] condition, cost 1, plays to the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Úlairë Nertëa, Sanctified for Cruelty
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Each time you play a minion, you may exert it to play a possession on it from your discard pile
		 * 	(or an artifact if you also exert this minion).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Sanctified for Cruelty", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedForFiveAndTwoMoreWhileRoamingAndIsNotAnsweredWithNoPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var destroyed = scn.GetShadowCard("destroyed");
		var blade = scn.GetShadowCard("blade");
		scn.MoveCardsToHand(nertea, blade);
		// A Shadow card in the discard pile that is no possession, and a possession that is in hand.
		scn.MoveCardsToDiscard(destroyed);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(nertea));

		scn.ShadowPlayCard(nertea);

		// 13 - 5 to play - 2 roaming: his site number is 3 and the fellowship is at site 2
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, destroyed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMinionPlayedMayExertToHaveAPossessionPlayedOnItFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		// Two possessions a Nazgul can bear and two a [moria] Orc can.
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		var spear = scn.GetShadowCard("spear");
		var axe = scn.GetShadowCard("axe");
		// A Shadow condition in the discard pile; possessions a Nazgul can bear that are in hand, in the draw
		// deck and in the other player's discard pile.
		var destroyed = scn.GetShadowCard("destroyed");
		var blade = scn.GetShadowCard("blade");
		var dolguldur = scn.GetShadowCard("dolguldur");
		var theirBlade = scn.GetFreepsCard("blade");

		scn.MoveCardsToHand(lemenya, troop, blade);
		scn.MoveCardsToDiscard(steed, sword, spear, axe, destroyed);
		scn.MoveCardsToDiscard(theirBlade);

		scn.StartGame();
		// Another Nazgul in play, who could bear whatever Lemenya can.
		scn.MoveMinionsToTable(nertea, nelya);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(23, scn.GetTwilight());
		assertInZone(Zone.DECK, dolguldur);

		scn.ShadowPlayCard(lemenya);

		// 23 - 4 to play - 2 roaming
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowChooseOptionalTrigger(nertea);

		// The minion that was played exerts: not Nertea, and not the Nazgul who was there already.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, axe, destroyed, blade, dolguldur, theirBlade));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(steed);

		assertAttachedTo(steed, lemenya);
		assertInZone(Zone.DISCARD, sword, spear, axe, destroyed);
		// 17 - 1 for the Black Steed
		assertEquals(16, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second minion, of another culture and race.
		scn.ShadowPlayCard(troop);

		// 16 - 6 to play - 2 roaming
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowChooseOptionalTrigger(nertea);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		// The Nazgul Sword is still in the discard pile, and no Orc can bear it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, axe));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, destroyed, blade, dolguldur, theirBlade));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(axe);

		assertAttachedTo(axe, troop);
		assertInZone(Zone.DISCARD, sword, spear, destroyed);
		// 8 - 1 for the Moria Axe
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheMinionUnexertedAndThePossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);

		// 10 set + 1 for Frodo + 2 for the site, - 4 to play - 2 roaming
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertInZone(Zone.DISCARD, steed, sword);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheMinionPlayedCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToHand(runner, troop);
		scn.MoveCardsToDiscard(spear, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 1 for Frodo + 2 for the site
		assertEquals(23, scn.GetTwilight());

		// A Goblin Runner has a vitality of 1.
		scn.ShadowPlayCard(runner);

		// 23 - 1 to play - 2 roaming + 2 the Runner adds
		assertEquals(22, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.DISCARD, spear, axe);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [moria] Orc who can exert, on the same board.
		scn.ShadowPlayCard(troop);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
	}

	@Test
	public void StillOfferedWithOneLeftInThePoolForAPossessionThatCostsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(nelya);
		// Each costs 1.
		scn.MoveCardsToDiscard(steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());

		// Nelya's site number is 2, so he is not roaming: 6 - 5 leaves 1.
		scn.ShadowPlayCard(nelya);

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));

		scn.ShadowChooseOptionalTrigger(nertea);
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, sword));
		scn.ShadowChooseCard(sword);

		// The possession is paid for like any other card played.
		assertAttachedTo(sword, nelya);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolIsEmptyOnceTheMinionIsPaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(lemenya, nelya);
		// Each costs 1.
		scn.MoveCardsToDiscard(steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the site
		assertEquals(11, scn.GetTwilight());

		scn.ShadowPlayCard(lemenya);

		// 11 - 4 to play - 2 roaming
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));

		scn.ShadowDeclineOptionalTrigger();

		// Nelya is not roaming at site 2: 5 - 5 leaves nothing for a possession that costs 1.
		scn.ShadowPlayCard(nelya);

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInZone(Zone.DISCARD, steed, sword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotAnsweredWhenAPossessionOrAConditionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var blade = scn.GetShadowCard("blade");
		var destroyed = scn.GetShadowCard("destroyed");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(blade, destroyed, lemenya);
		scn.MoveCardsToDiscard(steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the site
		assertEquals(13, scn.GetTwilight());

		// A Shadow possession, played from hand on a minion.
		scn.ShadowPlayCard(blade);
		assertTrue(scn.ShadowHasCardChoiceAvailable(nertea, nelya));
		scn.ShadowChooseCard(nelya);

		assertAttachedTo(blade, nelya);
		assertEquals(12, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow condition.
		scn.ShadowPlayCard(destroyed);

		assertInZone(Zone.SUPPORT, destroyed);
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertInZone(Zone.DISCARD, steed, sword);

		// A minion, on the same board.
		scn.ShadowPlayCard(lemenya);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
	}

	@Test
	public void AMinionPlayedFromTheDiscardPileIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var squealer = scn.GetShadowCard("squealer");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToDiscard(lemenya, steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, squealer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.DISCARD, lemenya);

		// Morgul Squealer is discarded to play a [wraith] minion from the discard pile: Lemenya is the only one.
		scn.ShadowUseCardAction(squealer);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		// 13 - 4 to play - 2 roaming
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));

		scn.ShadowChooseOptionalTrigger(nertea);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, sword));
		// The Squealer is in the discard pile now, and is no possession.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(squealer));

		scn.ShadowChooseCard(steed);

		assertAttachedTo(steed, lemenya);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AlsoExertingNerteaPlaysAnArtifactOnTheMinionInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		// Two artifacts a Nazgul can bear.
		var rancor = scn.GetShadowCard("rancor");
		var savagery = scn.GetShadowCard("savagery");
		// An artifact only The Witch-king can bear, and one that plays to the support area.
		var ire = scn.GetShadowCard("ire");
		var crown = scn.GetShadowCard("crown");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(rancor, savagery, ire, crown, steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea, nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);

		// 10 set + 1 for Frodo + 2 for the site, - 4 to play - 2 roaming
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));

		scn.ShadowChooseOptionalTrigger(nertea);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertTrue(scn.ShadowChoiceAvailable("possession"));
		assertTrue(scn.ShadowChoiceAvailable("artifact"));

		scn.ShadowChoose("artifact");

		// Nertea exerts as well, and nobody else.
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(rancor, savagery));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ire, crown, steed, sword));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(savagery);

		assertAttachedTo(savagery, lemenya);
		assertInZone(Zone.DISCARD, rancor, ire, crown, steed, sword);
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnArtifactOnOfferAPossessionMayStillBeTakenAndNerteaDoesNotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var savagery = scn.GetShadowCard("savagery");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(rancor, savagery, steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		scn.ShadowChooseOptionalTrigger(nertea);

		assertTrue(scn.ShadowChoiceAvailable("possession"));
		assertTrue(scn.ShadowChoiceAvailable("artifact"));
		assertEquals(0, scn.GetWoundsOn(nertea));

		scn.ShadowChoose("possession");

		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rancor, savagery));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCard(sword);

		assertAttachedTo(sword, lemenya);
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.DISCARD, rancor, savagery, steed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyArtifactsInTheDiscardPileTheOfferIsToExertBothForAnArtifact()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var savagery = scn.GetShadowCard("savagery");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(rancor, savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nertea));
		scn.ShadowChooseOptionalTrigger(nertea);

		// Taking the offer with only artifacts to play was the one "may": both exert, and the game goes
		// straight to the choice of artifact without asking about Nertea's exertion.
		assertFalse(scn.ShadowDecisionAvailable("play an artifact from discard?"));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(rancor, savagery));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(rancor);

		assertAttachedTo(rancor, lemenya);
		assertInZone(Zone.DISCARD, savagery);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyArtifactsInTheDiscardPileTakingTheOfferExertsBothWithoutAskingAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var savagery = scn.GetShadowCard("savagery");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(rancor, savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowChooseOptionalTrigger(nertea);

		// The text has one "may": taking the offer with only artifacts to play is exerting both.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(rancor, savagery));

		scn.ShadowChooseCard(rancor);

		assertAttachedTo(rancor, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnArtifactIsNotOfferedWhileNerteaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(rancor, steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(nertea, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowChooseOptionalTrigger(nertea);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowChoiceAvailable("artifact"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rancor));
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCard(steed);

		assertAttachedTo(steed, lemenya);
		assertInZone(Zone.DISCARD, rancor, sword);
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenOnlyAnArtifactCouldBePlayedAndNerteaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");
		var savagery = scn.GetShadowCard("savagery");
		scn.MoveCardsToHand(lemenya);
		scn.MoveCardsToDiscard(rancor, savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(nertea, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);

		// Nothing in the discard pile can be played: an artifact needs Nertea to exert as well.
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertInZone(Zone.DISCARD, rancor, savagery);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedOnNerteasOwnPlayWithOnlyArtifactsInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var rancor = scn.GetShadowCard("rancor");
		var savagery = scn.GetShadowCard("savagery");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(rancor, savagery);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);

		// He is the minion played, so he would exert once for the offer; at vitality 2 he could not "also"
		// exert for an artifact, and nothing else is there to play.
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(nertea));
		assertInZone(Zone.DISCARD, rancor, savagery);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoPossessionInTheDiscardPileCanGoOnTheMinionPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(troop, lemenya);
		// Possessions that Nertea, who is in play, could bear, and no Orc can.
		scn.MoveCardsToDiscard(steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(troop);

		// 20 set + 1 for Frodo + 2 for the site, - 6 to play - 2 roaming
		assertEquals(15, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, steed, sword);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Nazgul, on the same board.
		scn.ShadowPlayCard(lemenya);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
	}

	@Test
	public void PlayingNerteaHimselfMayExertHimForAPossessionButNotForAnArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var rancor = scn.GetShadowCard("rancor");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(nertea);
		scn.MoveCardsToDiscard(rancor, steed, sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(nertea);

		// 10 set + 1 for Frodo + 2 for the site, - 5 to play - 2 roaming
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(0, scn.GetWoundsOn(nertea));

		scn.ShadowChooseOptionalTrigger(nertea);

		// He is the minion played and exerts as that. With a vitality of 2 he cannot exert again as "this
		// minion", so there is no artifact for him.
		assertEquals(1, scn.GetWoundsOn(nertea));
		assertFalse(scn.ShadowChoiceAvailable("artifact"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(rancor));

		scn.ShadowChooseCard(sword);

		assertAttachedTo(sword, nertea);
		assertInZone(Zone.DISCARD, rancor, steed);
		assertEquals(1, scn.GetWoundsOn(nertea));
		// 6 - 1 for the Nazgul Sword
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
