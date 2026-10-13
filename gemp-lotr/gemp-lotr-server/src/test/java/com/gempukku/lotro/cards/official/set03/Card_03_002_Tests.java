package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_2");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPipesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "3_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					// Gandalf's Pipe: "Bearer must be Gandalf.  Fellowship: Discard a pipeweed possession and spot X
					// pipes to remove X burdens."  Its X is where the number of pipes that can be spotted is read.
					put("gandalfspipe", "1_74");
					// Old Toby: a pipeweed possession in the support area; its own text answers its being played.
					put("toby1", "1_305");
					put("toby2", "1_305");
					put("toby3", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Gimli's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be Gimli.<br><b>Fellowship:</b> Exert Gimli to add 1 to the number of pipes you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Gimli's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingGimliLetsOneMorePipeBeSpottedThanIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPipesScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfsPipe = scn.GetFreepsCard("gandalfspipe");
		var toby1 = scn.GetFreepsCard("toby1");
		var toby2 = scn.GetFreepsCard("toby2");
		var toby3 = scn.GetFreepsCard("toby3");
		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.AttachCardsTo(gimli, pipe);
		scn.AttachCardsTo(gandalf, gandalfsPipe);
		scn.MoveCardsToSupportArea(toby1, toby2, toby3);

		scn.StartGame();
		scn.AddBurdens(7);

		// Two pipes are in play, Gimli's and Gandalf's: 2 is the most that can be spotted.
		scn.FreepsUseCardAction(gandalfsPipe);
		scn.FreepsChooseCard(toby1);
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(5, scn.GetBurdens());

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(pipe));
		scn.FreepsUseCardAction(pipe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The same two pipes, and one more may be spotted: 3.
		scn.FreepsUseCardAction(gandalfsPipe);
		scn.FreepsChooseCard(toby2);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A fellowship action lasts the phase it is used in: in this player's next fellowship phase it is 2 again.
		scn.SkipToSite(2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsUseCardAction(gandalfsPipe);
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
