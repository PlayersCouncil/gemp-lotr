package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_016_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "102_16");
					put("veowyn", "4_270");
					put("vgamling", "5_82");
					put("vrscout", "5_90");
					put("gandalf", "1_72");
					put("grima", "5_51");
					put("twk", "1_237");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Aragorn, Last Hope Of Men
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 4
		 * Signet: Theoden
		 * Game Text: Valiant.
		* 	While you can spot 3 valiant companions, the number of free peoples cultures that can be counted is-1.
		* 	Skirmish: If a valiant companion is skirmishing a minion with a strength of 12 or higher, exert Aragorn to make that companion strength +2 (or +3 if that companion is exhausted).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Last Hope Of Men", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet()); 
	}

	// @Test
	public void ReducesFPsCulturesSpottedWhenThreeValiantCompanionsArePresent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		PhysicalCardImpl veowyn = scn.GetFreepsCard("veowyn");
		PhysicalCardImpl vgamling = scn.GetFreepsCard("vgamling");
		PhysicalCardImpl vrscout = scn.GetFreepsCard("vrscout");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		scn.FreepsMoveCardToHand(card, veowyn, gandalf, vgamling, vrscout);

		scn.StartGame();

		// Play Aragorn
		scn.FreepsPlayCard(card);
		assertEquals(4, scn.GetTwilight());

		// Play companions of two additional (non-gondor, non-shire) cultures to get to 4 total to spot
		scn.FreepsPlayCard(veowyn);
		scn.FreepsPlayCard(gandalf);

		// TODO assert that there are 4 cultures spotted (2 valiant companions)

		scn.FreepsPlayCard(vgamling);

		// TODO assert that there are 3 cultures spotted even though 4 are on the board (3 valiant companions)

		scn.FreepsPlayCard(vrscout);

		// TODO assert that there are 3 cultures spotted even though 4 are on the board (gt 3 valiant companions)
	}


	@Test
	public void ExertsToBoostValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		PhysicalCardImpl veowyn = scn.GetFreepsCard("veowyn");
		scn.FreepsMoveCardToHand(card, veowyn);

		PhysicalCardImpl twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCardToHand(twk);

		scn.StartGame();

		// Play Aragorn
		scn.FreepsPlayCard(card);
		assertEquals(4, scn.GetTwilight());

		// Play companions of two additional (non-gondor, non-shire) cultures to get to 4 total to spot
		scn.FreepsPlayCard(veowyn);

		// Get the game state to where Grima is on the table and it's the shadow player's turn during the maneuver phase
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(twk);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(veowyn, twk);
		scn.FreepsResolveSkirmish(veowyn);

		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals(0, scn.GetWoundsOn(card));
		assertEquals(0, scn.GetWoundsOn(veowyn));
		assertEquals(6, scn.GetStrength(veowyn));

		scn.FreepsUseCardAction(card);
		assertEquals(1, scn.GetWoundsOn(card));
		assertEquals(0, scn.GetWoundsOn(veowyn));
		// strength +2 because Eowyn isn't exhausted
		assertEquals(8, scn.GetStrength(veowyn));

		// Wound Eowyn twice and have the shadow player pass their skirmish action
		scn.AddWoundsToChar(veowyn, 2);
		scn.ShadowPassCurrentPhaseAction();

		assertTrue(scn.FreepsActionAvailable(card));
		assertEquals(1, scn.GetWoundsOn(card));
		assertEquals(2, scn.GetWoundsOn(veowyn));
		assertEquals(8, scn.GetStrength(veowyn));

		scn.FreepsUseCardAction(card);
		assertEquals(2, scn.GetWoundsOn(card));
		assertEquals(2, scn.GetWoundsOn(veowyn));
		// strength +3 because Eowyn isn't exhausted, now all she needs is Merry with a dagger to help her out
		assertEquals(11, scn.GetStrength(veowyn));
	}

	@Test
	public void CantExertToBoostNonValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		scn.FreepsMoveCardToHand(card, gandalf);

		PhysicalCardImpl twk = scn.GetShadowCard("twk");
		scn.ShadowMoveCardToHand(twk);

		scn.StartGame();

		// Play Aragorn
		scn.FreepsPlayCard(card);
		assertEquals(4, scn.GetTwilight());

		scn.FreepsPlayCard(gandalf);

		// Get the game state to where Grima is on the table and it's the shadow player's turn during the maneuver phase
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(twk);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, twk);
		scn.FreepsResolveSkirmish(gandalf);

		// Despite all evidence to the contrary, Gandalf isn't valiant so shouldn't be boostable
		assertFalse(scn.FreepsActionAvailable(card));
	}
}
