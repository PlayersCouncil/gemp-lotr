
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_014_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>() {{
					put("gandalf", "101_14");
					put("elrond", "1_40");
					put("elrondcomp", "7_21");
					put("galadriel", "1_45");
					put("celeborn", "1_34");
					put("orophin", "1_56");

					put("boats", "1_46");
					put("bb", "1_70");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Gandalf, Olorin
		* Side: Free Peoples
		* Culture: gandalf
		* Twilight Cost: 4
		* Type: companion
		* Subtype: Wizard
		* Strength: 6
		* Vitality: 4
		* Signet: Gandalf
		* Game Text: At the start of your fellowship phase, you may spot 2 [elven] allies and exert Gandalf to shuffle a [Gandalf] or [elven] card from your discard pile into your draw deck.
		* 	While you can spot 3 [elven] allies, Gandalf is strength +2.
		* 	 While you can spot Elrond, Galadriel, and Celeborn, Gandalf is strength +2.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");

		assertTrue(gandalf.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gandalf.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, gandalf.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, gandalf.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, gandalf.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(gandalf, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(4, gandalf.getBlueprint().getTwilightCost());
		assertEquals(6, gandalf.getBlueprint().getStrength());
		assertEquals(4, gandalf.getBlueprint().getVitality());
		//assertEquals(, gandalf.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, gandalf.getBlueprint().getSignet());
		//assertEquals(, gandalf.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void GandalfExertsAndSpotsTwoElvenAlliesToShuffleACardFromDiscardIntoDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl boats = scn.GetFreepsCard("boats");
		PhysicalCardImpl bb = scn.GetFreepsCard("bb");

		scn.FreepsMoveCharToTable(gandalf);
		scn.FreepsMoveCharToTable("galadriel", "orophin");
		scn.FreepsMoveCardToDiscard(boats, bb);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(Zone.DISCARD, boats.getZone());
		assertEquals(Zone.DISCARD, bb.getZone());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		//One each of Elven and Gandalf cards in the discard to choose from
		assertEquals(2, scn.GetFreepsCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(boats);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(Zone.DECK, boats.getZone());
		assertEquals(Zone.DISCARD, bb.getZone());
	}

	@Test
	public void WhileThereAre3ElvenAlliesGandalfIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl orophin = scn.GetFreepsCard("orophin");

		scn.FreepsMoveCharToTable(gandalf);
		scn.FreepsMoveCardToHand(elrond, galadriel, orophin);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(elrond);
		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(galadriel);
		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(orophin);
		assertEquals(8, scn.GetStrength(gandalf));
	}

	@Test
	public void WhileThereAre3WhiteCouncilMembersGandalfIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrondcomp = scn.GetFreepsCard("elrondcomp");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl celeborn = scn.GetFreepsCard("celeborn");

		scn.FreepsMoveCharToTable(gandalf);
		scn.FreepsMoveCardToHand(elrondcomp, galadriel, celeborn);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(galadriel);
		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(elrondcomp);
		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(celeborn);
		assertEquals(8, scn.GetStrength(gandalf));
	}

	@Test
	public void WhileThereAreBothAlliesAndWhiteCouncilMembersGandalfIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl gandalf = scn.GetFreepsCard("gandalf");
		PhysicalCardImpl elrond = scn.GetFreepsCard("elrond");
		PhysicalCardImpl galadriel = scn.GetFreepsCard("galadriel");
		PhysicalCardImpl celeborn = scn.GetFreepsCard("celeborn");

		scn.FreepsMoveCharToTable(gandalf);
		scn.FreepsMoveCardToHand(elrond, galadriel, celeborn);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(elrond);
		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(galadriel);
		assertEquals(6, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(celeborn);
		assertEquals(10, scn.GetStrength(gandalf));
	}
}
