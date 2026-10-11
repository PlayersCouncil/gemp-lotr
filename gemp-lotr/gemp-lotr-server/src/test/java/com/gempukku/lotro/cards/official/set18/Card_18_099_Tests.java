package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_99");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanPossessionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gamling", "18_99");
					put("guard", "5_83");       // Household Guard: [rohan] Man; Valiant is its whole text
					// Eowyn, Dernhelm: [rohan] Man; valiant, and stronger only while she skirmishes a mounted or a
					// fierce minion.  She skirmishes nobody here.
					put("eowyn", "7_228");
					// Rohirrim Shield: [rohan] possession (shield).  "Bearer must be a [rohan] Man."  The minion
					// archery total is -1, and no minion here is an archer; its regroup ability is never used.
					put("shield1", "5_91");
					put("shield2", "5_91");
					put("spear", "4_288");      // Rider's Spear: [rohan] possession (hand weapon), strength +2.  "Bearer must be a [rohan] Man."
					// Heavy Chain: [rohan] possession (armor).  "Bearer must be a Man."  Bearer may not be
					// overwhelmed unless his strength is tripled, and the minion here is the weaker.
					put("chain", "4_278");
					// Eowyn's Sword: [rohan] possession (hand weapon), strength +3.  "Bearer must be Eowyn."  Damage
					// +1 only while a villager can be spotted.
					put("sword", "4_272");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GamlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gamling, Dutiful Marshal
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.<br><b>Skirmish:</b> Transfer a [rohan] possession that Gamling can bear to him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gamling", card.getBlueprint().getTitle());
		assertEquals("Dutiful Marshal", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishActionOffersOnlyTheRohanPossessionsGamlingCouldBearAndIsNotOfferedWhenThereIsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanPossessionsScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var eowyn = scn.GetFreepsCard("eowyn");
		var gamlingsShield = scn.GetFreepsCard("shield1");
		var guardsShield = scn.GetFreepsCard("shield2");
		var spear = scn.GetFreepsCard("spear");
		var chain = scn.GetFreepsCard("chain");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gamling, guard, eowyn);
		scn.AttachCardsTo(gamling, gamlingsShield);
		scn.AttachCardsTo(guard, guardsShield, spear, chain);
		scn.AttachCardsTo(eowyn, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignAndResolve(gamling, savage);

		assertEquals(6, scn.GetStrength(gamling));
		assertTrue(scn.FreepsActionAvailable(gamling));
		scn.FreepsUseCardAction(gamling);

		// Five [rohan] possessions are in play.  Gamling has no hand weapon and no armor: he could bear the Spear
		// and the Chain.  He bears his own Shield already, and with it could not bear the Guard's.  He has no hand
		// weapon, and still could not bear the Sword: its bearer must be Eowyn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gamlingsShield, guardsShield, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(spear);

		assertAttachedTo(spear, gamling);
		// 6 + 2 for the Spear
		assertEquals(8, scn.GetStrength(gamling));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();

		// The Chain is now the one possession he could take, and comes to him with nothing asked.
		assertTrue(scn.FreepsActionAvailable(gamling));
		scn.FreepsUseCardAction(gamling);

		assertAttachedTo(chain, gamling);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();

		// Two [rohan] possessions are left that he does not bear, neither of which he could: the ability costs
		// nothing and would move nothing.
		assertAttachedTo(guardsShield, guard);
		assertAttachedTo(sword, eowyn);
		assertFalse(scn.FreepsActionAvailable(gamling));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
