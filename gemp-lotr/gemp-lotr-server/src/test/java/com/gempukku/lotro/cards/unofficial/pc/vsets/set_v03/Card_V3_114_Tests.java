package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippinspipe", "103_114");
					put("pippin", "1_306");
					put("merry", "1_302");
					put("merryspipe", "103_113");
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinsPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pippin's Pipe
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be an unbound Hobbit.
		 * 	Fellowship: Discard a pipeweed to remove X threats, where X is the number of pipes you can spot. Hinder this pipe or hinder bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippinspipe");

		assertEquals("Pippin's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PippinsPipeRemovesThreatsEqualToPipeCountAndCanHinderPipe() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var merryspipe = scn.GetFreepsCard("merryspipe");
		var toby = scn.GetFreepsCard("toby");

		// 3 companions (Frodo + Pippin + Merry), threat limit = 3
		scn.MoveCompanionsToTable(pippin, merry);
		scn.AttachCardsTo(pippin, pippinspipe);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();

		// Add 3 threats (cheat, won't fire triggers)
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());

		// Fellowship phase: use Pippin's Pipe — discard pipeweed, remove threats equal to pipe count
		// 2 pipes on table (Pippin's + Merry's), so remove 2 threats
		scn.FreepsUseCardAction(pippinspipe);
		scn.FreepsDeclineOptionalTrigger(); //merry's pipe

		assertEquals(1, scn.GetThreats());

		// Choose to hinder the pipe
		scn.FreepsChoose("pipe");

		assertTrue(scn.IsHindered(pippinspipe));
		assertFalse(scn.IsHindered(pippin));
	}

	@Test
	public void PippinsPipeCanChooseToHinderBearerInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippinspipe = scn.GetFreepsCard("pippinspipe");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var merryspipe = scn.GetFreepsCard("merryspipe");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCompanionsToTable(pippin, merry);
		scn.AttachCardsTo(pippin, pippinspipe);
		scn.AttachCardsTo(merry, merryspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();

		scn.AddThreats(3);

		// Use Pippin's Pipe
		scn.FreepsUseCardAction(pippinspipe);
		scn.FreepsDeclineOptionalTrigger(); //merry's pipe

		// Choose to hinder bearer instead
		scn.FreepsChoose("Friend to Frodo");

		assertFalse(scn.IsHindered(pippinspipe));
		assertTrue(scn.IsHindered(pippin));
	}
}
