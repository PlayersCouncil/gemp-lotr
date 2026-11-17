package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "18_39");
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man companion, no knight; a regroup ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, no knight; an ability that needs initiative
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit, no Man; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmoroftheWhiteCityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Armor of the White City
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Strength: 1
		 * Game Text: Bearer must be a [gondor] Man.<br>Bearer is a <b>knight</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Armor of the White City", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAGondorManAndIsAKnight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var frodo = scn.GetRingBearer();
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(ranger, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		assertInZone(Zone.HAND, armor);
		assertFalse(scn.HasKeyword(merchant, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(ranger, Keyword.KNIGHT));
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);
		// A [gondor] Man, companion or ally; not Pippin ([gondor], no Man), the Household Guard (a Man, [rohan]) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(armor, merchant);
		// The ally who bears it is a knight; the [gondor] Man who does not is none.
		assertTrue(scn.HasKeyword(merchant, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(ranger, Keyword.KNIGHT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsItIsAKnight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveAlliesToTable(merchant);

		scn.StartGame();

		assertFalse(scn.HasKeyword(ranger, Keyword.KNIGHT));
		scn.AttachCardsTo(ranger, armor);

		assertAttachedTo(armor, ranger);
		assertTrue(scn.HasKeyword(ranger, Keyword.KNIGHT));
		assertFalse(scn.HasKeyword(merchant, Keyword.KNIGHT));
	}
}
