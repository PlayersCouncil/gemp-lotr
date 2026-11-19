package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "5_90");
					put("scout2", "5_90");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant is its whole text
					put("sword", "4_291");      // Sword of Rohan: [rohan] possession, hand weapon on a [rohan] Man
					put("stronghold", "102_48"); // Stronghold of My Fathers: [rohan] possession in the support area; a wound response only
					put("theoden", "5_93");     // Théoden, King of the Golden Hall: a maneuver ability only
					put("herugrim", "54_280");  // Herugrim: a [rohan] ARTIFACT, bearer Théoden; damage +1
					put("hall", "11_149");      // Protecting the Hall: a [rohan] CONDITION on a [rohan] Man; an assignment ability
					put("shield", "1_107");     // Great Shield: a [gondor] possession on a Man; archery total -1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rohirrim Scout
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.<br><b>Regroup:</b> Exert this companion and spot a [rohan] possession to return that possession to hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Rohirrim Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void RegroupAbilityExertsThisScoutToReturnABorneOrUnborneRohanPossessionToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var scout2 = scn.GetFreepsCard("scout2");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var stronghold = scn.GetFreepsCard("stronghold");
		var theoden = scn.GetFreepsCard("theoden");
		var herugrim = scn.GetFreepsCard("herugrim");
		var hall = scn.GetFreepsCard("hall");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(scout, scout2, guard, theoden);
		scn.AttachCardsTo(guard, sword, shield, hall);
		scn.AttachCardsTo(theoden, herugrim);
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		assertAttachedTo(sword, guard);
		assertAttachedTo(shield, guard);
		assertAttachedTo(hall, guard);
		assertAttachedTo(herugrim, theoden);
		assertInZone(Zone.SUPPORT, stronghold);
		assertEquals(0, scn.GetFreepsHandCount());

		// No minions: the fellowship moves to site 2 and the game goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		assertTrue(scn.FreepsActionAvailable(scout));
		assertTrue(scn.FreepsActionAvailable(scout2));

		scn.FreepsUseCardAction(scout);
		// The Scout who used the ability exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(scout2));
		// A borne [rohan] possession and one in the support area; not a [rohan] artifact, a [rohan]
		// condition or a [gondor] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, stronghold));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herugrim, hall, shield));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		assertInHand(sword);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInPlay(stronghold, herugrim, hall, shield);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(scout2));
		scn.FreepsUseCardAction(scout2);
		// The Stronghold is the only [rohan] possession left, and is returned unasked.
		assertEquals(1, scn.GetWoundsOn(scout2));
		assertInHand(stronghold);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInPlay(herugrim, hall, shield);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithOnlyARohanArtifactARohanConditionAndAGondorPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var guard = scn.GetFreepsCard("guard");
		var theoden = scn.GetFreepsCard("theoden");
		var herugrim = scn.GetFreepsCard("herugrim");
		var hall = scn.GetFreepsCard("hall");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(scout, guard, theoden);
		scn.AttachCardsTo(guard, shield, hall);
		scn.AttachCardsTo(theoden, herugrim);

		scn.StartGame();
		assertAttachedTo(shield, guard);
		assertAttachedTo(hall, guard);
		assertAttachedTo(herugrim, theoden);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// He could exert: only the missing [rohan] possession keeps the ability off.
		assertEquals(0, scn.GetWoundsOn(scout));
		assertFalse(scn.FreepsActionAvailable(scout));
	}

	@Test
	public void RegroupAbilityIsNotOfferedOnAnExhaustedScoutButIsOnAnotherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var scout2 = scn.GetFreepsCard("scout2");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(scout, scout2, guard);
		scn.AttachCardsTo(guard, sword);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(scout, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(scout));
		assertAttachedTo(sword, guard);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scout));
		assertTrue(scn.FreepsActionAvailable(scout2));
	}
}
