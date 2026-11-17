package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_195_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "7_195");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, cost 4, site 3; her text is about
					// companions bearing [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider]; fierce, and his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc who is no Nazgûl; its text needs 3 burdens
					put("creeper", "8_75");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a second companion, room for a threat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "7_195");
					// Elrond, Lord of Rivendell: an ally; "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					// Barliman Butterbur, Prancing Pony Proprietor: an ally; "Fellowship: Exert Barliman Butterbur
					// to take a [gandalf] event into hand from your discard pile."
					put("barliman", "1_70");
					put("wizard", "1_78");          // Mysterious Wizard: a [gandalf] event, for the discard pile
					// Aragorn, Ranger of the North: a second companion, so that two threats have room
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTriggerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "7_195");
					// Elrond, Lord of Rivendell: an ally, vitality 4.  "Fellowship: Exert Elrond to draw a card."
					put("elrond", "1_40");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a companion, room for threats
					// Legolas, Greenleaf: an Elf companion, room for a third threat; spotted by Cast It Into the Fire!,
					// and the Elf played by Rivendell Valley's own text
					put("legolas", "1_50");
					// Cast It Into the Fire!: [elven] Maneuver event.  "Spot an Elf to make each opponent discard a card
					// from his or her hand. Draw a card for each card discarded in this way."
					put("cast", "3_11");
					// Seeing Stone of Orthanc: [gondor] support-area artifact (to play, spot a [gondor] Man: cheated in).
					// "Regroup: Add a threat or discard this artifact to remove (2) or to draw a card."
					put("stone", "9_38");
					// Drawing His Eye: [gondor] Fellowship event.  "For each [gondor] companion you spot, reveal 1 card
					// at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner
					// draws a card."
					put("eye", "10_29");
					put("runner", "1_178");         // Goblin Runner: the Shadow card in the Shadow player's hand
					// Morgul Creeper: a minion, so that there is a maneuver phase; its text needs 3 burdens
					put("creeper", "8_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulontheMarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul on the March
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Nazgûl.<br>Each time the Free Peoples player draws a card (except during the regroup phase), add a threat.<br><b>Regroup:</b> Discard a Nazgûl and this condition to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("march");

		assertEquals("Morgul on the March", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACardTakenIntoHandFromTheDiscardPileIsNotACardDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var barliman = scn.GetFreepsCard("barliman");
		var wizard = scn.GetFreepsCard("wizard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(elrond, barliman);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(wizard);
		scn.MoveCardsToSupportArea(scn.GetShadowCard("march"));
		assertInDiscard(wizard);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());

		// A card drawn: one threat.
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());

		// A card taken into hand from the discard pile is in hand and was not drawn: no second threat,
		// though two companions leave room for one.
		scn.FreepsUseCardAction(barliman);
		assertInHand(wizard);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(march, lemenya);
		assertInHand(march, lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [wraith] Orc is no Nazgûl.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(march));

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		assertTrue(scn.ShadowPlayAvailable(march));
		scn.ShadowPlayCard(march);
		assertInZone(Zone.SUPPORT, march);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(march);
		assertInHand(march);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(march));
		scn.ShadowPlayCard(march);
		assertInZone(Zone.SUPPORT, march);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCardTheFreePeoplesPlayerDrawsInTheFellowshipPhaseAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var march = scn.GetShadowCard("march");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(march);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());

		// Three companions: room for a second threat.
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardDrawnInTheManeuverPhaseAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var march = scn.GetShadowCard("march");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var cast = scn.GetFreepsCard("cast");
		var runner = scn.GetShadowCard("runner");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToSupportArea(march);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetFreepsHand(cast);
		scn.SetShadowHand(runner);
		scn.FreepsRefreshDecision();
		assertInHand(cast, runner);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		// Legolas is the Elf spotted; the Shadow player's one card is discarded, and one card is drawn for it.
		assertTrue(scn.FreepsPlayAvailable(cast));
		scn.FreepsPlayCard(cast);

		assertInDiscard(runner, cast);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACardDrawnByTheSitesOwnTextAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var march = scn.GetShadowCard("march");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var valley = scn.GetCurrentSite();
		scn.SetFreepsHand(legolas);
		scn.MoveCardsToSupportArea(march);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, march);
		assertInHand(legolas);
		assertEquals(1, scn.GetFreepsHandCount());
		int threats = scn.GetThreats();
		int deck = scn.GetFreepsDeckCount();

		// Rivendell Valley: "Fellowship: Play an Elf to draw a card."  A site is no card of the Free Peoples side.
		assertTrue(scn.FreepsActionAvailable(valley));
		scn.FreepsUseCardAction(valley);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		// Legolas left the hand and the top card of the deck came into it.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(threats + 1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardDrawnInTheRegroupPhaseAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var march = scn.GetShadowCard("march");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stone = scn.GetFreepsCard("stone");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stone, march);
		assertInZone(Zone.SUPPORT, stone);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// No minion: the Shadow phase is followed by the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(stone));

		scn.FreepsUseCardAction(stone);
		scn.FreepsChoose("Discard");
		scn.FreepsChoose("Draw");

		assertInDiscard(stone);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		// Two companions leave room for a threat.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerDrawingInTheFellowshipPhaseAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTriggerScenario();

		var march = scn.GetShadowCard("march");
		var eye = scn.GetFreepsCard("eye");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(march);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		scn.SetFreepsHand(eye);
		scn.SetShadowHand(runner);
		scn.FreepsRefreshDecision();
		assertInHand(eye, runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetThreats());
		int shadowDeck = scn.GetShadowDeckCount();

		// Drawing His Eye is the Free Peoples player's card, and the Shadow player has no choice in his draw; it is
		// still the drawing hand's player's draw (Q-0334).
		assertTrue(scn.FreepsPlayAvailable(eye));
		scn.FreepsPlayCard(eye);
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");
		// Aragorn spotted: one card revealed, the Goblin Runner, the only card there.
		scn.FreepsDismissRevealedCards();

		// The Runner is discarded and the Shadow player draws in its place.
		assertInDiscard(runner, eye);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		// Two companions leave room for a threat.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsANazgulAndThisConditionToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(march);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		// No minion in the Shadow phase: straight to the regroup phase, where minions that lived through the
		// skirmishes stand.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(lemenya, khamul, creeper);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(march));

		scn.ShadowUseCardAction(march);
		// A Nazgûl of either culture; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertInDiscard(lemenya, march);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul, creeper);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoNazgulToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(march);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(creeper);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(march));
		assertInZone(Zone.SUPPORT, march);
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(march);
		assertInZone(Zone.SUPPORT, march);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(march));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
	}
}
