package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_113");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("countryside", "3_113");
					// Elf-song: an [elven] Fellowship event of cost 1.  "Spot an Elf to remove a burden."
					put("elfsong", "1_39");
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: the Elf; an ally, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("merry", "1_303");      // Merry, From O'er the Brandywine: vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheShireCountrysideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Shire Countryside
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, spot 2 [shire] companions. Plays to your support area.<br>Each time you remove a burden (except by a Hobbit's game text), you may heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("The Shire Countryside", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovingABurdenWithAnEventMayHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var elfsong = scn.GetFreepsCard("elfsong");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(merry, 1);
		// A wounded ally: no companion.
		scn.AddWoundsToChar(elrond, 1);

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elfsong));
		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, elrond));

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenABurdenIsRemovedAndNoCompanionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var elfsong = scn.GetFreepsCard("elfsong");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.AddBurdens(2);
		// A wounded ally, and no wounded companion: the heal would do nothing.
		scn.AddWoundsToChar(elrond, 1);

		assertInZone(Zone.SUPPORT, countryside);
		assertTrue(scn.FreepsPlayAvailable(elfsong));
		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
