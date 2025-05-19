package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("gandalf", "101_14");
					put("elrond", "1_40");
					put("elrondcomp", "7_21");
					put("galadriel", "1_45");
					put("celeborn", "1_34");
					put("orophin", "1_56");

					put("boats", "1_46");
					put("bb", "1_70");
					put("defiance", "1_37");
					put("sleep", "1_84");

					put("saruman", "3_68");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Gandalf, Olorin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Signet: Gandalf
		 * Game Text: At the start of your fellowship phase, you may spot 3 [elven] allies and exert Gandalf to take a [Gandalf] or [elven] event from your discard pile into your hand.
		* 	Gandalf is strength +1 for each of these characters you can spot: Celeborn, Elrond, Galadriel, Saruman.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Olorin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void GandalfExertsAndSpotsThreeElvenAlliesToShuffleACardFromDiscardIntoDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var boats = scn.GetFreepsCard("boats");
		var bb = scn.GetFreepsCard("bb");
		var defiance = scn.GetFreepsCard("defiance");
		var sleep = scn.GetFreepsCard("sleep");

		scn.MoveCompanionToTable(gandalf);
		scn.MoveCompanionToTable("galadriel", "orophin");
		scn.MoveCardsToDiscard(boats, bb, defiance, sleep);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(Zone.DISCARD, boats.getZone());
		assertEquals(Zone.DISCARD, bb.getZone());

		//only 2 elven allies
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.MoveCompanionToTable("celeborn");

		scn.SkipCurrentSite(); //Also skips through Shadow turn

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));

		//4 cards in discard: 1 gandalf event, 1 elven event, 1 gandalf ally, 1 elven condition
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(sleep);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(Zone.HAND, sleep.getZone());
		assertEquals(Zone.DISCARD, defiance.getZone());
		assertEquals(Zone.DISCARD, bb.getZone());
		assertEquals(Zone.DISCARD, boats.getZone());
	}

	@Test
	public void GandalfIsStrengthPlus1PerWhiteCouncilMember() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var elrondcomp = scn.GetFreepsCard("elrondcomp");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");

		scn.MoveCompanionToTable(gandalf);
		scn.MoveCardsToHand(elrondcomp, galadriel, celeborn);

		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(galadriel);
		assertEquals(8, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(elrondcomp);
		assertEquals(9, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(celeborn);
		assertEquals(10, scn.GetStrength(gandalf));

		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(10, scn.GetStrength(gandalf));
		scn.ShadowPlayCard(saruman);
		assertEquals(11, scn.GetStrength(gandalf));
	}

}
