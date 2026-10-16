package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "3_2");
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] Dwarf companion who is no Gimli, vitality 2
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Wizard companion with no ability
					// Gandalf's Pipe: "Bearer must be Gandalf.  Fellowship: Discard a pipeweed possession and spot X
					// pipes to remove X burdens."  Its X is where the number of pipes that can be spotted is read.
					put("gandalfspipe", "1_74");
					// Wizard's Stash: a pipeweed possession on a Wizard.  "Regroup: Exert bearer and discard this.
					// This phase you may use special abilities on pipes as if it were the Fellowship phase."
					put("stash", "103_7");
					// Old Toby: a pipeweed possession in the support area; its own text answers its being played.
					put("toby1", "1_305");
					put("toby2", "1_305");
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

		var card = scn.GetFreepsCard("pipe");

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
		assertEquals(7, scn.GetBurdens());

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

	@Test
	public void PlaysOnGimliAndOnNoOtherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(pipe);

		scn.StartGame();
		assertInHand(pipe);
		// A Dwarf companion who is no Gimli, and the Ring-bearer: no bearer
		assertFalse(scn.FreepsPlayAvailable(pipe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCompanionsToTable(gimli);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(pipe));
		scn.FreepsPlayCard(pipe);
		// Gimli is the one bearer it may have, so none is asked for.
		assertAttachedTo(pipe, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithGimliExhaustedTheAbilityIsNotOfferedThoughAnotherDwarfCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, pipe);
		// Vitality 3 with 2 wounds: exhausted
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertAttachedTo(pipe, gimli);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(pipe));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheAddedPipeIsGoneOnceTheFellowshipPhaseIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalfsPipe = scn.GetFreepsCard("gandalfspipe");
		var stash = scn.GetFreepsCard("stash");
		var toby1 = scn.GetFreepsCard("toby1");
		var toby2 = scn.GetFreepsCard("toby2");
		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.AttachCardsTo(gimli, pipe);
		scn.AttachCardsTo(gandalf, gandalfsPipe, stash);
		scn.MoveCardsToSupportArea(toby1, toby2);
		assertAttachedTo(stash, gandalf);
		assertInZone(Zone.SUPPORT, toby1, toby2);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(pipe);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli's pipe and Gandalf's, and one more: 3 in the fellowship phase it was used in.
		scn.FreepsUseCardAction(gandalfsPipe);
		scn.FreepsChooseCard(toby1);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("3");
		assertEquals(2, scn.GetBurdens());

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsActionAvailable(gandalfsPipe));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsUseCardAction(stash);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(stash);
		scn.ShadowPass();

		// In the regroup phase the same two pipes are 2 again.
		assertTrue(scn.FreepsActionAvailable(gandalfsPipe));
		scn.FreepsUseCardAction(gandalfsPipe);
		assertInDiscard(toby2);
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
