package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_74");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPipeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_74");
					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: bears the second pipe
					put("aragornspipe", "1_91");    // Aragorn's Pipe: a pipe of another culture, on a [gondor] companion
					put("toby", "1_305");           // Old Toby: [shire] pipeweed possession, support area
					put("leaf", "1_300");           // Longbottom Leaf: [shire] pipeweed possession, support area
					put("book", "2_113");           // Red Book of Westmarch: a [shire] possession in the support area that is no pipeweed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gandalf's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be Gandalf.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to remove X burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gandalf's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAPipeweedAndSpotsXPipesToRemoveXBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPipeScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");
		var leaf = scn.GetFreepsCard("leaf");
		var book = scn.GetFreepsCard("book");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, pipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby, leaf, book);

		scn.StartGame();
		// Three burdens and two pipes: "X burdens" is neither every burden nor one.
		scn.AddBurdens(3);

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		// The pipeweed to discard: either pipeweed, and no other possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, leaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(book, pipe, aragornspipe));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(leaf);
		assertInZone(Zone.DISCARD, leaf);
		assertInZone(Zone.SUPPORT, toby, book);

		// X may be as many as the two pipes in play.
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(aragornspipe, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
