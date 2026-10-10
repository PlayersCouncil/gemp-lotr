package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rack", "11_158");
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3; Valiant is its whole text
					// Heavy Chain: [rohan] armor possession, "Bearer must be a Man"; its text is about being
					// overwhelmed
					put("chain", "4_278");
					// Rohirrim Shield: [rohan] shield possession on a [rohan] Man; its regroup ability is not used
					put("shield", "5_91");
					// Beyond the Height of Men: [isengard] Maneuver event, cost 2.  "Exert an Uruk-hai to discard
					// an armor possession, helm possession, or shield possession (or all such Free Peoples
					// possessions if you can spot 6 companions)."
					put("height", "2_39");
					// Uruk Scout: [isengard] Uruk-hai, vitality 2.  Its Response answers an event that spots or
					// exerts a ranger, and nobody here is one.
					put("scout", "2_47");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rack", "11_158");
					put("guard", "5_83");       // Household Guard: [rohan] Man, vitality 3; Valiant is its whole text
					// Rohirrim Shield: [rohan] possession on a [rohan] Man.  "Regroup: Discard this possession to
					// heal bearer."
					put("shield", "5_91");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordRackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Sword Rack
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If a [rohan] possession is discarded from play and no more than one possession is stacked here, stack it here.<br><b>Fellowship:</b> Add (1) to play a possession stacked here as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sword Rack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost whose whole effect would do nothing is not offered.  Once
	// the discarded possession is stacked here it is no longer in the discard pile, and a second use of the
	// Response for the same discard would stack nothing.  The game used to offer it again each time it was taken,
	// for as long as the player went on taking it (F-0357).
	@Test
	public void OnceTheDiscardedPossessionIsStackedHereTheResponseIsNotOfferedForItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var rack = scn.GetFreepsCard("rack");
		var guard = scn.GetFreepsCard("guard");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, shield);
		scn.MoveCardsToSupportArea(rack);

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetStackedCards(rack).size());

		// The shield is discarded from play as the cost of its own ability
		scn.FreepsUseCardAction(shield);
		assertInZone(Zone.DISCARD, shield);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack));

		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, shield);
		assertEquals(1, scn.GetStackedCards(rack).size());
		assertTrue(scn.GetStackedCards(rack).contains(shield));

		// Nothing is left in the discard pile for the Response to stack: the shield's own ability goes on, one
		// wound of the two is healed, and it is the Shadow player's turn to act
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rack));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ResponseStacksHereARohanPossessionThatHasJustBeenDiscardedFromPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowDiscardScenario();

		var rack = scn.GetFreepsCard("rack");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain, shield);
		scn.MoveCardsToSupportArea(rack);

		var height = scn.GetShadowCard("height");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(height);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertAttachedTo(chain, guard);
		assertAttachedTo(shield, guard);
		assertEquals(0, scn.GetStackedCards(rack).size());
		assertTrue(scn.ShadowPlayAvailable(height));

		// The Scout is the one Uruk-hai and exerts with nothing asked; the Chain and the Shield are the two
		// possessions the event could discard.
		scn.ShadowPlayCard(height);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertTrue(scn.ShadowHasCardChoiceAvailable(chain, shield));
		scn.ShadowChooseCard(chain);

		// The Chain is in the discard pile already when its owner is offered the Response.
		assertInZone(Zone.DISCARD, chain);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rack));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, chain);
		assertEquals(1, scn.GetStackedCards(rack).size());
		assertTrue(scn.GetStackedCards(rack).contains(chain));
		assertAttachedTo(shield, guard);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
