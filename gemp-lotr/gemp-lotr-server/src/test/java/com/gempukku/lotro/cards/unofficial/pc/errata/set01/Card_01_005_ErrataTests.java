package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_005_ErrataTests
{

// ----------------------------------------
// CLEAVING BLOW (ERRATA) TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blow", "51_5");          // Cleaving Blow (Errata)
					put("gimli", "1_13");         // Gimli, Son of Gloin

					put("orc1", "1_271");         // Orc Soldier
					put("orc2", "1_271");         // Second Orc Soldier
					put("vileblade", "2_95");     // Vile Blade - attaches to orc
					put("ships", "8_65");         // Ships of Great Draught - support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CleavingBlowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Cleaving Blow
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Skirmish: Make a Dwarf strength +2 and <b>damage +1</b>. If that Dwarf wins this skirmish, you may discard a Shadow possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blow");

		assertEquals("Cleaving Blow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void CleavingBlowMakesDwarfStrengthPlus2AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var orc = scn.GetShadowCard("orc1");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(orc);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		int gimliStrength = scn.GetStrength(gimli);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsPlayCard(blow);
		// Gimli auto-selected as only Dwarf

		assertEquals(gimliStrength + 2, scn.GetStrength(gimli));
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}

	@Test
	public void CleavingBlowDiscardsItemOnSkirmishingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var orc = scn.GetShadowCard("orc1");
		var vileblade = scn.GetShadowCard("vileblade");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(orc);
		scn.AttachCardsTo(orc, vileblade);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertInZone(Zone.ATTACHED, vileblade);

		scn.FreepsPlayCard(blow);
		// Gimli auto-selected
		// Vile Blade auto-selected as only valid item

		assertInZone(Zone.DISCARD, vileblade);
	}

	@Test
	public void CleavingBlowCannotTargetSupportAreaItems() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var orc = scn.GetShadowCard("orc1");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, orc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsPlayCard(blow);
		// Gimli auto-selected
		// No valid items to discard - effect should fizzle

		// Ships should still be in support area
		assertInZone(Zone.SUPPORT, ships);
	}



	@Test
	public void CleavingBlowCannotTargetItemOnNonSkirmishingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blow = scn.GetFreepsCard("blow");
		var gimli = scn.GetFreepsCard("gimli");
		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var vileblade = scn.GetShadowCard("vileblade");
		scn.MoveCardsToHand(blow);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(orc1, orc2);
		scn.AttachCardsTo(orc2, vileblade);  // Blade on orc2

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, orc1);  // Gimli fights orc1
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsPlayCard(blow);
		// Gimli auto-selected
		// No valid items - vileblade is on orc2 who isn't in this skirmish

		// Vile Blade should still be attached to orc2
		assertInZone(Zone.ATTACHED, vileblade);
		assertAttachedTo(vileblade, orc2);
	}
}
