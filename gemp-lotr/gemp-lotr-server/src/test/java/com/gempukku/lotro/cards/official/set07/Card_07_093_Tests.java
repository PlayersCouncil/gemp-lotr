package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_93");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOverwhelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_93");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, no text, not unique
					put("brigand2", "4_10");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					// The Free Peoples hand: two companions, which offer nothing in a skirmish.
					put("lorien", "1_53");      // Lórien Elf
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FootmansArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Footman's Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Response:</b> If bearer is about to be overwhelmed, discard a [gondor] fortification or a card from hand to make bearer strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Footman's Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseDiscardsACardFromHandToMakeTheBearerStrengthPlusTwoWhenHeIsAboutToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(lorien, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.SkipToAssignments();
		// The Free Peoples player may give him one minion; the Shadow player gives him the one left over.
		scn.FreepsAssignToMinions(aragorn, brigand1);
		scn.ShadowAssignToMinions(aragorn, brigand2);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 + 8 against 8: exactly double.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(8, scn.GetStrength(brigand2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		scn.FreepsChooseOptionalTrigger(armor);
		// No [gondor] fortification is in play, so the card comes from hand: either of the two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.HAND, lorien);
		// 16 against 10 is no longer double: he loses to two minions with no damage bonus, takes one wound and
		// lives.  He is not about to be overwhelmed any more, so the response is not offered again, though a card
		// is still in hand to pay for it.
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertAttachedTo(armor, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsOfferedAgainWhileTheBearerIsStillAboutToBeOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lorien = scn.GetFreepsCard("lorien");
		var guard = scn.GetFreepsCard("guard");
		var brigand1 = scn.GetShadowCard("brigand1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(lorien, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand1);
		scn.ShadowAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 + 13 against 8.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		scn.FreepsChooseOptionalTrigger(armor);
		scn.FreepsChooseCard(guard);
		// 21 against 10 is still more than double: whether he is about to be overwhelmed is checked again, and the
		// response is offered again (CR 4.2, the ruling on Footman's Armor).
		assertEquals(10, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.HAND, lorien);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));

		// The one card left in hand pays, with nothing to choose.  21 against 12 is no longer double: he loses,
		// takes one wound and lives.
		scn.FreepsChooseOptionalTrigger(armor);
		assertInZone(Zone.DISCARD, lorien);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
