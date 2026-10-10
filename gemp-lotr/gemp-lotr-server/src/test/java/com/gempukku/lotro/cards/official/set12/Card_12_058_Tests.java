package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_58");
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
					put("companies", "12_58");
					put("corps", "11_73");      // Corps of Harad: [men] Man; fierce only at a battleground
					// Henchman's Dagger: [men] possession, cost 0, on a [men] minion; a hand weapon that may be
					// borne beside another
					put("dagger", "18_69");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, vitality 4
					put("vengeance", "1_106");  // Gondor's Vengeance: Regroup event. Exert a ranger companion to discard a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CountlessCompaniesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Countless Companies
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If a [men] possession is discarded from play, stack it here.<br><b>Shadow:</b> Remove (1) to play a possession stacked here as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Countless Companies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost whose whole effect would do nothing is not offered.  Once
	// the discarded possession is stacked here it is no longer in the discard pile, and a second use of the
	// Response for the same discard would stack nothing.  The game used to offer it again each time it was taken,
	// for as long as the player went on taking it (F-0357).
	@Test
	public void OnceTheDiscardedPossessionIsStackedHereTheResponseIsNotOfferedForItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance);

		var companies = scn.GetShadowCard("companies");
		var corps = scn.GetShadowCard("corps");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCardsToSupportArea(companies);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, dagger);
		// Nobody is assigned to the Corps, so it is in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertAttachedTo(dagger, corps);
		assertEquals(0, scn.GetStackedCards(companies).size());

		// The Corps is discarded, and the dagger it bore is discarded from play with it
		scn.FreepsPlayCard(vengeance);
		assertInZone(Zone.DISCARD, corps, dagger);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(companies));

		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, dagger);
		assertEquals(1, scn.GetStackedCards(companies).size());
		assertTrue(scn.GetStackedCards(companies).contains(dagger));

		// Nothing is left in the discard pile for the Response to stack, and it is the Shadow player's turn to
		// act in the regroup phase
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(companies));
		assertInZone(Zone.DISCARD, corps);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
