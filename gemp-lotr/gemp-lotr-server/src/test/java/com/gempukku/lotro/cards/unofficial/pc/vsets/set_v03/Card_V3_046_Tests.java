package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.CantHinderModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_046_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helmsman1", "103_46");
					put("helmsman2", "103_46");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, vitality 2; nothing more below 2 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text at all
					put("marauder", "8_57");    // Corsair Marauder: [raider]; when played, may discard a possession to add 2 [raider] tokens to a card that has one
					put("initiate", "103_47");  // Desert Wind Initiate: [raider]; when played, remove (3) or hinder this minion
					put("halberd", "7_156");    // Raider Halberd: [raider] possession borne by a [raider] Man; a skirmish ability never used here
					put("ships1", "8_65");      // Ships of Great Draught: [raider] possession in the support area; abilities never used here
					put("ships2", "8_65");
					put("harad", "4_238");      // Men of Harad: [raider] condition in the support area; a Shadow ability never used here
					put("tidings", "7_130");    // Dark Tidings: [raider] condition in the support area; a regroup ability never used here
					put("seeking", "1_276");    // Seeking Its Master: [sauron] condition in the support area; a Shadow ability never used here

					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger
					put("legolas", "1_50");     // Legolas, Greenleaf: Archery: exert Legolas to wound a minion
					put("enter1", "103_9");     // You Cannot Enter Here: Maneuver: exert your Wizard to hinder a Shadow card
					put("enter2", "103_9");
					put("thunder1", "4_99");    // Roll of Thunder: Maneuver: spot Gandalf to discard a Shadow possession or artifact
					put("thunder2", "4_99");
					put("sleep", "1_84");       // Sleep, Caradhras: Fellowship: exert Gandalf to discard every condition
					put("what1", "1_119");      // What Are They?: Maneuver: spot a ranger to discard a roaming minion
					put("what2", "1_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairHelmsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Corsair Helmsman
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair.</b>
		 * 	Response: If your [raider] card is about to be hindered or discarded by another card, exert or hinder this minion to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helmsman1");

		assertEquals("Corsair Helmsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForThreeAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		scn.MoveCardsToHand(helmsman);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.HAND, helmsman);
		assertTrue(scn.ShadowPlayAvailable(helmsman));

		scn.ShadowPlayCard(helmsman);

		// 10 - 3 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, helmsman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheHelmsmanChosenExertsToSaveAPossessionFromDiscardAndAConditionFromHinderAndDecliningLetsTheNextGo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman1 = scn.GetShadowCard("helmsman1");
		var helmsman2 = scn.GetShadowCard("helmsman2");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var harad = scn.GetShadowCard("harad");
		// The Free Peoples player's own copy, in his support area: a [raider] card that is not the Shadow player's.
		var theirShips = scn.GetFreepsCard("ships1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thunder1 = scn.GetFreepsCard("thunder1");
		var thunder2 = scn.GetFreepsCard("thunder2");
		var enter = scn.GetFreepsCard("enter1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(thunder1, thunder2, enter);
		scn.MoveCardsToSupportArea(ships1, ships2, harad, theirShips);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman1, helmsman2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(thunder1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ships1, ships2));
		// His own Shadow cards are not active on his turn, and nothing reaches them.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theirShips));
		scn.FreepsChooseCard(ships1);

		// Each copy offers its own response to the discard.
		assertInZone(Zone.SUPPORT, ships1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman2));
		assertEquals(0, scn.GetWoundsOn(helmsman1));
		assertEquals(0, scn.GetWoundsOn(helmsman2));

		scn.ShadowChooseOptionalTrigger(helmsman2);
		assertTrue(scn.ShadowChoiceAvailable("Exert"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));
		scn.ShadowChoose("Exert");

		// The copy used pays; the possession stays where it was.
		assertEquals(1, scn.GetWoundsOn(helmsman2));
		assertEquals(0, scn.GetWoundsOn(helmsman1));
		assertFalse(scn.IsHindered(helmsman2));
		assertFalse(scn.IsHindered(helmsman1));
		assertInZone(Zone.SUPPORT, ships1);
		assertInZone(Zone.SUPPORT, ships2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(harad);

		assertFalse(scn.IsHindered(harad));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman1));

		scn.ShadowChooseOptionalTrigger(helmsman1);
		scn.ShadowChoose("Exert");

		assertEquals(1, scn.GetWoundsOn(helmsman1));
		assertEquals(1, scn.GetWoundsOn(helmsman2));
		assertFalse(scn.IsHindered(helmsman1));
		assertFalse(scn.IsHindered(harad));
		assertInZone(Zone.SUPPORT, harad);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(thunder2);
		scn.FreepsChooseCard(ships2);

		// Vitality 2 with 1 wound: neither can exert now, but each can still hinder itself.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman2));

		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, ships2);
		assertInZone(Zone.SUPPORT, ships1, theirShips);
		assertFalse(scn.IsHindered(helmsman1));
		assertFalse(scn.IsHindered(helmsman2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void HinderingItselfSavesARaiderMinionFromBeingHinderedAndACardOfAnotherCultureIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var axeman = scn.GetShadowCard("axeman");
		var brigand = scn.GetShadowCard("brigand");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enter1 = scn.GetFreepsCard("enter1");
		var enter2 = scn.GetFreepsCard("enter2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter1, enter2);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman, axeman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(helmsman, axeman, brigand));
		scn.FreepsChooseCard(brigand);

		// A [dunland] Man of the same player: hindered with nothing offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.IsHindered(brigand));
		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The event exhausted Gandalf for choosing a minion; he is healed so that he can pay for the second.
		scn.RemoveWoundsFromChar(gandalf, 3);
		scn.ShadowPass();
		scn.FreepsPlayCard(enter2);
		scn.FreepsChooseCard(axeman);

		assertFalse(scn.IsHindered(axeman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));

		scn.ShadowChooseOptionalTrigger(helmsman);
		assertTrue(scn.ShadowChoiceAvailable("Exert"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));
		scn.ShadowChoose("Hinder");

		assertTrue(scn.IsHindered(helmsman));
		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(axeman));
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OfSeveralCardsDiscardedAtOnceItSavesOneRaiderCardForEachUseAndNoCardOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var harad = scn.GetShadowCard("harad");
		var tidings = scn.GetShadowCard("tidings");
		var seeking = scn.GetShadowCard("seeking");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);
		scn.MoveCardsToSupportArea(harad, tidings, seeking);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman);

		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));

		scn.FreepsPlayCard(sleep);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		scn.ShadowChooseOptionalTrigger(helmsman);
		scn.ShadowChoose("Exert");

		// Two [raider] conditions and a [sauron] one are about to go: one of the [raider] two is saved.
		assertEquals(1, scn.GetWoundsOn(helmsman));
		assertTrue(scn.ShadowHasCardChoiceAvailable(harad, tidings));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(seeking));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(harad);

		// The same discard may be answered again (CR 4.2 "response"). Vitality 2 with 1 wound: the Helmsman
		// cannot exert, so hindering itself is the payment, and Dark Tidings is the only card left to save.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		scn.ShadowChooseOptionalTrigger(helmsman);

		assertTrue(scn.IsHindered(helmsman));
		assertEquals(1, scn.GetWoundsOn(helmsman));
		assertInZone(Zone.SUPPORT, harad);
		assertInZone(Zone.SUPPORT, tidings);
		assertInZone(Zone.DISCARD, seeking);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfAnotherCultureDiscardedOnItsOwnIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var what = scn.GetFreepsCard("what1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		// A site 4 and a site 3 minion at site 2: both are roaming.
		scn.FreepsPlayCard(what);
		assertTrue(scn.FreepsHasCardChoiceAvailable(helmsman, brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		scn.FreepsChooseCard(brigand);

		// A [dunland] Man of the same player, and the only card being discarded: nothing is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void APossessionThatGoesWithItsDiscardedBearerIsNotAnsweredOnItsOwnAndTheHelmsmanSavesItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var axeman = scn.GetShadowCard("axeman");
		var halberd = scn.GetShadowCard("halberd");
		var aragorn = scn.GetFreepsCard("aragorn");
		var what1 = scn.GetFreepsCard("what1");
		var what2 = scn.GetFreepsCard("what2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(what1, what2);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman, axeman);
		scn.AttachCardsTo(axeman, halberd);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 4 minions at site 2: both are roaming.
		scn.FreepsPlayCard(what1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(helmsman, axeman));
		scn.FreepsChooseCard(axeman);

		// One offer, for the Axeman. Declined, he is discarded, and his Halberd goes with him with
		// nothing more offered.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		assertAttachedTo(halberd, axeman);
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, axeman);
		assertInZone(Zone.DISCARD, halberd);
		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();
		// The Helmsman is the only roaming minion left, and is itself the [raider] card about to be discarded.
		scn.FreepsPlayCard(what2);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		scn.ShadowChooseOptionalTrigger(helmsman);
		scn.ShadowChoose("Exert");

		assertEquals(1, scn.GetWoundsOn(helmsman));
		assertInZone(Zone.SHADOW_CHARACTERS, helmsman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ARaiderMinionKilledByAWoundIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var axeman = scn.GetShadowCard("axeman");
		var halberd = scn.GetShadowCard("halberd");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman, axeman);
		scn.AttachCardsTo(axeman, halberd);
		// Vitality 2 with 1 wound: the next wound kills him.
		scn.AddWoundsToChar(axeman, 1);
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(helmsman, axeman));
		scn.FreepsChooseCard(axeman);

		// He is killed, not discarded, and the Halberd he bore goes because its bearer has: neither is
		// offered to the Helmsman, and the Shadow player is asked only for an archery action.
		assertInZone(Zone.DISCARD, axeman);
		assertInZone(Zone.DISCARD, halberd);
		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ADiscardThePlayersOwnCardPaysAsACostIsPreventedTooAndThatCostIsThenUnpaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var marauder = scn.GetShadowCard("marauder");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		scn.MoveCardsToHand(marauder);
		scn.MoveCardsToSupportArea(ships1, ships2);
		scn.AddTokensToCard(ships1, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		// The Helmsman is the other corsair the Marauder needs.
		scn.ShadowPlayCard(marauder);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(marauder));
		scn.ShadowChooseOptionalTrigger(marauder);
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships1, ships2));
		scn.ShadowChooseCard(ships2);

		// The Marauder, a Shadow card of the same player, is the other card doing the discarding.
		assertInZone(Zone.SUPPORT, ships2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		assertEquals(1, scn.GetCultureTokensOn(ships1));

		scn.ShadowChooseOptionalTrigger(helmsman);
		scn.ShadowChoose("Exert");

		assertEquals(1, scn.GetWoundsOn(helmsman));
		assertInZone(Zone.SUPPORT, ships2);
		// The discard was the Marauder's cost. Prevented, it is not paid, and no tokens are added.
		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertEquals(0, scn.GetCultureTokensOn(ships2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Ignore("Q-0079: 'by another card' is read here as a card other than the [raider] card itself; the game reads it as a card other than this Helmsman and offers the response when a [raider] minion hinders itself")
	@Test
	public void ARaiderMinionThatHindersItselfIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman = scn.GetShadowCard("helmsman1");
		var initiate = scn.GetShadowCard("initiate");
		scn.MoveCardsToHand(initiate);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(initiate);
		assertFalse(scn.IsHindered(initiate));
		scn.ShadowChoose("Hinder");

		// The Initiate is hindered by its own text and by no other card: there is nothing to answer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(helmsman));
		assertTrue(scn.IsHindered(initiate));
		assertEquals(0, scn.GetWoundsOn(helmsman));
		assertFalse(scn.IsHindered(helmsman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHelmsmanThatCanNeitherExertNorBeHinderedIsNotOfferedTheResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helmsman1 = scn.GetShadowCard("helmsman1");
		var helmsman2 = scn.GetShadowCard("helmsman2");
		var axeman = scn.GetShadowCard("axeman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var enter = scn.GetFreepsCard("enter1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(enter);

		scn.StartGame();
		scn.MoveMinionsToTable(helmsman1, helmsman2, axeman);
		// Vitality 2 with 1 wound: neither Helmsman can exert.
		scn.AddWoundsToChar(helmsman1, 1);
		scn.AddWoundsToChar(helmsman2, 1);
		// And the first cannot be hindered by a Shadow card, itself included.
		scn.ApplyAdHocModifier(new CantHinderModifier(null, "Cannot be hindered by Shadow cards", null, null, helmsman1, Side.SHADOW));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(enter);
		scn.FreepsChooseCard(axeman);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(helmsman1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(helmsman2));

		// The second pays the only way it can, with no choice of payment to make.
		scn.ShadowChooseOptionalTrigger(helmsman2);

		assertTrue(scn.IsHindered(helmsman2));
		assertFalse(scn.IsHindered(helmsman1));
		assertEquals(1, scn.GetWoundsOn(helmsman1));
		assertEquals(1, scn.GetWoundsOn(helmsman2));
		assertFalse(scn.IsHindered(axeman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
