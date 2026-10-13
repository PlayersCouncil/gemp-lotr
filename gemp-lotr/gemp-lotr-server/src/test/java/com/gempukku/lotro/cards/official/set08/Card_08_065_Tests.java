package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_08_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ships1", "8_65");
					put("ships2", "8_65");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that a threat can lie; a maneuver ability used only on purpose
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man minion; strength +3 and fierce only while 2 burdens can be spotted (none here)
					put("rhun", "4_239");       // Men of Rhûn: [raider] Skirmish event, not playable in the phases used here
					put("galley", "8_59");      // Corsair War Galley: [raider] support-area possession; lies in a discard pile here
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, no text
					put("buccaneer", "8_53");   // Corsair Buccaneer: [raider] Man minion; lies in the Free Peoples player's discard pile here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShipsofGreatDraughtStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Ships of Great Draught
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Shadow:</b> Remove a threat or discard a [raider] card from hand to add a [raider] token
		 * here.<br><b>Regroup:</b> Remove 2 [raider] tokens here to place a [raider] card from your discard pile on
		 * top of your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ships1");

		assertEquals("Ships of Great Draught", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemovesAThreatOrDiscardsARaiderCardToAddATokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");

		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var axeman = scn.GetShadowCard("axeman");
		var rhun = scn.GetShadowCard("rhun");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ships1, ships2);
		scn.AddTokensToCard(ships2, 1);
		scn.AddThreats(1);
		scn.MoveCardsToHand(axeman, rhun, patrol);

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetCultureTokensOn(ships1));
		assertEquals(1, scn.GetCultureTokensOn(ships2));
		assertTrue(scn.ShadowActionAvailable(ships1));

		scn.ShadowUseCardAction(ships1);

		// Both costs can be paid: the player chooses which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Remove a threat"));
		assertTrue(scn.ShadowChoiceAvailable("card from hand"));
		scn.ShadowChooseOption("card from hand");

		// A [raider] card of either type; not the card of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, rhun));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(patrol));
		scn.ShadowChooseCard(rhun);

		assertInZone(Zone.DISCARD, rhun);
		assertInZone(Zone.HAND, axeman, patrol);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetThreats());
		// The token is here, on the copy used, and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertEquals(1, scn.GetCultureTokensOn(ships2));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Used again, paying with the threat.
		scn.ShadowUseCardAction(ships1);
		scn.ShadowChooseOption("Remove a threat");

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetCultureTokensOn(ships1));
		assertEquals(1, scn.GetCultureTokensOn(ships2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoThreatAndNoRaiderCardInHandAndTakesWhicheverCostCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("aragorn");

		var ships1 = scn.GetShadowCard("ships1");
		var axeman = scn.GetShadowCard("axeman");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ships1);
		scn.MoveCardsToHand(patrol);

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(ships1));

		// A threat: only that cost can be paid, and it is, with nothing asked.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(ships1));
		scn.ShadowUseCardAction(ships1);

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, patrol);
		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ships1));

		// A [raider] card in hand and no threat: the discard is the cost.
		scn.MoveCardsToHand(axeman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(ships1));
		scn.ShadowUseCardAction(ships1);

		assertInZone(Zone.DISCARD, axeman);
		assertInZone(Zone.HAND, patrol);
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetCultureTokensOn(ships1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesTwoTokensHereToPlaceARaiderCardFromTheDiscardPileOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var buccaneer = scn.GetFreepsCard("buccaneer");

		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var axeman = scn.GetShadowCard("axeman");
		var galley = scn.GetShadowCard("galley");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveCardsToSupportArea(ships1, ships2);
		scn.AddTokensToCard(ships1, 3);
		scn.AddTokensToCard(ships2, 2);
		scn.MoveCardsToDiscard(axeman, galley, patrol, buccaneer);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetCultureTokensOn(ships1));
		assertEquals(2, scn.GetCultureTokensOn(ships2));
		assertNotSame(axeman, scn.GetShadowTopOfDeck());
		assertTrue(scn.ShadowActionAvailable(ships1));

		scn.ShadowUseCardAction(ships1);

		assertEquals(1, scn.GetCultureTokensOn(ships1));
		assertEquals(2, scn.GetCultureTokensOn(ships2));

		// A [raider] card of either type in his own discard pile; not the card of another culture, and not the
		// [raider] card in the other player's pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, galley));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(patrol, buccaneer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		assertSame(axeman, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DISCARD, galley, patrol, buccaneer);

		// The copy used has 1 token left and cannot pay again; the other copy, with 2, can.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ships1));
		assertTrue(scn.ShadowActionAvailable(ships2));
	}
}
