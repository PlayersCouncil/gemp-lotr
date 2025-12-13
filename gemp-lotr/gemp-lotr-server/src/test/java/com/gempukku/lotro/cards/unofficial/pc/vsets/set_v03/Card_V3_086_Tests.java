package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_086_Tests
{

	// Northern Signal-fire, Flame of Calenhad (103_86) Tests

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("calenhad", "103_86");
					put("beacon1", "103_35");
					put("beacon2", "103_35");

					// Men with various keywords
					put("ranger", "1_89"); // Ranger
					put("heir2elendil", "4_109");  // Defender +1 (loaded)
					put("knight", "8_39");        // Knight
					put("eowyn", "5_122");        // Valiant
					put("boromir", "1_97");       // Plain Man (no relevant keywords)

					put("explorer", "4_250");     // Shadow Man - should not receive keywords
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Calenhad
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		* 	<b>Maneuver</b>: Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of your other Men gain each of the unloaded keywords on that Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("calenhad");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Calenhad", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



//
// Extra Cost tests - requires hindering 2 beacons
//

	@Test
	public void CalenhadCannotPlayWithoutTwoBeaconsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(calenhad);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(calenhad));
	}

	@Test
	public void CalenhadCanPlayByHinderingTwoBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToHand(calenhad);
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(beacon1, beacon2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(calenhad));

		scn.FreepsPlayCard(calenhad);
		scn.FreepsChooseCards(beacon1, beacon2);

		assertInZone(Zone.SUPPORT, calenhad);
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

//
// Maneuver ability tests - hinder self, exert Man, share keywords
//

	@Test
	public void CalenhadManeuverAbilityHindersSelfAndExertsMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var boromir = scn.GetFreepsCard("boromir");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(calenhad);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(calenhad));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(calenhad));

		scn.FreepsUseCardAction(calenhad);
		// Only one unbound Man, auto-selected

		assertTrue(scn.IsHindered(calenhad));
		assertEquals(1, scn.GetWoundsOn(boromir));
	}

	@Test
	public void CalenhadTransfersRangerKeywordToOtherMenButNotShadowMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var ranger = scn.GetFreepsCard("ranger");		  // Ranger
		var boromir = scn.GetFreepsCard("boromir");         // No keywords
		var explorer = scn.GetShadowCard("explorer");       // Shadow Man
		scn.MoveCompanionsToTable(ranger, boromir);
		scn.MoveCardsToSupportArea(calenhad);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// Verify initial state
		assertTrue(scn.HasKeyword(ranger, Keyword.RANGER));
		assertFalse(scn.HasKeyword(boromir, Keyword.RANGER));
		assertFalse(scn.HasKeyword(explorer, Keyword.RANGER));

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(ranger); // Exert Aragorn to share his keywords

		// Boromir should now have Ranger
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));
		// Aragorn still has Ranger
		assertTrue(scn.HasKeyword(ranger, Keyword.RANGER));
		// Shadow Man should NOT have Ranger
		assertFalse(scn.HasKeyword(explorer, Keyword.RANGER));
	}

	@Test
	public void CalenhadTransfersKnightKeywordToOtherMenButNotShadowMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var knight = scn.GetFreepsCard("knight");   // Knight
		var boromir = scn.GetFreepsCard("boromir"); // No keywords
		var explorer = scn.GetShadowCard("explorer"); // Shadow Man
		scn.MoveCompanionsToTable(knight, boromir);
		scn.MoveCardsToSupportArea(calenhad);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(knight, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(boromir, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(explorer, Keyword.KNIGHT));

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(knight);

		assertTrue(scn.HasKeyword(boromir, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(explorer, Keyword.KNIGHT));
	}

	@Test
	public void CalenhadTransfersValiantKeywordToOtherMenButNotShadowMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var eowyn = scn.GetFreepsCard("eowyn");     // Valiant
		var boromir = scn.GetFreepsCard("boromir"); // No keywords
		var explorer = scn.GetShadowCard("explorer"); // Shadow Man
		scn.MoveCompanionsToTable(eowyn, boromir);
		scn.MoveCardsToSupportArea(calenhad);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(eowyn, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(boromir, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(explorer, Keyword.VALIANT));

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(eowyn);

		assertTrue(scn.HasKeyword(boromir, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(explorer, Keyword.VALIANT));
	}

	@Test
	public void CalenhadDoesNotTransferLoadedKeywords() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var heir2elendil = scn.GetFreepsCard("heir2elendil"); // Defender +1 (loaded)
		var boromir = scn.GetFreepsCard("boromir");         // No keywords
		var explorer = scn.GetShadowCard("explorer");       // Shadow Man
		scn.MoveCompanionsToTable(heir2elendil, boromir);
		scn.MoveCardsToSupportArea(calenhad);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// Verify Aragorn has Defender
		assertTrue(scn.HasKeyword(heir2elendil, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(heir2elendil);

		// Boromir should NOT have Defender (loaded)
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));
		// Shadow Man shouldn't either
		assertFalse(scn.HasKeyword(explorer, Keyword.DEFENDER));
	}

	@Test
	public void CalenhadKeywordTransferLastsUntilRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calenhad = scn.GetFreepsCard("calenhad");
		var ranger = scn.GetFreepsCard("ranger"); // Ranger
		var boromir = scn.GetFreepsCard("boromir");         // No keywords
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCompanionsToTable(ranger, boromir);
		scn.MoveCardsToSupportArea(calenhad);
		scn.MoveMinionsToTable(explorer);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(calenhad);
		scn.FreepsChooseCard(ranger);

		// Boromir has Ranger during Maneuver
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));

		// Still has it during Archery
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));

		// Still has it during Assignment
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.HasKeyword(boromir, Keyword.RANGER));

		// Skip to Regroup - keyword should wear off
		scn.BothPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(boromir, Keyword.RANGER));
	}
}
