package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_01_068_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrows", "51_68");
					put("galadriel", "1_45");
					put("legolas", "1_50");

					put("scout", "1_191");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWhiteArrowsofLorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: The White Arrows of Lorien
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: <b>Tale.</b> Bearer must be an Elf companion. 
		* 	<b>Archery:</b> If bearer is an archer, exert bearer to exert a minion.  An opponent may discard a card from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");

		assertFalse(arrows.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, arrows.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, arrows.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, arrows.getBlueprint().getCardType());
		assertEquals(1, arrows.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhiteArrowsPlaysOnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCompanionToTable(legolas, galadriel);
		scn.MoveCardsToHand(arrows);

		scn.StartGame();
		scn.FreepsPlayCard(arrows);

		assertEquals(Zone.ATTACHED, arrows.getZone());
		//Should have automatically gone to Legolas and not had galadriel as a valid choice, where Gemp would ask the user to decide
		assertAttachedTo(arrows, legolas);
	}

	@Test
	public void WhiteArrowsExertsBearerToExertAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);

		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToShadowHand("legolas", "galadriel", "arrows");
		scn.MoveMinionsToTable("scout", "runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(arrows));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(scout));
		scn.FreepsUseCardAction(arrows);
		scn.ShadowChooseNo();

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(scout));
	}

	@Test
	public void WhiteArrowsActionCanBePreventedByShadowDiscardingCardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);

		var scout = scn.GetShadowCard("scout");
		var card1 = scn.GetShadowCard("arrows");
		scn.MoveCardsToShadowHand("legolas", "galadriel", "arrows");
		scn.MoveMinionsToTable("scout", "runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(arrows));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(Zone.HAND, card1.getZone());
		scn.FreepsUseCardAction(arrows);
		scn.ShadowChooseYes();
		scn.ShadowChooseCard(card1);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(Zone.DISCARD, card1.getZone());
	}
}
