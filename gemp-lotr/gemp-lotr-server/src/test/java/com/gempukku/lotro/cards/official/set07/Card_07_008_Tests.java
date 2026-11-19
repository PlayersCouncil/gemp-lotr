package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "7_8");
					// Gimli, Son of Gloin: strength 6, vitality 3, damage +1; his skirmish ability is used only on
					// purpose
					put("gimli", "1_13");
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, strength 5, vitality 3; +2 only against an Orc
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 is its whole text

					// Cards held in hand, one of each kind.
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion
					put("axe", "1_9");            // Dwarven Axe: a possession
					put("strike", "1_3");         // Axe Strike: a skirmish event
					put("savage", "1_151");       // Uruk Savage: a Shadow minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gimli's Armor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be Gimli.<br><b>Response:</b> If you have initiative and Gimli is about to take a wound, discard 2 cards from hand to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Gimli's Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnGimliAndNotOnAnotherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// Gimli is the one bearer it may have, and it goes on him unasked.
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertEquals(0, scn.GetAttachedCards(farin).size());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutGimli() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// Farin is a [dwarven] Dwarf companion, and no Gimli.
		assertInHand(armor);
		assertFalse(scn.FreepsPlayAvailable(armor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithInitiativeTwoCardsFromHandPreventAWoundAndTheNextWoundFindsNoInitiativeLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var savage = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(aragorn, axe, strike, savage);

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertAttachedTo(armor, gimli);
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(aragorn, axe, strike, savage);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);

		// 9 against 6: the Guard wins, and its damage +1 makes two wounds.
		scn.PassSkirmishActions();

		// Four cards in hand: the Free Peoples player has initiative.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(armor));
		scn.FreepsAcceptOptionalTrigger();

		// Any two cards: a companion, a possession, an event and a Shadow card are all offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, axe, strike, savage));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(axe, savage);

		assertInDiscard(axe, savage);
		assertInHand(aragorn, strike);

		// The second wound comes with two cards left in hand: the Shadow has initiative, and it is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInHand(aragorn, strike);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(aragorn, axe, strike);

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertAttachedTo(armor, gimli);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, guard);
		scn.FreepsResolveSkirmish(gimli);

		scn.PassSkirmishActions();

		// Two cards could be discarded, but with three in hand the Shadow has initiative.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundOnAnotherDwarfBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var strike = scn.GetFreepsCard("strike");
		var savage = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(aragorn, axe, strike, savage);

		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertAttachedTo(armor, gimli);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, guard);
		scn.FreepsResolveSkirmish(farin);

		// 9 against 5: Farin takes two wounds, and Gimli none.
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
