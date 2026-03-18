package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merryspipe", "103_113");
					put("merry", "1_302");
					put("aragorn", "1_89");
					put("aragornspipe", "1_91");
					put("toby", "1_305");
					put("beauty", "1_205");
					put("witchking", "1_237");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerrysPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Merry's Pipe
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be an unbound Hobbit.
		 * 	Each time a pipeweed is discarded, you may exert bearer to play that pipeweed from your discard pile.  Hinder it and this pipe.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merryspipe");

		assertEquals("Merry's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MerrysPipeRecoversPipeweedDiscardedByFriendlyPipe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, merryspipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();

		// Fellowship phase: use Aragorn's Pipe to discard the pipeweed
		scn.FreepsUseCardAction(aragornspipe);

		// Merry's Pipe trigger fires — accept to recover the pipeweed
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		//Old Toby trigger
		scn.FreepsDeclineOptionalTrigger();

		// Toby played from discard to support, both toby and Merry's Pipe hindered
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(1, scn.GetWoundsOn(merry)); // exerted as cost
	}

	@Test
	public void MerrysPipeCanDeclineRecoveryAndPipeweedStaysInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, merryspipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();

		// Fellowship phase: use Aragorn's Pipe to discard the pipeweed
		scn.FreepsUseCardAction(aragornspipe);

		// Merry's Pipe trigger fires — accept to recover the pipeweed
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		// Pipeweed stays in discard, Merry's Pipe not hindered, Merry not exerted
		assertInZone(Zone.DISCARD, toby);
		assertFalse(scn.IsHindered(merryspipe));
		assertEquals(0, scn.GetWoundsOn(merry));
	}

	@Test
	public void MerrysPipeRecoversPipeweedDiscardedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merryspipe = scn.GetFreepsCard("merryspipe");
		var merry = scn.GetFreepsCard("merry");
		var toby = scn.GetFreepsCard("toby");
		var beauty = scn.GetShadowCard("beauty");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(beauty);
		scn.MoveMinionsToTable(witchking);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);

		// FP passes, Shadow plays Beauty Is Fading — exert Witch-King, discard a FP possession
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(beauty);
		// Shadow chooses the pipeweed to discard
		scn.ShadowChooseCard(toby);

		// Merry's Pipe trigger fires — accept to recover the pipeweed
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		//Old Toby trigger
		scn.FreepsDeclineOptionalTrigger();

		// Toby played from discard to support, both toby and Merry's Pipe hindered
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(1, scn.GetWoundsOn(merry)); // exerted as cost
	}


}
