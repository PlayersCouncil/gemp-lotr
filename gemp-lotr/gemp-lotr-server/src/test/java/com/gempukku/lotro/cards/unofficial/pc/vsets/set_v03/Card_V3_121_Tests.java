package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jetsam", "103_121");
					put("gaffers", "1_292");
					put("gandalfs", "1_74");
					put("aragorns", "1_91");

					put("oldtoby", "1_305");
					put("leaf", "1_300");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void JetsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Jetsam
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Pipeweed. Bearer must bear a pipe. When you play this, you may discard a Shadow card on bearer.
		* 	When this is discarded by a pipe, add 1 to the number of pipes you can spot this phase and make your character lose <b>unhasty</b> until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jetsam");

		assertEquals("Jetsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void JetsamIncreasesPipeCountDuringPipeUsageAndAfter() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var jetsam = scn.GetFreepsCard("jetsam");
		var gaffers = scn.GetFreepsCard("gaffers");
		var aragorns = scn.GetFreepsCard("aragorns");
		var gandalfs = scn.GetFreepsCard("gandalfs");
		var oldtoby = scn.GetFreepsCard("oldtoby");
		scn.MoveCardsToSupportArea(oldtoby);
		scn.AttachCardsTo(frodo, jetsam, gaffers, aragorns, gandalfs);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsUseCardAction(gaffers);
		scn.FreepsChooseCard(jetsam);

		//Gaffer's, Gandalf's, and Aragorn's pipes, +1 from the Jetsam
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("4");
		assertEquals(6, scn.GetTwilight());

		scn.FreepsUseCardAction(gaffers);
		//Gaffer's, Gandalf's, and Aragorn's pipes, +1 from the Jetsam even tho another pipeweed was discarded
		assertEquals(4, scn.FreepsGetChoiceMax());
	}
}
