package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrow", "9_46");
					put("aragorn", "1_89");
					put("eomer", "4_267");
					put("spear", "4_268");

					put("runner", "1_178");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheRedArrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: The Red Arrow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: <b>Assignment:</b> Exert a [gondor] Man to play a [rohan] Man. You may exert that [rohan] Man to play a possession on him or her. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrow");

		assertEquals("The Red Arrow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheRedArrowExertsAGondorCompToPlayRohanCompDuringAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCompanionToTable(aragorn);
		scn.MoveCardsToHand(eomer, spear);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		assertEquals(Zone.HAND, eomer.getZone());
		assertEquals(Zone.HAND, spear.getZone());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsActionAvailable(arrow));
		scn.FreepsUseCardAction(arrow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.FREE_CHARACTERS, eomer.getZone());

		assertTrue(scn.FreepsDecisionAvailable("Would you like exert the played"));
		scn.FreepsChooseNo();
		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertEquals(Zone.HAND, spear.getZone());
	}

	@Test
	public void TheRedArrowCanOptionallyPlayPossessionOnPlayedCompByExerting() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrow = scn.GetFreepsCard("arrow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCardsToSupportArea(arrow);
		scn.MoveCompanionToTable(aragorn);
		scn.MoveCardsToHand(eomer, spear);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		assertEquals(Zone.HAND, eomer.getZone());
		assertEquals(Zone.HAND, spear.getZone());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsActionAvailable(arrow));
		scn.FreepsUseCardAction(arrow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.FREE_CHARACTERS, eomer.getZone());

		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.FreepsDecisionAvailable("Would you like exert the played"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetWoundsOn(eomer));
		assertEquals(Zone.ATTACHED, spear.getZone());
		assertEquals(eomer, spear.getAttachedTo());
		assertTrue(scn.ShadowAnyDecisionsAvailable());
	}
}
