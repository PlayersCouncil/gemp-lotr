package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCultureTokensScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "18_9");
					put("defender2", "18_9");
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion, strength 6; an archery ability only
					// Elven Supplies: [elven] condition. "Fellowship: Remove a culture token from a Free Peoples
					// card to add an [elven] token here." Its other ability is a skirmish one.
					put("supplies", "18_10");
					// Wish For Our Kinfolk: [dwarven] condition that holds [dwarven] tokens; its abilities need a Dwarf
					// to exert (fellowship) or a skirmish.
					put("wish", "8_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenDefenderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Elven Defender
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot an [elven] token, this companion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Elven Defender", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void IsStrengthPlus2WhileAnElvenTokenCanBeSpottedAndNoMoreForASecondToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCultureTokensScenario();

		var defender = scn.GetFreepsCard("defender");
		var defender2 = scn.GetFreepsCard("defender2");
		var legolas = scn.GetFreepsCard("legolas");
		var supplies = scn.GetFreepsCard("supplies");
		var wish = scn.GetFreepsCard("wish");
		var opponentsSupplies = scn.GetShadowCard("supplies");
		scn.MoveCompanionsToTable(defender, defender2, legolas);
		scn.MoveCardsToSupportArea(supplies, wish, opponentsSupplies);
		scn.AddTokensToCard(wish, 2);
		// The other player's Free Peoples cards are not active on this turn, so his token cannot be spotted.
		scn.AddTokensToCard(opponentsSupplies, 1);

		scn.StartGame();

		// Two [dwarven] tokens, an [elven] card that could hold a token and holds none, and a token of the
		// opponent's: no [elven] token to spot.
		assertEquals(0, scn.GetCultureTokensOn(supplies));
		assertEquals(2, scn.GetCultureTokensOn(wish));
		assertEquals(1, scn.GetCultureTokensOn(opponentsSupplies));
		assertEquals(5, scn.GetStrength(defender));
		assertEquals(5, scn.GetStrength(defender2));
		assertEquals(6, scn.GetStrength(legolas));

		// Elven Supplies turns a [dwarven] token into the first [elven] token.
		scn.FreepsUseCardAction(supplies);
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		// 5 + 2, each copy for itself; Legolas is no Elven Defender.
		assertEquals(7, scn.GetStrength(defender));
		assertEquals(7, scn.GetStrength(defender2));
		assertEquals(6, scn.GetStrength(legolas));

		// A second [elven] token adds nothing: "an [elven] token", not "for each".
		scn.FreepsUseCardAction(supplies);
		scn.FreepsChooseCard(wish);
		assertEquals(2, scn.GetCultureTokensOn(supplies));
		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertEquals(7, scn.GetStrength(defender));
		assertEquals(7, scn.GetStrength(defender2));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
