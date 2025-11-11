package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_124_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stash", "103_124");
					put("gandalf", "1_72");
					put("aragornspipe", "1_91");
					put("aragorn", "1_89");
					put("leaf", "1_300");
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardsPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Wizard's Pipe
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be an unbound companion. Each companion bearing a pipe is strength +1.
		* 	Regroup: Exert a Wizard twice and discard a pipeweed; this phase you may use special abilities on pipes as if it were the Fellowship phase. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stash");

		assertEquals("Wizard's Stash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().hasKeyword(Keyword.PIPEWEED));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WizardsStashPermitsPipeFellowshipActionsToBeUsedInRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var stash = scn.GetFreepsCard("stash");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leaf = scn.GetFreepsCard("leaf");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(leaf, toby);

		scn.AttachCardsTo(gandalf, stash);
		scn.AttachCardsTo(aragorn, aragornspipe);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(aragornspipe));
		assertTrue(scn.FreepsActionAvailable(frodo));
		assertFalse(scn.FreepsActionAvailable(stash));

		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SUPPORT, leaf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsActionAvailable(aragornspipe));
		assertFalse(scn.FreepsActionAvailable(frodo));
		assertTrue(scn.FreepsActionAvailable(stash));

		scn.FreepsUseCardAction(stash);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.HasKeyword(toby, Keyword.PIPEWEED));
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(aragornspipe));
		assertFalse(scn.FreepsActionAvailable(frodo));


	}

}
