package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_016_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kingdom", "51_16");

					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3
					put("guard", "1_7");          // Dwarf Guard: [dwarven] Dwarf companion, cost 1, vitality 2
					put("grimir", "1_17");        // Grimir, Dwarven Elder: [dwarven] Dwarf ally
					put("strike", "1_3");         // Axe Strike: [dwarven] event
					put("armor", "1_8");          // Dwarven Armor: [dwarven] possession
					put("heart", "1_10");         // Dwarven Heart: [dwarven] condition
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("worker", "3_62");        // Isengard Worker: [isengard] Orc, cost 2, a regroup ability
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatestKingdomofMyPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Greatest Kingdom of My People
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale.</b>  To play, exert a Dwarf.
		 * The first time your opponent plays an Orc each turn, you may take a [dwarven] card into hand from
		 * your discard pile.  The Shadow player may discard the bottom 2 cards of their draw deck to prevent
		 * this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kingdom");

		assertEquals("Greatest Kingdom of My People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneToTheSupportAreaByExertingADwarfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(kingdom);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// A Man of the [dwarven] culture is still no Dwarf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsPlayAvailable(kingdom));

		scn.FreepsPlayCard(kingdom);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, kingdom);
		assertNotAttachedTo(kingdom, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilADwarfIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(kingdom, guard);
		// Vitality 3 with 2 wounds: Gimli is exhausted.  Aragorn could exert, and is of the Dwarves' culture,
		// but is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(kingdom));

		scn.FreepsPlayCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsPlayAvailable(kingdom));

		// The Guard is the only Dwarf who can exert, so he is the one who does.
		scn.FreepsPlayCard(kingdom);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 1 for the Guard + 1 for the condition
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, kingdom);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFirstOrcPlayedLetsADwarvenCardBeTakenIntoHandFromTheDiscardPileAndASecondOrcDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowHeart = scn.GetShadowCard("heart");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike, armor, aragorn);
		// A [dwarven] card in the other player's discard pile, of a blueprint this player's pile does not hold.
		scn.MoveCardsToDiscard(shadowHeart);
		// A Dwarf of another culture is no [dwarven] card.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.DWARF));
		scn.MoveCardsToHand(savage, worker, troop);

		scn.StartGame();
		scn.SetTwilight(30);

		var shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.FreepsPass();

		// An Uruk-hai is no Orc: nothing is offered, and it does not use up the first time.
		scn.ShadowPlayCard(savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(worker);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kingdom));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, strike, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, shadowHeart));
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(strike);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertInZone(Zone.DISCARD, strike);

		scn.ShadowChooseNo();
		assertInZone(Zone.HAND, strike);
		assertInZone(Zone.DISCARD, gimli, armor, aragorn);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Orc in the same turn, with [dwarven] cards still in the discard pile.
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedAndThenNothingIsTakenAndTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(30);

		var shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPass();
		scn.ShadowPlayCard(troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kingdom));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, gimli, strike);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertFalse(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondOrcIsNotTheFirstTimeThoughTheOfferAtTheFirstWasDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var troop = scn.GetShadowCard("troop");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike);
		scn.MoveCardsToHand(troop, worker);

		scn.StartGame();
		scn.SetTwilight(30);

		scn.FreepsPass();
		scn.ShadowPlayCard(troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		assertInZone(Zone.DISCARD, gimli, strike);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoDwarvenCardInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowGimli = scn.GetShadowCard("gimli");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		// Aragorn in this player's pile and a [dwarven] card in the other player's: nothing to take.
		scn.MoveCardsToDiscard(aragorn, shadowGimli);
		// A Dwarf of another culture is no [dwarven] card.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.DWARF));
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(30);

		scn.FreepsPass();
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerIsNotAskedToPreventATakeOfNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(30);

		var shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPass();
		scn.ShadowPlayCard(troop);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, strike));

		scn.FreepsDeclineChoosing();
		assertFalse(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertInZone(Zone.DISCARD, gimli, strike);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardTheBottom2CardsOfHisDrawDeckToPreventTheTaking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike);
		scn.MoveCardsToHand(worker, troop);

		scn.StartGame();
		scn.SetTwilight(30);

		var bottom1 = scn.GetFromBottomOfShadowDeck(1);
		var bottom2 = scn.GetFromBottomOfShadowDeck(2);
		var bottom3 = scn.GetFromBottomOfShadowDeck(3);
		var top = scn.GetShadowTopOfDeck();
		var shadowDeck = scn.GetShadowDeckCount();
		var freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(shadowDeck > 3);
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsPass();
		scn.ShadowPlayCard(worker);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(gimli);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertInZone(Zone.DECK, bottom1, bottom2);

		scn.ShadowChooseYes();
		assertInZone(Zone.DISCARD, bottom1, bottom2);
		assertInZone(Zone.DECK, bottom3, top);
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		// Nothing is taken, and the other player's deck is not the one that pays.
		assertInZone(Zone.DISCARD, gimli, strike);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The first time has been and gone: a second Orc brings no second offer.
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerIsNotAskedWithOneCardInHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike);
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		scn.SetTwilight(30);

		// Every card of the Shadow player's draw deck but his own Gimli is put in his discard pile.
		scn.MoveCardsToDiscard(scn.GetShadowCard("kingdom"), scn.GetShadowCard("guard"), scn.GetShadowCard("grimir"),
				scn.GetShadowCard("strike"), scn.GetShadowCard("armor"), scn.GetShadowCard("heart"),
				scn.GetShadowCard("aragorn"), scn.GetShadowCard("troop"), scn.GetShadowCard("savage"));
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowDiscardCount());

		scn.FreepsPass();
		scn.ShadowPlayCard(worker);
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, strike));

		scn.FreepsChooseCardBPFromSelection(gimli);
		assertFalse(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DISCARD, strike);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcPlayedInASecondShadowPhaseOfTheSameTurnBringsNoSecondOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var armor = scn.GetFreepsCard("armor");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike, armor);
		scn.MoveCardsToHand(worker, troop);

		scn.StartGame();
		scn.SetTwilight(30);

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(worker);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(strike);
		scn.ShadowChooseNo();
		assertInZone(Zone.HAND, strike);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.DISCARD, gimli, armor);

		// The first Orc of this Shadow phase is the second of the turn.
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kingdom));
		assertInZone(Zone.DISCARD, gimli, armor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstOrcOfTheNextTurnBringsTheOfferAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var strike = scn.GetFreepsCard("strike");
		var armor = scn.GetFreepsCard("armor");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToDiscard(gimli, strike, armor);
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		scn.SetTwilight(30);

		scn.FreepsPass();
		scn.ShadowPlayCard(worker);
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(strike);
		scn.ShadowChooseNo();
		assertInZone(Zone.HAND, strike);

		// The rest of this turn, and the whole of the other player's.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertInZone(Zone.SUPPORT, kingdom);
		assertInZone(Zone.DISCARD, gimli, armor);
		scn.MoveCardsToHand(troop);
		// The Shadow player's reconciles have drawn all but a card or two of his deck, and which are left is
		// chance: two are put back, so that he has the 2 cards he is to be asked about.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard"), scn.GetShadowCard("heart"));
		assertTrue(scn.GetShadowDeckCount() >= 2);
		scn.SetTwilight(30);

		scn.FreepsPass();
		scn.ShadowPlayCard(troop);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kingdom));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, armor));
		scn.FreepsChooseCardBPFromSelection(gimli);
		scn.ShadowChooseNo();
		assertInZone(Zone.HAND, gimli);
		assertInZone(Zone.DISCARD, armor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
