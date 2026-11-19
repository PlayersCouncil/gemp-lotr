package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_103_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("recruit1", "18_103");
					put("recruit2", "18_103");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; valiant is its whole text
					// We Left None Alive: [rohan] support-area condition; "When you play this condition, you may spot a
					// [rohan] Man to place 2 [rohan] tokens here." Its skirmish ability is not used.
					put("noneleft", "6_97");
					// Cast from the Hall: [rohan] support-area condition; each time you play a [rohan] possession from
					// hand it takes a [rohan] token. Its skirmish ability is not used.
					put("cast", "18_94");
					put("chain", "4_278");      // Heavy Chain: [rohan] armor, "Bearer must be a Man"; no strength bonus
					// Garrison of Osgiliath: [gondor] support-area condition holding [gondor] tokens; its skirmish
					// ability is not used
					put("garrison", "6_52");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimRecruitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Rohirrim Recruit
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.<br>While you can spot a [rohan] token, this companion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("recruit1");

		assertEquals("Rohirrim Recruit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
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
	public void EachCopyIsStrengthPlus2WhileARohanTokenCanBeSpottedAndNotOnceTheTokensAreGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var recruit1 = scn.GetFreepsCard("recruit1");
		var recruit2 = scn.GetFreepsCard("recruit2");
		var guard = scn.GetFreepsCard("guard");
		var noneleft = scn.GetFreepsCard("noneleft");
		scn.MoveCompanionsToTable(recruit1, recruit2, guard);
		scn.MoveCardsToHand(noneleft);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(recruit1));
		assertEquals(6, scn.GetStrength(recruit2));
		assertEquals(6, scn.GetStrength(guard));

		// We Left None Alive played by legal play, spotting a [rohan] Man for its 2 [rohan] tokens.
		scn.FreepsPlayCard(noneleft);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noneleft));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(noneleft));

		// Each Recruit is +2 for himself alone (8, not 10 for the two copies), and the Guard beside them is unchanged.
		assertEquals(8, scn.GetStrength(recruit1));
		assertEquals(8, scn.GetStrength(recruit2));
		assertEquals(6, scn.GetStrength(guard));

		// The tokens taken off to the last: no [rohan] token is left to spot.
		scn.RemoveTokensFromCard(noneleft, 2);
		assertEquals(0, scn.GetCultureTokensOn(noneleft));
		assertEquals(6, scn.GetStrength(recruit1));
		assertEquals(6, scn.GetStrength(recruit2));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ATokenOfAnotherCultureOrARohanConditionWithNoTokenDoesNotMakeHimStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var recruit1 = scn.GetFreepsCard("recruit1");
		var guard = scn.GetFreepsCard("guard");
		var cast = scn.GetFreepsCard("cast");
		var chain = scn.GetFreepsCard("chain");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(recruit1, guard);
		scn.MoveCardsToSupportArea(cast, garrison);
		scn.MoveCardsToHand(chain);
		// Two [gondor] tokens, as the Garrison's own play would put there.
		scn.AddTokensToCard(garrison, 2);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(cast));

		scn.StartGame();

		assertEquals(6, scn.GetStrength(recruit1));

		// A [rohan] possession played from hand puts a [rohan] token on Cast from the Hall: now he is +2.
		scn.FreepsPlayCard(chain);
		scn.FreepsChooseCard(guard);
		assertAttachedTo(chain, guard);
		assertEquals(1, scn.GetCultureTokensOn(cast));
		assertEquals(8, scn.GetStrength(recruit1));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
